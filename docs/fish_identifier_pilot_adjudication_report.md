# Fish Identifier Pilot — Initial Candidate Adjudication Report

**Review date:** 2026-08-14

## 1. Scope

This report records the first item-level rights and visual/metadata label review of all 58 discovery candidates plus a narrow additional *Alectis ciliaris* search. The review used exact Commons file-description pages, source-hosted images, NOAA item/gallery pages, and the NOAA Fisheries copyright policy.

Temporary source-image copies were used only for visual review outside the repository and were deleted after adjudication. No image was acquired into the controlled dataset workspace, no `pilot_metadata.csv` row was created, and no dataset snapshot or partition was created.

This is an initial adjudication pass, not legal approval. The same agent performed discovery and this review, so the independent Dataset Approver gate remains unsatisfied. HIGH and VERY_HIGH label requirements also remain unsatisfied because no second qualified/expert reviewer was available.

## 2. Inventory reviewed

| Species | Existing reviewed | Added | Final candidates |
|---|---:|---:|---:|
| *Coryphaena hippurus* | 14 | 0 | 14 |
| *Alectis ciliaris* | 7 | 3 | 10 |
| *Scomberomorus cavalla* | 10 | 0 | 10 |
| *Seriola dumerili* | 14 | 0 | 14 |
| *Sphyraena barracuda* | 13 | 0 | 13 |
| **Total** | **58** | **3** | **61** |

## 3. Rights outcomes

| Species | APPROVED | PENDING | REJECTED |
|---|---:|---:|---:|
| *C. hippurus* | 12 | 1 | 1 |
| *A. ciliaris* | 10 | 0 | 0 |
| *S. cavalla* | 10 | 0 | 0 |
| *S. dumerili* | 13 | 1 | 0 |
| *S. barracuda* | 12 | 1 | 0 |
| **Total** | **57** | **3** | **1** |

All 55 Commons candidates displayed an allowed public-domain, CC0, or CC BY license on the exact file page, returned HTTP 200, and showed no detected contradictory rights/deletion warning. Their creator, license, attribution, and evidence references remain recorded per item.

NOAA decisions were item-specific:

- Gray's Reef credits the king-mackerel photo to NOAA; the NOAA Fisheries policy says NOAA-created photos are not subject to U.S. copyright and requests NOAA credit. Rights review: APPROVED.
- NOAA Photo Library item `reef1926.jpg` credits the NOAA CCMA Biogeography Team. Rights review: APPROVED.
- The SLICK gallery credits the adult mahi-mahi photograph to © Blue Planet Archive/Masa Ushioda. Rights review: REJECTED.
- The general mahi species page contains multiple NOAA and third-party assets, but the candidate did not preserve a unique asset identifier. Rights review: PENDING.
- The bottom-longline amberjack gallery's rendered metadata did not expose a complete stable item credit. Rights review: PENDING.
- The legacy Mona Island page did not provide sufficient item-level photographer/federal-authorship evidence during review. Rights review: PENDING.

## 4. Label outcomes

| Species | APPROVED | PENDING | REJECTED |
|---|---:|---:|---:|
| *C. hippurus* | 11 | 2 | 1 |
| *A. ciliaris* | 5 | 5 | 0 |
| *S. cavalla* | 0 | 7 | 3 |
| *S. dumerili* | 0 | 14 | 0 |
| *S. barracuda* | 0 | 13 | 0 |
| **Total** | **16** | **41** | **4** |

Clear adult *C. hippurus* and *A. ciliaris* photographs passed the initial visual identity gate. Alectis juveniles remain PENDING because their life-stage morphology triggers enhanced review.

Every non-rejected *S. cavalla* and *S. dumerili* candidate remains PENDING for the second independent HIGH-confusion review. Every *S. barracuda* candidate remains PENDING with `EXPERT_REVIEW_REQUIRED`; qualified barracuda expertise was not available and was not simulated.

## 5. Acquisition eligibility

| Candidate decision | Count |
|---|---:|
| APPROVED_FOR_ACQUISITION | 0 |
| PENDING | 52 |
| REJECTED | 9 |

No candidate passed all governance gates. Although 16 LOW/MEDIUM candidates have both an initial rights approval and initial label approval, the discovery/review separation and independent Dataset Approver gate remain incomplete. They must not be downloaded as pilot assets yet.

## 6. Alectis additional discovery

