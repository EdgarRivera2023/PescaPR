#!/usr/bin/env python3
"""Create or verify an immutable PescaPR fish-dataset metadata snapshot."""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import shutil
import sys
from collections import Counter
from pathlib import Path

from validate_dataset import validate


SCHEMA_VERSION = "pescapr-fish-dataset-snapshot/v1"
DATASET_SCHEMA_VERSION = "1.0.0"
PARTITION_SEED = "pescapr-partition-v1"
ALLOWED_DISPOSITIONS = {
    "EXPECTED_NOT_APPLICABLE",
    "EXPECTED_METADATA_LIMITATION",
    "MANUAL_REVIEW_RESOLVED",
    "MUST_FIX_BEFORE_SNAPSHOT",
}


class SnapshotError(RuntimeError):
    pass


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def read_csv(path: Path) -> list[dict[str, str]]:
    with path.open(newline="", encoding="utf-8-sig") as handle:
        return [{key: (value or "").strip() for key, value in row.items()} for row in csv.DictReader(handle)]


def canonical_checksum(manifest: dict, checksum_field: str = "snapshotChecksumSha256") -> str:
    content = {key: value for key, value in manifest.items() if key != checksum_field}
    encoded = json.dumps(content, ensure_ascii=False, sort_keys=True, separators=(",", ":")).encode("utf-8")
    return hashlib.sha256(encoded).hexdigest()


def _approved_test_rows(rows: list[dict[str, str]], approvals_path: Path, testset_version: str) -> list[dict[str, str]]:
    test_rows = [row for row in rows if row["datasetPartition"] == "TEST"]
    approvals = {row["internalImageId"]: row for row in read_csv(approvals_path)}
    missing_approval = []
    unlocked = []
    for row in test_rows:
        approval = approvals.get(row["internalImageId"])
        if not approval or approval.get("testsetVersion") != testset_version or approval.get("decision") != "APPROVE_FOR_LOCKED_TEST" or not approval.get("reviewer") or not approval.get("reviewDate"):
            missing_approval.append(row["internalImageId"])
        if row.get("partitionLockStatus") != "LOCKED":
            unlocked.append(row["internalImageId"])
    if missing_approval or unlocked:
        details = []
        if missing_approval:
            details.append("approval missing: " + ", ".join(missing_approval))
        if unlocked:
            details.append("metadata not LOCKED: " + ", ".join(unlocked))
        raise SnapshotError("TEST approval/lock gate failed: " + "; ".join(details))
    return [approvals[row["internalImageId"]] for row in test_rows]


def _disposition_warnings(warnings, dispositions_path: Path) -> list[dict[str, str]]:
    dispositions = read_csv(dispositions_path)
    for item in dispositions:
        if item.get("classification") not in ALLOWED_DISPOSITIONS:
            raise SnapshotError(f"Invalid warning classification: {item.get('classification')!r}")
        if item.get("status") != "RESOLVED" or item.get("classification") == "MUST_FIX_BEFORE_SNAPSHOT":
            raise SnapshotError(f"Unresolved warning disposition: {item.get('warningCode')} {item.get('matchText')}")
    unresolved = []
    for warning in warnings:
        if not any(item.get("warningCode") == warning.code and item.get("matchText", "") in warning.message for item in dispositions):
            unresolved.append(f"{warning.code}: {warning.message}")
    if unresolved:
        raise SnapshotError("Warnings lack resolved dispositions: " + "; ".join(unresolved))
    return dispositions


def _partition_overrides(rows: list[dict[str, str]], overrides_path: Path) -> list[dict[str, str]]:
    by_id = {row["internalImageId"]: row for row in rows}
    overrides = read_csv(overrides_path)
    seen: set[str] = set()
    for override in overrides:
        image_id = override.get("internalImageId", "")
        if not image_id or image_id in seen or image_id not in by_id:
            raise SnapshotError(f"Invalid or duplicate partition override ID: {image_id!r}")
        seen.add(image_id)
        if override.get("toPartition") != by_id[image_id].get("datasetPartition"):
            raise SnapshotError(f"Partition override does not match metadata: {image_id}")
        if not override.get("fromPartition") or not override.get("decision") or not override.get("reviewer") or not override.get("reviewDate") or not override.get("reason"):
            raise SnapshotError(f"Incomplete partition override: {image_id}")
    return overrides


