# Fish Identifier Stage 1 Acquisition Plan

**Plan date:** 2026-08-14

**Roadmap scope:** FI-A.6 — planning and coverage enforcement only

**Dataset state:** No approved training images have been acquired. All tracker counts begin at zero.

## 1. Stage 1 objective

Stage 1 should produce the smallest legally cleared, correctly labeled, diverse dataset capable of supporting a serious first 39-class training and evaluation experiment. It is a learning milestone, not a production-readiness claim. It must represent PescaPR-style caught/handled phone photography while retaining enough clean and natural views to teach diagnostic anatomy.

The frozen membership and machine labels come only from `app/src/main/assets/fish_classifier_manifest.json`: 39 output classes mapped to exact `FichaPez.id` values. The current 124 Guía Oficial images remain visual references only and contribute zero to every acquisition count unless separately cleared and re-reviewed later.

## 2. Dataset stages

| Stage | Purpose | Per-class planning range | Positive total in this plan |
|---|---|---:|---:|
| Stage 1 — Initial training | First informative classifier and held-out evaluation | 225–450 accepted originals | **14,525** |
| Stage 2 — Production improvement | Close class/condition gaps revealed by Stage 1 | 500–900 | **29,050** |
| Stage 3 — Mature dataset | Long-term robustness where additional volume remains valuable | 1,000–1,500 | **48,800** |

Stage 2 and Stage 3 are maturity targets. They do not block initial experimentation. Counts mean independent, rights-cleared accepted originals; crops, mirrors, re-encodes, burst frames, and augmentations do not increase them.

## 3. Confusion-risk method and target distribution

Risk measures visual similarity to another frozen class, not rarity or source availability. HIGH and VERY_HIGH classes need more independent examples because the useful signal may be a fin position, head profile, stripe, spot pattern, body proportion, color phase, or life-stage trait rather than the broad “fish” silhouette.

| Risk | Classes | Stage 1/class | Stage 1 subtotal | Treatment |
|---|---:|---:|---:|---|
| LOW | 4 | 225 | 900 | Distinctive within this catalog; normal label review |
| MEDIUM | 4 | 275 | 1,100 | Some lookalikes or life-stage change; targeted hard negatives |
| HIGH | 19 | 375 | 7,125 | Stronger expert review and deliberate within-group examples |
| VERY_HIGH | 12 | 450 | 5,400 | Critical expert adjudication, paired hard examples, stricter completion gate |
| **Total** | **39** | — | **14,525** | — |

Sourcing difficulty is tracked separately as LOW, MEDIUM, or HIGH. `PROMISING`, `LIMITED`, `UNKNOWN`, and `EXPECTED_DIFFICULT` describe unverified acquisition outlook—not confirmed source counts.

## 4. Per-species targets and gap plan

Tier order is normally: (1) PescaPR-owned/explicit-permission contributions, (2) item-verified government/public-domain works, (3) Smithsonian assets explicitly marked CC0, (4) per-file-approved Wikimedia Commons, (5) GBIF media whose upstream rights and label pass review, then (6) other approved sources. Protected species prioritize lawful pre-existing government/archive imagery and must never drive new handling.

