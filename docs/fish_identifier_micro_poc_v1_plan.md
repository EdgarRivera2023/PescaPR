# Fish Identifier technical micro-POC v1 plan

**Date:** 2026-08-14
**Status:** CONTRACT FROZEN — ACQUISITION NOT YET APPROVED
**Classification:** `EXPERIMENT_ONLY_NOT_PRODUCTION`

## 1. Why the five-class POCs failed

POC v1 and the first POC v2 replacement shortlist are preserved as historical engineering
evidence. Both failed their five-class GO gates because permitted public sources did not yield a
credible path to 30 rights-cleared, independent originals for every class. Raw file totals were
inflated by same-creator/session runs, derivatives, non-photographs, prohibited sources, uncertain
rights, or difficult labels. No five-class manifest should be silently rewritten or acquired
against merely to advance the roadmap.

## 2. Why the three-class micro-POC exists

The micro-POC changes the question. It does not ask whether PescaPR has a production-quality fish
dataset. It asks whether the project can execute the complete technical chain:

rights-cleared originals → group-safe development data → transfer learning → validation
predictions → canonical `FichaPez.id` outputs → mobile-compatible export → FI-A.8 evaluation.

It is not a production classifier, does not replace the 39-class target, and cannot establish
production accuracy or rejection performance.

## 3. Frozen classes

Experiment ordering is ascending production classifier index and is immutable for
`fish-identifier-micro-poc-v1`.

| Micro index | Production index | `FichaPez.id` | Scientific name | Common name |
|---:|---:|---|---|---|
| 0 | 15 | `RO2iuTVLAX11dy3aNgdf` | *Haemulon plumieri* | Boquicolorao / cachicata / ronco blanco |
| 1 | 22 | `XTLHUX6xHya0BOisyR6E` | *Ocyurus chrysurus* | Colirrubia / rabirrubia |
| 2 | 36 | `qDlhElFdSz5UOHDkU8Pe` | *Lactophrys triqueter* | Chapín liso |

These classes provide different body shapes and patterns while retaining a meaningful
snapper/grunt distinction. Output mapping depends only on micro output index → canonical
`FichaPez.id`; names are non-authoritative audit metadata.

## 4. Dataset-size exception and limits

| Gate | Per class | Total positive originals |
|---|---:|---:|
| Minimum technical experiment | 10 | 30 |
| Preferred | 12–15 | 36–45 |

Only accepted, rights-cleared, deduplicated, independently grouped originals count. Augmentation
does not count. This exception applies only to the technical micro-POC and does not change the
Stage 1, five-class, or 39-class production targets.

Current preliminary supply from the corrected POC v2 feasibility queue is 17 raw / 15 adjusted
groups for *O. chrysurus*, 19 / 13 for *H. plumieri*, and 21 / 13 for *L. triqueter*. These are candidates,
not approvals. The queue contains source concentration and records that may fail exact origin,
rights, label, quality, or grouping review.

## 5. Rights and label rules

The existing rights SOP remains unchanged. Each selected item must have an exact source page,
identified creator, license allowing commercial reuse and derivatives, no applicable ML
restriction, captured attribution requirements, and no contradictory third-party warning. Public
availability or Commons API metadata alone is insufficient.

All three classes use **ENHANCED_REVIEW**:

- *O. chrysurus*: verify the yellow stripe/tail, reject ambiguous juvenile or faded snapper views,
  and reject prohibited-origin imports even if Commons displays an otherwise acceptable license.
- *H. plumieri*: check related grunts, mixed schools, post-catch color loss, full-fish visibility,
  and the source-taxonomy spelling `plumierii` versus the frozen Guía audit spelling `plumieri`.
- *L. triqueter*: check juvenile/color-phase morphology and related trunkfish; source-concentrated
  sequences must remain one controlling group.

A competent reviewer may approve clear examples. Uncertain or conflicting examples are rejected
or escalated; no specialist credential is invented. Rights approval remains independent from
label approval. Codex may perform technical review but cannot serve as sole Dataset Approver.
Edgar Rivera must independently approve an acquisition packet before any binary is acquired.

## 6. Acquisition gate and workflow

Reuse `experiments/poc_v2_feasibility/candidate_review.csv`; do not rediscover the same records.
The next task must select approximately 10–15 candidates per class from independent groups,
perform exact item-level rights and visual label adjudication, and prepare a compact approval
packet with decisions initially PENDING. If any class cannot present at least 10 qualified,
independent candidates, stop: the micro-POC is not feasible and the gate must not be lowered again
automatically.

Only after Edgar's approvals may original bytes be acquired into the external controlled dataset
workspace, hashed, perceptually compared, grouped, and entered into canonical metadata. Dataset
binaries never enter normal Git history.

## 7. Development OOD reuse

