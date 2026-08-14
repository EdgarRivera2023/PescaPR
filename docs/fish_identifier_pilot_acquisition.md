# Fish Identifier Pilot Acquisition Package

**Plan date:** 2026-08-14

**Roadmap scope:** FI-A.7-PILOT preparation; no images acquired

**Pilot size:** 125 accepted positive originals plus 50 accepted OOD originals

## 1. Pilot purpose

This pilot is a process test, not a classifier dataset. It must exercise legal-source review, canonical labeling, metadata creation, checksums, perceptual duplicate review, grouping, deterministic partition proposals, leakage validation, OOD handling, and a small locked test set before PescaPR attempts Stage 1-scale acquisition.

The pilot does not need enough images to train a useful model. No candidate counts until it becomes a rights-cleared accepted original. The 124 current Guía Oficial images remain excluded because their provenance/training rights are unresolved.

## 2. Selected species

The machine-readable selection is `datasets/fish_identifier/pilot_species_plan.csv`. Risk describes within-catalog visual confusion; sourcing difficulty remains separate.

| `FichaPez.id` | Scientific name | Common name | Risk | Sourcing | Target | Why selected |
|---|---|---|---|---|---:|---|
| `AYGYpLjkS9LEv7AXVAuk` | *Coryphaena hippurus* | Dorado | LOW | LOW / PROMISING | 25 | Distinct ordinary field species; exercises sex/head-profile and fresh/post-catch color metadata |
| `5SkWhUQgG6JuanpfSLUT` | *Alectis ciliaris* | Corcobado | MEDIUM | MEDIUM / UNKNOWN | 25 | Jack with pronounced juvenile/adult change; exercises biological-variation and label review |
| `V39KoLAZkg0MBjiWaa46` | *Scomberomorus cavalla* | Carite | HIGH | LOW / PROMISING | 25 | Known mackerel/wahoo confusion family without selecting a rare class |
| `u1JpMvcENOy98cd31Za5` | *Seriola dumerili* | Medregal | HIGH | LOW / PROMISING | 25 | Exercises amberjack review against frozen *Seriola rivoliana* |
| `ejX0Cx9YYxsmOQTJb8kK` | *Sphyraena barracuda* | Picúa | VERY_HIGH | LOW / PROMISING | 25 | Exercises enhanced barracuda review while being more obtainable than *S. guachancho* or *S. picudilla* |

The set contains LOW, MEDIUM, HIGH, and VERY_HIGH risk; mackerel, amberjack, barracuda, and jack confusion cases; and no rare/deepwater-only selection. “PROMISING” is still an acquisition assumption, not confirmed inventory.

## 3. Positive and partition targets

Target **25 accepted independent originals per species**, **125 positives total**. Discovery should expect rejections and must not manufacture the accepted target through crops or bursts.

For an ideal group-compatible pilot, aim approximately at:

| Sample type | Train | Validation | Locked test | Total |
|---|---:|---:|---:|---:|
| Positives | 85 | 20 | 20 | 125 |
| OOD | 34 | 8 | 8 | 50 |
| Combined | 119 | 28 | 28 | 175 |

These are component-level targets, not row-by-row quotas. A derivative/session/individual/source component stays intact even when this shifts ratios. Each selected species should contribute at least two independent groups to validation and two to locked test where the 25-image target permits.

## 4. Pilot OOD target

Acquire **50 accepted OOD originals**:

| OOD category | Target |
|---|---:|
| `unsupported_fish` | 10 |
| `shark_ray` | 5 |
| `crustacean` | 5 |
| `fishing_gear` | 5 |
| `person` | 5 |
| `cooler` | 5 |
| `boat` | 5 |
| `beach_water` | 5 |
| `blurry_invalid` | 5 |
| **Total** | **50** |

Hands may be tagged within `person`; unrelated non-animal objects may substitute for up to two examples in another scene/object category if recorded as `other_nonfish`. The pilot tests metadata breadth, not mature OOD balance. OOD receives the same rights, hash, grouping, and partition controls as positives and never receives a `FichaPez.id`.

## 5. Allowed pilot sources

Use the clearest-rights sources in this order:

1. PescaPR-owned originals with documented chain of title.
2. Contributor originals covered by separately approved commercial ML/derivative permission and required likeness/property releases.
3. Item-verified NOAA or other U.S. federal works whose government creator/public-domain status is documented; exclude credited third-party works.
4. Smithsonian items explicitly marked CC0.
5. Wikimedia Commons only when the individual file is public domain, CC0, or CC BY; uploader authority/provenance and attribution must pass manual review. CC BY-SA is outside the default pilot allowlist pending legal approval.

Source targets: use at least three independent source families across the positive pilot where feasible; no source domain should exceed 40% of accepted positives, no contributor 20%, and no session/individual 5%. If first-party supply cannot achieve this, stop and reassess instead of relaxing rights.

## 6. Forbidden pilot sources

Do not use:

