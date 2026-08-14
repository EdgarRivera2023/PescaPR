# Fish Identifier POC v2 replacement feasibility report

**Audit date:** 2026-08-14
**Decision:** **NO_GO** — do not freeze POC v2 from this shortlist

## 1. Why POC v1 failed

POC v1 remains an immutable historical feasibility failure. Its five classes produced too few
independent, policy-compliant photographic candidates to support 30 accepted originals per class:
*Alectis ciliaris*, *Centropomus undecimalis*, and *Albula vulpes* were NOT_VIABLE, while
*Coryphaena hippurus* and *Megalops atlanticus* were only MARGINAL. No POC v1 binaries were
acquired and its manifest was not changed by this audit.

## 2. Scope and method

The five requested replacements were resolved through the frozen production manifest, then
reviewed using metadata only. The discovery queue records real source items; it does not contain
downloaded media or approvals. Wikimedia Commons taxon categories were enumerated through the
Commons API and filtered to photographs carrying Public Domain, CC0, or CC BY metadata. Known
iNaturalist imports, disallowed licenses, drawings, maps, prints, anatomy artifacts, and obvious
non-photographic material were excluded. Creator/title families were conservatively collapsed to
estimate independent source/session supply.

Federal and Smithsonian-family searches covered NOAA Photo Library, NOAA Sanctuaries, USGS, FDA
RSSL, Smithsonian Open Access/repository, and STRI Shorefishes. Two unique federal records were
added to the queue: an explicitly public-domain USGS *L. triqueter* item and an FDA *L. analis*
reference whose image rights remain unclear. NOAA items found for *O. chrysurus* and
*L. quadricornis* duplicated assets already represented on Commons or carried third-party credits.
STRI galleries are useful taxonomy references, but were not counted as rights-usable because
item-level training permission was not established.

This is a feasibility census, not final rights or visual adjudication. All plausible records remain
`rightsStatus=PENDING` and `labelStatus=PENDING`.

## 3. Production identities

| Production index | `FichaPez.id` | Scientific name | Guía common name | Existing acquired/candidate data |
|---:|---|---|---|---|
| 22 | `XTLHUX6xHya0BOisyR6E` | *Ocyurus chrysurus* | Colirrubia / rabirrubia | Catalog tracker only; no acquired sample |
| 7 | `Hjr9sFSdUEW1RVpR09mV` | *Lactophrys quadricornis* | Chapín veteado / chapín toro | Catalog tracker only; no acquired sample |
| 15 | `RO2iuTVLAX11dy3aNgdf` | *Haemulon plumieri* | Boquicolorao / cachicata / ronco blanco | Catalog tracker only; no acquired sample |
| 26 | `eBZEv2F3RUvtST6fx0cK` | *Lutjanus analis* | Sama / pargo criollo | Catalog tracker only; no acquired sample |
| 36 | `qDlhElFdSz5UOHDkU8Pe` | *Lactophrys triqueter* | Chapín liso | Catalog tracker only; no acquired sample |

The authoritative production IDs and indices were not changed. The external taxonomy sources use
*Haemulon plumierii* (two terminal i characters) while the frozen Guía record and manifest audit
metadata use *Haemulon plumieri*. This is a label-review concern, not authority to rewrite the
canonical ID or production manifest.

## 4. Candidate and independence-adjusted supply

Raw candidates are plausible photographic item records after the preliminary source/license-type
filter. Independence-adjusted counts conservatively collapse likely same-creator/session/sequence
families. Likely rights-usable supply is an upper-bound estimate pending exact item review.

| Species | Raw candidates | Independent groups | Likely rights-usable independent supply | 35–45 buffer met? |
|---|---:|---:|---:|---|
| *O. chrysurus* | 17 | 15 | about 12–15 | No |
| *L. quadricornis* | 9 | 9 | about 7–9 | No |
| *H. plumieri* | 19 | 13 | about 10–13 | No |
| *L. analis* | 7 | 6 | about 5–6 | No |
| *L. triqueter* | 21 | 13 | about 10–13 | No |

The initial raw totals for both boxfish were misleading: 37 *L. quadricornis* and 25
*L. triqueter* Commons items originated on iNaturalist and are excluded by project policy. The
remaining *L. triqueter* records also collapse into a limited number of creator/session families.
Neither provides 30 independent originals.

## 5. Rights/license mix

These counts describe preliminary license metadata among raw candidates, not final approval.

| Species | Public Domain | CC0 | CC BY | Unclear rights candidate | Excluded license/non-photo |
|---|---:|---:|---:|---:|---:|
| *O. chrysurus* | 7 | 5 | 5 | 0 | 30 |
| *L. quadricornis* | 4 | 2 | 3 | 0 | 56 |
| *H. plumieri* | 4 | 2 | 13 | 0 | 24 |
| *L. analis* | 6 | 0 | 0 | 1 FDA image | 19 |
| *L. triqueter* | 3 | 1 | 17 | 0 | 43 |

Every retained Commons item still requires exact file-page adjudication, creator verification,
third-party-rights review, and attribution capture. API license metadata alone is not approval.

## 6. Field-photo availability

Context is conservatively inferred from source metadata/title and must be visually reviewed.
`OTHER` means metadata did not establish a stronger context; it must not be counted as field data.

| Species | Field caught/held | Natural underwater | Controlled/reference | Aquarium | Other/unknown |
|---|---:|---:|---:|---:|---:|
| *O. chrysurus* | 0 | 7 | 0 | 0 | 10 |
| *L. quadricornis* | 0 | 0 | 1 | 0 | 8 |
| *H. plumieri* | 1 | 9 | 0 | 0 | 9 |
| *L. analis* | 0 | 0 | 2 | 1 | 4 |
| *L. triqueter* | 0 | 9 | 0 | 1 | 11 |

