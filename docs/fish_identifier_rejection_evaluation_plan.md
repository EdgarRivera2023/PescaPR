# Fish Identifier Rejection Evaluation Plan

**Date:** 2026-08-14
**Roadmap scope:** FI-A.8 framework only
**Status:** READY FOR FUTURE EMPIRICAL EXECUTION; NO MODEL OUTPUTS OR THRESHOLDS EXIST

## 1. Purpose

Define how a future 39-class on-device classifier will decide whether to return one canonical `FichaPez.id`, defer to several plausible supported candidates, or decline identification. Guía Oficial remains authoritative for names, aliases, characteristics, regulations, and images. The model and rejection policy produce IDs and scores only.

The policy's primary objective is to minimize confidently wrong species identifications. Its secondary objective is to avoid excessive rejection of genuinely identifiable supported fish. Its tertiary objective is to offer useful top-2/top-3 alternatives when evidence is close. These tradeoffs require empirical validation; this plan assigns no target percentage or production threshold.

## 2. Current limitations

- The frozen contract has 39 supported `FichaPez.id` outputs and no UNKNOWN/OTHER output.
- No model architecture, trained weights, logits, scores, probabilities, or calibration results exist.
- `pilot-snapshot-v1` proves dataset mechanics with 22 rows, not statistical rejection performance.
- Locked `testset-v1` has four rows and is too small for meaningful accuracy, per-class, calibration, or OOD-rate claims.
- FI-A.7-PILOT acquisition and specialist review remain parallel open work.

Accordingly, all numeric thresholds in future experiments must be supplied as experiment parameters and selected from development/validation evidence. A softmax value, if a future model uses softmax, is a **model score** until calibration is demonstrated.

## 3. Why a closed-set classifier needs rejection

A 39-output classifier must rank one supported species first even for an unsupported Caribbean fish, shark, crab, boat, or unusable photograph. Top-1 ranking alone therefore cannot prove membership in Guía Oficial. Rejection evaluation measures when score magnitude, separation, concentration, calibration, or a future auxiliary gate provides enough evidence to trust that ranking.

OOD rejection means “not sufficiently consistent with the supported classifier catalog under the validated policy.” It does not mean “definitely not a fish.”

## 4. Model-agnostic input and decision states

A future policy input should carry:

- model version and model checksum;
- classifier-manifest/catalog version and checksum;
- preprocessing version;
- ordered `(outputIndex, fichaPezId, modelScore)` values, at least top 3 and preferably all 39;
- top-1, top-2, and top-3 scores;
- computed top-1 minus top-2 margin;
- entropy or another concentration statistic only when score semantics make it meaningful;
- calibration method/version, if one was fitted.

Runtime mapping remains `output index → frozen manifest → FichaPez.id`. Names and display strings never participate in classifier identity.

Typed response states:

| State | Meaning | Canonical result |
|---|---|---|
| `ACCEPTED` | Evidence passes the frozen policy for one supported class | Exactly one accepted `FichaPez.id` |
| `AMBIGUOUS` | Supported neighborhood appears plausible but one species is unsafe | No accepted ID; retain ranked top 2–3 supported IDs for UI choice |
| `REJECTED_LOW_CONFIDENCE` | Supported-class evidence/separation is insufficient | No accepted ID |
| `REJECTED_OOD` | Empirically validated OOD behavior indicates inconsistency with supported data | No accepted ID; not a claim that the subject is non-fish |
| `INVALID_INPUT` | Decode/preprocessing or technical-quality checks cannot produce a usable input | No classifier identification |

Manual correction may resolve to a Guía record in the UI, but it must be logged separately from model acceptance.

## 5. Supported-positive metrics

Report overall and, when sample size permits, per class and confusion group:

- top-1 accuracy and top-3 accuracy;
- per-class recall and precision;
- confusion-matrix counts;
- supported-image acceptance rate;
- supported-image false-rejection rate;
- ambiguous/deferred rate;
- incorrect accepted-identification rate among accepted supported images.

The last metric is the core safety metric: a policy that raises apparent coverage by confidently accepting wrong species is worse than an honest rejection.

## 6. OOD and acceptance metrics

Report:

- OOD rejection rate;
- OOD false-acceptance rate;
- OOD ambiguous/deferred rate;
- results by OOD category;
- supported-vs-OOD tradeoff for every candidate policy;
- counts as well as rates, with confidence intervals when future sample size supports them.

Never hide easy-object dominance inside an overall OOD rate. Report `unsupported_fish`, `shark_ray`, `crustacean`, `cephalopod`, `person`, `fishing_gear`, `boat`, `cooler`, `beach_water`, `blurry_invalid`, and `other_nonfish` separately when present.

## 7. Unsupported-fish priority

`unsupported_fish` is the hardest and most important rejection dimension. Acquisition and reporting should emphasize visually similar Caribbean species, close relatives outside Guía, neighboring genera/families, and baitfish—not merely obvious objects. Each item still needs evidence proving it is outside the frozen 39.

Policy selection should inspect unsupported-fish false accepts individually and by the supported class they were forced toward. An overall OOD result cannot compensate for unsafe unsupported-fish behavior.

## 8. Candidate rejection strategies

Compare without preselecting a winner:

- minimum top-1 model score;
- minimum top-1/top-2 score margin;
- combined top-1 and margin gates;
- entropy/distribution concentration when mathematically appropriate;
- class-specific thresholds only when enough independent validation data shows stable benefit;
- confusion-family-specific ambiguity handling only when predeclared evaluation shows benefit;
- calibrated-score variants;
- a dedicated future supported/OOD or image-validity gate only if score-based policies are insufficient.

Complex policies must justify their added overfitting and maintenance risk against a simpler global policy.

## 9. Threshold-selection method

1. Fit the candidate model using TRAIN only.
2. Produce immutable prediction artifacts for development/VALIDATION positives and OOD.
3. Sweep explicitly supplied score and margin grids; add concentration or class/family variants as separate versioned policies.
4. Compare incorrect accepted identifications and OOD false accepts first, then supported false rejection and ambiguity/coverage.
5. Inspect per-class, per-confusion-family, and per-OOD-category behavior.
6. Freeze preprocessing, model, calibration, thresholds, and `thresholdPolicyVersion` before any locked-test checkpoint.
7. Run the locked test once for the checkpoint and publish all metrics, including failures.

The repository tool deliberately defines no default grid and no production values. Synthetic numbers demonstrate mechanics only.

## 10. Calibration strategy

Evaluate raw ranking scores first. If score semantics permit, fit temperature scaling or another simple calibration method on development/validation predictions only. Compare reliability diagrams and expected calibration error (or an equivalent calibration measure) alongside task metrics.

Calibration and OOD rejection are separate: well-calibrated supported-class probabilities can remain overconfident on unfamiliar inputs. Every calibrated policy still requires OOD false-accept evaluation, especially `unsupported_fish`.

## 11. Confusion-family handling

Predeclare analysis slices for groupers, snappers, barracudas, mackerels/wahoo, amberjacks, and boxfish/trunkfish. Measure within-group top-1/top-3 errors, margins, incorrect accepts, and ambiguity behavior. If close candidates repeatedly belong to one confusion group, evaluate showing top 2–3 candidates rather than forcing one result.

Do not hard-code family rules now. A family-specific policy is eligible only with adequate validation examples, expert-confirmed labels, measurable benefit, and a versioned fallback for sparse classes.

## 12. Hierarchical decision concept

The future evaluation should keep these stages separable:

1. deterministic decode, EXIF, and technical-validity checks;
2. model inference and supported-class ranking;
3. supported-vs-reject policy using empirically selected evidence;
4. ambiguity decision and ranked alternatives;
5. canonical ID resolution through `OfficialGuideRepository`.

A separate neural OOD gate is optional research, not an architectural requirement. Start by evaluating the 39-output model's score behavior; add a gate only if evidence shows a meaningful improvement.

## 13. Locked-test discipline

`testset-v1` must not choose architecture, preprocessing, hyperparameters, calibration, thresholds, or policy variants. It may be evaluated only after a checkpoint is frozen. Its four rows are enough to test evaluation plumbing but not to estimate production accuracy or rejection rates.

Do not repeatedly inspect it while tuning. Do not mutate or silently expand it. A larger independently curated test set receives a new version and snapshot, and previous reported checkpoints remain reproducible.

## 14. Evaluation artifact schema

`datasets/fish_identifier/evaluation/evaluation_run_template.json` binds an evaluation to model/preprocessing/policy versions and checksums, the frozen classifier manifest, dataset snapshot, test-set version, and timestamp. Null template values explicitly mean unresolved.

`prediction_template.csv` is header-only. Each future row records truth semantics, ranked canonical IDs and model scores, margin, optional entropy, declared decision, accepted ID, derived correctness/false-accept/false-reject fields, and notes. The evaluator recomputes metrics rather than trusting supplied derived flags. OOD truth has no `FichaPez.id`; all ranked output IDs must belong to the frozen manifest.

## 15. Tooling workflow and success criteria

`tools/fish_identifier/evaluate_predictions.py`:

- validates run metadata and classifier-manifest checksum;
- optionally binds snapshot/test-set references to a snapshot manifest;
- validates positive/OOD truth and ranked frozen IDs;
- computes overall, per-class, per-OOD, and confusion inputs;
- evaluates declared typed decisions;
- sweeps caller-supplied top-1 and margin combinations without defaults;
- emits deterministic JSON suitable for audit and comparison.

FI-A.8 empirical completion requires a trained candidate model, frozen TRAIN/VALIDATION prediction artifacts, meaningful supported and category-balanced OOD predictions, policy comparison, calibration evaluation, a selected/versioned policy, and a final locked-test checkpoint. Success means the selected policy's tradeoff is documented and reproducible with no hidden test tuning—not that a synthetic test passes.

## 16. What cannot be completed yet

No real score distribution exists, so the project cannot select thresholds, decide whether entropy/calibration helps, justify class/family rules, quantify OOD false acceptance, or determine whether an auxiliary OOD gate is needed. The current 14 OOD originals and four-row locked test are pipeline evidence only.

## 17. Exact next action

Continue FI-A.7-PILOT acquisition and specialist reviews until an experiment-quality development/validation subset exists. Then, under Phase B, train the first candidate model, export versioned validation predictions for supported and OOD images, and run this evaluator to compare predeclared policies. Do not open `testset-v1` for threshold selection.