The narrow follow-up search found only three additional policy-compatible photographic candidates: Commons items 49117828, 49117829, and 49117830. Each exact page marks the work CC0 and each image visibly shows juvenile *Alectis*.

All three came from the same Mystery Bay capture sequence and must be one controlling source/session/individual group. They add juvenile variation but only one independent diversity unit. Their labels remain PENDING for enhanced juvenile review.

Other category results were excluded because they were CC BY-SA, iNaturalist-origin, a distribution map, video, or otherwise outside pilot policy. Alectis therefore improves from seven to ten candidates but remains **UNCERTAIN** for a 25-independent-image pilot target.

Because Scomberomorus adjudication left only five non-rejected candidates, the required narrow replacement search was also run. It found no suitable additions: the only unqueued records were a historical illustration, a duplicate/hash-demo artifact, CC BY-SA photographs outside the pilot allowlist, and a range map. No weak replacement row was added merely to raise the count.

## 7. Rejections

Nine candidate decisions were rejected:

- rights: one third-party copyrighted NOAA-gallery mahi image;
- non-field material: one museum/taxidermy mahi display;
- insufficient target visibility/ambiguity: two king-mackerel candidates;
- contradictory/mixed label evidence: one king-mackerel candidate;
- duplicate/source variants: one Alectis and two king-mackerel candidates;
- technical quality: one low-resolution watermarked Seriola image.

Rejection does not imply the source page is inaccurate; it means the item does not meet this pilot's rights, label, independence, or field-photo quality gate.

## 8. Pending reasons and expert gaps

- Three NOAA candidates need better item-level rights evidence.
- Five Alectis candidates need enhanced juvenile review.
- Seven non-rejected Scomberomorus candidates need a second reviewer competent in king-mackerel/cero/wahoo distinctions.
- Fourteen Seriola candidates need a second reviewer competent in greater/almaco amberjack and related jack distinctions.
- Thirteen Sphyraena candidates need two reviews including a qualified barracuda expert.
- Sixteen initially cleared LOW/MEDIUM labels still need independent Dataset Approver confirmation because the same agent performed discovery and initial review.

## 9. Source concentration and correlated groups

The final queue contains 55 Commons and 6 NOAA candidates. Commons is an aggregation layer rather than one photographer, but the pool remains operationally concentrated and requires upstream-source grouping.

Confirmed or suspected controlling groups include:

- FDA Alectis `wc-152794810` / `wc-19295725`: same original; latter rejected as duplicate.
- Mystery Bay Alectis 49117828/49117829/49117830: same juvenile/session; one group.
- Akumal mahi 165855570/165855571: same catch/session; one group.
- King mackerel 16466351/7031830 and 17940353/8017189: duplicate/source variants; one item from each pair rejected.
- Juvenile Seriola 158222300/158222302: same source/session; one group.
- Barracuda 1758734/1758736 and aquarium items 6185265/6185422: correlated-source checks remain required when hashes exist.

## 10. Variation coverage

The viable pool includes caught/held, boat/dock, whole lateral reference, underwater/natural, aquarium, adult, juvenile, left/right orientation, partial/head detail, and difficult lighting. Controlled reference imagery is still overrepresented, while independent Caribbean catch sessions remain scarce. Alectis juvenile coverage improved, but all three additions are one session.

## 11. First binary batch decision

A binary acquisition batch is **not yet authorized** because zero rows are `APPROVED_FOR_ACQUISITION`.

After an independent Dataset Approver confirms the 16 initially cleared LOW/MEDIUM decisions, the recommended first batch is **8 images**:

- five *C. hippurus*: one FDA/reference lateral, one caught/held boat image, one underwater image, one post-catch lateral image, and one different caught/background group;
- three adult *A. ciliaris*: NOAA underwater, FDA reference, and aquarium/natural-context items from independent upstream groups.

Avoid duplicate pairs and do not include HIGH/VERY_HIGH species until their required label reviews are complete. This eight-image batch is sufficient to exercise storage, SHA-256, pHash, metadata ingestion, grouping, deterministic partition proposal, and validator behavior without treating pending candidates as approved.

## 12. Exact next action

Assign an independent Dataset Approver to the 16 initially rights-and-label-cleared LOW/MEDIUM candidates, obtain the required second/expert label reviews for HIGH/VERY_HIGH candidates, and resolve the three NOAA rights-pending records. Once at least the proposed eight source-diverse candidates are explicitly marked `APPROVED_FOR_ACQUISITION`, authorize that first controlled binary batch.
