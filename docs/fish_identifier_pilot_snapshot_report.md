# Fish Identifier Pilot — Immutable Snapshot Report

**Prepared:** 2026-08-14
**Status:** CREATED, LOCKED, AND VERIFIED
**Snapshot ID:** `pilot-snapshot-v1`
**Test-set version:** `testset-v1`
**Human TEST approval:** APPROVED — Edgar Rivera, 2026-08-14

## 1. Dataset and partition state

The immutable snapshot contains 22 real approved rows: 8 positive and 14 OOD. Final partitions are TRAIN 13, VALIDATION 5, and TEST 4. Exactly the four TEST rows are `LOCKED`; TRAIN and VALIDATION remain unlocked because the schema does not require locking them.

Positive distribution is *Coryphaena hippurus* 5 and *Alectis ciliaris* 3. OOD has two rows each for `unsupported_fish`, `shark_ray`, `crustacean`, `fishing_gear`, `person`, `boat`, and `beach_water`.

All 22 metadata sources are individually reviewed Wikimedia Commons items. License distribution is public domain 6, CC BY 2.0 8, CC BY 3.0 2, CC0 5, and CC BY 4.0 1. Rights and label/category states are APPROVED for all rows.

## 2. TEST composition review and revision

The first deterministic proposal was independently rejected by Edgar Rivera on 2026-08-14: the FDA and aquarium positives moved to VALIDATION, and both boat OOD rows moved to TRAIN. These four historical decisions and all replacement overrides are machine-readable in `pilot_partition_overrides.csv`.

Approved and locked revision:

| Image ID | Type | Identity | TEST suitability assessment |
|---|---|---|---|
| `pilot-b1-wc-1734232` | POSITIVE | *Coryphaena hippurus* | Realistic caught/held boat context and independent source/session. |
| `pilot-b1-wc-107176216` | POSITIVE | *Alectis ciliaris* | Different supported species and independent natural-underwater context. |
| `pilot-ood-b1-wc-12104762` | OOD | `shark_ray` | Natural fish-like unsupported morphology from an independent source/session. |
| `pilot-ood-b1-wc-92100670` | OOD | `beach_water` | Puerto Rico non-fish environmental context, independent from other groups. |

Edgar independently approved all four revised rows on 2026-08-14. The set remains a pipeline pilot, not a statistically meaningful accuracy benchmark.

## 3. Grouping review

All 22 assignments were reviewed. No new evidence supports merging a group across partitions. The JaredMcKenzie gear pair remains a shared source/session component in TRAIN. The two FDA images remain separate source-item/session/individual groups because they are different numbered assets and species; their common collection is disclosed to the TEST reviewer. All other singleton decisions remain evidence-supported. No derivative, source, session, or individual group crosses partitions.

## 4. Warning dispositions and validator semantics

The validator now requires `individualFishGroupId` only for positives and fish-like OOD (`unsupported_fish`, `shark_ray`). It no longer emits misleading fish-individual warnings for boats, beaches, gear, people, or crustaceans. Strictness for real fish is covered by unit tests.

Current validation is 22 rows, 0 errors, 2 warnings:

| Warning | Disposition | Rationale |
|---|---|---|
| JaredMcKenzie gear pHash distance 8 | `MANUAL_REVIEW_RESOLVED` | Distinct exact hashes/source items; correlated family grouped together in TRAIN. |
| `contributorGroupId` absent for 22/22 | `EXPECTED_METADATA_LIMITATION` | These are public-source acquisitions, not PescaPR contributor submissions; photographer/source provenance is retained and fake contributor IDs are prohibited. |

The machine-readable dispositions are in `pilot_warning_dispositions.csv` and snapshot creation refuses any warning without a resolved disposition.

## 5. Snapshot format and tooling

`create_snapshot.py` prepares a canonical JSON manifest containing snapshot/schema versions, creation time, classifier schema/catalog versions and checksum, metadata and validator checksums, partition algorithm/seed, explicit human-curated partition overrides, `testset-v1` approvals, total/sample/partition counts, per-species and per-OOD counts, source/license summaries, rights/label summary, grouping coverage, warning dispositions, all image SHA-256/pHash identities, and a canonical manifest checksum.

The external root is:

`<configured-controlled-dataset-root>/fish_identifier/pilot/snapshots/`

Creation copies only immutable metadata plus the sanitized manifest into `<root>/<snapshotId>/`; acquired binaries remain in the controlled raw workspace and are referenced by identity. Reusing an existing snapshot ID with different content fails. Verification checks the manifest checksum, snapshotted metadata checksum, frozen classifier-manifest checksum, validator checksum, and dataset validity.

Automated tests cover deterministic creation, checksum changes after metadata changes, missing TEST approval, invalid datasets, mutation detection, immutable-ID conflict, and positive/OOD counts.

## 6. Snapshot creation and verification

The first proposal was never locked. The revised four rows are recorded as `APPROVE_FOR_LOCKED_TEST` and `LOCKED`. `pilot-snapshot-v1` was created under the controlled external snapshot root with immutable metadata, `snapshot_manifest.json`, and `testset_manifest.json`. Sanitized copies of both manifests are stored under `datasets/fish_identifier/snapshots/pilot-snapshot-v1/`; no image binary or private path is included.

Checksums:

| Artifact | SHA-256 |
|---|---|
| Canonical snapshot content | `a40b31f3a58b8fbae6d7e16682782fd5efd08c28274044ae2187566aeae877b2` |
| `testset-v1` content | `8a76f69dda03329972b07ace320a9a5448a5c7ff6e37daa5e7f437030f204976` |
| Snapshotted metadata | `e6fcebfe83a040677190155b3af7b2236dbc9d130c2e0eaf4dae33206ff3b353` |
| Frozen classifier manifest | `c42eea6e8b4a3696aa0b963e580fc588e844f181a74baeb3c3b0c182df5b3eb0` |
| Dataset validator | `1b0e66201d514a80a6b88b3cdd0b094e5db2f2285502e93639a90aa56b9950f0` |

Verification passed against the external manifest and snapshotted metadata. Re-running creation from unchanged inputs returned the same immutable snapshot. Automated tests prove that metadata mutation, classifier/validator checksum changes, missing approvals, invalid data, and reuse of an ID with different content fail closed.

TEST identities may never silently move into TRAIN/VALIDATION after publication. Corrections require a new test-set/snapshot version. Training runs must name the exact test-set version, and repeated TEST-driven tuning is prohibited.

## 7. Status and next action

FI-A.7 is complete: real images were grouped and partitioned; exact/perceptual and controlling-group leakage checks passed; the revised field/OOD TEST set was independently reviewed and locked; the immutable snapshot was created and verified; and validation has zero unresolved errors.

FI-A.7-PILOT remains incomplete under its explicit 125-positive/50-OOD five-species scope. The current snapshot proves the framework with 8 positives across two pilot species and 14 OOD, but does not satisfy broader acquisition, source-diversity, or specialist-review gates.

Outstanding specialist work remains: 10 *Scomberomorus cavalla* candidates require the planned second/specialist review (7 remain label-pending), 14 *Seriola dumerili* candidates remain pending, and 13 *Sphyraena barracuda* candidates require pending reviews including qualified barracuda expertise.

**Exact next action:** proceed to FI-A.8 execution planning: use approved future OOD/positive data and model outputs to validate confidence/margin rejection behavior without adding an OOD classifier label. In parallel, continue the still-open pilot acquisition and HIGH/VERY_HIGH expert-review work; do not use `testset-v1` for repeated tuning.
