#!/usr/bin/env python3
"""Validate PescaPR fish-dataset metadata without Android, Firebase, or image files."""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
import sys
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable


FINAL_PARTITIONS = {"TRAIN", "VALIDATION", "TEST"}
ALLOWED_PARTITIONS = {"", "UNASSIGNED", "TRAIN", "VALIDATION", "TEST", "EXCLUDED"}
ALLOWED_SAMPLE_TYPES = {"POSITIVE", "OOD"}
ALLOWED_OOD_CATEGORIES = {
    "unsupported_fish",
    "shark_ray",
    "crustacean",
    "cephalopod",
    "person",
    "fishing_gear",
    "boat",
    "cooler",
    "beach_water",
    "blurry_invalid",
    "other_nonfish",
}
FISH_LIKE_OOD_CATEGORIES = {"unsupported_fish", "shark_ray"}
GROUP_FIELDS = (
    "derivativeGroupId",
    "sourceGroupId",
    "sessionGroupId",
    "individualFishGroupId",
)
COUNTED_GROUP_FIELDS = GROUP_FIELDS + ("contributorGroupId",)
REQUIRED_COLUMNS = (
    "internalImageId",
    "sampleType",
    "oodCategory",
    "fichaPezId",
    "scientificName",
    "sourceTier",
    "sourceName",
    "sourceItemId",
    "originalSourceUrl",
    "originalFilename",
    "license",
    "licenseUrl",
    "photographerAuthor",
    "attributionText",
    "rightsStatus",
    "labelStatus",
    "labelReviewer",
    "acquisitionDate",
    "checksumSha256",
    "perceptualHash",
    "derivativeGroupId",
    "sourceGroupId",
    "sessionGroupId",
    "individualFishGroupId",
    "contributorGroupId",
    "originalOrDerivative",
    "parentImageId",
    "poseTags",
    "environmentTags",
    "biologicalVariationTags",
    "qualityFlags",
    "datasetPartition",
    "partitionLockStatus",
    "exclusionReason",
    "notes",
)
SHA256_RE = re.compile(r"^[0-9a-fA-F]{64}$")
PHASH_RE = re.compile(r"^[0-9a-fA-F]{16}$")


@dataclass(frozen=True)
class Issue:
    severity: str
    code: str
    message: str


@dataclass
class ValidationResult:
    issues: list[Issue]
    rows: list[dict[str, str]]
    missing_group_counts: dict[str, int]

    @property
    def errors(self) -> list[Issue]:
        return [issue for issue in self.issues if issue.severity == "ERROR"]

    @property
    def warnings(self) -> list[Issue]:
        return [issue for issue in self.issues if issue.severity == "WARNING"]


class BKTree:
    """Small dependency-free index for 64-bit perceptual hashes."""

    def __init__(self) -> None:
        self.root: tuple[int, int, dict[int, tuple]] | None = None

    @staticmethod
    def distance(left: int, right: int) -> int:
        return (left ^ right).bit_count()

    def add(self, value: int, row_index: int) -> None:
        node = (value, row_index, {})
        if self.root is None:
            self.root = node
            return
        current = self.root
        while True:
            distance = self.distance(value, current[0])
            child = current[2].get(distance)
            if child is None:
                current[2][distance] = node
                return
            current = child

    def search(self, value: int, threshold: int) -> Iterable[tuple[int, int]]:
        if self.root is None:
            return []
        matches: list[tuple[int, int]] = []
        stack = [self.root]
        while stack:
            current = stack.pop()
            distance = self.distance(value, current[0])
            if distance <= threshold:
                matches.append((current[1], distance))
            low, high = distance - threshold, distance + threshold
            stack.extend(child for edge, child in current[2].items() if low <= edge <= high)
        return matches


def load_catalog(manifest_path: Path) -> tuple[set[str], dict[str, str]]:
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    classes = manifest["classes"]
    ids = {entry["fichaPezId"] for entry in classes}
    names = {entry["fichaPezId"]: entry["scientificName"].strip() for entry in classes}
    if len(ids) != manifest["expectedClassCount"]:
        raise ValueError("Manifest class IDs are not unique or expectedClassCount is incorrect")
    return ids, names


def read_metadata(metadata_path: Path) -> tuple[list[dict[str, str]], list[Issue]]:
    issues: list[Issue] = []
    with metadata_path.open(newline="", encoding="utf-8-sig") as handle:
        reader = csv.DictReader(handle)
        columns = set(reader.fieldnames or [])
        missing = [column for column in REQUIRED_COLUMNS if column not in columns]
        if missing:
            issues.append(Issue("ERROR", "MISSING_COLUMNS", ", ".join(missing)))
        rows = [{key: (value or "").strip() for key, value in row.items()} for row in reader]
    return rows, issues


