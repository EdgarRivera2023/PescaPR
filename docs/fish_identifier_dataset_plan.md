# Fish Identifier Dataset Plan

**Plan date:** 2026-08-14

**Roadmap scope:** FI-A.5 — dataset requirements and external-source strategy only

**Status:** Planning contract; no images have been acquired and no model architecture has been selected

## 1. Scope and objectives

This plan defines a legally defensible, correctly labeled, and photographically diverse dataset for the first production-oriented PescaPR on-device fish classifier. It does not authorize scraping, downloading, training, or using the 124 current Guía Oficial images as model data.

The dataset is deliberately focused on realistic Puerto Rico fishing photographs and the 39 installed classifier classes. It is not intended to recognize every fish worldwide. A smaller set with strong rights, labels, and source grouping is preferable to a larger set with uncertain provenance or identity.

The prior reference-image audit remains controlling: all 124 current Guía Oficial images have unknown provenance/training rights and are excluded from train, validation, and test until individually cleared. They may be consulted only as visual reference material. The known cross-label duplicate conflicts between *Sphyraena guachancho* and *Sphyraena picudilla* must not enter a future dataset without independent species review.

## 2. Frozen 39-class catalog

The authoritative catalog is `app/src/main/assets/fish_classifier_manifest.json`, catalog version `1.0.0`. Its 39 entries and output order are immutable for the associated model version. Each positive image maps to exactly one existing `FichaPez.id`; common, English, scientific, and alias strings are descriptive metadata only.

Firestore additions must not change an installed classifier's classes. Adding an explicit unknown class or changing participation requires a new catalog/model version and a compatible manifest/model pair.

## 3. Dataset-size targets

Targets below count **accepted, rights-cleared, deduplicated source images**, not search results, augmentations, crops, video frames, or repeated photographs of the same individual/session.

| Risk band | Absolute minimum/class | Preferred/class | Stretch/class | Intended use |
|---|---:|---:|---:|---|
| Standard | 200 | 500 | 1,000 | Species with comparatively distinctive shape/color and reasonable availability |
| High | 350 | 750 | 1,200 | Similar-family classes or meaningful life-stage/color variation |
| Very high | 400 | 900 | 1,500 | Snappers, groupers, and barracudas where small visual differences drive the label |

Applied to this catalog, the acquisition matrix yields approximately **13,800 minimum**, **30,900 preferred**, and **51,900 stretch** accepted positives. Plan raw sourcing above these figures because rights review, label review, deduplication, low quality, and partition grouping will reject candidates. No class may fall below its minimum merely because another class has excess images.

These ranges are practical starting points for transfer learning: they are large enough to represent real catch-photo variation and hard negatives without pretending that augmentation creates new biological evidence. The preferred target reduces overfitting to photographers, boats, backgrounds, or a few individual fish. Final sufficiency is an evaluation result, not an image-count claim; acquisition continues for classes whose held-out field performance misses the accuracy gates.

## 4. Required photographic variation

Coverage is measured per class, not merely attempted globally. Each class should contain multiple independent sources/sessions for the applicable cells below:

- **Angle:** left and right lateral views are required; oblique views are strongly represented; frontal/rear views are included only where realistic and diagnostically useful.
- **Framing:** full-body images dominate, with deliberate partial-body and close-up examples that retain enough diagnostic anatomy. Crops derived from one original do not count as independent images.
- **Lighting:** daylight, shade, artificial boat/dock/cooler light, and realistic low light. Reject images whose color is unrecoverably clipped or whose identity cannot be verified.
- **Setting:** boat, dock, beach/shore, cooler, held fish/hands, natural backgrounds, and underwater imagery where the species is realistically photographed that way. Avoid allowing any one background to predict a label.
- **Biology:** juvenile/adult, size, natural color, post-capture color change, sex or color phase when meaningful. Do not create artificial quotas for traits that do not affect appearance or field use.
- **Device:** independent phones/cameras, resolutions, compression levels, portrait/landscape orientation, and normal consumer processing. Screenshots and re-encoded web copies are not independent originals.

Recommended coverage gates at preferred scale: at least 20 independent source/session groups per class; neither lateral side below 15% where anatomy permits; no single contributor/source domain above 20%; no single setting above 45%; and at least 10% challenging-but-labelable images (partial, oblique, cluttered, or low-light). These are acquisition controls, not reasons to retain unusable samples.

## 5. Confusion groups

