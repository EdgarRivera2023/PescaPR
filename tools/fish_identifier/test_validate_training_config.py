import copy
import json
import sys
import unittest
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
sys.path.insert(0, str(HERE))

from validate_training_config import (  # noqa: E402
    TrainingConfigError,
    load_manifest_labels,
    validate_prediction_records,
    validate_sample_metadata,
    validate_training_config,
)

MANIFEST = ROOT / "app/src/main/assets/fish_classifier_manifest.json"


class TrainingConfigTest(unittest.TestCase):
    def setUp(self):
        version, self.labels = load_manifest_labels(MANIFEST)
        self.config = {
            "schemaVersion": "pescapr-fish-training-config/v1",
            "experimentRunId": "synthetic-mobilenetv3-run-001",
            "architecture": {"family": "MobileNetV3-Large", "version": "1.0.0", "inputWidth": 224, "inputHeight": 224},
            "classifierManifestVersion": version,
            "outputIndexToFichaPezId": self.labels.copy(),
            "dataset": {
                "snapshotId": "approved-synthetic-snapshot-v1", "snapshotChecksumSha256": "a" * 64,
                "trainPartitionId": "train-v1", "trainPartitionRole": "TRAIN",
                "validationPartitionId": "validation-v1", "validationPartitionRole": "VALIDATION",
            },
            "randomSeed": 20260815, "batchSize": 16, "maximumEpochs": 40,
            "optimizer": "ADAM", "learningRate": 0.001, "fineTuningLearningRate": 0.0001,
            "pretrainedWeights": {"source": "ImageNet", "identity": "synthetic-mobilenetv3-imagenet-v1"},
            "stages": [{"stageId": "HEAD_ONLY", "backbone": "FROZEN", "classifierHead": "REPLACED_39_OUTPUTS"}],
            "augmentationPolicy": {"version": "fish-catch-conservative-v1", "enabled": False},
            "sourceRevision": "synthetic-source-revision", "evaluationOutputLogicalId": "synthetic-eval-output-001",
            "thresholdPolicyVersion": "UNSELECTED",
        }

    def test_valid_mobilenet_config_and_exact_manifest_order_pass(self):
        self.assertIs(validate_training_config(self.config, MANIFEST), self.config)

    def test_reordered_extra_missing_and_name_order_fail_closed(self):
        for labels in (list(reversed(self.labels)), self.labels[:-1], self.labels + ["extra"]):
            config = copy.deepcopy(self.config)
            config["outputIndexToFichaPezId"] = labels
            with self.assertRaises(TrainingConfigError):
                validate_training_config(config, MANIFEST)

    def test_train_validation_binding_and_locked_test_rejection(self):
        config = copy.deepcopy(self.config)
        config["dataset"]["lockedTestPartitionRole"] = "LOCKED_TEST"
        with self.assertRaises(TrainingConfigError):
            validate_training_config(config, MANIFEST)

    def test_seed_and_snapshot_are_preserved(self):
        validated = validate_training_config(self.config, MANIFEST)
        self.assertEqual(20260815, validated["randomSeed"])
        self.assertEqual("approved-synthetic-snapshot-v1", validated["dataset"]["snapshotId"])

    def test_stage_one_and_optional_stage_two(self):
        validate_training_config(self.config, MANIFEST)
        config = copy.deepcopy(self.config)
        config["stages"].append({"stageId": "CONTROLLED_FINE_TUNING", "backbone": "LATER_BLOCKS", "learningRate": 0.00005})
        validate_training_config(config, MANIFEST)
        config["stages"][1]["learningRate"] = 0.001
        with self.assertRaises(TrainingConfigError):
            validate_training_config(config, MANIFEST)

    def test_enabled_augmentation_requires_version_and_group_rule(self):
        config = copy.deepcopy(self.config)
        config["augmentationPolicy"] = {"version": "fish-catch-conservative-v1", "enabled": True, "operations": ["HORIZONTAL_FLIP"], "augmentedDerivativesCountAsIndependentGroups": False}
        validate_training_config(config, MANIFEST)
        config["augmentationPolicy"]["augmentedDerivativesCountAsIndependentGroups"] = True
        with self.assertRaises(TrainingConfigError):
            validate_training_config(config, MANIFEST)

    def test_augmented_derivative_retains_independent_group(self):
        validate_sample_metadata([
            {"sampleId": "source", "independentGroupId": "group-1"},
            {"sampleId": "augmented", "isAugmented": True, "augmentedFromSampleId": "source", "independentGroupId": "group-1"},
        ])
        with self.assertRaises(TrainingConfigError):
            validate_sample_metadata([{"sampleId": "source", "independentGroupId": "group-1"}, {"sampleId": "augmented", "isAugmented": True, "augmentedFromSampleId": "source", "independentGroupId": "group-2"}])

    def test_prediction_scores_align_to_manifest_and_support_ood(self):
        records = [
            {"sampleId": "fish-1", "partitionRole": "VALIDATION", "modelRunId": "run", "sampleType": "SUPPORTED", "trueFichaPezId": self.labels[0], "scores": [0.0] * 39},
            {"sampleId": "ood-1", "partitionRole": "VALIDATION", "modelRunId": "run", "sampleType": "OOD", "trueFichaPezId": None, "scores": [0.0] * 39},
        ]
        validate_prediction_records(records, self.labels)
        records[0]["scores"] = [0.0] * 38
        with self.assertRaises(TrainingConfigError):
            validate_prediction_records(records, self.labels)

    def test_threshold_is_not_selected(self):
        config = copy.deepcopy(self.config)
        config["scoreThreshold"] = 0.5
        with self.assertRaises(TrainingConfigError):
            validate_training_config(config, MANIFEST)

    def test_manifest_has_frozen_39_classes_and_no_model_artifact(self):
        manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
        self.assertEqual(39, len(self.labels))
        self.assertIsNone(manifest["modelFilename"])


if __name__ == "__main__":
    unittest.main()
