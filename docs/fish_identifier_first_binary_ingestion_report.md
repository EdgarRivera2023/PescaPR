# Fish Identifier Pilot — First Controlled Binary Ingestion Report

**Ingestion date:** 2026-08-14

## 1. Human approval

Edgar Rivera independently approved all eight candidates in `fish_identifier_first_batch_approval_packet.md` on 2026-08-14. The matching candidate rows record Edgar as Dataset Approver, `datasetApproverDecision=APPROVED`, and `candidateDecision=APPROVED_FOR_ACQUISITION`. All eight satisfied the pre-existing rights, label, source, third-party-warning, and expert-review gates before acquisition.

## 2. Controlled workspace

The binary workspace is outside the PescaPR Git repository:

`<configured-controlled-dataset-root>/fish_identifier/pilot`

Original bytes are under its `raw` area. The workspace also contains `approved`, `quarantine`, `evidence`, `snapshots`, and `tmp` areas. No binary was copied into Android resources or the repository dataset directory.

## 3. Acquisition result

Exactly eight candidates were attempted and all eight original source files were acquired. No substitute candidate or thumbnail was used.

| Candidate | Species | Format | Resolution | Result |
|---|---|---|---:|---|
| `wc-10058528` | *Coryphaena hippurus* | JPEG | 1017×511 | ACQUIRED |
| `wc-152817751` | *Coryphaena hippurus* | JPEG | 1280×960 | ACQUIRED |
| `wc-1734232` | *Coryphaena hippurus* | JPEG | 1200×1048 | ACQUIRED |
| `wc-33573857` | *Coryphaena hippurus* | JPEG | 800×600 | ACQUIRED |
| `wc-92968530` | *Coryphaena hippurus* | JPEG | 2304×1728 | ACQUIRED |
| `wc-107176216` | *Alectis ciliaris* | JPEG | 2272×1704 | ACQUIRED |
| `wc-152817762` | *Alectis ciliaris* | JPEG | 1280×960 | ACQUIRED |
| `wc-25501633` | *Alectis ciliaris* | JPEG | 4429×2792 | ACQUIRED |

Acquisition failures: **0**.

## 4. Binary identity

SHA-256 covers the unchanged downloaded original bytes. The 64-bit pHash uses the framework's normalized-image strategy: high-quality resize to 32×32, luminance conversion, 8×8 low-frequency DCT, and median thresholding. The helper is repository-local dataset tooling and adds no Android dependency.

| Candidate | SHA-256 | 64-bit pHash |
|---|---|---|
| `wc-10058528` | `652eb0b669b0f44cc3c478791a5161bd3f82b9c9a29626c1b934a20c74347625` | `f03497caec363299` |
| `wc-107176216` | `2e597c3be8efe9b605d78e1b46b9ba6e7a28614e6b4526e94d989a5360c4d1f6` | `d1a417da8c0ff266` |
| `wc-152817751` | `26cf559f83c58773818a3ea82a4208d9903565f6a053bdb0f07801aee854634f` | `b59dc1c02f371c9c` |
| `wc-152817762` | `a0df7dda5e0b367184962702c3c9a682025ffd9663c19def06a107ea0897a8ac` | `b3974c4c4e331772` |
| `wc-1734232` | `8942f7a3a298f8305a03b6e28054f28c71429ba39fc6d6d88b02f237b8803ec0` | `e41ae591daa51ae6` |
| `wc-25501633` | `5f4498c196d37ac78b7832631acf5876d5e20c866856ef035846924746d8e8a1` | `d19fc2683d31e4d2` |
| `wc-33573857` | `51d79de83224103e1e1fc7c30af4d11239f176577a0757eaeb63bd20facfc452` | `c2c23e3dcfc2603d` |
| `wc-92968530` | `5c0af94a2e515a0afa9f4514f23a4489680c1dd8f48b21307c1daf3ccb6a5e8c` | `91a1cec0ec9b572e` |

pHash repeatability on the same original passed. The validator format requirement of exactly 16 hexadecimal characters is satisfied for every row.

## 5. Duplicate and near-duplicate review

- Exact SHA-256 duplicate groups: **0**.
- pHash pairs at or below the framework threshold of Hamming distance 10: **0**.
- Minimum pairwise pHash distance: **22**.
- Suspected crop, resize, mirror, or source variant within this batch: **none**.

pHash is only a candidate-generation heuristic. This result does not prove that future source additions are unrelated; every later image must be compared with this batch.

## 6. Grouping

Binary and source review support the selection assumption that all eight are separate source/session groups. Each row has distinct `derivativeGroupId`, `sourceGroupId`, `sessionGroupId`, and `individualFishGroupId` values. The individual/session identifiers are provisional singleton groups: future source evidence or duplicate analysis may merge them, but no certainty beyond the current evidence is asserted.

Two controlled FDA reference images are separate source items and different species. The five Coryphaena and three Alectis records use different selected source/session groups; the previously identified Akumal, FDA duplicate, and juvenile Alectis sequence candidates were not acquired.

## 7. Metadata and partitions

Eight real rows were added to `datasets/fish_identifier/pilot_metadata.csv`. They preserve canonical IDs, scientific names, source item/media URLs, original filenames, licenses, creators/attribution, review states, acquisition date, exact and perceptual hashes, grouping, variation tags, quality flags, and unlocked partition state.

The existing stable-seed component function was applied to `sourceGroupId` twice with identical output:

| Proposed partition | Rows |
|---|---:|
| TRAIN | 5 |
| VALIDATION | 1 |
| TEST | 2 |

These are deterministic **proposals**, not a curated or locked test set. The distribution is not interpreted as statistically meaningful for an eight-image process test.

## 8. Validator results

Real metadata validation:

- rows: 8
- errors: **0**
- warnings: **1**
- deterministic repeatability: PASS
- group leakage: none detected
- exact/perceptual cross-partition conflict: none detected

Expected warning:

- `GROUP_COVERAGE`: `contributorGroupId` is blank for 8/8 rows. These are public institutional/Commons sources rather than PescaPR contributor submissions, so no contributor identity was invented. Upstream creator/source concentration remains available through `photographerAuthor` and `sourceGroupId`.

The dataset-validator unit suite also passes all four tests.

## 9. Pipeline outcome and limitations

The first real pipeline test **succeeded** for this narrow batch: independent approval, controlled acquisition, unchanged originals, SHA-256, 64-bit pHash, duplicate comparison, grouping, metadata creation, deterministic proposal, and zero-error validation all completed.

This does not complete FI-A.7 or FI-A.7-PILOT. The broader pilot still lacks OOD examples, HIGH/VERY_HIGH expert-reviewed positives, a reviewed locked-test set, snapshot/version artifacts, and representative real-world volume. Partition assignments remain unlocked proposals.

## 10. Recommended next action

Review and formally accept the eight provisional grouping and partition proposals, then acquire a small rights-cleared OOD batch through the same pipeline. In parallel, obtain the required independent mackerel/amberjack and barracuda-expert label reviews. After positive and OOD data exercise locked-test review and snapshot/version generation with zero unresolved validator errors, reassess FI-A.7 completion.