- the current 124 Guía images unless individually cleared in a future rights process;
- iNaturalist data;
- social media, forums, search-result thumbnails, or copied/reposted images;
- FishBase contributor images without direct explicit permission;
- research datasets lacking image-level commercial ML and derivative rights;
- all-rights-reserved, editorial-only, research-only, NC, ND, or unclear-license images;
- an agency/museum page merely because its host is public or reputable;
- normal PescaPR user uploads without separate voluntary ML consent.

## 7. Candidate workflow

`pilot_candidate_review.csv` is a header-only pre-ingestion queue. It prevents incomplete candidates from appearing in canonical model metadata.

```text
DISCOVERED
  → RIGHTS_REVIEW
  → LABEL_REVIEW
  → APPROVED
  → INGESTED
  → GROUPED
  → PARTITION_PROPOSED
  → VALIDATED
```

- **DISCOVERED:** record durable source item/page, proposed sample type/species/OOD category, and source tier. Do not download into approved storage.
- **RIGHTS_REVIEW:** complete the rights checklist and retain evidence. Failure becomes `REJECT_RIGHTS` or `REJECT_UNSUPPORTED_SOURCE`.
- **LABEL_REVIEW:** confirm positive scientific identity/frozen ID or OOD category. Failure becomes `REJECT_LABEL`.
- **APPROVED:** rights, label/category, and initial quality decisions are all approved. This still is not a partition.
- **INGESTED:** acquire the approved original into controlled raw storage, assign `internalImageId`, record original technical metadata, compute SHA-256/pHash, and create the canonical `pilot_metadata.csv` row as UNASSIGNED.
- **GROUPED:** exact/perceptual/source review establishes derivative/source/session/individual/contributor groups. Redundant candidates become `REJECT_DUPLICATE` or EXCLUDED audit rows.
- **PARTITION_PROPOSED:** connected components receive deterministic partition proposals; nobody manually places a single convenient image across a group boundary.
- **VALIDATED:** validator has zero unresolved ERRORs and warnings have approved dispositions; locked-test review is complete.

Other rejection state: `REJECT_QUALITY`. Rejected candidates retain their decision/evidence in the candidate queue but never enter TRAIN, VALIDATION, or TEST.

## 8. Rights-review checklist

Use `APPROVED`, `PENDING`, or `REJECTED`; blank is PENDING, never approval.

- Identify the original source and durable source item/page ID.
- Identify creator/photographer and rights holder; do not assume uploader equals creator.
- Record exact license/version and license URL or signed permission evidence.
- Confirm commercial reuse is permitted.
- Confirm modifications/derivatives and dataset preprocessing are permitted.
- Confirm site/source terms do not prohibit commercial ML training.
- Record attribution requirements and exact attribution text.
- Preserve a dated evidence snapshot/reference and original source URL.
- Review third-party material, trademarks, recognizable people, privacy/publicity, and property concerns.
- For private contributions, link the signed consent/permission record and confirm contributor authority.
- Have a named rights reviewer make the final decision; software/license keyword filtering cannot approve an image.

Reject public availability without proof, conflicting source terms, uncertain ownership, NC/ND/editorial restrictions, missing contributor permission, or an unresolved third-party warning.

## 9. Label-review checklist

For a positive candidate:

- Establish scientific identity using source evidence and visible diagnostic features.
- Resolve exactly one frozen `FichaPez.id`; never use common name alone.
- Compare the proposed scientific name with manifest audit metadata by ID.
- Reject unresolved frozen-class ambiguity or hybrid/complex uncertainty.
- For multiple fish, accept only when one target is clearly dominant and unambiguous; record bystanders.
- Reject fish that are cleaned, damaged, obscured, or partial beyond reliable identification.
- Investigate any visible mismatch with source metadata rather than choosing the closest class.
- Record reviewer, evidence, decision, and reason.

Pilot review levels:

- *Coryphaena hippurus*: one qualified primary reviewer; escalate disagreement/juvenile uncertainty.
- *Alectis ciliaris*: two reviews when juvenile or the source label is not authoritative.
- *Scomberomorus cavalla* and *Seriola dumerili*: two independent reviews, one qualified in the confusion family.
- *Sphyraena barracuda*: two independent reviews with at least one qualified expert; disagreement is rejection/quarantine, not majority vote.
- OOD `unsupported_fish`: review sufficiently to prove it is not one of the frozen 39. Other OOD categories need a second check only when ambiguous.

## 10. Storage layout

Logical working layout—do not create empty directories or add images during preparation:

```text
datasets/fish_identifier/pilot/
  raw/          # immutable acquired originals; external/local controlled storage, not normal Git
  approved/     # snapshot materialization or links, not a second uncontrolled copy
  quarantine/   # pending/rejected restricted candidates
  metadata/     # canonical metadata and evidence references
  reports/      # validator, rights, grouping, and partition reports
```

Never place dataset images in Android assets/resources, `drawable`, or `raw`. Separate contributor identity/consent documents from shareable metadata. Source/license evidence that contains personal or restricted material belongs in access-controlled storage, referenced by opaque ID.

## 11. Git and binary-storage policy

Normal Git should track schemas, scripts, source plans, sanitized metadata snapshots, checksums, and reports. It should not track pilot or future dataset image binaries, contributor PII, permission documents, or mutable raw/quarantine directories.

