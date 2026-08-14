# Fish Identifier POC v1 plan

**Date:** 2026-08-14
**Status:** Class contract frozen; acquisition not started

## 1. Purpose and production relationship

`fish-identifier-poc-v1` is a five-output experiment that proves the rights-to-predictions
workflow before PescaPR invests in a 39-class production dataset. It does not replace, shrink,
reorder, or reinterpret the production catalog.

The POC retains the production identity rule: output index maps to canonical `FichaPez.id`, and
display data must later resolve through `OfficialGuideRepository`. Names are non-authoritative
audit metadata. The source production manifest remains the 39-class Android asset with SHA-256
`c42eea6e8b4a3696aa0b963e580fc588e844f181a74baeb3c3b0c182df5b3eb0`. The POC manifest lives
outside runtime assets and is explicitly `EXPERIMENT_ONLY_NOT_PRODUCTION`.

POC ordering is ascending production index and immutable for this experiment ID. Changed
membership or order requires a new experiment version.

## 2. Evaluation of all 39 classes

The existing acquisition tracker supplied confusion, sourcing, and expert-review assessments.

| Prod. | Species | Risk / sourcing | Review gate | Decision |
|---:|---|---|---|---|
| 0 | *Acanthocybium solandri* | HIGH / MEDIUM | Yes | Exclude: mackerel/wahoo confusion |
| 1 | *Lactophrys bicaudalis* | HIGH / HIGH | Yes | Exclude: boxfish confusion/sourcing |
| 2 | *Cephalopholis fulva* | VERY_HIGH / MEDIUM | Critical | Exclude: grouper phases |
| 3 | *Lutjanus buccanella* | VERY_HIGH / HIGH | Critical | Exclude: deepwater snapper |
| 4 | *Alectis ciliaris* | MEDIUM / MEDIUM | Clear adults routine; juveniles escalated | **Select** |
| 5 | *Coryphaena hippurus* | LOW / LOW | No | **Select** |
| 6 | *Epinephelus adscensionis* | VERY_HIGH / MEDIUM | Critical | Exclude: grouper confusion |
| 7 | *Lactophrys quadricornis* | HIGH / MEDIUM | Yes | Exclude: related boxfish |
| 8 | *Lactophrys trigonus* | HIGH / HIGH | Yes | Exclude: boxfish sourcing/confusion |
| 9 | *Lutjanus cyanopterus* | HIGH / MEDIUM | Yes | Exclude: snapper review |
| 10 | *Megalops atlanticus* | LOW / LOW-to-medium | No | **Select** |
| 11 | *Lutjanus jocu* | HIGH / MEDIUM | Yes | Exclude: snapper review |
| 12 | *Scomberomorus regalis* | HIGH / MEDIUM | Yes | Exclude: mackerel review |
| 13 | *Sphyraena guachancho* | VERY_HIGH / HIGH | Critical | Exclude: known barracuda conflict |
| 14 | *Centropomus undecimalis* | LOW / public-source HIGH | No | **Select with sourcing gate** |
| 15 | *Haemulon plumieri* | MEDIUM / MEDIUM | Yes | Exclude: grunt label review |
| 16 | *Epinephelus mystacinus* | VERY_HIGH / HIGH | Critical | Exclude: deepwater grouper |
| 17 | *Epinephelus itajara* | HIGH / HIGH-limited | Yes | Exclude: protected-species constraints |
| 18 | *Scomberomorus cavalla* | HIGH / LOW | Yes | Exclude: current blocked review |
| 19 | *Lutjanus synagris* | HIGH / LOW | Yes | Exclude: snapper review |
| 20 | *Mycteroperca venenosa* | VERY_HIGH / HIGH | Critical | Exclude: grouper phases |
| 21 | *Pristipomoides aquilonaris* | HIGH / HIGH | Yes | Exclude: deepwater sourcing |
| 22 | *Ocyurus chrysurus* | HIGH / LOW | Yes | Exclude: snapper review despite availability |
| 23 | *Sphyraena picudilla* | VERY_HIGH / HIGH | Critical | Exclude: known barracuda conflict |
| 24 | *Lactophrys polygonia* | HIGH / HIGH | Yes | Exclude: boxfish sourcing/confusion |
| 25 | *Caranx lugubris* | MEDIUM / MEDIUM | Yes | Exclude: jack review |
| 26 | *Lutjanus analis* | HIGH / LOW | Yes | Exclude: snapper review |
| 27 | *Sphyraena barracuda* | VERY_HIGH / LOW | Critical | Exclude: current expert bottleneck |
| 28 | *Etelis oculatus* | HIGH / HIGH | Yes | Exclude: deepwater sourcing |
| 29 | *Lutjanus vivanus* | VERY_HIGH / HIGH | Critical | Exclude: deepwater snapper |
| 30 | *Seriola rivoliana* | HIGH / MEDIUM | Yes | Exclude: amberjack review |
| 31 | *Rhomboplites aurorubens* | HIGH / MEDIUM | Yes | Exclude: snapper review |
| 32 | *Albula vulpes* | LOW / public-source HIGH | No | **Select with sourcing gate** |
| 33 | *Epinephelus guttatus* | VERY_HIGH / MEDIUM | Critical | Exclude: grouper phases |
| 34 | *Epinephelus striatus* | VERY_HIGH / HIGH-limited | Critical | Exclude: protected/sourcing constraints |
| 35 | *Anisotremus surinamensis* | MEDIUM / HIGH | Yes | Exclude: sourcing/grunt review |
| 36 | *Lactophrys triqueter* | HIGH / MEDIUM | Yes | Exclude: boxfish review |
| 37 | *Seriola dumerili* | HIGH / LOW | Yes | Exclude: current blocked review |
| 38 | *Cephalopholis cruentata* | VERY_HIGH / MEDIUM | Critical | Exclude: grouper phases |

