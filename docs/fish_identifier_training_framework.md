# Fish Identifier training framework contract

**Roadmap:** FI-B.2-FRAMEWORK  
**Status:** metadata/tooling only; FI-B.2 real training remains blocked  
**Schema:** `pescapr-fish-training-config/v1`

## 1. Purpose and current blocker

This document defines the reproducible contract a future Fish Identifier training run must satisfy.
It does not train a model. PescaPR still lacks enough rights-cleared, independent images for a
legitimate 39-class production training run; the current pilot and `pilot-snapshot-v1` prove
metadata mechanics, not training adequacy. FI-B.2 therefore remains open.

The dependency-free validator `tools/fish_identifier/validate_training_config.py` checks metadata
against the frozen classifier manifest. It does not inspect arbitrary image folders, discover files,
download weights, approve data, or invoke an ML framework.

## 2. Experiment identity and architecture

Each config supplies a caller-chosen `experimentRunId`, schema/config version, source revision,
random seed, dataset snapshot/checksum, partition identities, architecture version, and logical
evaluation-output identity. Core logic must never silently generate a random run ID.

The FI-B.1 ordering is:

1. `MobileNetV3-Large`, 224×224 — first controlled 39-class experiment;
2. `EfficientNet-Lite0`, 224×224 — fallback/comparator;
3. `MobileNetV2`, 224×224 — simple reproducibility baseline.

Only these current candidates are represented. The architecture field is metadata, not a TensorFlow,
PyTorch, LiteRT, or Android object.

## 3. Frozen output contract

The config must contain an explicit `outputIndexToFichaPezId` array that exactly equals the ordered
39-class `app/src/main/assets/fish_classifier_manifest.json`. Validation rejects reordered, missing,
extra, alphabetical, common-name, filesystem, or discovery-order labels. A classifier head may never
infer its output order from folders or display names.

The future head has exactly 39 outputs. The manifest remains immutable and is not modified by the
training framework.

## 4. Dataset and partition contract

Training consumes only an already-approved immutable snapshot binding with a snapshot ID, checksum,
TRAIN partition identity, and VALIDATION partition identity. Those identities must differ. The
config cannot include a LOCKED_TEST/TEST partition as a training input; locked TEST remains outside
ordinary fitting and tuning. `pilot-snapshot-v1` must not be treated as sufficient production
training data.

The framework does not discover or ingest image paths, bypass eligibility, approve candidates, alter
snapshots, or move locked rows. TRAIN is for fitting; VALIDATION is for architecture, preprocessing,
and tuning; locked TEST is evaluated only at defined frozen checkpoints.

## 5. Transfer-learning stages

Stage 1 is required and represented as:

- ImageNet-pretrained backbone;
- frozen backbone;
- replaced classifier head with 39 outputs;
- head-only training.

Stage 2 is optional and must explicitly document later-backbone unfreezing and a learning rate lower
than the head stage. It is used only when validation evidence justifies it; the framework does not
assume it is always needed.

The config records the pretrained source and exact weight identity. No weights are downloaded by the
validator.

## 6. Training settings and augmentation

The minimal reproducibility settings are batch size, maximum epochs, optimizer, head learning rate,
fine-tuning learning rate, early-stopping configuration when supplied, seed, architecture/input,
manifest version, augmentation-policy version, and provenance fields. GPU environments can still
contain nondeterministic operations, so the contract promises reproducibility inputs and best effort,
not universal bit-for-bit identity.

Augmentation is a versioned policy. The validator permits only the current conservative operation
names: safe horizontal flip, small rotation, limited crop/scale, brightness/contrast, and mild color
variation. Enabling augmentation requires a policy version and an explicit rule that augmented
derivatives do not count as independent evaluation groups. Transformations that remove markings,
alter morphology, or create implausible fish remain subject to later review.

## 7. Run provenance and prediction handoff

A future run record must retain the run/config and architecture versions, manifest version, exact
snapshot and partitions, seed, augmentation policy, pretrained-weight identity, executed stages,
source revision, checkpoint/model logical ID, and FI-A.8 evaluation-output logical ID.

The prediction contract validated by the same tooling contains sample ID, partition role, model-run
ID, sample type, optional canonical true `FichaPez.id`, and a score vector exactly 39 entries long
in manifest order. Supported samples require a canonical true label. OOD/non-fish development samples
may omit it and must not invent a supported truth label. Scores are ordinary outputs for FI-A.8;
this framework selects no confidence, margin, or rejection threshold.

## 8. Evaluation and export boundaries

Real outputs will be handed to FI-A.8 tooling and judged using FI-A.9 top-1/top-3, per-class,
macro, confusion-slice, OOD/non-fish, ambiguity, calibration, coverage, and locked-TEST gates.
Training metadata alone cannot pass those gates.

Export, float32/float16/INT8 comparison, Android runtime selection, device benchmarking, and model
artifact creation belong to later slices. No TensorFlow/PyTorch/LiteRT dependency, pretrained weight,
checkpoint, image, or Android inference code is introduced here.

## 9. Explicit non-goals

This framework does not train, fine-tune, download data or weights, create model binaries, export or
quantize models, modify the 39-class manifest, select FI-A.8 thresholds, use locked TEST for tuning,
change UI, add Firebase, or restart FI-CONTRIB. FI-B.2 remains incomplete until approved data is
actually used in a controlled training run.