| Group | Catalog members | Acquisition and review emphasis |
|---|---|---|
| Snappers/deepwater snapper-like fish | *Etelis oculatus*; *Lutjanus analis, buccanella, cyanopterus, jocu, synagris, vivanus*; *Ocyurus chrysurus*; *Pristipomoides aquilonaris*; *Rhomboplites aurorubens* | Very-high targets; adult/juvenile and post-capture color; fin, eye, stripe, tail, and body-depth visibility; expert review and paired hard examples |
| Groupers | *Cephalopholis cruentata, C. fulva*; *Epinephelus adscensionis, guttatus, itajara, mystacinus, striatus*; *Mycteroperca venenosa* | Very-high targets; color-phase and size variation; spot/bar patterns; avoid labels inferred from background/depth; expert review |
| Barracudas | *Sphyraena barracuda, S. guachancho, S. picudilla* | Very-high targets; diagnostic head, fins, lateral pattern, and body proportions; mandatory expert adjudication; explicitly quarantine the audit's guachancho/picudilla conflicts |
| Boxfish/trunkfish/cowfish | *Lactophrys bicaudalis, polygonia, quadricornis, trigonus, triqueter* | High targets; dorsal/ventral/lateral geometry, horn and pattern visibility, juvenile/adult variation |
| Mackerels/wahoo | *Acanthocybium solandri*; *Scomberomorus cavalla, S. regalis* | High targets; lateral bars/spots, head and first dorsal, different sizes and post-capture color |
| Amberjacks | *Seriola dumerili, S. rivoliana* | High targets; head profile, body depth, eye stripe and fin proportions; large/small individuals |
| Other jacks | *Alectis ciliaris, Caranx lugubris* | High targets; juveniles differ strongly from adults; collect both stages and hard negatives from unsupported Carangidae |
| Silvery coastal fish | *Albula vulpes, Centropomus undecimalis, Megalops atlanticus* and unsupported lookalikes | Deliberate hard negatives; maintain full head, mouth, lateral line, fin placement, and body profile |
| Grunts | *Anisotremus surinamensis, Haemulon plumieri* and unsupported Haemulidae | Deliberate hard negatives, life-stage coverage, and expert review when coloration is muted |

Every high/very-high group requires review by a fisheries biologist, taxonomist, or demonstrably qualified regional identifier. Acquisition should actively seek side-by-side failure cases rather than only clean “hero” photographs.

## 6. Unknown and unsupported inputs

Use a **hybrid rejection strategy** for the first implementation:

1. Keep the frozen output tensor at 39 classes; do not add `UNKNOWN_OTHER` to catalog v1.
2. Build a separately labeled out-of-distribution (OOD) corpus covering unsupported fish, baitfish, sharks/rays, crustaceans, squid/octopus, people, hands, gear, coolers, boats/beaches, empty scenes, blur, and non-animal objects.
3. Use held-out positives plus OOD validation/test inputs to calibrate a conservative top-1 score and top-1/top-2 margin policy. The UI must reject low-confidence cases and offer manual selection rather than force a species.
4. Evaluate a lightweight fish/OOD gate or training-time outlier exposure only if it preserves the manifest's 39 output mapping. Keep OOD provenance and partitions as rigorous as positive data.

Pure confidence rejection is often overconfident on unseen inputs, while one catch-all class has unbounded visual diversity and can become a background shortcut. The hybrid supplies explicit negative evidence without pretending “unknown” is one coherent species. If evaluation later proves that an explicit unknown output materially improves safety, create catalog/model v2; never append it to a published v1 mapping.

Suggested OOD scale is at least 5,000 accepted independent images for development and 3,000 for a locked test set, balanced across the listed categories and including unsupported Caribbean lookalikes. These are additional to the positive totals.

## 7. Labeling rules and review