| `FichaPez.id` | Scientific name | Risk | Difficulty / outlook | S1 | S2 | S3 | Required Stage 1 variation | Desirable | Expected primary gap |
|---|---|---|---|---:|---:|---:|---|---|---|
| `0SCZ4miCcNiVY684bCwg` | *Acanthocybium solandri* | HIGH | MEDIUM / UNKNOWN | 375 | 750 | 1200 | both lateral; full catch; head/dorsal; sizes; daylight/shade; device diversity | oblique; boat/dock/cooler; low light; underwater; partial | field sizes and Scomberomorus hard examples |
| `0hjc5oWRBLg9dyujatSy` | *Lactophrys bicaudalis* | HIGH | HIGH / EXPECTED_DIFFICULT | 375 | 750 | 1200 | both lateral; caught and underwater; full pattern/tail; adult; device diversity | oblique; juvenile; detail; low light | cleared catch photos and juveniles |
| `1s69lkvuYEYoQkL6esVp` | *Cephalopholis fulva* | VERY_HIGH | MEDIUM / UNKNOWN | 450 | 900 | 1500 | both lateral; caught and underwater; spot/color phases; adult; device diversity | oblique; juvenile; detail; low light | independent color phases and grouper hard examples |
| `3qAJ1d8NdETc7HAsvJtg` | *Lutjanus buccanella* | VERY_HIGH | HIGH / EXPECTED_DIFFICULT | 450 | 900 | 1500 | both lateral; full catch; eye/fins/flank; sizes; daylight/shade | oblique; boat/cooler; low light; juvenile; detail | deepwater field volume and snapper distinctions |
| `5SkWhUQgG6JuanpfSLUT` | *Alectis ciliaris* | MEDIUM | MEDIUM / UNKNOWN | 275 | 500 | 1000 | both lateral; full catch; juvenile and adult; device diversity | oblique; underwater; dock; low light | balanced life stages |
| `AYGYpLjkS9LEv7AXVAuk` | *Coryphaena hippurus* | LOW | LOW / PROMISING | 225 | 500 | 1000 | both lateral; full catch; head/sex and fresh/catch color; device diversity | oblique; cooler; low light; juvenile; underwater | sex/head profiles and session concentration |
| `CIfxxdN70JcakCqA0IxY` | *Epinephelus adscensionis* | VERY_HIGH | MEDIUM / UNKNOWN | 450 | 900 | 1500 | both lateral; caught and underwater; flank/fins; sizes; device diversity | oblique; juvenile; detail; low light | Cephalopholis/Epinephelus hard examples |
| `Hjr9sFSdUEW1RVpR09mV` | *Lactophrys quadricornis* | HIGH | MEDIUM / UNKNOWN | 375 | 750 | 1200 | both lateral; caught and underwater; horns/pattern; juvenile/adult | oblique; detail; low light | juvenile form and horn visibility |
| `IBd1JsryE7jTh1tpyCN8` | *Lactophrys trigonus* | HIGH | HIGH / EXPECTED_DIFFICULT | 375 | 750 | 1200 | both lateral; caught and underwater; geometry/pattern; adult | oblique; juvenile; detail; low light | cleared field photos and juvenile identity |
| `J4JKaRrOWzuHfxq9ihKM` | *Lutjanus cyanopterus* | HIGH | MEDIUM / UNKNOWN | 375 | 750 | 1200 | both lateral; full catch; head/mouth/fins; juvenile/adult | oblique; cooler; low light; underwater; detail | juveniles and dog/mutton snapper hard examples |
| `MjsvzQbyRzyWqtjGYSBM` | *Megalops atlanticus* | LOW | LOW / PROMISING | 225 | 500 | 1000 | both lateral; full catch; head/mouth/scales; sizes; device diversity | oblique; shore/dock; low light; natural water | smaller fish and poor-light catches |
| `NZjA3AcJ6gb2ddsUNOPw` | *Lutjanus jocu* | HIGH | MEDIUM / UNKNOWN | 375 | 750 | 1200 | both lateral; full catch; head/mouth/fins; juvenile/adult | oblique; cooler; low light; underwater; detail | juvenile and cubera/mutton distinctions |
| `OF8bIJWNGjtMOJnLeVgf` | *Scomberomorus regalis* | HIGH | MEDIUM / UNKNOWN | 375 | 750 | 1200 | both lateral; full catch; marks/head/dorsal; sizes | oblique; cooler; low light; underwater; partial | S. cavalla/wahoo hard examples |
| `PM6TqnpdmFQMMDQFoQAy` | *Sphyraena guachancho* | VERY_HIGH | HIGH / EXPECTED_DIFFICULT | 450 | 900 | 1500 | both lateral; full catch; head/fins/proportions; sizes | oblique; underwater; low light; detail | independently verified labels and audit conflict |
| `PtbnNsBSRDwJzqGwvmv9` | *Centropomus undecimalis* | LOW | LOW / PROMISING | 225 | 500 | 1000 | both lateral; full catch; mouth/line/fins; sizes | oblique; shore/dock; low light; juvenile | smaller specimens and cluttered shore photos |
| `RO2iuTVLAX11dy3aNgdf` | *Haemulon plumieri* | MEDIUM | MEDIUM / UNKNOWN | 275 | 500 | 1000 | both lateral; caught and underwater; head/stripes/fins; juvenile/adult | oblique; dock; low light; detail | muted catch color and unsupported grunts |
| `SQ7eid3h0Fk2ToVnnqm6` | *Epinephelus mystacinus* | VERY_HIGH | HIGH / EXPECTED_DIFFICULT | 450 | 900 | 1500 | both lateral; full catch; bars/head/fins; sizes | oblique; underwater; low light; detail | deepwater field volume and life-stage labels |
| `TvWu2VyCwDofI4RfOmmU` | *Epinephelus itajara* | HIGH | HIGH / LIMITED | 375 | 750 | 1200 | lateral; lawful natural/legacy handled; juvenile/adult; head/flank/fins | oblique; low light; habitat/detail | lawful rights-cleared imagery without new handling |
| `V39KoLAZkg0MBjiWaa46` | *Scomberomorus cavalla* | HIGH | LOW / PROMISING | 375 | 750 | 1200 | both lateral; full catch; marks/head/dorsal; sizes | oblique; cooler; low light; underwater; partial | post-catch color and mackerel hard examples |
| `VL21Dl6MaY4SDJkmvIIz` | *Lutjanus synagris* | HIGH | LOW / PROMISING | 375 | 750 | 1200 | both lateral; full catch; stripes/spot/fins; juvenile/adult | oblique; cooler; low light; underwater; detail | faded markings and snapper hard examples |
| `WSIwTi77Bdy2KEUtE26k` | *Mycteroperca venenosa* | VERY_HIGH | HIGH / EXPECTED_DIFFICULT | 450 | 900 | 1500 | both lateral; caught and underwater; color phases; sizes | oblique; juvenile; low light; detail | independent phases and cleared catch photos |
| `WaSqNZuItzfXizCKyei7` | *Pristipomoides aquilonaris* | HIGH | HIGH / EXPECTED_DIFFICULT | 375 | 750 | 1200 | both lateral; full catch; eye/fins/flank; sizes | oblique; cooler; low light; detail | deepwater field volume and expert labels |
| `XTLHUX6xHya0BOisyR6E` | *Ocyurus chrysurus* | HIGH | LOW / PROMISING | 375 | 750 | 1200 | both lateral; caught and underwater; stripe/tail; juvenile/adult | oblique; dock; low light; detail | juveniles and faded catch markings |
| `Ya1VhjdpdBABqWClLAnW` | *Sphyraena picudilla* | VERY_HIGH | HIGH / EXPECTED_DIFFICULT | 450 | 900 | 1500 | both lateral; full catch; head/fins/proportions; sizes | oblique; underwater; low light; detail | independently verified labels and audit conflict |
| `bUROGweaABz6GRUedycl` | *Lactophrys polygonia* | HIGH | HIGH / EXPECTED_DIFFICULT | 375 | 750 | 1200 | both lateral; caught and underwater; pattern/geometry; adult | oblique; juvenile; detail; low light | cleared field photos and juvenile coverage |
| `cEgkcDR0JUI8GdxEx5LA` | *Caranx lugubris* | MEDIUM | MEDIUM / UNKNOWN | 275 | 500 | 1000 | both lateral; full catch; head/fins/depth; sizes | oblique; underwater; low light; juvenile | juvenile images and unsupported jacks |
| `eBZEv2F3RUvtST6fx0cK` | *Lutjanus analis* | HIGH | LOW / PROMISING | 375 | 750 | 1200 | both lateral; full catch; flank spot/head/fins; juvenile/adult | oblique; cooler; low light; underwater; detail | faded color and dog/cubera hard examples |
| `ejX0Cx9YYxsmOQTJb8kK` | *Sphyraena barracuda* | VERY_HIGH | LOW / PROMISING | 450 | 900 | 1500 | both lateral; caught and underwater; head/fins/proportions; juvenile/adult | oblique; low light; detail | juveniles and smaller Sphyraena hard examples |
| `fZog3T6cou99saWzsQyE` | *Etelis oculatus* | HIGH | HIGH / EXPECTED_DIFFICULT | 375 | 750 | 1200 | both lateral; full catch; eye/tail/fins; sizes | oblique; cooler; low light; detail | deepwater volume and red-snapper hard examples |
| `giJGGBQxEBmzjTMhZsPg` | *Lutjanus vivanus* | VERY_HIGH | HIGH / EXPECTED_DIFFICULT | 450 | 900 | 1500 | both lateral; full catch; eye/fins/flank; sizes | oblique; cooler; low light; juvenile; detail | deepwater volume and red-snapper distinctions |
| `iOXBIJjjwRw2FELFnSe1` | *Seriola rivoliana* | HIGH | MEDIUM / UNKNOWN | 375 | 750 | 1200 | both lateral; full catch; profile/depth/stripe/fins; sizes | oblique; underwater; low light; juvenile | S. dumerili hard examples |
| `oH2T6KbHxVuRYK0EgI5D` | *Rhomboplites aurorubens* | HIGH | MEDIUM / UNKNOWN | 375 | 750 | 1200 | both lateral; full catch; eye/fins/flank; sizes | oblique; cooler; low light; detail | catch color and deepwater snapper examples |
| `osXhShrxuuFLdr0ftgmb` | *Albula vulpes* | LOW | MEDIUM / UNKNOWN | 225 | 500 | 1000 | both lateral; full catch; mouth/scales/fins; sizes | oblique; shore; natural water; low light | small fish and unsupported silvery fish |
| `pFJ38O9TeYjWUt2n8XRS` | *Epinephelus guttatus* | VERY_HIGH | MEDIUM / UNKNOWN | 450 | 900 | 1500 | both lateral; caught and underwater; spots/bars/fins; juvenile/adult | oblique; low light; detail | phases and Cephalopholis/Epinephelus examples |
| `pS0UEezomaklOqZsflTt` | *Epinephelus striatus* | VERY_HIGH | HIGH / LIMITED | 450 | 900 | 1500 | lateral; lawful natural/legacy handled; bars/spots; juvenile/adult | oblique; low light; habitat/detail | lawful imagery without new handling and life stages |
| `ptq705ot5CnYod63Xs8E` | *Anisotremus surinamensis* | MEDIUM | HIGH / EXPECTED_DIFFICULT | 275 | 500 | 1000 | both lateral; caught and underwater; head/flank/fins; juvenile/adult | oblique; dock; low light; detail | field volume and unsupported grunt negatives |
| `qDlhElFdSz5UOHDkU8Pe` | *Lactophrys triqueter* | HIGH | MEDIUM / UNKNOWN | 375 | 750 | 1200 | both lateral; caught and underwater; pattern/geometry; juvenile/adult | oblique; detail; low light | juveniles and cleared catch photos |
| `u1JpMvcENOy98cd31Za5` | *Seriola dumerili* | HIGH | LOW / PROMISING | 375 | 750 | 1200 | both lateral; full catch; profile/depth/stripe/fins; sizes | oblique; underwater; low light; juvenile | S. rivoliana hard examples |
| `wk7kjNwc7FzD9WT3c3Ui` | *Cephalopholis cruentata* | VERY_HIGH | MEDIUM / UNKNOWN | 450 | 900 | 1500 | both lateral; caught and underwater; spots/phases/fins; juvenile/adult | oblique; low light; detail | phases and C. fulva/Epinephelus examples |

