import csv
import sys
import tempfile
import unittest
from pathlib import Path


HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
sys.path.insert(0, str(HERE))

from validate_dataset import deterministic_partition, validate  # noqa: E402


MANIFEST = ROOT / "app/src/main/assets/fish_classifier_manifest.json"
FIXTURES = HERE / "fixtures"


class DatasetValidatorTest(unittest.TestCase):
    def test_valid_fixture_has_no_errors(self):
        result = validate(FIXTURES / "partition_valid.csv", MANIFEST)
        self.assertEqual([], result.errors)

    def test_invalid_fixture_catches_required_failures(self):
        result = validate(FIXTURES / "partition_invalid.csv", MANIFEST)
        codes = {issue.code for issue in result.errors}
        self.assertTrue(
            {
                "DUPLICATE_SHA256",
                "DUPLICATE_SOURCE_ITEM",
                "GROUP_LEAKAGE",
                "PERCEPTUAL_CROSS_PARTITION_REVIEW",
                "UNKNOWN_CANONICAL_ID",
                "RIGHTS_NOT_APPROVED",
            }.issubset(codes)
        )

    def test_valid_ood_row_has_no_positive_label(self):
        result = validate(FIXTURES / "partition_valid.csv", MANIFEST)
        ood = next(row for row in result.rows if row["sampleType"] == "OOD")
        self.assertEqual("", ood["fichaPezId"])
        self.assertEqual("boat", ood["oodCategory"])

    def test_non_fish_ood_does_not_require_individual_fish_group(self):
        result = validate(FIXTURES / "partition_valid.csv", MANIFEST)
        messages = [issue.message for issue in result.warnings]
        self.assertFalse(any("fake-ood-001" in message and "individualFishGroupId" in message for message in messages))

    def test_fish_like_ood_still_requires_individual_fish_group(self):
        with (FIXTURES / "partition_valid.csv").open(newline="", encoding="utf-8") as handle:
            reader = csv.DictReader(handle)
            fieldnames = reader.fieldnames
            rows = list(reader)
        next(row for row in rows if row["sampleType"] == "OOD")["oodCategory"] = "unsupported_fish"
        with tempfile.TemporaryDirectory() as directory:
            metadata = Path(directory) / "fish-like.csv"
            with metadata.open("w", newline="", encoding="utf-8") as handle:
                writer = csv.DictWriter(handle, fieldnames=fieldnames)
                writer.writeheader()
                writer.writerows(rows)
            result = validate(metadata, MANIFEST)
        self.assertTrue(
            any(
                issue.code == "MISSING_GROUP_METADATA"
                and "individualFishGroupId" in issue.message
                for issue in result.warnings
            )
        )

    def test_partition_suggestion_is_deterministic(self):
        first = deterministic_partition("source-group-123", "test-seed")
        self.assertEqual(first, deterministic_partition("source-group-123", "test-seed"))
        self.assertIn(first, {"TRAIN", "VALIDATION", "TEST"})

    def test_locked_row_cannot_silently_move_out_of_test(self):
        with (FIXTURES / "partition_valid.csv").open(newline="", encoding="utf-8") as handle:
            reader = csv.DictReader(handle)
            fieldnames = reader.fieldnames
            rows = list(reader)
        rows[0]["partitionLockStatus"] = "LOCKED"
        with tempfile.TemporaryDirectory() as directory:
            metadata = Path(directory) / "locked-train.csv"
            with metadata.open("w", newline="", encoding="utf-8") as handle:
                writer = csv.DictWriter(handle, fieldnames=fieldnames)
                writer.writeheader()
                writer.writerows(rows)
            result = validate(metadata, MANIFEST)
        self.assertIn("LOCKED_NON_TEST", {issue.code for issue in result.errors})


if __name__ == "__main__":
    unittest.main()