Target 10–20 development OOD originals by reusing eligible, unlocked TRAIN/VALIDATION OOD rows
from the existing pilot. Locked `testset-v1` rows are excluded. Rights, category approval,
grouping, and duplicate rules continue to apply. This pool exists only to exercise FI-A.8 score
and rejection mechanics; it is far too small for production OOD claims or threshold selection.

## 8. Development split

Use a stable-seed, group-safe deterministic assignment targeting roughly 75–80% TRAIN and
20–25% VALIDATION. Derivative, source, session, individual, and contributor controlling groups
must remain together. With only 10–15 originals per class, exact ratios are secondary to retaining
each class in both partitions and preventing leakage.

Do not create or claim a meaningful micro-POC TEST set. Do not use, inspect, move, expand, or tune
against production `testset-v1`. Architecture, preprocessing, augmentation, and FI-A.8 policy
exploration use only micro-POC TRAIN/VALIDATION and eligible development OOD.

## 9. Transfer-learning approach

The candidate set is intentionally small:

| Candidate | Role in the experiment | Tradeoff |
|---|---|---|
| MobileNetV2 | Recommended first baseline | Mature, small, conventional operators, and a straightforward mobile-export path. |
| EfficientNet-Lite0 | One controlled comparator if the baseline works | Designed for edge classification and may provide stronger features, but adds no value until the pipeline baseline is reproducible. |
| MobileNetV3Small | Optional later size/latency comparator | Potentially smaller, but should be attempted only after confirming checkpoint and converter support in the selected training stack. |

The smallest practical first run is **MobileNetV2**, initialized from a standard pretrained
checkpoint, with a new three-output head. Freeze the backbone, train only the head, use early
stopping and validation monitoring, then attempt limited low-learning-rate fine-tuning only if the
frozen-backbone result is stable and underfits. EfficientNet-Lite0 should be evaluated only as a
single controlled comparator, not as a broad architecture search on the tiny validation set.

Architecture selection remains a Phase B experiment decision. Prefer the smallest backbone with a
straightforward TensorFlow Lite/LiteRT export path and conventional image operators; do not add an
Android runtime dependency during dataset preparation. Record the exact checkpoint, input shape,
normalization, preprocessing, random seed, and exported-model checksum with every run.

## 10. Augmentation policy

Apply augmentation only to TRAIN and only after partition assignment:

- horizontal flip where the image and labels remain biologically valid;
- modest random crop/scale that usually retains the diagnostic body and tail;
- modest rotation appropriate to realistic camera tilt;
- modest brightness/contrast and limited color variation;
- light compression/resolution variation when it resembles consumer photos.

Do not use vertical flips, aggressive hue shifts, heavy warping, crops that remove diagnostic
features, or transformations that make an implausible fish. VALIDATION receives deterministic
preprocessing only, never stochastic training augmentation.

## 11. Training and evaluation objectives

For three balanced classes, random top-1 accuracy is approximately 33.3%. The experiment succeeds
technically when:

- the reproducible training pipeline runs and loss behaves sensibly;
- development validation performance is materially above random, interpreted cautiously;
- output indices resolve exactly to the frozen micro manifest and canonical `FichaPez.id` values;
- prediction artifacts satisfy the FI-A.8 evaluation schema and exercise score/margin sweeps;
- a mobile-compatible model export succeeds with matching outputs;
- model size is plausible for later Android benchmarking;
- the deterministic preprocessing contract can be recorded and reproduced.

There is deliberately no production accuracy target. A high validation score on such a small,
source-correlated dataset is weak evidence and must not be presented as 39-class performance.

## 12. Failure interpretation

| Failure | Engineering interpretation |
|---|---|
| Fewer than 10 approved independent originals in any class | Rights/source diversity remains the blocker; do not train or lower the gate automatically. |
| Training and validation remain near 33.3% | Labels, preprocessing, data quality, backbone suitability, or optimization may be inadequate. |
| High training score with poor validation | Likely overfitting or inadequate independent diversity. |
| Export fails or changes predictions materially | Training/export/runtime operator contract problem. |
| Predictions cannot enter FI-A.8 tooling | Evaluation schema or canonical mapping contract problem. |
| Strong tiny-set validation | Pipeline signal only; not production accuracy or OOD evidence. |

## 13. Production limitations

The model recognizes only three experiment classes, has no UNKNOWN output, uses a tiny
development-only dataset, and will not produce reliable Puerto Rico field accuracy estimates. It
must not replace Gemini in production, drive regulations, or be shipped as the production Fish
Identifier. Guía Oficial remains authoritative for names and fish data.

## 14. Exact next action

Run `FI-MICRO-POC.2` item-level adjudication against the existing feasibility queue. Select one
candidate per controlling group where practical, verify rights and visual labels, reject uncertain
items, and prepare an Edgar Rivera approval packet targeting 10–15 qualified candidates per class.
Stop before acquisition if any class cannot reach 10.