*O. chrysurus* and *H. plumieri* have the strongest verified natural-scene signal. None of the
five currently demonstrates a robust metadata-verified caught/held supply. The two boxfish have
useful morphology and underwater potential, but extreme creator concentration. A large `OTHER`
bucket is a reason for visual adjudication, not evidence of adequate field coverage.

## 7. Visual confusion and review burden

- **Ocyurus chrysurus — ENHANCED_REVIEW.** The yellow stripe/tail are distinctive in good adult
  views, but juveniles, faded catch photos, and other snappers can confuse the label. Competent
  review is sufficient for clear adults; ambiguous samples should be escalated.
- **Lactophrys quadricornis — ENHANCED_REVIEW.** Clear horned adults are distinctive, while
  juveniles may lack obvious horns and can be confused with related trunkfish/cowfish. Life stage
  and body pattern require deliberate review.
- **Haemulon plumieri — ENHANCED_REVIEW.** Related grunts, post-catch color loss, mixed schools,
  and the `plumieri`/`plumierii` source-name discrepancy increase label risk. Clear full adults do
  not justify a blanket specialist gate, but ambiguous specimens do.
- **Lutjanus analis — SPECIALIST_REQUIRED.** Juveniles and faded adults overlap visually with
  other *Lutjanus* in the production catalog. Its limited supply does not justify accepting that
  specialist bottleneck for the first POC.
- **Lactophrys triqueter — ENHANCED_REVIEW.** Adults are recognizable by body pattern and shape,
  but juveniles and unusual color phases require careful separation from other boxfish. Putting
  both shortlisted *Lactophrys* classes in a five-class POC would also add deliberate sibling
  discrimination before enough independent data exists.

One Commons asset (`sourceItemId=10333908`, “BLW Coffer Fish”) appears in both boxfish categories
and describes both taxa. It is license-ineligible under the present policy and is excluded, but
the cross-label occurrence reinforces the need for exact visual label adjudication. The queue
uses species-qualified candidate IDs while retaining the shared source item ID so this conflict
cannot be hidden.

## 8. Feasibility ratings and ranking

| Rank | Species | Rating | Reason |
|---:|---|---|---|
| 1 | *Ocyurus chrysurus* | MARGINAL | Best mix of independent sources, clear adult cues, and natural imagery; still only 15 adjusted groups. |
| 2 | *Haemulon plumieri* | MARGINAL | Thirteen adjusted groups and the best natural/field metadata mix, offset by grunt and taxonomy-name review risk. |
| 3 | *Lactophrys triqueter* | MARGINAL | Distinctive morphology and underwater examples, but only 13 adjusted groups after prohibited-origin filtering. |
| 4 | *Lactophrys quadricornis* | NOT_VIABLE | Only nine adjusted permitted-source groups remain after excluding 37 iNaturalist-origin items. |
| 5 | *Lutjanus analis* | NOT_VIABLE | Only six adjusted groups, weak field coverage, and a specialist snapper-review burden. |

The best two replacement leads are *O. chrysurus* and *H. plumieri*. They are recommended for a
second-source/permission feasibility expansion, **not** yet as frozen POC v2 classes.

## 9. Reassessment of retained POC v1 classes

- **Coryphaena hippurus remains MARGINAL.** Four unlocked development images and roughly six
  independent non-acquired candidate groups are useful seed material, but do not establish a path
  to 30. It should not be retained merely because data already exists.
- **Megalops atlanticus remains MARGINAL.** About eight plausible independent candidates were
  found previously; public search volume is inflated by historical illustrations and reference
  artifacts. No new evidence raises it to VIABLE.
- **Alectis ciliaris remains NOT_VIABLE.** This task found no material new evidence and did not
  repeat broad discovery.

## 10. Complete POC v2 recommendation and GO gate

There is **no defensible exact five-class POC v2 set yet**. None of the five replacements, nor
Coryphaena or Megalops, currently demonstrates a credible rights- and independence-adjusted route
to 30 accepted originals. Freezing any five would repeat the POC v1 error and violate the GO gate.

For prioritizing the next feasibility round only, the strongest provisional five-class research
set is:

1. *Ocyurus chrysurus* — `XTLHUX6xHya0BOisyR6E`, production index 22
2. *Haemulon plumieri* — `RO2iuTVLAX11dy3aNgdf`, production index 15
3. *Lactophrys triqueter* — `qDlhElFdSz5UOHDkU8Pe`, production index 36
4. *Coryphaena hippurus* — existing POC v1 class, still MARGINAL
5. *Megalops atlanticus* — existing POC v1 class, still MARGINAL

This list is **not** an output-index recommendation and must not become a manifest. It identifies
where targeted new-source discovery or explicit permission efforts would be most informative.
Every member must independently clear the 30-original credibility gate before POC v2 is frozen.

**POC v2 decision: NO_GO.** Zero of the five replacement candidates currently meets the requested
35–45 independence-adjusted discovery buffer, and none has a documented route to 30 accepted
independent originals after expected attrition.

## 11. Exact next action

Do not acquire binaries and do not create `poc_v2`. Run another metadata-first shortlist round
focused on production classes with demonstrably larger multi-source photographic categories, while
in parallel checking concrete item-level permission routes for *O. chrysurus* and *H. plumieri*.
The next shortlist must establish at least five classes with 35–45 adjusted reviewable candidates
each before `FI-POC.2-REVISION` can freeze a manifest.
