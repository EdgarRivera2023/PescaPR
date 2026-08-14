import csv
import json
import sys
import tempfile
import unittest
from pathlib import Path
from types import SimpleNamespace


HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
sys.path.insert(0, str(HERE))

from create_snapshot import (  # noqa: E402
    SnapshotError,
    build_manifest,
    create_snapshot,
    verify_snapshot,
)


MANIFEST = ROOT / "app/src/main/assets/fish_classifier_manifest.json"
VALIDATOR = HERE / "validate_dataset.py"
FIXTURES = HERE / "fixtures"


class SnapshotToolTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.directory = Path(self.temporary.name)
        self.metadata = self.directory / "metadata.csv"
        self.metadata.write_bytes((FIXTURES / "partition_valid.csv").read_bytes())
        self.approvals = self.directory / "approvals.csv"
        self.approvals.write_text(
            "internalImageId,testsetVersion,decision,reviewer,reviewDate,notes\n"
            "fake-ood-001,testset-v1,APPROVE_FOR_LOCKED_TEST,Fixture Approver,2026-08-14,\n",
            encoding="utf-8",
        )
        self.dispositions = self.directory / "dispositions.csv"
        self.dispositions.write_text(
            "warningCode,matchText,classification,status,reviewer,reviewDate,rationale\n",
            encoding="utf-8",
        )
        self.overrides = self.directory / "overrides.csv"
        self.overrides.write_text(
            "internalImageId,fromPartition,toPartition,decision,reviewer,reviewDate,reason\n",
            encoding="utf-8",
        )

    def tearDown(self):
        self.temporary.cleanup()

    def build(self, metadata=None):
        return build_manifest(
            metadata_path=metadata or self.metadata,
            classifier_manifest_path=MANIFEST,
            validator_path=VALIDATOR,
            approvals_path=self.approvals,
            dispositions_path=self.dispositions,
            overrides_path=self.overrides,
            snapshot_id="pilot-snapshot-v1",
            testset_version="testset-v1",
            created_at="2026-08-14T00:00:00Z",
        )

    def args(self):
        return SimpleNamespace(
            metadata=self.metadata,
            classifier_manifest=MANIFEST,
            validator=VALIDATOR,
            approvals=self.approvals,
            warning_dispositions=self.dispositions,
            partition_overrides=self.overrides,
            snapshot_root=self.directory / "snapshots",
            snapshot_id="pilot-snapshot-v1",
            testset_version="testset-v1",
            created_at="2026-08-14T00:00:00Z",
            repository_manifest_root=None,
        )

    def test_manifest_creation_is_deterministic_and_counts_are_correct(self):
        first = self.build()
        second = self.build()
        self.assertEqual(first, second)
        self.assertEqual(
            {"total": 3, "positive": 2, "ood": 1, "train": 2, "validation": 0, "test": 1},
            first["counts"],
        )

    def test_changed_metadata_changes_snapshot_checksum(self):
        first = self.build()
        with self.metadata.open(newline="", encoding="utf-8") as handle:
            reader = csv.DictReader(handle)
            fields = reader.fieldnames
            rows = list(reader)
        rows[0]["notes"] = "changed synthetic note"
        with self.metadata.open("w", newline="", encoding="utf-8") as handle:
            writer = csv.DictWriter(handle, fieldnames=fields)
            writer.writeheader()
            writer.writerows(rows)
        second = self.build()
        self.assertNotEqual(first["snapshotChecksumSha256"], second["snapshotChecksumSha256"])

    def test_missing_test_approval_blocks_snapshot(self):
        self.approvals.write_text(
            "internalImageId,testsetVersion,decision,reviewer,reviewDate,notes\n"
            "fake-ood-001,testset-v1,PENDING,,,\n",
            encoding="utf-8",
        )
        with self.assertRaises(SnapshotError):
            self.build()

    def test_invalid_dataset_blocks_snapshot(self):
        invalid = FIXTURES / "partition_invalid.csv"
        with self.assertRaises(SnapshotError):
            self.build(invalid)

    def test_existing_snapshot_detects_metadata_mutation(self):
        manifest_path = create_snapshot(self.args())
        copied_metadata = manifest_path.parent / "pilot_metadata.csv"
        copied_metadata.write_text(copied_metadata.read_text(encoding="utf-8") + "\n", encoding="utf-8")
        with self.assertRaises(SnapshotError):
            verify_snapshot(manifest_path, MANIFEST, VALIDATOR)

    def test_existing_snapshot_id_refuses_different_content(self):
        create_snapshot(self.args())
        with self.metadata.open(newline="", encoding="utf-8") as handle:
            reader = csv.DictReader(handle)
            fields = reader.fieldnames
            rows = list(reader)
        rows[0]["notes"] = "valid content change after snapshot creation"
        with self.metadata.open("w", newline="", encoding="utf-8") as handle:
            writer = csv.DictWriter(handle, fieldnames=fields)
            writer.writeheader()
            writer.writerows(rows)
        with self.assertRaises(SnapshotError):
            create_snapshot(self.args())


if __name__ == "__main__":
    unittest.main()
