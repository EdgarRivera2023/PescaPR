# Fish Identifier Pilot — First OOD Binary Ingestion Report

**Date:** 2026-08-14
**Controlled workspace:** `<configured-controlled-dataset-root>/fish_identifier/pilot/raw/ood`
**Result:** Successful pipeline validation; 14/14 approved OOD originals ingested.

## 1. Human approval

Edgar Rivera independently approved category and acquisition decisions for all 14 candidates in `fish_identifier_first_ood_approval_packet.md`. The authoritative candidate queue records `labelStatus=APPROVED`, `datasetApproverDecision=APPROVED`, and `candidateDecision=APPROVED_FOR_ACQUISITION` with review date 2026-08-14. Rights had already passed item-level review.

## 2. Acquired candidates and binary identity

| Candidate | OOD category | Format | Resolution | SHA-256 | 64-bit pHash |
|---|---|---|---:|---|---|
| `ood-wc-35648764` | `unsupported_fish` | JPEG | 3320×2485 | `324a9215c597c115b32002b0d8e25cf577971988eca3a64768b2b89afdb603a1` | `9e3161ce30db663a` |
| `ood-wc-5191439` | `unsupported_fish` | JPEG | 1837×1235 | `628ea1d48f68f109413365d9dfe74d80c3f29a377a01439105897508a4038c54` | `d435269a6b0f9ba1` |
| `ood-wc-12104762` | `shark_ray` | JPEG | 1796×1180 | `1f38c1b43dab3b18191fb4724662e5fd49e4b3a229e4413afcd8e51378e6ceb8` | `c048caee3d4d693d` |
| `ood-wc-346756` | `shark_ray` | JPEG | 640×424 | `7aaa8219ad63b9e867872f8b0416df3bb5a71d6a99e78889cb2575a6ba444d7d` | `91297483175ef933` |
| `ood-wc-29209984` | `crustacean` | JPEG | 2176×2763 | `590640abe7c16c92946e6beab974089655313a102c7c053eac1f6907cf0982e9` | `ec95cada85f890e4` |
| `ood-wc-26396862` | `crustacean` | JPEG | 2048×1536 | `ae942ae39b468ad770ca737b69eecf0b053729ef7efc0313614cf4d53a96937c` | `a978c85ac2cf1c9d` |
| `ood-wc-181803762` | `fishing_gear` | JPEG | 3000×4000 | `ddd961096e1f9f431fb3725ed3acdcd319d173db6ea3d8d48f76a276ff3f897f` | `faac85752a99a14e` |
| `ood-wc-181803763` | `fishing_gear` | JPEG | 3000×4000 | `e63c3c92d8cfe62acec2f5edd12d00b910ca5d38c7342b1a251b084b03b2a64f` | `f28d8d75289ba34c` |
| `ood-wc-33200360` | `boat` | JPEG | 4608×3072 | `931aeb212bd5bd1c3d3d85df2556fe2b91d404ced418f88ba6441f7f1cf144d6` | `807e4ba8a39f98e3` |
| `ood-wc-37313734` | `boat` | JPEG | 3631×5339 | `58643e04d236cccd88cd1e1da6de47e4381633ff4d38b47f968cedd4c7d19bbe` | `d48b37c89728d42f` |
| `ood-wc-92100670` | `beach_water` | JPEG | 2048×1360 | `241f977e4b8fbb1a6d6708716ebb835faab2c401817452d337463dcf034ce167` | `93616b5997316c33` |
| `ood-wc-119270848` | `beach_water` | JPEG | 5658×3772 | `b5ffd8690665f9adab1be19a879fa39ea92ba77254d9c9492852a9ffb605baf5` | `a39cbcab72d50d60` |
| `ood-wc-140503658` | `person` | PNG | 708×988 | `878ab56a3fe9f27411d79d43bbf2b057f4a32268cda31e92ad4c254bb5e8b07d` | `e7a71dc8247692c9` |
| `ood-wc-61708668` | `person` | JPEG | 6000×4000 | `1e6164b76843a3617673f96e41f0db0d35872a77ded9ae26a2a48a53710580de` | `c16a383f43b0ce4f` |

Thirteen originals are JPEG and one is PNG. Two downloads initially received Wikimedia HTTP 429 (`ood-wc-37313734`, `ood-wc-61708668`) and succeeded on retry from the same approved original URLs. There were no final acquisition failures or substitutions.

## 3. Distribution and licensing

Each of seven categories contributes two originals: `unsupported_fish`, `shark_ray`, `crustacean`, `fishing_gear`, `boat`, `beach_water`, and `person`. All source records are individually reviewed Wikimedia Commons items; one public-domain record is NOAA-origin sanctuary work.

License mix: CC0 5, CC BY 2.0 5, CC BY 3.0 1, CC BY 4.0 1, and public domain 2. Attribution and canonical evidence remain in metadata.

## 4. Duplicate and grouping findings

- All 22 combined positive/OOD SHA-256 values are unique.
- No positive/OOD pHash pair is within the review threshold of Hamming distance ≤10.
- The two JaredMcKenzie fishing-gear originals are a pHash candidate at distance 8. They are distinct bytes and separate source items but appear correlated by creator/session and sequential source IDs.
- Those two records therefore share `source-ood-jaredmckenzie-gear-family` and `session-ood-jaredmckenzie-gear`; they receive the same partition. They retain separate derivative groups because neither is established as a derivative of the other.
- Other records have evidence-based singleton derivative/source/session groups. Fish-like OOD records have individual-fish groups; non-fish objects/scenes leave that field blank as not applicable.
- No controlling derivative, source, session, or individual group crosses partitions.

## 5. Metadata and deterministic partitions

Fourteen real OOD rows were added to `pilot_metadata.csv`, bringing the pilot to 22 rows: 8 positive and 14 OOD. Every OOD row has `sampleType=OOD`, an approved controlled `oodCategory`, and blank `fichaPezId`/`scientificName` fields.

The existing stable-seed group proposal was run twice with identical results. Partitions remain unlocked proposals:

| Partition | Positive | OOD | Combined |
|---|---:|---:|---:|
| TRAIN | 5 | 8 | 13 |
| VALIDATION | 1 | 4 | 5 |
| TEST | 2 | 2 | 4 |

The small distribution is a pipeline test, not an accuracy or coverage claim. TEST has not been independently curated, approved, versioned, or locked.

## 6. Validator result

Combined validation completed with **22 rows, 0 errors, 13 warnings**. Unit tests passed (4/4).

Expected warnings:

- 10 row-level missing `individualFishGroupId` warnings for non-fish OOD objects/scenes;
- one aggregate individual-fish-group coverage warning (10/22 missing);
- one aggregate contributor-group coverage warning (22/22 missing; public-source images are not PescaPR contributors);
- one same-partition pHash review warning for the correlated JaredMcKenzie gear pair at distance 8.

These warnings do not indicate leakage. Their disposition must be preserved before any warnings-as-errors snapshot gate.

## 7. Outcome and remaining gates

The real OOD ingestion pipeline is proven for rights approval, blank canonical-label semantics, controlled categories, external binary storage, exact/pHash identity, correlated grouping, deterministic partition proposals, and combined validation.

FI-A.7 and the broader pilot remain incomplete. Outstanding work includes independent locked-test curation/review, test-set versioning and locking, immutable snapshot/manifest generation, warning dispositions, broader pilot acquisition targets, and HIGH/VERY_HIGH specialist label reviews.

**Next action:** independently review the proposed combined TEST rows and grouping decisions, document warning dispositions, then create and validate the first immutable pilot snapshot/test-set version without using it for tuning.
