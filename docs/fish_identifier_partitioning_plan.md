# Fish Identifier Dataset Partitioning and Leakage-Control Plan

**Plan date:** 2026-08-14

**Roadmap scope:** FI-A.7 framework readiness; real-image execution remains pending

**Current dataset state:** Zero approved training, validation, or test images

## 1. Purpose

This framework defines how future PescaPR image metadata is approved, grouped, deduplicated, assigned, locked, and validated before model training. It preserves the frozen 39-class `index → FichaPez.id` contract and supports out-of-distribution (OOD) evaluation without inventing an OOD classifier label.

The framework is designed to prevent evaluation inflation caused by the same source asset, derivative, photo sequence, individual fish, or strongly correlated source context appearing across partitions. It does not claim that leakage has been eliminated from a real dataset: no approved images exist yet.

## 2. Partition philosophy

Partition connected groups, never individual rows independently. Start from the FI-A.5 target of approximately **70% train / 15% validation / 15% locked test**, then balance at group level by class, confusion group, field condition, and OOD category. Ratios are target ranges rather than permission to split a group.

- **Train:** model fitting and training-only augmentation.
- **Validation:** architecture, hyperparameter, threshold, and rejection-policy selection.
- **Locked test:** one-time or release-gated evaluation; never used to select preprocessing, thresholds, model variants, or acquisition priorities for the evaluated version.
- **UNASSIGNED:** ingested or approved metadata not yet included in an immutable snapshot.
- **EXCLUDED:** retained audit metadata for a rejected item; never read as model data.

For normal snapshots, target 68–72% train, 13–17% validation, and 13–17% test by accepted image count while reporting both image and connected-component counts. Class-level deviations are allowed when a large group cannot be safely split.

## 3. Canonical image metadata

`datasets/fish_identifier/image_metadata_template.csv` is the header-only ingestion contract. It stores:

- stable internal identity and POSITIVE/OOD type;
- exact canonical `FichaPez.id` plus scientific-name audit metadata for positives;
- source identity, original URL/filename, license, author, attribution, rights, and label review;
- SHA-256 and 64-bit perceptual hash;
- derivative, source, session, individual-fish, and anonymized contributor grouping;
- original/derivative relationship and parent ID;
- pose, environment, biological, and quality tags;
- partition, lock, exclusion, and notes.

Unknown fields remain blank at ingestion. Blank does not mean approved or “not applicable.” Before final partition assignment, reviewers must either populate required values or record why an optional grouping dimension cannot be known.

## 4. Group hierarchy and precedence

Grouping is a linkage graph. Every nonblank group value creates an edge between its member images. Partition assignment operates on the transitive connected component across all controlling identifiers. No weaker field may override a stronger link.

1. **`derivativeGroupId` — mandatory for finalized rows.** One original plus every crop, resize, mirror, color edit, watermark variant, re-encode, screenshot, or other derivative. An untouched original uses its own stable derivative group.
2. **`sourceGroupId` — mandatory for finalized rows.** One upstream source item or asset family, including reposts/copies known to originate from the same source photograph.
3. **`sessionGroupId`.** One burst, video sequence, catch event, dive, sampling session, or tightly related capture period.
4. **`individualFishGroupId`.** All recognizable photographs of the same physical fish, even across devices or short sessions.
5. **Contributor context.** `contributorGroupId` is required when available for concentration reporting. It does not automatically force every lifetime contribution into one partition. Group a contributor's same trip/session through `sessionGroupId`; consider contributor-level holdout when contributor style, boat, background, or processing is highly repetitive. Reserve entire contributors for test when measuring new-user generalization, but do not unnecessarily strand diverse independent submissions from prolific contributors.

If image A shares a derivative group with B and B shares an individual group with C, A, B, and C form one component and receive one partition. Conflicting pre-existing assignments are an ERROR; reviewers merge/fix metadata and reassign the component rather than choosing a field to ignore.

Collection or domain holds can sit above row groups. For example, a museum collection, photographer, charter operation, or government survey may be intentionally reserved wholly for validation/test when its imaging setup is uniform. This guards against source-level shortcuts even when individual source item IDs differ.

## 5. Deterministic partition strategy

For each immutable dataset snapshot:

1. Validate IDs, rights, labels, checksums, exclusions, and group fields.
2. Build transitive connected components from derivative, source, session, and individual-fish edges plus approved source/domain holds.
3. Create a canonical component key from the sorted stable member `internalImageId` values and snapshot grouping rules.
4. Hash `stableSeed + NUL + componentKey` with SHA-256. Map the unsigned prefix to 70% train, 15% validation, or 15% test.
5. At component level only, apply a recorded deterministic balancing pass for class/OOD/field-condition coverage. Tie-break with the same hash. Every override records its reason in the partition manifest.
6. Write assignments into the snapshot metadata and lock them. Never recompute a released snapshot in place.