This is not five trivial silhouettes. Tarpon, snook, and bonefish deliberately create a useful
silvery/elongate-fish problem, while dolphinfish and adult African pompano add strong shape and
color contrast. Juvenile Alectis must not dominate or bypass enhanced review.

## 3. Selected five classes

| POC | Production | `FichaPez.id` | Species | Common name | Risk | Rationale |
|---:|---:|---|---|---|---|---|
| 0 | 4 | `5SkWhUQgG6JuanpfSLUT` | *Alectis ciliaris* | Corcobado | MEDIUM | Distinct adult shape, existing development images, meaningful life-stage variation |
| 1 | 5 | `AYGYpLjkS9LEv7AXVAuk` | *Coryphaena hippurus* | Dorado | LOW | Distinct profile/color, field-photo potential, four reusable development images |
| 2 | 10 | `MjsvzQbyRzyWqtjGYSBM` | *Megalops atlanticus* | Sábalo/tarpón | LOW | Distinct mouth/scales, angler relevance, moderate public coverage |
| 3 | 14 | `PtbnNsBSRDwJzqGwvmv9` | *Centropomus undecimalis* | Róbalo | LOW | Lateral-line/profile cue and meaningful silvery-fish contrast |
| 4 | 32 | `osXhShrxuuFLdr0ftgmb` | *Albula vulpes* | Macaco/macabí | LOW | Angler field context and nontrivial snook/tarpon contrast |

## 4. Metadata-only source feasibility

These are discovery estimates, not approved-image promises. Commons membership is not rights or
label approval. Prohibited iNaturalist imports, maps, illustrations, video-only records, obvious
derivatives, and correlated sequences are excluded from usable-volume reasoning.