- Establish identity from scientific species evidence, not common name, filename, search query, uploader tag, or model prediction.
- Map every positive sample to exactly one frozen `FichaPez.id`. Preserve the asserted scientific name and taxonomic source/version in metadata.
- Require source identification plus a second review for standard classes. High-risk classes require two independent reviewers, at least one qualified expert; disagreements go to expert adjudication.
- Exclude uncertain identity from positives. Track it in a quarantine ledger; never resolve uncertainty by choosing the nearest class.
- Exclude hybrids and ambiguous complexes from positives unless an expert can establish one frozen species. Track them separately as possible OOD evaluation material.
- Multi-fish images are positive only when one target individual is unambiguous and dominant and all visible fish identities are recorded. If multiple supported species are similarly prominent, exclude from single-label training and reserve for future multi-object work. Unsupported bystanders may be retained only after review confirms the target remains clear.
- Whole and field-identifiable fish are preferred. Severely cleaned, skinned, headed, filleted, cooked, mounted, or decomposed fish are excluded from positive training. Mildly cleaned/gutted catch images may enter a separately tagged challenge subset only if identity remains independently verifiable and matches expected app use.
- Exact duplicates, re-encodes, mirrored versions, crops, watermark variants, and edits of one original belong to one derivative group and one partition. Count the source original once for acquisition targets.
- Burst frames, video frames, the same fish, same catch event, contributor session, and likely reposts remain in one source/session group and one partition.
- Do not use the existing 124 guide images unless an item obtains documented rights clearance and renewed label/duplicate review. The barracuda conflicts require expert resolution even if rights are later cleared.

Review statuses: `PENDING`, `IDENTITY_REVIEWED`, `EXPERT_CONFIRMED`, `REJECTED_IDENTITY`, `RIGHTS_REVIEW`, `RIGHTS_CLEARED`, `QUARANTINED`, and `ACCEPTED`. `ACCEPTED` requires both label and rights gates.

## 8. Train/validation/test strategy

Use a starting allocation of **70% train / 15% validation / 15% test by source/session group**, stratified per class and photographic condition. At the minimum scale, each class still needs at least 40 validation and 40 test images; otherwise acquire more rather than moving related images across partitions.

Partition assignment happens after duplicate/derivative/session grouping and before augmentation:

- One original and every crop, resize, mirror, edit, or re-encode remain in one partition.
- One fish individual, burst/video sequence, catch event, photographer session, or museum specimen remains in one partition.
- When practical, reserve contributors, boats/locations, and source collections by partition to measure domain transfer rather than memorization.
- Perceptual hashes propose clusters; human review confirms them. URL or checksum differences do not establish independence.
- Synthetic augmentation is generated only inside training; no augmented derivative enters validation/test.

The final test set is locked, manually curated, and never used for threshold selection. It should resemble actual PescaPR submissions: phone photos, catch handling, clutter, partial framing, varied lighting, and a meaningful OOD component. Maintain a second diagnostic test slice of clean reference images so field failures can be separated from taxonomic failures. Report top-1, top-3, per-class recall/precision, confusion matrices, OOD false-accept rate, and results by condition.

## 9. License acceptance policy

License clearance is per image, retained as evidence, and reviewed at acquisition time because website terms can change. Accessibility, Firebase hosting, search visibility, or a species label is never rights evidence.

| Status | Commercial use | Derivatives/training | Obligations | Decision |
|---|---|---|---|---|
| Verified U.S. federal public-domain work | Yes, subject to non-copyright restrictions | Yes when the identified creator is the federal agency | Preserve source/creator evidence; credit as requested; no endorsement; review people/privacy/trademarks | Preferred |
| CC0 / valid public-domain dedication | Yes | Yes | Retain license evidence and provenance; attribution recommended even when not required | Preferred |
| CC BY 4.0 (or compatible version) | Yes | Yes | Creator attribution, license link/reference, change indication, no implied endorsement | Accepted with automated attribution ledger |
| PescaPR-owned original | Yes | Yes | Written chain of title; model/dataset use covered; likeness/property releases where applicable | Preferred |
| Explicit permission/license grant | Only if grant says so | Must explicitly cover ML training, modification/derivatives, storage, and commercial distribution | Retain signed, perpetual-worldwide rights record; honor attribution/withdrawal terms | Preferred when complete |
| CC BY-SA | Yes | Yes, but share-alike applies to adaptations | Attribution plus share-alike; model/weights implications require counsel and distribution design | Legal review; not a default source |
| CC BY-ND / any ND | Commercial copies may be allowed | Adaptations are prohibited; preprocessing/training rights are not sufficiently safe for this plan | — | Reject absent separate permission |
| Any NC license | No for a commercial app/dataset workflow | Commercial ML use not allowed | — | Reject |
| All-rights-reserved, editorial/research-only, “free to view,” no license, unclear uploader authority | Unclear or no | Unclear or no | Separate written permission required | Reject/quarantine |

