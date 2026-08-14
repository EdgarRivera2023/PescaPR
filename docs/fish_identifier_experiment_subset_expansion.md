# Fish Identifier five-species experiment-subset expansion

**Assessment date:** 2026-08-14

## Status

The experiment-quality subset is **NOT READY**. This checkpoint performed a metadata-only source
availability and governance assessment. It did not download images, alter `testset-v1` or
`pilot-snapshot-v1`, approve a candidate, or simulate a specialist review.

The current real positive dataset remains eight originals: five *Coryphaena hippurus* and three
*Alectis ciliaris*. The other three pilot species have no acquisition-eligible positives because
their required independent specialist reviews have not occurred.

## Existing candidate queue

| Species | Existing candidates | Rights-approved | Label-approved | Acquired | Experiment target | Acquired shortfall to 50 |
|---|---:|---:|---:|---:|---:|---:|
| *Coryphaena hippurus* | 14 | 12 | 11 | 5 | 50–75 | 45 |
| *Alectis ciliaris* | 10 | 10 | 5 | 3 | 50–75 | 47 |
| *Scomberomorus cavalla* | 10 | 10 | 0 | 0 | 50–75 | 50 |
| *Seriola dumerili* | 14 | 13 | 0 | 0 | 50–75 | 50 |
| *Sphyraena barracuda* | 13 | 12 | 0 | 0 | 50–75 | 50 |

Rights approval alone is not acquisition permission. Candidate acquisition still requires all
label/expert gates and an independent Dataset Approver decision.

## Specialist-review queues

### *Scomberomorus cavalla* — qualified second review required

Pending candidates: `noaa-king-mackerel-graysreef`, `wc-152769457`, `wc-16466351`,
`wc-17940353`, and `wc-7011418`. Three additional discovered candidates were already rejected at
label review. The reviewer must distinguish *S. cavalla* from *S. regalis* and
*Acanthocybium solandri*; no decision may be inferred from a common name or filename.

### *Seriola dumerili* — qualified amberjack second review required

Pending candidates: `noaa-greater-amberjack-bottom-longline`, `wc-107181935`, `wc-107476832`,
`wc-152817285`, `wc-158222300`, `wc-158222302`, `wc-175061978`, `wc-17940231`, `wc-17940247`,
`wc-20057402`, `wc-20057422`, `wc-25971989`, and `wc-3475438`. Review must explicitly consider
*Seriola rivoliana* and other jacks, and must group same-photo/session material conservatively.

### *Sphyraena barracuda* — qualified barracuda expert required

Pending candidates: `noaa-barracuda-mona-2003`, `noaa-barracuda-reef1926`, `wc-16288520`,
`wc-17153958`, `wc-1758734`, `wc-1758736`, `wc-18763929`, `wc-25258225`, `wc-3293028`,
`wc-5963706`, `wc-6185265`, `wc-6185422`, and `wc-80689247`. The expert must resolve identity
against other *Sphyraena*, including the known Guía audit conflict. None is acquisition-eligible
until that review is recorded.

## Approved-source availability census

Wikimedia Commons taxon-category metadata was inspected only as a discovery census; category
membership is not label approval and each future item still needs item-level rights and visual
review.

| Species | Direct Commons media | Immediate limitation |
|---|---:|---|
| *Coryphaena hippurus* | 60 | Several entries are forbidden iNaturalist imports, crops of the same original, illustrations, or controlled/reference images. |
| *Alectis ciliaris* | 44 | Below the 50-original minimum before removing illustrations, video, correlated sessions, low-resolution files, and unsuitable licenses. |
| *Scomberomorus cavalla* | 14 | Far below target; the category includes illustrations, a range map, low-resolution files, and apparent duplicate/source variants. |
| *Seriola dumerili* | 44 | Below target before removing food/illustration/reference material and correlated sessions; specialist review is mandatory. |
| *Sphyraena barracuda* | 202 | Numerically promising, but many underwater sequences are correlated and every accepted label requires qualified expertise. |

NOAA states that its Digital Photo Collection is public domain, subject to item-level third-party
credit and privacy/publicity review. Smithsonian Open Access assets explicitly designated CC0
remain acceptable candidates, but collection/specimen availability must not be confused with
field-photo suitability. Neither source currently provides verified volume sufficient to close
the five-species target without item-by-item discovery and review.

## Field-photo and diversity assessment

No new approved images were added, so the measured acquired mix is unchanged:

| Species | Field/caught/handled | Natural underwater | Controlled/reference/aquarium | Total acquired |
|---|---:|---:|---:|---:|
| *Coryphaena hippurus* | 3 | 1 | 1 | 5 |
| *Alectis ciliaris* | 0 | 1 | 2 | 3 |
| Other pilot species | 0 | 0 | 0 | 0 |

These counts are metadata-level classifications and should be rechecked during the next binary
ingestion batch. Alectis particularly needs caught/handled field imagery. No source or session may
be allowed to dominate merely because many files are available from one sequence.

## Readiness decision

The readiness gate is not met. The exact minimum acquired-original shortfall is 242 images to
reach 50 per species. More importantly, all three HIGH/VERY_HIGH species remain at zero because
qualified reviews are unavailable, and the current eight positives are too small and too
controlled/reference-heavy for meaningful five-class model development.

A smaller experiment using only the two represented species would be mechanically possible but
would not answer the intended five-species confusion problem and must not be presented as Phase B
readiness.

## Exact next operational action

1. Assign real qualified reviewers to the three queues above and record independent decisions.
2. In parallel, run item-level metadata discovery beyond the exhausted/limited direct Commons
   categories, emphasizing PescaPR-owned or separately consented field photos and individually
   verified federal/CC0 items.
3. Prepare source-diverse Dataset Approver packets in small batches; download only after approval.
4. Ingest approved originals into the controlled external workspace, hash/group them, propose
   TRAIN/VALIDATION only, and rerun leakage validation. `testset-v1` remains immutable.