Use an access-controlled local dataset workspace for the pilot and later content-addressed external object storage with immutable versions, retention/backups, and least-privilege access. Metadata in Git references opaque object keys and SHA-256, allowing integrity verification without embedding media. Before acquisition, add narrowly scoped ignore rules only after the external/local root and evidence policy are approved.

Git LFS is not the default for a 10,000+ image dataset because it still couples dataset lifecycle/access/cost to repository operations. Evaluate it only for a small, redistributable, rights-cleared benchmark if operational needs justify it. Do not configure LFS or external storage in this task.

## 12. Metadata and hash/group workflow

`pilot_metadata.csv` exactly matches `image_metadata_template.csv` and is header-only. After approval:

1. Copy the acquired original bytes into controlled raw storage without transformation.
2. Generate a stable internal ID unrelated to display names.
3. Record source, rights, label, and original filename metadata from the approved candidate.
4. Compute lowercase SHA-256 from original bytes.
5. Compute the documented 64-bit pHash; record implementation/version in snapshot notes/report.
6. Search SHA-256, source IDs/URLs, pHash neighbors, filenames, contributors, and known sessions.
7. Assign derivative/source/session/individual/contributor groups; use the original internal ID as the derivative group for a unique original until linked.
8. Keep the row UNASSIGNED/UNLOCKED until grouping review completes.

No crop, mirror, download resize, or burst frame increases the accepted-original target. If retained, it inherits the original's connected component.

## 13. Partition and locked-test workflow

Build transitive connected components using the FI-A.7 framework. Apply stable 70/15/15 suggestions at component level, then deterministically balance within the pilot targets without splitting groups. Record every override.

Independently review proposed TEST rows for realistic PescaPR conditions, source/contributor separation, risk-family coverage, device/quality variation, partial-but-usable photos, and OOD breadth. Freeze approved test rows as `LOCKED` in a pilot snapshot such as `pilot-testset-v1`; never use them to tune the pilot process/model. If a test row is defective, version the set rather than silently replacing it.

## 14. Validator workflow

Run before partition proposal to expose metadata gaps, then after grouping/partitioning as the gate:

```text
python tools/fish_identifier/validate_dataset.py \
  --metadata datasets/fish_identifier/pilot_metadata.csv \
  --manifest app/src/main/assets/fish_classifier_manifest.json
```

For snapshot approval, rerun with `--warnings-as-errors`. A warning may be dispositioned only when the reason is documented—for example, `individualFishGroupId` is not applicable to an empty boat OOD scene. Retain command, validator checksum/version, output, metadata checksum, and reviewer decision in `pilot/reports` outside normal Git until sanitized.

## 15. Pilot success criteria

The pilot succeeds only with real accepted images demonstrating all of the following:

- complete, reviewable rights evidence and attribution metadata;
- positive labels resolve to unchanged frozen IDs and required review levels pass;
- SHA-256 and pHash generation is reproducible;
- derivative/source/session/individual groups are practically usable;
- exact/perceptual duplicates are detected, adjudicated, and handled;
- deterministic component-level partition proposals are reproducible;
- no confirmed controlling group crosses partitions;
- OOD rows have blank fish IDs and controlled categories;
- a realistic small locked test set is independently approved and frozen;
- the approved pilot snapshot has zero unresolved validator ERRORs and every WARNING has an approved disposition;
- metadata/snapshot hashes and external binary references reproduce the same inventory.

The pilot may succeed below 125/50 if every mechanism is exercised and shortfall reasons are documented. It does not satisfy Stage 1 counts, demonstrate classifier accuracy, or complete FI-A.8.

## 16. Stop conditions

Stop collection and escalate when:

- source or contributor rights cannot be proven;
- positive labels cannot meet the required confidence/reviewer policy;
- exact/perceptual/source grouping cannot reliably identify related assets;
- one source exceeds 40%, a contributor exceeds 20%, or a session/individual exceeds 5% without an approved pilot exception;
- a source yields heavily correlated reference/specimen imagery rather than intended field diversity;
- required metadata/evidence is operationally impractical or exposes unmanaged personal data;
- validator behavior/schema cannot express a real case or produces a structural false pass/failure;
- selected-species availability is materially worse than FI-A.6 assumed;
- protected/ethical constraints or third-party rights create risk;
- someone proposes moving pending candidates directly into a final partition.

## 17. After successful pilot validation

Publish a sanitized pilot report describing candidates, acceptance/rejection rates, rights sources, reviewer burden, group coverage, duplicate findings, component/partition counts, warnings/dispositions, and storage costs. Correct the framework through a versioned schema/tool change if needed. Then mark FI-A.7 complete only when the roadmap's real-image validation condition is met and decide whether the process is ready for controlled Stage 1 acquisition. Do not proceed automatically to bulk acquisition or model training.

## 18. Exact next operational action

Approve the contributor permission language, rights-review SOP, reviewer roster, external/local binary workspace, and narrow ignore policy. Then run a metadata-only discovery pass for the five pilot species and OOD categories, populating only `pilot_candidate_review.csv` at `DISCOVERED`; no image is acquired until its rights and label/category decisions are APPROVED.
