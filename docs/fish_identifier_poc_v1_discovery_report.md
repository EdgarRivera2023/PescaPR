# Fish Identifier POC v1 metadata discovery report

**Date:** 2026-08-14
**Decision:** **NO_GO** for POC v1 acquisition as frozen

## 1. Scope and method

This pass tested whether each frozen POC v1 class has a credible path to 30 rights-cleared,
independent photographic originals. No media binaries were downloaded. The production and POC
manifests, locked `testset-v1`, and `pilot-snapshot-v1` were not changed.

Discovery started with *Centropomus undecimalis* and *Albula vulpes*. Direct Wikimedia Commons
taxon categories were enumerated with item/license metadata, then NOAA/federal, Smithsonian/STRI,
and authoritative web results were checked for additional permitted photographic supply. Existing
non-acquired Alectis/Coryphaena pilot candidates were reused by reference rather than duplicated in
the POC queue. Maps, drawings, stamps, anatomy-only crops, obvious derivatives, iNaturalist
imports, video-only items, prohibited licenses, and same-session inflation were excluded.

The POC queue contains only newly discovered tarpon/snook/bonefish records. Existing Alectis and
Coryphaena candidate facts remain authoritative in `pilot_candidate_review.csv`.

## 2. Sources searched

- Wikimedia Commons per-taxon categories and item-level license metadata.
- NOAA/NOAA Fisheries and other discoverable federal pages.
- Smithsonian Open Access/repository and STRI Shorefishes records.
- Existing PescaPR pilot candidate metadata and rights decisions.

STRI is useful label-reference evidence but not an approved image source here: its image pages
identify copyright holders, state all rights reserved, and require direct permission. Smithsonian
repository search primarily returned publications/specimen references, not sufficient independent
CC0 field photographs. Search-engine results were discovery aids only, never candidate sources.

## 3. Supply summary

Raw count means non-acquired, plausible photographic candidates. Existing unlocked development
images are shown separately and locked TEST images are excluded.

| Species | Existing dev | Raw candidates | Independence-adjusted groups | Expected rights pass | Feasibility |
|---|---:|---:|---:|---:|---|
| *Alectis ciliaris* | 2 | 6 | 4 | 6 | NOT_VIABLE |
| *Coryphaena hippurus* | 4 | 7 | 6 | 6 | MARGINAL |
| *Megalops atlanticus* | 0 | 8 | 8 | about 7 | MARGINAL |
| *Centropomus undecimalis* | 0 | 4 | 4 | at most 4 | NOT_VIABLE |
| *Albula vulpes* | 0 | 3 | 2 | at most 3 | NOT_VIABLE |

Even before label and quality attrition, no class reaches the requested 35–40-candidate discovery
buffer. None has a documented, currently approved route to 30 independent accepted originals.

## 4. Rights/license mix

| Species | Public domain | CC0 | CC BY | Rights pending | Prohibited/rejected supply |
|---|---:|---:|---:|---:|---:|
| *A. ciliaris* | 1 | 3 | 2 | 0 | Numerous iNaturalist-origin/new API records excluded; 1 earlier candidate rejected |
| *C. hippurus* | 2 | 0 | 4 | 1 federal asset | 10 iNaturalist imports/derivatives plus unrelated/category-noise files excluded; 2 earlier candidates rejected |
| *M. atlanticus* | 2 | 1 | 5 | 8 item decisions | Most remaining allowed-license records are drawings/historical reproductions |
| *C. undecimalis* | 3 | 1 | 0 | 4 item decisions | Map and non-passing records excluded; 17 STRI images are all-rights-reserved |
| *A. vulpes* | 0 | 0 | 3 | 3 item decisions | 21 allowed-license records rejected as drawings, stamps, anatomy crops, or non-independent derivatives |

License metadata is only a proposal until the exact file page, creator, third-party warnings, and
source authority pass the rights SOP. Therefore the new queue remains `rightsStatus=PENDING` and
`acquisitionDecision=PENDING`.

## 5. Field/context mix

| Species | Field caught/held | Natural underwater | Controlled/reference | Aquarium | Other/unknown |
|---|---:|---:|---:|---:|---:|
| *A. ciliaris* | 0 | 3 (one correlated sequence) | 2 | 1 | 0 |
| *C. hippurus* | about 5 | 0 | 1 | 0 | 1 |
| *M. atlanticus* | about 3 | 1 | 3 | 0 | 1 |
| *C. undecimalis* | 0 | 1 | 2 | 0 | 1 |
| *A. vulpes* | 2 (same creator/session family) | 1 | 0 | 0 | 0 |

