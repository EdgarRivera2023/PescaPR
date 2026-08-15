"""Validate dependency-free FI-B.2 training metadata and prediction contracts.

This module deliberately does not import an ML framework, inspect image folders, or train.
"""

from __future__ import annotations

import argparse
import json
import math
from pathlib import Path
from typing import Any


SUPPORTED_ARCHITECTURES = {
    ("MobileNetV3-Large", "1.0.0"),
    ("EfficientNet-Lite0", "1.0.0"),
    ("MobileNetV2", "1.0.0"),
}
ALLOWED_PARTITIONS = {"TRAIN", "VALIDATION", "LOCKED_TEST"}
TRAINING_PARTITIONS = {"TRAIN", "VALIDATION"}
SUPPORTED_OPERATIONS = {"HORIZONTAL_FLIP", "SMALL_ROTATION", "LIMITED_CROP_SCALE", "BRIGHTNESS_CONTRAST", "MILD_COLOR"}


class TrainingConfigError(ValueError):
    pass


def load_manifest_labels(path: Path) -> tuple[str, list[str]]:
    try:
        manifest = json.loads(path.read_text(encoding="utf-8"))
        classes = manifest["classes"]
        labels = [item["fichaPezId"] for item in sorted(classes, key=lambda item: item["index"])]
        if [item["index"] for item in classes] != list(range(len(classes))):
            raise TrainingConfigError("Manifest indexes must be contiguous and ordered")
        if len(labels) != manifest["expectedClassCount"] or len(set(labels)) != len(labels):
            raise TrainingConfigError("Manifest class count or labels are invalid")
        return manifest["catalogVersion"], labels
    except (OSError, json.JSONDecodeError, KeyError, TypeError) as error:
        raise TrainingConfigError(f"Invalid classifier manifest: {error}") from error


def _required(mapping: dict[str, Any], key: str) -> Any:
    if key not in mapping or mapping[key] in (None, ""):
        raise TrainingConfigError(f"Missing required field: {key}")
    return mapping[key]


def _number(value: Any, field: str, minimum: float = 0.0) -> float:
    if isinstance(value, bool) or not isinstance(value, (int, float)) or not math.isfinite(value) or value < minimum:
        raise TrainingConfigError(f"Invalid numeric field: {field}")
    return float(value)


def validate_augmentation_policy(policy: dict[str, Any]) -> None:
    version = _required(policy, "version")
    if not isinstance(version, str):
        raise TrainingConfigError("Augmentation policy version must be text")
    if policy.get("enabled") is not True:
        return
    operations = policy.get("operations")
    if not isinstance(operations, list) or not operations:
        raise TrainingConfigError("Enabled augmentation requires operations")
    if any(operation not in SUPPORTED_OPERATIONS for operation in operations):
        raise TrainingConfigError("Unsupported augmentation operation")
    if policy.get("augmentedDerivativesCountAsIndependentGroups") is not False:
        raise TrainingConfigError("Augmentations must not count as independent groups")


