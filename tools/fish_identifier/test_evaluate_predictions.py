import csv
import json
import sys
import tempfile
import unittest
from pathlib import Path


HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
sys.path.insert(0, str(HERE))

from evaluate_predictions import (  # noqa: E402
    EvaluationError,
    evaluate,
    load_manifest,
    load_predictions,
    load_run_metadata,
    threshold_sweep,
)


MANIFEST = ROOT / "app/src/main/assets/fish_classifier_manifest.json"
FIXTURES = HERE / "fixtures"
PREDICTIONS = FIXTURES / "evaluation_predictions_synthetic.csv"
RUN = FIXTURES / "evaluation_run_synthetic.json"


class PredictionEvaluationTest(unittest.TestCase):
    def setUp(self):
        self.labels = load_manifest(MANIFEST)
        self.rows = load_predictions(PREDICTIONS, self.labels)

    def test_declared_policy_metrics(self):
        result = evaluate(self.rows)
        self.assertEqual({"total": 4, "positive": 2, "ood": 2}, result["counts"])
        self.assertEqual(0.5, result["supportedPositive"]["top1Accuracy"])
        self.assertEqual(1.0, result["supportedPositive"]["top3Accuracy"])
        self.assertEqual(0.5, result["supportedPositive"]["acceptanceRate"])
        self.assertEqual(0.5, result["supportedPositive"]["ambiguousRate"])
        self.assertEqual(0.5, result["ood"]["rejectionRate"])
        self.assertEqual(0.5, result["ood"]["falseAcceptanceRate"])
        self.assertEqual(0.5, result["perClass"]["0SCZ4miCcNiVY684bCwg"]["top1Precision"])

    def test_threshold_sweep_changes_rejection_behavior(self):
        results = threshold_sweep(self.rows, [0.6, 0.8], [0.1])
        first, second = results
        self.assertEqual(0.5, first["metrics"]["ood"]["falseAcceptanceRate"])
        self.assertEqual(0.0, second["metrics"]["ood"]["falseAcceptanceRate"])
        self.assertEqual(0.5, first["metrics"]["supportedPositive"]["falseRejectionRate"])
        self.assertEqual(0.5, second["metrics"]["supportedPositive"]["falseRejectionRate"])

    def test_run_metadata_binds_classifier_checksum(self):
        run = load_run_metadata(RUN, MANIFEST)
        self.assertEqual("synthetic-run-only", run["evaluationRunId"])
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "run.json"
            changed = dict(run)
            changed["classifierManifestChecksum"] = "0" * 64
            path.write_text(json.dumps(changed), encoding="utf-8")
            with self.assertRaises(EvaluationError):
                load_run_metadata(path, MANIFEST)

    def test_run_metadata_binds_snapshot_and_testset_versions(self):
        snapshot_path = ROOT / "datasets/fish_identifier/snapshots/pilot-snapshot-v1/snapshot_manifest.json"
        snapshot = json.loads(snapshot_path.read_text(encoding="utf-8"))
        run = json.loads(RUN.read_text(encoding="utf-8"))
        run["datasetSnapshotId"] = snapshot["snapshotId"]
        run["testsetVersion"] = snapshot["testset"]["testsetVersion"]
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "run.json"
            path.write_text(json.dumps(run), encoding="utf-8")
            loaded = load_run_metadata(path, MANIFEST, snapshot_path)
            self.assertEqual("pilot-snapshot-v1", loaded["datasetSnapshotId"])
            run["testsetVersion"] = "wrong-version"
            path.write_text(json.dumps(run), encoding="utf-8")
            with self.assertRaises(EvaluationError):
                load_run_metadata(path, MANIFEST, snapshot_path)

    def test_ood_cannot_carry_positive_truth(self):
        with PREDICTIONS.open(newline="", encoding="utf-8") as handle:
            reader = csv.DictReader(handle)
            fields = reader.fieldnames
            rows = list(reader)
        next(row for row in rows if row["sampleType"] == "OOD")["trueFichaPezId"] = "0SCZ4miCcNiVY684bCwg"
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "invalid.csv"
            with path.open("w", newline="", encoding="utf-8") as handle:
                writer = csv.DictWriter(handle, fieldnames=fields)
                writer.writeheader()
                writer.writerows(rows)
            with self.assertRaises(EvaluationError):
                load_predictions(path, self.labels)

    def test_repository_prediction_template_is_header_only(self):
        template = ROOT / "datasets/fish_identifier/evaluation/prediction_template.csv"
        with template.open(newline="", encoding="utf-8") as handle:
            reader = csv.DictReader(handle)
            self.assertEqual([], list(reader))
            self.assertIn("decisionState", reader.fieldnames)
            self.assertIn("acceptedFichaPezId", reader.fieldnames)


if __name__ == "__main__":
    unittest.main()