def validate(metadata_path: Path, manifest_path: Path) -> ValidationResult:
    catalog_ids, catalog_names = load_catalog(manifest_path)
    rows, issues = read_metadata(metadata_path)
    missing_groups = {field: 0 for field in COUNTED_GROUP_FIELDS}
    internal_ids: dict[str, list[int]] = defaultdict(list)
    hashes: dict[str, list[int]] = defaultdict(list)
    source_items: dict[tuple[str, str], list[int]] = defaultdict(list)
    groups: dict[str, dict[str, set[str]]] = {
        field: defaultdict(set) for field in GROUP_FIELDS
    }

    for index, row in enumerate(rows, start=2):
        image_id = row.get("internalImageId", "")
        sample_type = row.get("sampleType", "")
        fish_id = row.get("fichaPezId", "")
        partition = row.get("datasetPartition", "")
        prefix = f"row {index} ({image_id or 'missing internalImageId'})"

        if not image_id:
            issues.append(Issue("ERROR", "MISSING_INTERNAL_ID", prefix))
        else:
            internal_ids[image_id].append(index)

        if sample_type not in ALLOWED_SAMPLE_TYPES:
            issues.append(Issue("ERROR", "INVALID_SAMPLE_TYPE", f"{prefix}: {sample_type!r}"))
        elif sample_type == "POSITIVE":
            if not fish_id:
                issues.append(Issue("ERROR", "MISSING_CANONICAL_ID", prefix))
            elif fish_id not in catalog_ids:
                issues.append(Issue("ERROR", "UNKNOWN_CANONICAL_ID", f"{prefix}: {fish_id}"))
            elif row.get("scientificName", "") != catalog_names[fish_id]:
                issues.append(
                    Issue(
                        "ERROR",
                        "SCIENTIFIC_NAME_MISMATCH",
                        f"{prefix}: expected {catalog_names[fish_id]!r}",
                    )
                )
            if row.get("oodCategory", ""):
                issues.append(Issue("ERROR", "POSITIVE_HAS_OOD_CATEGORY", prefix))
        elif sample_type == "OOD":
            if fish_id:
                issues.append(Issue("ERROR", "OOD_HAS_CANONICAL_ID", prefix))
            if row.get("scientificName", ""):
                issues.append(Issue("ERROR", "OOD_HAS_SCIENTIFIC_NAME", prefix))
            category = row.get("oodCategory", "")
            if category not in ALLOWED_OOD_CATEGORIES:
                issues.append(Issue("ERROR", "INVALID_OOD_CATEGORY", f"{prefix}: {category!r}"))

        if partition not in ALLOWED_PARTITIONS:
            issues.append(Issue("ERROR", "INVALID_PARTITION", f"{prefix}: {partition!r}"))
        lock_status = row.get("partitionLockStatus", "")
        if lock_status not in {"", "UNLOCKED", "LOCKED"}:
            issues.append(Issue("ERROR", "INVALID_LOCK_STATUS", prefix))
        elif lock_status == "LOCKED" and partition != "TEST":
            issues.append(Issue("ERROR", "LOCKED_NON_TEST", prefix))
        if partition in FINAL_PARTITIONS:
            if row.get("rightsStatus", "") != "APPROVED":
                issues.append(Issue("ERROR", "RIGHTS_NOT_APPROVED", prefix))
            if row.get("labelStatus", "") != "APPROVED":
                issues.append(Issue("ERROR", "LABEL_NOT_APPROVED", prefix))
            if row.get("exclusionReason", ""):
                issues.append(Issue("ERROR", "PARTITIONED_BUT_EXCLUDED", prefix))
            for required_group in ("derivativeGroupId", "sourceGroupId"):
                if not row.get(required_group, ""):
                    issues.append(Issue("ERROR", "MISSING_REQUIRED_GROUP", f"{prefix}: {required_group}"))

        checksum = row.get("checksumSha256", "")
        if checksum:
            if SHA256_RE.fullmatch(checksum):
                hashes[checksum.lower()].append(index)
            else:
                issues.append(Issue("ERROR", "INVALID_SHA256", prefix))
        elif partition in FINAL_PARTITIONS:
            issues.append(Issue("ERROR", "MISSING_SHA256", prefix))

        source_name, source_item = row.get("sourceName", ""), row.get("sourceItemId", "")
        if source_name and source_item:
            source_items[(source_name, source_item)].append(index)

        individual_group_applies = sample_type == "POSITIVE" or (
            sample_type == "OOD" and row.get("oodCategory", "") in FISH_LIKE_OOD_CATEGORIES
        )
        for field in COUNTED_GROUP_FIELDS:
            value = row.get(field, "")
            applicable = field != "individualFishGroupId" or individual_group_applies
            if not value and applicable:
                missing_groups[field] += 1
                if partition in FINAL_PARTITIONS and field in {
                    "sessionGroupId",
                    "individualFishGroupId",
                }:
                    issues.append(Issue("WARNING", "MISSING_GROUP_METADATA", f"{prefix}: {field}"))
            if field in GROUP_FIELDS and value and partition in FINAL_PARTITIONS:
                groups[field][value].add(partition)

        original_or_derivative = row.get("originalOrDerivative", "")
        if original_or_derivative not in {"ORIGINAL", "DERIVATIVE"}:
            issues.append(Issue("ERROR", "INVALID_ORIGINAL_STATUS", prefix))
        if original_or_derivative == "DERIVATIVE" and not row.get("parentImageId", ""):
            issues.append(Issue("ERROR", "DERIVATIVE_WITHOUT_PARENT", prefix))

    for image_id, line_numbers in internal_ids.items():
        if len(line_numbers) > 1:
            issues.append(Issue("ERROR", "DUPLICATE_INTERNAL_ID", f"{image_id}: rows {line_numbers}"))
    for checksum, line_numbers in hashes.items():
        if len(line_numbers) > 1:
            issues.append(Issue("ERROR", "DUPLICATE_SHA256", f"{checksum}: rows {line_numbers}"))
    for source_item, line_numbers in source_items.items():
        if len(line_numbers) > 1:
            issues.append(
                Issue("ERROR", "DUPLICATE_SOURCE_ITEM", f"{source_item}: rows {line_numbers}")
            )
    for field, values in groups.items():
        for group_id, partitions in values.items():
            if len(partitions) > 1:
                issues.append(
                    Issue(
                        "ERROR",
                        "GROUP_LEAKAGE",
                        f"{field}={group_id!r} crosses {sorted(partitions)}",
                    )
                )

    _validate_perceptual_hashes(rows, issues)
    for field, count in missing_groups.items():
        if count:
            issues.append(
                Issue("WARNING", "GROUP_COVERAGE", f"{field}: missing for {count}/{len(rows)} rows")
            )
    return ValidationResult(issues, rows, missing_groups)


