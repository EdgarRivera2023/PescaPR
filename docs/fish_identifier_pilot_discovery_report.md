# Fish Identifier Pilot — Metadata Discovery Report

**Discovery date:** 2026-08-14
**Scope:** Metadata-only discovery for the five approved pilot species. No image binary was downloaded, approved, ingested, hashed, partitioned, or added to `pilot_metadata.csv`.

## Method and sources searched

Discovery used scientific-name categories and item metadata from the Wikimedia Commons API plus specific NOAA/NOAA Fisheries source pages. Search results were used only to locate canonical source pages. Smithsonian Open Access was considered under the approved policy, but no item-specific Smithsonian candidate was advanced in this pass.

Every retained row represents a real source item or a specific image-bearing NOAA page. Commons media URLs are recorded only as discovery metadata; they were not fetched. All candidates entered `DISCOVERED / PENDING` and still require item-level rights review and visual species-label review.

## Candidate inventory and pilot viability

| Species | `FichaPez.id` | Candidates | Pilot viability | Observation |
|---|---|---:|---|---|
| *Coryphaena hippurus* | `AYGYpLjkS9LEv7AXVAuk` | 14 | VIABLE | Multiple public-domain/CC candidates plus NOAA pages; reference material may not pass field-photo quality review. |
| *Alectis ciliaris* | `5SkWhUQgG6JuanpfSLUT` | 7 | UNCERTAIN | Below the 10–15 discovery goal and concentrated in Commons/reference sources; additional Tier 1 discovery is needed. |
| *Scomberomorus cavalla* | `V39KoLAZkg0MBjiWaa46` | 10 | PROBABLY_VIABLE | Candidate volume is adequate for adjudication, but likely source variants and HIGH-confusion review reduce the usable yield. |
| *Seriola dumerili* | `u1JpMvcENOy98cd31Za5` | 14 | PROBABLY_VIABLE | Useful caught, juvenile, and reference leads; HIGH-confusion review and correlated source groups remain concerns. |
| *Sphyraena barracuda* | `ejX0Cx9YYxsmOQTJb8kK` | 13 | PROBABLY_VIABLE | NOAA Puerto Rico/natural-context leads exist, but VERY_HIGH-confusion expert review and suspected variants constrain yield. |
| **Total** |  | **58** |  | The target range was 50–75 genuine candidates. |

These ratings concern only the small 25-approved-image pilot target, not Stage 1 feasibility.

## Source, status, and license distribution

| Source | Candidates |
|---|---:|
| Wikimedia Commons | 52 |
| NOAA / NOAA Fisheries | 6 |

| Proposed item metadata | Candidates |
|---|---:|
| Public domain | 27 |
| CC0 | 3 |
| CC BY 2.0 | 8 |
| CC BY 2.5 | 1 |
| CC BY 3.0 | 8 |
| CC BY 4.0 | 5 |
| Federal public-domain claim requiring item verification | 6 |

- Rights status: 58 PENDING, 0 APPROVED, 0 REJECTED.
- Label status: 58 PENDING, 0 APPROVED, 0 REJECTED.
- Candidate decision: 58 PENDING.

License names above are proposed source metadata, not PescaPR approval. NOAA-hosted content remains pending because agency hosting alone does not establish federal authorship or rule out third-party rights. Commons candidates likewise require per-file license, creator, attribution, source-provenance, and third-party-rights adjudication.

## Variation observations

Source metadata suggests a useful mixture of caught/handled, boat/survey, aquarium/reference, underwater/natural, juvenile/larval, and adult examples. It also suggests different environments and source institutions. These are discovery leads only: pose, orientation, fish dominance, condition, image quality, and actual species identity cannot be accepted without visual review of the binary.

The retained pool is not source-balanced enough to become a dataset directly. Commons dominates this pass, several records trace back to NOAA/FDA/Flickr source families, and controlled/reference material may be overrepresented. Adjudication should prefer independent field/catch-photo groups and prevent any single source family from filling a pilot class.

## Duplicate and source-variant risks

No duplicate candidate IDs, source-item IDs within a source, or source-page URLs were added. Metadata nevertheless flags suspected correlated pairs/families for grouping review:

- *C. hippurus*: Commons 165855570 / 165855571 (Akumal source variants).
- *S. cavalla*: 7031830 / 16466351 and 8017189 / 17940353 (probable shared NOAA source assets).
- *S. dumerili*: 17940231 / 17940247 and 158222300 / 158222302 (source-family or capture-session risk).
- *S. barracuda*: 1758734 / 1758736 and 6185265 / 6185422 (source/aquarium-session risk).

Binary SHA-256 and perceptual-duplicate checks are intentionally deferred until a small adjudicated acquisition batch is authorized.

## Taxonomy and label concerns

All retained rows claim the exact frozen scientific name and resolve to the existing canonical ID. No common-name-only mapping or silent synonym mapping was accepted. Historical names encountered during screening—notably *Seriola dumerilii* and *Seriola purpurascens*—were not advanced as if they were automatically equivalent; any future synonym candidate requires explicit taxonomic review.

All five species remain label PENDING. *Scomberomorus cavalla* and *Seriola dumerili* retain two-reviewer HIGH-confusion requirements. *Sphyraena barracuda* retains the VERY_HIGH requirement for two reviews including a qualified expert. The *Alectis ciliaris* pool needs special attention because the small candidate count makes weak labels or correlated sources disproportionately harmful.

## Screened-out records

The initial Commons category queries returned 262 metadata records across the five species. Ninety-four were excluded by the discovery license allowlist. A further 110 allowlisted records were not advanced because they were redundant/noisy, illustrations or non-photo media, food/landscape records, forbidden iNaturalist-origin material, ambiguous items, likely variants, or beyond the conservative first-pass cap. These are discovery-screening exclusions, not final legal rejection decisions.

No candidate row was created from the current Guía Oficial images, contributors, social media, FishBase, search-engine result pages, NC/ND material, or an unclear research dataset.

## Recommended next action

Perform human item-level rights adjudication and visual label/quality review on a deliberately small, source-diverse subset. Prioritize additional Tier 1 discovery for *Alectis ciliaris*. Only after candidates satisfy the independent rights and label gates should a first small binary acquisition batch be authorized; acquisition must not proceed directly from this report.