The CSV tracker is the machine-readable checklist. Its longer fields define REQUIRED, DESIRABLE, and not-important/not-applicable variation explicitly per species. Unless a species row says otherwise, a frontal/rear quota is not important: those poses may be retained when useful but must not displace diagnostic lateral and oblique views.

### Catalog metadata correction

ITIS identifies the valid tarpon name as *Megalops atlanticus* ([ITIS TSN 161116](https://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value=161116)). The bundled Guía, manifest audit metadata, tracker, and planning/audit documents use that spelling while retaining the existing `FichaPez.id` and class index. A read-only live Firestore check on 2026-08-14 returned `Megalops Atlanticus`; the genus correction is present there, but the species epithet still requires lowercase normalization to match the bundled record exactly.

## 5. Stage 1 coverage thresholds

Targets guide collection; the quality gates prevent a nominal count from hiding a weak class.

- **Independence:** count one accepted original per derivative group. Near-identical burst/video frames count conservatively as one source/session unit for progress even when multiple frames are retained for analysis.
- **Source/session diversity:** at least 20 independent source/session groups per class; no source domain over 30%; no contributor over 15%; no session/individual over 5%. Prefer lower concentration when volume permits.
- **Pose:** for anatomically suitable species, at least 15% left lateral, 15% right lateral, and 15% oblique; at least 65% must retain the full fish or nearly all diagnostic anatomy. Orientation is based on fish direction, not image metadata.
- **Usage domain:** at least 45% caught/held/boat/dock/cooler/shore field images for ordinary legally catchable species, including at least 25% explicitly caught/held. Controlled/specimen/reference imagery may not exceed 30% of a class. Protected species are exempt from caught/held quotas and emphasize lawful natural/legacy imagery.
- **Lighting/device:** at least 15% shade/cloudy and 10% artificial or realistic low light where lawful imagery exists; no single photographer/device pipeline over 15%. Do not retain unusable blur merely to satisfy low-light coverage.
- **Biology:** at least three useful size bands when appearance/availability supports them; juveniles are required when visibly different and must reach 10% where responsibly sourceable. Adult images are always required. Sex/color phases must each be independently represented for the rows that name them.
- **Hard examples:** HIGH classes need at least 10%, and VERY_HIGH classes 15%, of accepted images reviewed as within-group hard examples with the relevant diagnostic anatomy visible.
- **Rights/labels:** 100% accepted images have retained commercial-ML/derivative rights evidence. HIGH/VERY_HIGH positives have expert confirmation; LOW/MEDIUM have two-person review with escalation on disagreement.

Percentages are diagnostic thresholds, not permission to fill a class with repeated images. If a category is biologically irrelevant, unsafe, or unavailable, document the exception and substitute independent field diversity rather than fabricating a quota.

## 6. Source and sourcing-difficulty analysis

`PROMISING` means common catch/photo behavior makes first-party or verified government sourcing plausible, not confirmed. `LIMITED` is reserved for protected species where lawful media and ethical constraints narrow the plan. `EXPECTED_DIFFICULT` identifies rare/deepwater/less photographed classes or labels needing exceptional scrutiny. All other availability is `UNKNOWN` until metadata-only discovery and contributor outreach quantify it.

The hardest expected combination of field volume, rights, and label review is:

- *Sphyraena guachancho* and *S. picudilla*, because of fine-grained identification and the audit's cross-label conflicts.
- *Epinephelus mystacinus, Mycteroperca venenosa,* and *E. striatus*, because of group confusion, color/life-stage variation, or protected/deepwater constraints.
- *Lutjanus buccanella, L. vivanus, Etelis oculatus,* and *Pristipomoides aquilonaris*, because rights-cleared deepwater field-photo variety is expected to be limited.
- *Lactophrys bicaudalis, L. trigonus,* and *L. polygonia*, plus *Anisotremus surinamensis*, because diverse contributor-style volume is uncertain.

## 7. OOD acquisition plan

Do not add an unknown output class. Stage OOD acquisition alongside positives:

| OOD stage | Target | Purpose |
|---|---:|---|
| Stage 1 development | 2,500 | Threshold/margin calibration and optional 39-output-compatible outlier exposure |
| Stage 1 locked test | 1,500 | Unseen false-accept evaluation; never used to tune thresholds |
| Stage 1 total | **4,000** | Enough breadth for the first informative experiment |
| Later mature development | 5,000 | FI-A.5 maturity target |
| Later mature locked test | 3,000 | FI-A.5 maturity target |

Suggested Stage 1 balance, with each category internally diverse:

- 35% unsupported Caribbean fish and close relatives, including baitfish and hard family lookalikes.
- 10% sharks and rays.
- 10% crustaceans and squid/octopus.
- 15% people, hands, and mixed human/fish-free frames.
- 10% fishing gear, coolers, and containers without a target fish.
- 10% boats, beach, dock, and water scenes without a target fish.
- 5% blurry, occluded, empty, or badly exposed inputs that remain technically decodable.
- 5% other non-animal objects and unrelated photos.

OOD images require the same rights, duplicate, session, and partition controls as positives. Unsupported fish need scientific or family-level review sufficient to prove they are not one of the 39; non-fish does not require species taxonomy.

## 8. Future contributor-image strategy

Future anglers could provide the most relevant Puerto Rico catch-photo domain, but normal app upload terms must not be treated as ML consent.

Conceptual workflow:

1. Present a separate, voluntary contribution action independent of catch registration and normal app use.
2. Show concise purpose, retention, model-training/validation use, commercial app use, derivative/preprocessing permission, attribution choice, withdrawal policy, privacy, and contact terms before opt-in.
3. Require the contributor to declare authorship/authority and identify people whose likeness appears; collect releases or exclude images when necessary.
4. Accept the contributor's species label only as a suggestion. Keep the image quarantined from training while rights, quality, duplicate, and expert/admin label checks run.
5. Hash and group originals, derivative uploads, burst sequences, contributor sessions, and catch events before partitioning.
6. Approve into the dataset only after both `RIGHTS_CLEARED` and the required label-review status are recorded. Rejections remain excluded and access-controlled.
7. Keep contributor identity/consent records separately protected from the distributable image metadata.

Counsel must review language granting a sufficiently explicit, worldwide, durable right to store, reproduce, modify/preprocess, combine into datasets, train/evaluate commercial ML models, distribute model artifacts as intended, and retain compliance records. Counsel must also decide revocation effects after incorporation, attribution handling, minors/likeness/property releases, warranties, takedown procedure, privacy retention, and whether PescaPR receives ownership or a license. No collection UI should launch before that review.

## 9. Acquisition prioritization

1. Approve rights/consent templates and create the metadata/review system before collecting files.
2. Run metadata-only availability counts for every scientific name at Tier 1 and Tier 2 sources; retrieve no media during discovery.
3. Pilot the workflow with several PROMISING classes and at least one species from each HIGH/VERY_HIGH confusion group.
4. Recruit qualified reviewers and resolve barracuda diagnostic rules before admitting any *Sphyraena guachancho* or *S. picudilla* positive.
5. Acquire to close variation and source/session gaps, not merely the largest class deficit.
6. Escalate EXPECTED_DIFFICULT classes to permissioned photographers, fisheries partners, and rights-clear archives early.
7. Freeze derivative/session groups only after FI-A.7 applies partition assignments and confirms no leakage.

## 10. Stage 1 completion gate

The first model experiment may begin before every planned target is met only when all conditions below hold:

- At least **80% of the 14,525 positive target** is accepted (11,620 images), with at least 250 per VERY_HIGH class, 225 per HIGH class, 175 per MEDIUM class, and 150 per LOW class.
- No largest class exceeds 2.5 times the smallest class; training sampling/weights are documented but do not substitute for missing classes.
- Each HIGH/VERY_HIGH confusion group reaches at least 75% of its aggregate target, each HIGH/VERY_HIGH class has expert-confirmed labels, and barracuda audit conflicts are absent from accepted data.
- Every accepted positive and OOD image is rights-cleared; no current Guía image with unresolved provenance is counted.
- Exact/perceptual duplicate and derivative/session grouping has run, with no cross-partition leakage after FI-A.7.
- Each class passes source/session diversity and its required variation checklist, or has a documented exception approved by the dataset lead and species reviewer. Field imagery must materially represent intended PescaPR use.
- At least 3,000 of the Stage 1 OOD target is ready, including at least 1,000 locked-test OOD images and meaningful unsupported-fish coverage.
- Validation and locked-test partitions contain enough independent examples for per-class and OOD metrics; the locked test set remains untouched by threshold/model selection.

Starting below full target is explicitly an experiment authorization, not production authorization. Findings should produce a class/condition acquisition gap report before the next training iteration.

## 11. FI-A.7 operational meaning

FI-A.5 defined the partition strategy; FI-A.6 defined coverage and group metadata. FI-A.7 remains incomplete and operationally means: after candidate images exist, assign derivative, individual, session, contributor, and source groups; create stratified train/validation/test partitions by group; run exact/perceptual duplicate leakage checks; verify no related group crosses partitions; freeze a manually curated field-style/OOD test set; and publish partition counts/gaps. Planning a split is not the same as applying and verifying it on acquired data.

## 12. Exact next operational step

Before downloading any image, create and approve the rights/consent templates, reviewer SOP, controlled metadata vocabulary, and evidence-retention process; then conduct a **metadata-only source availability pass** for all 39 scientific names and update the tracker with candidate counts and gaps. Actual accepted counts remain zero until files pass rights, identity, quality, and duplicate review.