def validate_training_config(config: dict[str, Any], manifest_path: Path) -> dict[str, Any]:
    if not isinstance(config, dict) or config.get("schemaVersion") != "pescapr-fish-training-config/v1":
        raise TrainingConfigError("Unsupported training configuration schema")
    catalog_version, labels = load_manifest_labels(manifest_path)
    if _required(config, "classifierManifestVersion") != catalog_version:
        raise TrainingConfigError("Classifier manifest version mismatch")
    if config.get("outputIndexToFichaPezId") != labels:
        raise TrainingConfigError("Output ordering does not exactly match frozen manifest")
    if any(key in config for key in ("confidenceThreshold", "scoreThreshold", "marginThreshold")):
        raise TrainingConfigError("Training config must not select an FI-A.8 threshold")
    if config.get("thresholdPolicyVersion", "UNSELECTED") != "UNSELECTED":
        raise TrainingConfigError("Training config threshold policy must remain UNSELECTED")

    architecture = _required(config, "architecture")
    if not isinstance(architecture, dict) or (architecture.get("family"), architecture.get("version")) not in SUPPORTED_ARCHITECTURES:
        raise TrainingConfigError("Unsupported architecture configuration")
    if architecture.get("inputWidth") != 224 or architecture.get("inputHeight") != 224:
        raise TrainingConfigError("Initial training framework requires 224x224 input")

    _required(config, "experimentRunId")
    seed = _required(config, "randomSeed")
    if isinstance(seed, bool) or not isinstance(seed, int) or seed < 0:
        raise TrainingConfigError("randomSeed must be a caller-supplied non-negative integer")
    for field in ("batchSize", "maximumEpochs"):
        value = _required(config, field)
        if isinstance(value, bool) or not isinstance(value, int) or value <= 0:
            raise TrainingConfigError(f"{field} must be a positive integer")
    learning_rate = _number(_required(config, "learningRate"), "learningRate", 1e-12)
    fine_tuning_rate = _number(_required(config, "fineTuningLearningRate"), "fineTuningLearningRate", 1e-12)
    if fine_tuning_rate >= learning_rate:
        raise TrainingConfigError("Fine-tuning learning rate must be lower than head learning rate")
    if _required(config, "optimizer") not in {"ADAM", "SGD"}:
        raise TrainingConfigError("Unsupported optimizer")
    pretrained = _required(config, "pretrainedWeights")
    if not isinstance(pretrained, dict) or not _required(pretrained, "source") or not _required(pretrained, "identity"):
        raise TrainingConfigError("Pretrained-weight identity is incomplete")

    dataset = _required(config, "dataset")
    if not isinstance(dataset, dict):
        raise TrainingConfigError("Dataset binding must be an object")
    for field in ("snapshotId", "snapshotChecksumSha256", "trainPartitionId", "validationPartitionId"):
        _required(dataset, field)
    if dataset["trainPartitionRole"] != "TRAIN" or dataset["validationPartitionRole"] != "VALIDATION":
        raise TrainingConfigError("Training requires TRAIN and VALIDATION roles")
    if dataset.get("lockedTestPartitionRole") in {"LOCKED_TEST", "TEST"} or dataset.get("lockedTestPartitionId"):
        raise TrainingConfigError("LOCKED_TEST must not be supplied as a training partition")
    if dataset.get("trainPartitionId") == dataset.get("validationPartitionId"):
        raise TrainingConfigError("TRAIN and VALIDATION partition identities must differ")

    stages = _required(config, "stages")
    if not isinstance(stages, list) or not stages or stages[0] != {
        "stageId": "HEAD_ONLY",
        "backbone": "FROZEN",
        "classifierHead": "REPLACED_39_OUTPUTS",
    }:
        raise TrainingConfigError("Stage 1 must freeze the backbone and replace the 39-output head")
    for stage in stages[1:]:
        if stage.get("stageId") != "CONTROLLED_FINE_TUNING" or stage.get("backbone") != "LATER_BLOCKS":
            raise TrainingConfigError("Unsupported fine-tuning stage")
        if stage.get("learningRate") is None or _number(stage["learningRate"], "stage learningRate", 1e-12) > fine_tuning_rate:
            raise TrainingConfigError("Fine-tuning stage must use a lower documented learning rate")
    validate_augmentation_policy(_required(config, "augmentationPolicy"))
    _required(config, "sourceRevision")
    _required(config, "evaluationOutputLogicalId")
    return config


def validate_sample_metadata(rows: list[dict[str, Any]]) -> None:
    groups = {row.get("sampleId"): row.get("independentGroupId") for row in rows}
    for row in rows:
        if row.get("isAugmented"):
            source_id = _required(row, "augmentedFromSampleId")
            if source_id not in groups or row.get("independentGroupId") != groups[source_id]:
                raise TrainingConfigError("Augmented derivative must retain its source independent group")


def validate_prediction_records(records: list[dict[str, Any]], labels: list[str]) -> None:
    for record in records:
        for field in ("sampleId", "partitionRole", "modelRunId", "scores"):
            _required(record, field)
        if record["partitionRole"] not in ALLOWED_PARTITIONS:
            raise TrainingConfigError("Invalid prediction partition role")
        scores = record["scores"]
        if not isinstance(scores, list) or len(scores) != len(labels) or any(
            isinstance(value, bool) or not isinstance(value, (int, float)) or not math.isfinite(value) for value in scores
        ):
            raise TrainingConfigError("Prediction score vector must match manifest class count")
        sample_type = record.get("sampleType", "SUPPORTED")
        truth = record.get("trueFichaPezId")
        if sample_type == "OOD":
            if truth not in (None, ""):
                raise TrainingConfigError("OOD predictions cannot carry a supported true label")
        elif sample_type == "SUPPORTED":
            if truth not in labels:
                raise TrainingConfigError("Supported prediction requires a canonical true label")
        else:
            raise TrainingConfigError("Invalid prediction sample type")


def _main() -> int:
    parser = argparse.ArgumentParser(description="Validate PescaPR FI-B.2 training metadata without ML frameworks or images")
    parser.add_argument("--config", type=Path, required=True)
    parser.add_argument("--manifest", type=Path, required=True)
    args = parser.parse_args()
    try:
        validate_training_config(json.loads(args.config.read_text(encoding="utf-8")), args.manifest)
    except (OSError, json.JSONDecodeError, TrainingConfigError) as error:
        parser.error(str(error))
    print("training config: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(_main())