def _validate_perceptual_hashes(rows: list[dict[str, str]], issues: list[Issue]) -> None:
    tree = BKTree()
    parsed: dict[int, int] = {}
    for row_index, row in enumerate(rows):
        value = row.get("perceptualHash", "")
        partition = row.get("datasetPartition", "")
        image_id = row.get("internalImageId", f"row-{row_index + 2}")
        if not value:
            if partition in FINAL_PARTITIONS:
                issues.append(Issue("WARNING", "MISSING_PERCEPTUAL_HASH", image_id))
            continue
        if not PHASH_RE.fullmatch(value):
            issues.append(Issue("ERROR", "INVALID_PERCEPTUAL_HASH", image_id))
            continue
        numeric = int(value, 16)
        for other_index, distance in tree.search(numeric, 10):
            other = rows[other_index]
            other_partition = other.get("datasetPartition", "")
            crosses = (
                partition in FINAL_PARTITIONS
                and other_partition in FINAL_PARTITIONS
                and partition != other_partition
            )
            if crosses:
                severity, code = "ERROR", "PERCEPTUAL_CROSS_PARTITION_REVIEW"
            else:
                severity = "WARNING"
                code = "PERCEPTUAL_DUPLICATE_REVIEW" if distance <= 6 else "PERCEPTUAL_SIMILAR_REVIEW"
            issues.append(
                Issue(
                    severity,
                    code,
                    f"{other.get('internalImageId')} and {image_id}: Hamming distance {distance}",
                )
            )
        parsed[row_index] = numeric
        tree.add(numeric, row_index)


def deterministic_partition(component_key: str, seed: str = "pescapr-partition-v1") -> str:
    """Return a stable 70/15/15 suggestion for one already-connected group component."""
    digest = hashlib.sha256(f"{seed}\0{component_key}".encode("utf-8")).digest()
    bucket = int.from_bytes(digest[:8], "big") % 10_000
    if bucket < 7_000:
        return "TRAIN"
    if bucket < 8_500:
        return "VALIDATION"
    return "TEST"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--metadata", type=Path, required=True)
    parser.add_argument(
        "--manifest",
        type=Path,
        default=Path("app/src/main/assets/fish_classifier_manifest.json"),
    )
    parser.add_argument("--warnings-as-errors", action="store_true")
    args = parser.parse_args()
    result = validate(args.metadata, args.manifest)
    for issue in result.issues:
        print(f"{issue.severity} {issue.code}: {issue.message}")
    print(
        f"SUMMARY rows={len(result.rows)} errors={len(result.errors)} "
        f"warnings={len(result.warnings)}"
    )
    return 1 if result.errors or (args.warnings_as_errors and result.warnings) else 0


if __name__ == "__main__":
    sys.exit(main())