def build_manifest(
    *, metadata_path: Path, classifier_manifest_path: Path, validator_path: Path,
    approvals_path: Path, dispositions_path: Path, overrides_path: Path, snapshot_id: str,
    testset_version: str, created_at: str,
) -> dict:
    result = validate(metadata_path, classifier_manifest_path)
    if result.errors:
        raise SnapshotError("Dataset validator errors: " + "; ".join(f"{issue.code}: {issue.message}" for issue in result.errors))
    rows = result.rows
    if not rows:
        raise SnapshotError("Cannot snapshot an empty dataset")
    if any(not row.get("checksumSha256") or not row.get("perceptualHash") for row in rows):
        raise SnapshotError("Every snapshot row requires SHA-256 and pHash")
    approvals = _approved_test_rows(rows, approvals_path, testset_version)
    dispositions = _disposition_warnings(result.warnings, dispositions_path)
    overrides = _partition_overrides(rows, overrides_path)
    classifier = json.loads(classifier_manifest_path.read_text(encoding="utf-8"))
    partition_counts = Counter(row["datasetPartition"] for row in rows)
    sample_counts = Counter(row["sampleType"] for row in rows)
    species_counts = Counter((row["fichaPezId"], row["scientificName"]) for row in rows if row["sampleType"] == "POSITIVE")
    ood_counts = Counter(row["oodCategory"] for row in rows if row["sampleType"] == "OOD")
    source_counts = Counter(row["sourceName"] for row in rows)
    license_counts = Counter(row["license"] for row in rows)
    group_fields = ("derivativeGroupId", "sourceGroupId", "sessionGroupId", "individualFishGroupId", "contributorGroupId")
    approval_by_id = {row["internalImageId"]: row for row in approvals}
    test_rows = [row for row in rows if row["datasetPartition"] == "TEST"]
    testset_manifest = {
        "schemaVersion": "pescapr-fish-testset/v1",
        "testsetVersion": testset_version,
        "createdAt": created_at,
        "humanApprovalStatus": "APPROVED",
        "rows": [
            {
                "internalImageId": row["internalImageId"],
                "sampleType": row["sampleType"],
                "fichaPezId": row["fichaPezId"],
                "scientificName": row["scientificName"],
                "oodCategory": row["oodCategory"],
                "sha256": row["checksumSha256"],
                "sourceGroupId": row["sourceGroupId"],
                "sessionGroupId": row["sessionGroupId"],
                "lockStatus": row["partitionLockStatus"],
                "reviewer": approval_by_id[row["internalImageId"]]["reviewer"],
                "reviewDate": approval_by_id[row["internalImageId"]]["reviewDate"],
            }
            for row in sorted(test_rows, key=lambda row: row["internalImageId"])
        ],
    }
    testset_manifest["testsetChecksumSha256"] = canonical_checksum(testset_manifest, "testsetChecksumSha256")
    manifest = {
        "schemaVersion": SCHEMA_VERSION,
        "snapshotId": snapshot_id,
        "createdAt": created_at,
        "datasetSchemaVersion": DATASET_SCHEMA_VERSION,
        "classifierManifest": {
            "schemaVersion": classifier.get("schemaVersion"),
            "catalogVersion": classifier.get("catalogVersion"),
            "sha256": sha256_file(classifier_manifest_path),
        },
        "metadata": {"filename": "pilot_metadata.csv", "sha256": sha256_file(metadata_path)},
        "validator": {"filename": validator_path.name, "sha256": sha256_file(validator_path)},
        "partition": {"algorithmVersion": "source-group-hash/v1", "seed": PARTITION_SEED},
        "testset": testset_manifest,
        "counts": {
            "total": len(rows), "positive": sample_counts["POSITIVE"], "ood": sample_counts["OOD"],
            "train": partition_counts["TRAIN"], "validation": partition_counts["VALIDATION"], "test": partition_counts["TEST"],
        },
        "perSpeciesCounts": [
            {"fichaPezId": fish_id, "scientificName": name, "count": count}
            for (fish_id, name), count in sorted(species_counts.items())
        ],
        "perOodCategoryCounts": dict(sorted(ood_counts.items())),
        "sourceSummary": dict(sorted(source_counts.items())),
        "licenseSummary": dict(sorted(license_counts.items())),
        "approvalSummary": {
            "rightsApproved": sum(row["rightsStatus"] == "APPROVED" for row in rows),
            "labelsApproved": sum(row["labelStatus"] == "APPROVED" for row in rows),
        },
        "groupingCoverage": {
            field: {"populated": sum(bool(row.get(field)) for row in rows), "total": len(rows)}
            for field in group_fields
        },
        "warningDispositions": dispositions,
        "partitionOverrides": overrides,
        "imageInventory": [
            {"internalImageId": row["internalImageId"], "sha256": row["checksumSha256"], "pHash64": row["perceptualHash"]}
            for row in sorted(rows, key=lambda row: row["internalImageId"])
        ],
        "pHashReviewStatus": "RESOLVED_WITH_DOCUMENTED_DISPOSITIONS",
    }
    manifest["snapshotChecksumSha256"] = canonical_checksum(manifest)
    return manifest