| Species | Commons census | Readily reviewable estimate | Field/source outlook | Reach 30 | Reach 50 |
|---|---:|---:|---|---|---|
| *C. hippurus* | 60 | about 20–35 plus NOAA/PescaPR leads | Good; imports/crops require filtering | Likely | Probably, unverified |
| *A. ciliaris* | 44 | about 15–25 plus federal/PescaPR leads | Field adults are the gap | Probably | Uncertain |
| *M. atlanticus* | 40 | about 10–18 after historical/illustration filtering | Angler/federal additions needed | Probably | Uncertain |
| *C. undecimalis* | 8 including a map | about 5–7 | Targeted permissioned field sourcing required | Uncertain | Unlikely from public sources alone |
| *A. vulpes* | 32 | about 5–12 after anatomy/illustration filtering | Permissioned angler imagery required | Uncertain | Unlikely from public sources alone |

Allowed sources remain PescaPR-owned or separately permissioned originals, item-verified federal
public-domain works, Smithsonian items explicitly marked CC0, and individually reviewed Commons
public-domain/CC0/CC BY files. If snook or bonefish cannot show a credible path to 30 during
metadata discovery, stop and version a revised POC rather than padding the class.

## 5. Dataset and OOD targets

- Minimum: 30 accepted independent originals/class; 150 positives total.
- Preferred: 40–50/class; 200–250 positives total.
- Augmentations never count as originals.
- Reusable development data: four Coryphaena and two Alectis TRAIN/VALIDATION rows.
- The two selected-species rows locked in `testset-v1` are excluded and not counted.
- Development OOD: 50–100 originals, prioritizing unsupported fish, then shark/ray, gear,
  person/hands, beach/water, boat, crustacean, and obvious non-targets.
- Existing unlocked OOD TRAIN/VALIDATION may be reused only through a new POC dataset snapshot;
  locked TEST OOD remains excluded from tuning.

## 6. Development partition strategy

After acquisition, create a new immutable `poc-v1-dataset-*` snapshot. Apply the established
grouping hierarchy and deterministic group assignment. Target approximately 80% TRAIN and 20%
VALIDATION, adjusted for group integrity and per-class coverage. Any temporary POC checkpoint set
must be separately versioned and must not be called or treated as production `testset-v1`.

Threshold, preprocessing, and architecture selection use POC TRAIN/VALIDATION only. No new image
enters locked TEST in this acquisition track.

## 7. Manifest contract and acquisition workflow

The experiment manifest has contiguous indices 0–4 and references both canonical IDs and original
production indices. Acquisition follows the existing gates:

1. Metadata-only discovery with source/session caps.
2. Item-level rights and scientific-label review; uncertain Alectis juveniles escalate.
3. Independent Dataset Approver decisions in small, source-diverse batches.
4. Original bytes stored only in the controlled external workspace.
5. SHA-256, 64-bit pHash, canonical identity, rights, grouping, and field/reference tags recorded.
6. Derivatives and same-session/individual material rejected or grouped conservatively.
7. TRAIN/VALIDATION proposed and validated with zero unresolved errors.
8. Experiment dataset snapshot frozen before training.

## 8. Phase B objectives and success

Random top-1 for five balanced classes is 20%. The first model succeeds as an engineering POC if:

- group-safe validation performance materially exceeds 20% without obvious leakage;
- per-class/confusion metrics show useful signal rather than a dominant-class shortcut;
- output indices resolve exactly to canonical `FichaPez.id` values;
- export yields a five-output mobile-format artifact with recorded checksums;
- predictions satisfy `evaluate_predictions.py` and exercise FI-A.8 sweeps using validation and
  development OOD scores;
- model size and desktop inference justify later on-device benchmarking.

No production accuracy requirement is declared. Failure can reveal domain bias, inadequate field
coverage, label noise, imbalance, preprocessing errors, or an unsuitable architecture before the
39-class investment.

## 9. Production limitations and next action

This POC cannot validate all 39 classes, production confusion families, Android latency, or
production rejection thresholds. It does not waive parallel specialist requirements or authorize
user-facing predictions. Guía Oficial remains authoritative.

Next: run metadata-only discovery for these exact five IDs, starting with snook and bonefish. Stop
before binary acquisition if either lacks a credible path to 30 independent, legally reviewable
originals; revise under a new POC manifest version if necessary.
