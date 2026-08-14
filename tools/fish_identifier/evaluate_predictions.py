#!/usr/bin/env python3
"""Evaluate future PescaPR 39-class predictions and rejection policies offline."""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import math
import sys
from collections import Counter, defaultdict
from pathlib import Path


DECISION_STATES = {
    "ACCEPTED",
    "AMBIGUOUS",
    "REJECTED_LOW_CONFIDENCE",
    "REJECTED_OOD",
    "INVALID_INPUT",
}
REJECTION_STATES = {"REJECTED_LOW_CONFIDENCE", "REJECTED_OOD", "INVALID_INPUT"}
REQUIRED_RUN_FIELDS = (
    "evaluationRunId", "modelVersion", "modelChecksum", "classifierManifestChecksum",
    "datasetSnapshotId", "testsetVersion", "preprocessingVersion", "timestamp",
    "thresholdPolicyVersion",
)
REQUIRED_PREDICTION_FIELDS = (
    "internalImageId", "sampleType", "trueFichaPezId", "oodCategory",
    "top1FichaPezId", "top1Score", "top2FichaPezId", "top2Score",
    "top3FichaPezId", "top3Score", "scoreMargin", "entropy",
    "decisionState", "acceptedFichaPezId", "correctTop1", "correctTop3",
    "falseAccept", "falseReject", "notes",
)


class EvaluationError(RuntimeError):
    pass