def create_snapshot(args: argparse.Namespace) -> Path:
    manifest = build_manifest(
        metadata_path=args.metadata, classifier_manifest_path=args.classifier_manifest,
        validator_path=args.validator, approvals_path=args.approvals,
        dispositions_path=args.warning_dispositions, snapshot_id=args.snapshot_id,
        overrides_path=args.partition_overrides,
        testset_version=args.testset_version, created_at=args.created_at,
    )
    directory = args.snapshot_root / args.snapshot_id
    manifest_path = directory / "snapshot_manifest.json"
    encoded = json.dumps(manifest, ensure_ascii=False, sort_keys=True, indent=2) + "\n"
    testset_encoded = json.dumps(manifest["testset"], ensure_ascii=False, sort_keys=True, indent=2) + "\n"
    if manifest_path.exists():
        if manifest_path.read_text(encoding="utf-8") != encoded:
            raise SnapshotError(f"Immutable snapshot ID already exists with different content: {args.snapshot_id}")
        return manifest_path
    directory.mkdir(parents=True, exist_ok=False)
    shutil.copyfile(args.metadata, directory / "pilot_metadata.csv")
    manifest_path.write_text(encoded, encoding="utf-8")
    (directory / "testset_manifest.json").write_text(testset_encoded, encoding="utf-8")
    if args.repository_manifest_root:
        repository_directory = args.repository_manifest_root / args.snapshot_id
        repository_directory.mkdir(parents=True, exist_ok=True)
        for path, content in (
            (repository_directory / "snapshot_manifest.json", encoded),
            (repository_directory / "testset_manifest.json", testset_encoded),
        ):
            if path.exists() and path.read_text(encoding="utf-8") != content:
                raise SnapshotError(f"Repository manifest already exists with different content: {path}")
            path.write_text(content, encoding="utf-8")
    return manifest_path


def verify_snapshot(manifest_path: Path, classifier_manifest_path: Path, validator_path: Path) -> None:
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    if manifest.get("snapshotChecksumSha256") != canonical_checksum(manifest):
        raise SnapshotError("Snapshot manifest checksum mismatch")
    testset_path = manifest_path.parent / "testset_manifest.json"
    testset = json.loads(testset_path.read_text(encoding="utf-8"))
    if testset != manifest.get("testset"):
        raise SnapshotError("Test-set manifest does not match snapshot manifest")
    if testset.get("testsetChecksumSha256") != canonical_checksum(testset, "testsetChecksumSha256"):
        raise SnapshotError("Test-set manifest checksum mismatch")
    metadata = manifest_path.parent / manifest["metadata"]["filename"]
    if sha256_file(metadata) != manifest["metadata"]["sha256"]:
        raise SnapshotError("Snapshot metadata checksum mismatch")
    if sha256_file(classifier_manifest_path) != manifest["classifierManifest"]["sha256"]:
        raise SnapshotError("Frozen classifier manifest checksum mismatch")
    if sha256_file(validator_path) != manifest["validator"]["sha256"]:
        raise SnapshotError("Validator checksum mismatch")
    result = validate(metadata, classifier_manifest_path)
    if result.errors:
        raise SnapshotError("Snapshot metadata no longer validates")


def parser() -> argparse.ArgumentParser:
    root = argparse.ArgumentParser(description=__doc__)
    commands = root.add_subparsers(dest="command", required=True)
    create = commands.add_parser("create")
    create.add_argument("--metadata", type=Path, required=True)
    create.add_argument("--classifier-manifest", type=Path, required=True)
    create.add_argument("--validator", type=Path, required=True)
    create.add_argument("--approvals", type=Path, required=True)
    create.add_argument("--warning-dispositions", type=Path, required=True)
    create.add_argument("--partition-overrides", type=Path, required=True)
    create.add_argument("--snapshot-root", type=Path, required=True)
    create.add_argument("--snapshot-id", required=True)
    create.add_argument("--testset-version", required=True)
    create.add_argument("--created-at", required=True)
    create.add_argument("--repository-manifest-root", type=Path)
    verify = commands.add_parser("verify")
    verify.add_argument("--manifest", type=Path, required=True)
    verify.add_argument("--classifier-manifest", type=Path, required=True)
    verify.add_argument("--validator", type=Path, required=True)
    return root


def main() -> int:
    args = parser().parse_args()
    try:
        if args.command == "create":
            print(create_snapshot(args))
        else:
            verify_snapshot(args.manifest, args.classifier_manifest, args.validator)
            print("snapshot verification: PASS")
        return 0
    except (SnapshotError, OSError, ValueError, json.JSONDecodeError) as error:
        print(f"snapshot error: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