The counts are metadata/title-level classifications and require visual confirmation. Alectis,
snook, and bonefish cannot currently support a diverse field-photo domain. Two Albula files from
James-LaFontaine are deliberately one source/session group.

## 6. Label-review burden

- *Coryphaena hippurus*: **NORMAL_REVIEW**; check sex/head-profile and post-catch color.
- *Megalops atlanticus*: **NORMAL_REVIEW**; reject ambiguous juvenile or poor-angle silver fish.
- *Centropomus undecimalis*: **NORMAL_REVIEW**, with attention to related *Centropomus* and the
  diagnostic lateral line/head profile.
- *Alectis ciliaris*: **NORMAL_REVIEW** for clear adults; **ENHANCED_REVIEW** for juveniles.
- *Albula vulpes*: **ENHANCED_REVIEW**. Authoritative Caribbean references warn that
  *A. vulpes*, *A. goreensis*, and *A. cf. vulpes* are morphologically similar/cryptic, so a page
  title alone is insufficient for ambiguous samples.

No specialist decision was simulated.

## 7. Centropomus assessment

Commons has eight direct category files, but only four photographic records pass preliminary
license-type filtering. Other authoritative searches produced publications/specimen references or
the STRI Shorefishes gallery. STRI contains 17 labeled images, but its item pages explicitly state
all rights reserved and require permission from individual holders. That is not a current approved
acquisition route.

Result: **NOT_VIABLE** for 30 independent originals under current rights policy. A new, concrete
permission campaign could change the evidence, but hypothetical permission is not a GO basis.

## 8. Albula assessment

Commons has 32 direct files and 24 with superficially allowed license metadata, but most are
historical illustrations, stamps, anatomy/detail derivatives, or repeated representations. Only
three plausible full photographs remain; two share one creator/source family, leaving about two
independent groups. Authoritative references also identify cryptic related Albula taxa, increasing
label attrition risk.

Result: **NOT_VIABLE** for 30 independent originals under current rights and label policy.

## 9. Other class assessments

- **Alectis — NOT_VIABLE:** two reusable development rows plus four adjusted candidate groups;
  public supply is dominated by controlled images, iNaturalist-origin records, and one juvenile
  sequence.
- **Coryphaena — MARGINAL:** best field mix and ten approximate adjusted development/candidate
  groups, but still far below 30 after prohibited imports and correlations are removed.
- **Megalops — MARGINAL:** eight plausible independent candidates, while much of the apparent
  public-domain volume is illustration/history rather than field photography.

## 10. POC v1 decision

**NO_GO.** Centropomus and Albula do not have a credible current path to 30, and the other three
also lack the 35–40 candidate buffer required for expected attrition. POC v1 remains frozen as a
historical feasibility result. It must not be silently edited and FI-POC.2 acquisition must not
begin against it.

## 11. Replacement candidates for a future POC v2

No replacement eliminates review work entirely; the production catalog contains only four classes
marked as needing no expert review, and two of those failed feasibility here. Recommended classes
to investigate next, ranked by expected visual/source practicality:

1. *Ocyurus chrysurus* — distinctive yellow stripe/tail and promising underwater/field volume;
   requires snapper label review.
2. *Lactophrys quadricornis* — highly distinctive horned boxfish body and useful silhouette;
   requires boxfish review and juvenile controls.
3. *Haemulon plumieri* — medium confusion and mixed field/underwater potential; requires grunt
   review and canonical-name consistency checks.
4. *Lutjanus analis* — promising field-source volume, but introduces explicit snapper specialist
   review and related-class confusion.
5. *Lactophrys triqueter* — distinctive pattern/body contrast; source availability and boxfish
   review must be proven first.

POC v2 selection should run a metadata census before freezing membership. The likely pragmatic
tradeoff is accepting a bounded, available label reviewer for a visually distinctive/high-volume
class rather than freezing another low-risk but unsourceable species.

## 12. Exact next action

Do not acquire POC v1 binaries. Run `FI-POC.2-REVISION`: metadata-first feasibility for the ranked
replacement candidates, identify two replacements with at least 35 independence-adjusted,
rights-reviewable photographs, and then create a separate `poc_v2` manifest while preserving v1.