def sha256_file(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load_manifest(path: Path) -> set[str]:
    content = json.loads(path.read_text(encoding="utf-8"))
    labels = {row["fichaPezId"] for row in content["classes"]}
    if len(labels) != content["expectedClassCount"]:
        raise EvaluationError("Classifier manifest label contract is inconsistent")
    return labels


def load_run_metadata(path: Path, classifier_manifest: Path, snapshot_manifest: Path | None = None) -> dict:
    run = json.loads(path.read_text(encoding="utf-8"))
    missing = [field for field in REQUIRED_RUN_FIELDS if not run.get(field)]
    if missing:
        raise EvaluationError("Run metadata has unresolved fields: " + ", ".join(missing))
    if run["classifierManifestChecksum"] != sha256_file(classifier_manifest):
        raise EvaluationError("Run classifier-manifest checksum does not match the frozen manifest")
    if snapshot_manifest:
        snapshot = json.loads(snapshot_manifest.read_text(encoding="utf-8"))
        if run["datasetSnapshotId"] != snapshot.get("snapshotId"):
            raise EvaluationError("Run datasetSnapshotId does not match snapshot manifest")
        if run["testsetVersion"] != snapshot.get("testset", {}).get("testsetVersion"):
            raise EvaluationError("Run testsetVersion does not match snapshot manifest")
    return run


def _number(value: str, field: str, image_id: str, optional: bool = False) -> float | None:
    if optional and not value:
        return None
    try:
        number = float(value)
    except ValueError as error:
        raise EvaluationError(f"{image_id}: invalid {field}") from error
    if not math.isfinite(number):
        raise EvaluationError(f"{image_id}: non-finite {field}")
    return number


def load_predictions(path: Path, labels: set[str]) -> list[dict]:
    with path.open(newline="", encoding="utf-8-sig") as handle:
        reader = csv.DictReader(handle)
        missing = [field for field in REQUIRED_PREDICTION_FIELDS if field not in (reader.fieldnames or [])]
        if missing:
            raise EvaluationError("Prediction file missing columns: " + ", ".join(missing))
        raw_rows = list(reader)
    if not raw_rows:
        raise EvaluationError("Prediction file has no rows")
    rows = []
    seen = set()
    for raw in raw_rows:
        image_id = raw["internalImageId"].strip()
        if not image_id or image_id in seen:
            raise EvaluationError(f"Missing or duplicate internalImageId: {image_id!r}")
        seen.add(image_id)
        sample_type = raw["sampleType"].strip()
        true_id = raw["trueFichaPezId"].strip()
        category = raw["oodCategory"].strip()
        if sample_type == "POSITIVE":
            if true_id not in labels or category:
                raise EvaluationError(f"{image_id}: invalid positive truth semantics")
        elif sample_type == "OOD":
            if true_id or not category:
                raise EvaluationError(f"{image_id}: invalid OOD truth semantics")
        else:
            raise EvaluationError(f"{image_id}: invalid sampleType {sample_type!r}")
        ranked_ids = [raw[f"top{rank}FichaPezId"].strip() for rank in (1, 2, 3)]
        if any(label not in labels for label in ranked_ids) or len(set(ranked_ids)) != 3:
            raise EvaluationError(f"{image_id}: top-3 labels must be distinct frozen FichaPez.id values")
        scores = [_number(raw[f"top{rank}Score"].strip(), f"top{rank}Score", image_id) for rank in (1, 2, 3)]
        if not scores[0] >= scores[1] >= scores[2]:
            raise EvaluationError(f"{image_id}: scores do not follow declared rank order")
        margin = scores[0] - scores[1]
        supplied_margin = _number(raw["scoreMargin"].strip(), "scoreMargin", image_id, optional=True)
        if supplied_margin is not None and not math.isclose(supplied_margin, margin, rel_tol=1e-7, abs_tol=1e-9):
            raise EvaluationError(f"{image_id}: scoreMargin does not equal top1Score - top2Score")
        entropy = _number(raw["entropy"].strip(), "entropy", image_id, optional=True)
        state = raw["decisionState"].strip()
        if state not in DECISION_STATES:
            raise EvaluationError(f"{image_id}: invalid decisionState {state!r}")
        accepted = raw["acceptedFichaPezId"].strip()
        if state == "ACCEPTED" and accepted not in labels:
            raise EvaluationError(f"{image_id}: ACCEPTED requires a frozen acceptedFichaPezId")
        if state != "ACCEPTED" and accepted:
            raise EvaluationError(f"{image_id}: non-ACCEPTED state cannot carry acceptedFichaPezId")
        rows.append({
            "internalImageId": image_id, "sampleType": sample_type,
            "trueFichaPezId": true_id, "oodCategory": category,
            "rankedIds": ranked_ids, "scores": scores, "scoreMargin": margin,
            "entropy": entropy, "decisionState": state, "acceptedFichaPezId": accepted,
        })
    return rows


def _rate(numerator: int, denominator: int) -> float | None:
    return numerator / denominator if denominator else None


def evaluate(rows: list[dict]) -> dict:
    positives = [row for row in rows if row["sampleType"] == "POSITIVE"]
    ood = [row for row in rows if row["sampleType"] == "OOD"]
    top1_correct = sum(row["rankedIds"][0] == row["trueFichaPezId"] for row in positives)
    top3_correct = sum(row["trueFichaPezId"] in row["rankedIds"] for row in positives)
    supported_accepted = [row for row in positives if row["decisionState"] == "ACCEPTED"]
    supported_rejected = [row for row in positives if row["decisionState"] in REJECTION_STATES]
    supported_ambiguous = [row for row in positives if row["decisionState"] == "AMBIGUOUS"]
    incorrect_accepted = [row for row in supported_accepted if row["acceptedFichaPezId"] != row["trueFichaPezId"]]
    ood_rejected = [row for row in ood if row["decisionState"] in REJECTION_STATES]
    ood_false_accepted = [row for row in ood if row["decisionState"] == "ACCEPTED"]
    ood_ambiguous = [row for row in ood if row["decisionState"] == "AMBIGUOUS"]
    by_class = {}
    for fish_id in sorted({row["trueFichaPezId"] for row in positives}):
        group = [row for row in positives if row["trueFichaPezId"] == fish_id]
        predicted_as_class = [row for row in positives if row["rankedIds"][0] == fish_id]
        class_top1_correct = sum(row["rankedIds"][0] == fish_id for row in group)
        by_class[fish_id] = {
            "total": len(group),
            "top1Correct": class_top1_correct,
            "top1Recall": _rate(class_top1_correct, len(group)),
            "top1Precision": _rate(class_top1_correct, len(predicted_as_class)),
            "top3Correct": sum(fish_id in row["rankedIds"] for row in group),
            "top3Recall": _rate(sum(fish_id in row["rankedIds"] for row in group), len(group)),
            "accepted": sum(row["decisionState"] == "ACCEPTED" for row in group),
            "falseRejected": sum(row["decisionState"] in REJECTION_STATES for row in group),
            "incorrectAccepted": sum(row["decisionState"] == "ACCEPTED" and row["acceptedFichaPezId"] != fish_id for row in group),
        }
    by_ood = {}
    for category in sorted({row["oodCategory"] for row in ood}):
        group = [row for row in ood if row["oodCategory"] == category]
        by_ood[category] = {
            "total": len(group),
            "rejected": sum(row["decisionState"] in REJECTION_STATES for row in group),
            "falseAccepted": sum(row["decisionState"] == "ACCEPTED" for row in group),
            "ambiguous": sum(row["decisionState"] == "AMBIGUOUS" for row in group),
        }
    confusion = Counter((row["trueFichaPezId"], row["rankedIds"][0]) for row in positives)
    return {
        "counts": {"total": len(rows), "positive": len(positives), "ood": len(ood)},
        "supportedPositive": {
            "top1Accuracy": _rate(top1_correct, len(positives)),
            "top3Accuracy": _rate(top3_correct, len(positives)),
            "acceptanceRate": _rate(len(supported_accepted), len(positives)),
            "falseRejectionRate": _rate(len(supported_rejected), len(positives)),
            "ambiguousRate": _rate(len(supported_ambiguous), len(positives)),
            "incorrectAcceptedIdentificationRate": _rate(len(incorrect_accepted), len(supported_accepted)),
        },
        "ood": {
            "rejectionRate": _rate(len(ood_rejected), len(ood)),
            "falseAcceptanceRate": _rate(len(ood_false_accepted), len(ood)),
            "ambiguousRate": _rate(len(ood_ambiguous), len(ood)),
        },
        "perClass": by_class,
        "perOodCategory": by_ood,
        "confusionMatrixInputs": [
            {"trueFichaPezId": truth, "top1FichaPezId": predicted, "count": count}
            for (truth, predicted), count in sorted(confusion.items())
        ],
    }


def threshold_sweep(rows: list[dict], top1_thresholds: list[float], margin_thresholds: list[float]) -> list[dict]:
    results = []
    for top1 in top1_thresholds:
        for margin in margin_thresholds:
            evaluated = []
            for row in rows:
                copy = dict(row)
                accepted = row["scores"][0] >= top1 and row["scoreMargin"] >= margin
                copy["decisionState"] = "ACCEPTED" if accepted else "REJECTED_LOW_CONFIDENCE"
                copy["acceptedFichaPezId"] = row["rankedIds"][0] if accepted else ""
                evaluated.append(copy)
            results.append({"top1ScoreThreshold": top1, "marginThreshold": margin, "metrics": evaluate(evaluated)})
    return results


def parse_thresholds(value: str) -> list[float]:
    try:
        values = [float(item.strip()) for item in value.split(",") if item.strip()]
    except ValueError as error:
        raise EvaluationError(f"Invalid threshold list: {value!r}") from error
    if not values or any(not math.isfinite(item) for item in values):
        raise EvaluationError("Threshold lists must contain finite numeric values")
    return values


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--predictions", type=Path, required=True)
    parser.add_argument("--run-metadata", type=Path, required=True)
    parser.add_argument("--classifier-manifest", type=Path, required=True)
    parser.add_argument("--snapshot-manifest", type=Path)
    parser.add_argument("--top1-thresholds")
    parser.add_argument("--margin-thresholds")
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    try:
        labels = load_manifest(args.classifier_manifest)
        run = load_run_metadata(args.run_metadata, args.classifier_manifest, args.snapshot_manifest)
        rows = load_predictions(args.predictions, labels)
        result = {"runMetadata": run, "declaredPolicyMetrics": evaluate(rows), "thresholdSweeps": []}
        if bool(args.top1_thresholds) != bool(args.margin_thresholds):
            raise EvaluationError("Threshold sweeps require both --top1-thresholds and --margin-thresholds")
        if args.top1_thresholds:
            result["thresholdSweeps"] = threshold_sweep(
                rows, parse_thresholds(args.top1_thresholds), parse_thresholds(args.margin_thresholds)
            )
        encoded = json.dumps(result, ensure_ascii=False, sort_keys=True, indent=2) + "\n"
        if args.output:
            args.output.write_text(encoded, encoding="utf-8")
        else:
            print(encoded, end="")
        return 0
    except (EvaluationError, OSError, ValueError, json.JSONDecodeError) as error:
        print(f"evaluation error: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