`validate_dataset.py` contains and tests the stable 70/15/15 hash primitive. The final component builder and balancing pass must operate on actual approved metadata during FI-A.7 execution; implementing an assignment writer before real grouping patterns exist would create an unvalidated policy.

The same snapshot, grouping-policy version, stable seed, and component metadata must reproduce identical assignments. Adding data creates a new snapshot; it must not silently alter an earlier snapshot.

## 6. Locked test policy

Use a separately versioned test definition such as `testset-v1` referenced by a dataset snapshot. Once approved:

- set `datasetPartition=TEST` and `partitionLockStatus=LOCKED`;
- retain exact internal IDs, checksums, group-policy version, snapshot hash, approval date, and approvers;
- deny training/augmentation pipelines access to test rows and their derivative/source/session/individual components;
- never use test outcomes to tune the model being reported; use validation and create a later test-set version when genuine new evaluation is needed;
- require field-style PescaPR phone photos, HIGH/VERY_HIGH confusion classes, multiple devices/compression levels, partial and difficult-but-usable photographs, and appropriate OOD examples;
- cap clean specimen/reference imagery so it cannot make the test artificially easy;
- hold out contributors/source domains where practical to test generalization.

Corrections for proven rights or label defects create an append-only test-set revision (`testset-v1.1`) with a change log and new checksum. Material composition changes create `testset-v2`; old results remain tied to the old immutable version.

## 7. Exact duplicate and source-item control

Compute SHA-256 from the acquired original bytes before preprocessing. Store lowercase 64-hex output in `checksumSha256` and keep the original immutable. The validator reports as ERROR:

- duplicate SHA-256 across metadata records;
- duplicate `internalImageId`;
- duplicate `(sourceName, sourceItemId)`;
- missing/invalid SHA-256 on a finalized row.

Duplicates are not “fixed” by placing them in one partition. Reviewers designate one canonical original, link variants through `parentImageId`/`derivativeGroupId`, and exclude redundant rows or explicitly document why separate metadata records remain. No exact duplicate may cross partitions.

## 8. Perceptual duplicate strategy

Use a normalized-image 64-bit pHash and Hamming distance:

- distance 0–6: probable duplicate/near-duplicate;
- distance 7–10: similarity candidate requiring review;
- above 10: not automatically flagged, not proof of independence.

The dependency-free validator compares supplied 16-hex pHashes through a BK-tree. Any candidate at distance ≤10 across final partitions is an ERROR that blocks readiness pending manual adjudication. Same-partition candidates are WARNINGs and still require duplicate/derivative review.

pHash is not reliable enough by itself for tight crops, severe edits, overlays, perspective changes, or mirrors. During acquisition, compute original and horizontally mirrored pHash where feasible, supplement with dHash/crop-resistant or local-feature comparison in offline QA if evaluation supports it, and search by source URL/author/session. A reviewer either joins confirmed matches into one derivative/source component or records a false-positive disposition. No image dependency is added to the Android project; hash generation remains dataset tooling.

## 9. Leakage validator

`tools/fish_identifier/validate_dataset.py` validates CSV metadata without Android, Firebase, or network access. It emits:

### ERROR — blocks snapshot readiness

- invalid/missing schema or metadata identity;
- missing/unknown positive `FichaPez.id` or scientific-name mismatch;
- OOD falsely assigned a catalog ID or invalid OOD category;
- invalid partition/lock/original-derivative state;
- duplicate ID, SHA-256, or source item;
- derivative/source/session/individual group crossing TRAIN, VALIDATION, or TEST;
- unresolved perceptual candidate crossing final partitions;
- non-approved rights/label or an exclusion reason on a finalized row;
- missing SHA-256, `derivativeGroupId`, or `sourceGroupId` on a finalized row.

### WARNING — weakens confidence and requires review/reporting

- missing session, individual-fish, contributor, or other grouping coverage;
- missing perceptual hash;
- same-partition perceptual similarity requiring manual duplicate review.

The validator reports missing counts for every grouping field. Warnings do not make the CLI fail by default because some OOD/scene rows genuinely have no fish individual; snapshot release runs with `--warnings-as-errors` unless every warning has a documented, approved disposition.

Usage:

```text
python tools/fish_identifier/validate_dataset.py \
  --metadata path/to/snapshot/image_metadata.csv \
  --manifest app/src/main/assets/fish_classifier_manifest.json \
  --warnings-as-errors
```

## 10. Rights and label gates

Before TRAIN, VALIDATION, or TEST assignment:

- `rightsStatus=APPROVED` with retained license/permission evidence;
- `labelStatus=APPROVED` and required reviewer recorded;
- POSITIVE uses one exact frozen `FichaPez.id` and matching scientific audit name;
- OOD has a blank fish ID/name and one controlled OOD category;
- `exclusionReason` is blank;
- original bytes have valid SHA-256, perceptual hash has been attempted, and duplicate candidates are resolved;
- derivative/source groups are populated; session/individual/contributor values are populated when knowable;
- parent relationships are present for derivatives;
- HIGH/VERY_HIGH labels meet the expert-review policy from the acquisition plan.

Pending, quarantined, or excluded rows remain UNASSIGNED/EXCLUDED and outside all model data loaders.

## 11. OOD representation

OOD is metadata, not a frozen classifier class:

- `sampleType=OOD`;
- blank `fichaPezId` and `scientificName`;
- controlled `oodCategory`: `unsupported_fish`, `shark_ray`, `crustacean`, `cephalopod`, `person`, `fishing_gear`, `boat`, `cooler`, `beach_water`, `blurry_invalid`, or `other_nonfish`.

OOD rows receive the same rights, label/category review, checksum, derivative/source/session, partition, and lock controls. An OOD source asset related to a positive image remains linked and cannot cross partitions.

## 12. Dataset snapshots and versions

Keep raw acquisition metadata append-only and separate from approved snapshots. A future layout may be:

```text
datasets/fish_identifier/snapshots/dataset-v001/
  image_metadata.csv
  partition_manifest.json
  sha256sums.txt
  testset-v1.json
  validation_report.txt
```

`partition_manifest.json` records catalog version/hash, metadata-schema version, grouping-policy version, validator version/hash, stable seed, component/partition counts, deterministic overrides, rights/label approval summary, source snapshot date, and test-set version. `sha256sums.txt` covers metadata and manifests—not necessarily redistributable source images.

The dataset snapshot ID is immutable and content-addressable: hash canonical UTF-8 metadata sorted by `internalImageId` together with the partition manifest and frozen classifier-manifest checksum. Training configuration references this exact snapshot ID, model code revision, and test-set version. Corrections produce a new snapshot; never replace files under an existing released ID.

## 13. Synthetic fixtures and tests

No fish images are present. Fixtures use fake metadata and `invalid.example` URLs:

- `partition_valid.csv`: valid positive grouping and valid OOD representation;
- `partition_invalid.csv`: exact hash/source duplication, cross-partition perceptual similarity, derivative leakage, session/individual leakage, unknown classifier ID, unapproved rights, and a valid OOD control row.

Unit tests verify the valid fixture passes, required errors are detected, OOD has no positive label, and the hash partition suggestion is deterministic.

## 14. Partition-readiness criteria

A real snapshot is partition-ready only when:

1. every included row passes rights/label/catalog/exclusion gates;
2. exact duplicate/source conflicts are resolved;
3. perceptual candidates are adjudicated and no unresolved candidate crosses partitions;
4. connected grouping metadata has been reviewed and no group crosses partitions;
5. missing grouping coverage is quantified and every exception approved;
6. group-level ratios and per-class/OOD/field-condition coverage are acceptable;
7. the field-style locked test set is independently reviewed and frozen;
8. validator runs with zero ERRORs and all WARNINGs resolved or formally accepted;
9. snapshot/test manifests and checksums are immutable and retained.

## 15. What cannot be validated yet

With zero approved images, the project cannot validate:

- real duplicate/perceptual-hash distributions or appropriate threshold precision;
- whether source/session/individual IDs are complete and reliable;
- connected-component sizes and whether 70/15/15 is achievable per class;
- contributor/source-domain bias;
- actual field-condition and confusion-class test coverage;
- absence of mislabeled or rights-defective real assets;
- final test isolation from future source families.

Those are precisely the pending execution portion of FI-A.7.

## 16. Exact next step

After rights/consent and reviewer workflows are approved, acquire a small rights-cleared pilot batch into UNASSIGNED metadata. Compute hashes, assign real derivative/source/session/individual groups, review perceptual candidates, build connected components, generate the first proposed partitions, and run the validator. FI-A.7 completes only when that real pilot/snapshot produces a reviewed leakage report with no unresolved errors.