Creative Commons' current license descriptions confirm that CC BY permits commercial adaptation, CC BY-SA adds share-alike, ND prohibits adaptation, and NC limits use to noncommercial purposes ([Creative Commons license guide](https://creativecommons.org/cc-licenses/)). This policy is an engineering acquisition gate, not legal advice; counsel should approve the release form, CC BY-SA handling, and any jurisdiction-specific public-domain claim before bulk collection.

## 10. Evaluated external sources

“Availability” here is a research expectation, not a verified per-species count. No media were downloaded or collected during FI-A.5. The next acquisition phase must run metadata-only discovery first, then record per-image rights before retrieving approved originals.

| Source | Coverage/volume potential | Species metadata | Rights/commercial assessment | Access | Field-photo suitability | Tier |
|---|---|---|---|---|---|---|
| PescaPR commissioned/consented contributor program | Can deliberately fill all 39 classes; volume grows with recruitment | Required scientific identity and `FichaPez.id` at submission | Strongest path if PescaPR owns the image or receives explicit commercial ML/derivative rights and necessary releases | First-party upload/collection workflow to design | Highest: can request Puerto Rico catch-photo conditions | **1 — Preferred** |
| NOAA/NOAA Fisheries original media | Likely useful for a subset of managed Atlantic/Caribbean and pelagic species; exact 39-class counts unverified | Mixed; item captions/collections, often scientific context | NOAA-created images are not U.S.-copyrighted; third-party credited images require holder permission; credit NOAA and avoid endorsement/likeness issues ([NOAA Fisheries policy](https://www.fisheries.noaa.gov/website-policies-and-disclaimers)) | Browsable [NOAA Photo Library](https://www.noaa.gov/noaa-collections/collections/photo-library); no single guaranteed species-image bulk feed established | Mixed reference, survey, underwater, and handled imagery | **1 only for item-verified NOAA-created works** |
| Smithsonian Open Access / NMNH | Marine collection coverage may include many taxa; usable image count and all 39 coverage unverified | Strong catalog/taxonomic metadata | Assets explicitly marked CC0 permit commercial use and transformation; non-CC0 assets do not ([Smithsonian Open Access FAQ](https://www.si.edu/openaccess/faq)) | Public API via `api.data.gov`; item-level CC0 flag | Often specimens/collection views; useful label anchors but weaker field domain | **1 for CC0-marked assets** |
| Wikimedia Commons | Likely broad but uneven representation; counts unverified | Scientific categories/descriptions, quality varies | Per-file licenses differ; CC0/public domain/CC BY may pass, CC BY-SA needs review; Commons cannot guarantee metadata correctness ([reuse guide](https://commons.wikimedia.org/wiki/Commons:Reusing_content_outside_Wikimedia), [license details](https://commons.wikimedia.org/wiki/Commons:Reusing_content_outside_Wikimedia/licenses/en)) | MediaWiki API and file metadata | Mixed; potentially valuable field/underwater images, but labels and repost provenance require review | **2 — filter and verify every file** |
| GBIF-linked occurrence media | Potentially high and scientifically searchable; per-species counts unverified | Machine-readable taxon/occurrence fields; identification quality varies by publisher | Aggregator license fields are useful but media can be more restrictive; confirm multimedia terms and original publisher ([GBIF image API guidance](https://techdocs.gbif.org/en/openapi/images)) | Occurrence API/download and media records | Mixed occurrence, specimen, and observation images; source-domain bias likely | **2 — discovery/filtering only until upstream cleared** |
| U.S. Fish & Wildlife Service and other U.S. government item-level media | Likely limited/uneven for marine catalog; counts unverified | Agency metadata varies | Accept only a record explicitly established as a federal work/public domain; contractor, partner, contest, or credited third-party work is not presumed public domain | Agency libraries/pages; acquisition mechanism varies | Mixed; may add habitat/field views | **2 — item-level verification required** |
| iNaturalist | Likely useful taxonomic/field volume, but not eligible for this commercial ML plan | Strong community observations; identifications can change | Current Terms prohibit use of iNaturalist data for commercial AI/ML training, regardless of ordinary photo-license filtering ([iNaturalist Terms §7](https://www.inaturalist.org/pages/terms)) | API/open-data mechanisms exist but do not override the prohibition | Excellent field-domain appearance, legally unavailable here | **3 — reference only; do not ingest for training** |
| FishBase photos | Likely visual references for most classes; volume often small | Strong species pages | Contributor-specific photo rights and site/use restrictions require individual permission; do not treat a FishBase page as a reusable image license ([FishBase citation/copyright guidance](https://www.fishbase.se/summary/citation.php)) | Website/species pages; no approved acquisition pipeline established | Mostly clean reference views, less representative of user photos | **3 — reference/legal review only** |
| Published research datasets | Some fish-classification corpora may offer volume, but overlap with these exact Caribbean species is unknown | Varies; often dataset-level labels | A paper, code license, or downloadable archive does not grant image rights. Accept only a dataset with explicit commercial reuse, derivatives/ML, provenance, and redistribution terms | Dataset-specific | Frequently underwater or lab-domain biased | **3 until dataset-specific rights and overlap are verified** |
| Other museum/university collections | May supply rare/deepwater specimens | Usually strong catalog metadata | Rights differ by institution and object; use only explicit CC0/CC BY or a written grant | Institution-specific APIs/portals | Strong diagnostic specimens, limited catch-photo realism | **3 unless promoted after item-level rights review** |
| Search engines, social media, forums, fishing-charter pages, retailer/editorial sites | High apparent volume | Weak/uncertain | Discoverability is not permission; uploader may not own the work | No approved ingestion | Often realistic but unacceptable without direct signed permission | **Rejected as datasets; may identify owners to contact** |

### Source tiers

- **Tier 1 — Preferred:** PescaPR-owned/permissioned submissions; item-verified NOAA-created public-domain photographs; Smithsonian assets explicitly marked CC0. Preserve evidence even when attribution is optional.
- **Tier 2 — Usable with filtering:** Wikimedia Commons files individually passing license/provenance/label gates; GBIF media after both record and upstream-publisher verification; explicitly public-domain federal items. These are discovery channels, not blanket licenses.
- **Tier 3 — Reference/legal review:** iNaturalist, FishBase, generic research datasets, and institution assets lacking a passing item license. iNaturalist is specifically prohibited for commercial AI training under its current terms, so it is excluded unless PescaPR later obtains independent rights outside iNaturalist and counsel confirms the path.

## 11. Species acquisition matrix

Source shorthand: **PPR** = PescaPR-owned/explicit-permission collection; **NOAA-PD** = item-verified NOAA-created work; **SI-CC0** = Smithsonian item explicitly marked CC0; **WC-filter** = Wikimedia Commons item passing per-file review; **GBIF-filter** = GBIF discovery followed by upstream rights/label review; **FED-PD** = other item-verified federal public-domain work. “Unverified” means FI-A.5 did not query/download candidate media and makes no availability claim.

| `FichaPez.id` | Scientific name | Min | Preferred | Confusion risk | Tier 1 candidates (availability unverified) | Tier 2 candidates (availability unverified) | Difficulty | Expert priority |
|---|---|---:|---:|---|---|---|---|---|
| `0SCZ4miCcNiVY684bCwg` | *Acanthocybium solandri* | 350 | 750 | High—mackerels | PPR; NOAA-PD | WC-filter; GBIF-filter | Medium | High |
| `0hjc5oWRBLg9dyujatSy` | *Lactophrys bicaudalis* | 350 | 750 | High—boxfish | PPR; SI-CC0 | WC-filter; GBIF-filter | High | High |
| `1s69lkvuYEYoQkL6esVp` | *Cephalopholis fulva* | 400 | 900 | Very high—groupers | PPR; SI-CC0 | WC-filter; GBIF-filter | Medium | Critical |
| `3qAJ1d8NdETc7HAsvJtg` | *Lutjanus buccanella* | 400 | 900 | Very high—snappers | PPR; NOAA-PD | WC-filter; GBIF-filter | High | Critical |
| `5SkWhUQgG6JuanpfSLUT` | *Alectis ciliaris* | 350 | 750 | High—jacks/life stage | PPR; SI-CC0 | WC-filter; GBIF-filter | Medium | High |
| `AYGYpLjkS9LEv7AXVAuk` | *Coryphaena hippurus* | 200 | 500 | Standard—sex/color change | PPR; NOAA-PD | WC-filter; GBIF-filter | Low | Medium |
| `CIfxxdN70JcakCqA0IxY` | *Epinephelus adscensionis* | 400 | 900 | Very high—groupers | PPR; SI-CC0 | WC-filter; GBIF-filter | Medium | Critical |
| `Hjr9sFSdUEW1RVpR09mV` | *Lactophrys quadricornis* | 350 | 750 | High—boxfish | PPR; SI-CC0 | WC-filter; GBIF-filter | Medium | High |
| `IBd1JsryE7jTh1tpyCN8` | *Lactophrys trigonus* | 350 | 750 | High—boxfish | PPR; SI-CC0 | WC-filter; GBIF-filter | High | High |
| `J4JKaRrOWzuHfxq9ihKM` | *Lutjanus cyanopterus* | 400 | 900 | Very high—snappers | PPR; NOAA-PD | WC-filter; GBIF-filter | Medium | Critical |
| `MjsvzQbyRzyWqtjGYSBM` | *Megalops atlanticus* | 200 | 500 | Standard—silvery coastal | PPR; NOAA-PD | WC-filter; GBIF-filter | Low | Medium |
| `NZjA3AcJ6gb2ddsUNOPw` | *Lutjanus jocu* | 400 | 900 | Very high—snappers | PPR; NOAA-PD | WC-filter; GBIF-filter | Medium | Critical |
| `OF8bIJWNGjtMOJnLeVgf` | *Scomberomorus regalis* | 350 | 750 | High—mackerels | PPR; NOAA-PD | WC-filter; GBIF-filter | Medium | High |
| `PM6TqnpdmFQMMDQFoQAy` | *Sphyraena guachancho* | 400 | 900 | Very high—barracudas; audit conflict | PPR; SI-CC0 | WC-filter; GBIF-filter | High | Critical |
| `PtbnNsBSRDwJzqGwvmv9` | *Centropomus undecimalis* | 200 | 500 | Standard—silvery coastal | PPR; NOAA-PD | WC-filter; GBIF-filter | Low | Medium |
| `RO2iuTVLAX11dy3aNgdf` | *Haemulon plumieri* | 200 | 500 | Standard—grunts | PPR; SI-CC0 | WC-filter; GBIF-filter | Medium | High |
| `SQ7eid3h0Fk2ToVnnqm6` | *Epinephelus mystacinus* | 400 | 900 | Very high—groupers/deepwater | PPR; NOAA-PD; SI-CC0 | WC-filter; GBIF-filter | High | Critical |
| `TvWu2VyCwDofI4RfOmmU` | *Epinephelus itajara* | 400 | 900 | Very high—groupers/juveniles | PPR; NOAA-PD | WC-filter; GBIF-filter | Medium; protected-species constraints | Critical |
| `V39KoLAZkg0MBjiWaa46` | *Scomberomorus cavalla* | 350 | 750 | High—mackerels | PPR; NOAA-PD | WC-filter; GBIF-filter | Low | High |
| `VL21Dl6MaY4SDJkmvIIz` | *Lutjanus synagris* | 400 | 900 | Very high—snappers | PPR; NOAA-PD | WC-filter; GBIF-filter | Low | Critical |
| `WSIwTi77Bdy2KEUtE26k` | *Mycteroperca venenosa* | 400 | 900 | Very high—groupers/color phase | PPR; NOAA-PD; SI-CC0 | WC-filter; GBIF-filter | High | Critical |
| `WaSqNZuItzfXizCKyei7` | *Pristipomoides aquilonaris* | 400 | 900 | Very high—deepwater snappers | PPR; NOAA-PD; SI-CC0 | WC-filter; GBIF-filter | High | Critical |
| `XTLHUX6xHya0BOisyR6E` | *Ocyurus chrysurus* | 400 | 900 | Very high—snappers | PPR; NOAA-PD | WC-filter; GBIF-filter | Low | Critical |
| `Ya1VhjdpdBABqWClLAnW` | *Sphyraena picudilla* | 400 | 900 | Very high—barracudas; audit conflict | PPR; SI-CC0 | WC-filter; GBIF-filter | High | Critical |
| `bUROGweaABz6GRUedycl` | *Lactophrys polygonia* | 350 | 750 | High—boxfish | PPR; SI-CC0 | WC-filter; GBIF-filter | High | High |
| `cEgkcDR0JUI8GdxEx5LA` | *Caranx lugubris* | 350 | 750 | High—jacks | PPR; NOAA-PD | WC-filter; GBIF-filter | Medium | High |
| `eBZEv2F3RUvtST6fx0cK` | *Lutjanus analis* | 400 | 900 | Very high—snappers | PPR; NOAA-PD | WC-filter; GBIF-filter | Low | Critical |
| `ejX0Cx9YYxsmOQTJb8kK` | *Sphyraena barracuda* | 400 | 900 | Very high—barracudas | PPR; NOAA-PD | WC-filter; GBIF-filter | Low | Critical |
| `fZog3T6cou99saWzsQyE` | *Etelis oculatus* | 400 | 900 | Very high—deepwater snappers | PPR; NOAA-PD; SI-CC0 | WC-filter; GBIF-filter | High | Critical |
| `giJGGBQxEBmzjTMhZsPg` | *Lutjanus vivanus* | 400 | 900 | Very high—snappers | PPR; NOAA-PD | WC-filter; GBIF-filter | High | Critical |
| `iOXBIJjjwRw2FELFnSe1` | *Seriola rivoliana* | 350 | 750 | High—amberjacks | PPR; NOAA-PD | WC-filter; GBIF-filter | Medium | High |
| `oH2T6KbHxVuRYK0EgI5D` | *Rhomboplites aurorubens* | 400 | 900 | Very high—snappers | PPR; NOAA-PD | WC-filter; GBIF-filter | Medium | Critical |
| `osXhShrxuuFLdr0ftgmb` | *Albula vulpes* | 200 | 500 | Standard—silvery coastal | PPR; NOAA-PD | WC-filter; GBIF-filter | Medium | Medium |
| `pFJ38O9TeYjWUt2n8XRS` | *Epinephelus guttatus* | 400 | 900 | Very high—groupers | PPR; NOAA-PD | WC-filter; GBIF-filter | Medium | Critical |
| `pS0UEezomaklOqZsflTt` | *Epinephelus striatus* | 400 | 900 | Very high—groupers | PPR; NOAA-PD | WC-filter; GBIF-filter | Medium; protected-species constraints | Critical |
| `ptq705ot5CnYod63Xs8E` | *Anisotremus surinamensis* | 200 | 500 | Standard—grunts | PPR; SI-CC0 | WC-filter; GBIF-filter | High | High |
| `qDlhElFdSz5UOHDkU8Pe` | *Lactophrys triqueter* | 350 | 750 | High—boxfish | PPR; SI-CC0 | WC-filter; GBIF-filter | Medium | High |
| `u1JpMvcENOy98cd31Za5` | *Seriola dumerili* | 350 | 750 | High—amberjacks | PPR; NOAA-PD | WC-filter; GBIF-filter | Low | High |
| `wk7kjNwc7FzD9WT3c3Ui` | *Cephalopholis cruentata* | 400 | 900 | Very high—groupers | PPR; NOAA-PD; SI-CC0 | WC-filter; GBIF-filter | Medium | Critical |

The likely hardest classes are the deepwater/less commonly photographed species (*Epinephelus mystacinus, Etelis oculatus, Lutjanus buccanella, L. vivanus, Pristipomoides aquilonaris*), less prominent boxfish species (*Lactophrys bicaudalis, L. polygonia, L. trigonus*), *Anisotremus surinamensis*, and the two smaller barracudas. “Hard” reflects expected rights-cleared field-photo diversity and expert-label effort, not a verified shortage; metadata discovery must test these assumptions.

## 12. Image metadata schema

Store one immutable record per acquired original in a version-controlled manifest/database export (format to be chosen before acquisition). Required fields:

| Field | Requirement |
|---|---|
| `internalImageId` | Stable PescaPR dataset ID; never derived solely from filename |
| `fichaPezId` | Frozen positive class ID, or `null` for OOD |
| `scientificName` | Reviewed taxon name; non-authoritative for runtime mapping |
| `sampleRole` | `POSITIVE`, `OOD`, `QUARANTINE`, or `EXCLUDED` |
| `sourceName`, `sourceUrl`, `sourceImageId` | Origin system and durable original identifier/URL |
| `sourceTermsSnapshot` | URL plus retrieval date and retained evidence/hash of applicable terms |
| `licenseId`, `licenseUrl`, `rightsStatus` | Normalized license and clearance result |
| `author`, `rightsHolder`, `attributionText` | Creator/holder and exact compliance text, even if attribution is optional where known |
| `permissionRecordId` | Signed release/grant reference when applicable |
| `acquiredAt` | UTC acquisition timestamp |
| `originalFilename`, `mimeType`, `width`, `height`, `byteSize` | Original technical facts |
| `sha256`, `perceptualHash` | Integrity and duplicate discovery |
| `derivativeOf`, `derivativeGroupId` | Original relationship for crops/edits/re-encodes |
| `sourceGroupId`, `sessionGroupId`, `individualGroupId`, `contributorId` | Leakage-control groups; pseudonymize contributors as needed |
| `partition` | `TRAIN`, `VALIDATION`, `TEST`, `UNASSIGNED`, or `EXCLUDED`; assigned only after grouping |
| `labelEvidence`, `labelReviewerIds`, `labelReviewStatus`, `reviewedAt` | Identity evidence and review trail |
| `rightsReviewerId`, `rightsReviewedAt` | Rights decision trail |
| `variationTags` | Controlled tags for angle, framing, lighting, setting, life stage, handling, image quality/device traits |
| `qualityStatus`, `exclusionReasons`, `notes` | Acceptance decision and auditable rationale |

The system must retain original metadata and license evidence even if a source later disappears. Checksums identify bytes; derivative/source/session IDs prevent leakage. Personally identifying contributor data belongs in a separately protected consent system, not the distributable dataset manifest.

## 13. Dataset quality gates

Before an image becomes `ACCEPTED`, all gates must pass:

1. **Catalog:** positive label is one of the 39 exact manifest IDs; OOD has no positive ID.
2. **Identity:** required reviewer tier confirms scientific species; ambiguity is quarantined.
3. **Rights:** commercial ML training, preprocessing/derivatives, storage, and intended distribution are allowed; evidence is retained; attribution obligations are actionable.
4. **Technical:** decodable original, useful resolution/detail, target visible, no unrecoverable corruption or misleading composite.
5. **Deduplication:** cryptographic and perceptual checks completed; derivatives/session grouping recorded.
6. **Partition:** group-safe assignment; no related original, individual, sequence, contributor-session, or derivative crosses partitions.
7. **Balance/variation:** per-class minimum and coverage gates met without one source/background becoming a shortcut.
8. **Evaluation:** locked field-style and OOD test sets meet predeclared class and rejection criteria; failures trigger targeted acquisition, not test-set tuning.

## 14. Recommended acquisition workflow

1. Approve the contributor release/permission language and license allowlist with counsel.
2. Perform metadata-only availability queries for all 39 scientific names across Tier 1, then Tier 2. Record candidate counts by exact license, source collection, and likely field/specimen domain; download nothing in that pass.
3. Establish taxonomic reviewer assignments, especially for confusion groups and protected/rare species.
4. Create the metadata registry, controlled vocabulary, evidence retention, duplicate grouping, and audit log before accepting a file.
5. Pilot a small rights-cleared batch across several easy and hard classes. Test the full rights/label/deduplication workflow—not model performance—then correct process gaps.
6. Acquire by deficit: prioritize high-confusion classes, real Puerto Rico catch conditions, and missing variation cells rather than raw volume.
7. Freeze groups and partitions, then conduct an independent rights and label audit before training is authorized.

## 15. Risks and unknowns

- Exact per-species image counts at NOAA, Smithsonian, Wikimedia, and GBIF remain unknown because FI-A.5 intentionally performed policy/source research without collecting media.
- Public-domain claims can fail when an agency page hosts partner/contractor media; creator evidence remains mandatory.
- Open licenses do not guarantee correct labels, original uploader authority, privacy/publicity clearance, or absence of duplicate reposts.
- CC BY-SA's application to processed datasets and trained weights requires legal/product review; exclude it from the default automated pipeline.
- Rare/deepwater species may require commissioned photographers, fisheries partners, museums, or explicit contributor grants to reach field-photo diversity.
- Protected species must be sourced ethically from existing lawful media; the plan must never incentivize handling, targeting, or prohibited approach.
- Web-source terms and APIs can change. Snapshot the policy and per-item evidence at acquisition and re-audit before publication.
- Dataset scale alone cannot establish readiness. Predeclared per-class, field-domain, and OOD evaluation gates are required in later phases.

## 16. Exact recommended next step

**FI-A.6 — Enforce and measure the documented field/catch-photo variation requirements during acquisition planning; produce a per-species coverage checklist and gap report before accepting the dataset for training.**

FI-A.5 has specified the variation contract, so FI-A.6 should remain incomplete as the execution/evidence gate rather than repeat the definition. It should operationalize the tags and thresholds in this document. Dataset acquisition still must not begin until rights workflow, metadata registry, reviewer process, and the FI-A.6 coverage checklist are ready.
