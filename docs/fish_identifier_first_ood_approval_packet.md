# Fish Identifier Pilot — First OOD Batch Approval Packet

**Prepared:** 2026-08-14
**Status:** APPROVED BY INDEPENDENT DATASET APPROVER
**Reviewer:** Edgar Rivera
**Review date:** 2026-08-14
**Scope:** All 14 listed candidates were independently approved for controlled acquisition.

## Proposed batch

All 14 candidates have blank `FichaPez.id` and scientific-name fields. Item-level rights review, visual/category confirmation, and independent Dataset Approver review passed.

| Candidate ID | OOD category | Canonical source item | License | Source/session hint | Rights | Category | Human decision |
|---|---|---|---|---|---|---|---|
| `ood-wc-35648764` | `unsupported_fish` | [Common lion fish](https://commons.wikimedia.org/wiki/?curid=35648764) | CC BY 3.0 | Michael Gäbler / aquarium | APPROVED | APPROVED | APPROVED |
| `ood-wc-5191439` | `unsupported_fish` | [Lion fish](https://commons.wikimedia.org/wiki/?curid=5191439) | Public domain | Bachrach44 / National Aquarium | APPROVED | APPROVED | APPROVED |
| `ood-wc-12104762` | `shark_ray` | [Nurse shark under reef](https://commons.wikimedia.org/wiki/?curid=12104762) | Public domain | NOAA reef2574 | APPROVED | APPROVED | APPROVED |
| `ood-wc-346756` | `shark_ray` | [Belize shark](https://commons.wikimedia.org/wiki/?curid=346756) | CC BY 2.0 | Josh / Flickr item | APPROVED | APPROVED | APPROVED |
| `ood-wc-29209984` | `crustacean` | [Big blue crab](https://commons.wikimedia.org/wiki/?curid=29209984) | CC BY 2.0 | Virginia State Parks | APPROVED | APPROVED | APPROVED |
| `ood-wc-26396862` | `crustacean` | [Bio Lab Road image 72](https://commons.wikimedia.org/wiki/?curid=26396862) | CC BY 2.0 | Rusty Clark / Bio Lab Road | APPROVED | APPROVED | APPROVED |
| `ood-wc-181803762` | `fishing_gear` | [Spinning reel on rod](https://commons.wikimedia.org/wiki/?curid=181803762) | CC0 1.0 | JaredMcKenzie gear session | APPROVED | APPROVED | APPROVED |
| `ood-wc-181803763` | `fishing_gear` | [3000-size spinning reel](https://commons.wikimedia.org/wiki/?curid=181803763) | CC0 1.0 | JaredMcKenzie gear session | APPROVED | APPROVED | APPROVED |
| `ood-wc-33200360` | `boat` | [Flakensee motorboat scene](https://commons.wikimedia.org/wiki/?curid=33200360) | CC BY 4.0 | Marcus Cyron / Flakensee | APPROVED | APPROVED | APPROVED |
| `ood-wc-37313734` | `boat` | [Two boats and the moon](https://commons.wikimedia.org/wiki/?curid=37313734) | CC BY 2.0 | Grand Parc / Basque coast | APPROVED | APPROVED | APPROVED |
| `ood-wc-92100670` | `beach_water` | [Santa Isabel, Puerto Rico](https://commons.wikimedia.org/wiki/?curid=92100670) | CC BY 2.0 | chispy2 / Santa Isabel | APPROVED | APPROVED | APPROVED |
| `ood-wc-119270848` | `beach_water` | [Yehliu Geopark beach](https://commons.wikimedia.org/wiki/?curid=119270848) | CC0 1.0 | Balon Greyjoy / Yehliu | APPROVED | APPROVED | APPROVED |
| `ood-wc-140503658` | `person` | [Human hand](https://commons.wikimedia.org/wiki/?curid=140503658) | CC0 1.0 | Editor3458654 / isolated hand | APPROVED | APPROVED | APPROVED |
| `ood-wc-61708668` | `person` | [Bedford, United Kingdom](https://commons.wikimedia.org/wiki/?curid=61708668) | CC0 1.0 | Thomas Curryer / archived Unsplash | APPROVED | APPROVED | APPROVED |

The controlled schema's `person` value includes person/hands examples. The two JaredMcKenzie records are a correlated source/session family and must remain grouped if both are acquired.

## Rights evidence

- Each linked Commons file page is the canonical evidence reference and returned HTTP 200 on 2026-08-14.
- CC BY permits commercial reuse and adaptation when creator attribution and the recorded license/version are retained. Exact attribution fields are in `pilot_candidate_review.csv`.
- CC0 permits commercial reuse and adaptation without required attribution; provenance is still retained.
- Public-domain candidates retain creator/source provenance even though license attribution is not required. The nurse-shark item identifies Florida Keys National Marine Sanctuary staff; the lionfish item identifies uploader own work.
- No NC, ND, all-rights-reserved, editorial-only, current Guía, contributor, social-media, iNaturalist, or FishBase-sourced item is included. Two initially considered clownfish records were excluded because their provenance pointed to FishBase.
- No item metadata showed an unresolved third-party-rights warning. Contradictory evidence on the live page requires PENDING or REJECTED status.

## Independent review

For every linked candidate, the Dataset Approver must verify:

1. the page/displayed media corresponds to the candidate;
2. the image clearly fits the proposed OOD category;
3. it does not depict one of the frozen 39 species;
4. the recorded license/status is visible without a contradictory warning;
5. no obvious privacy, watermark, third-party-rights, or quality issue makes it unsuitable.

For `unsupported_fish`, confirm the subject is lionfish/*Pterois*, which is outside the frozen catalog. Ordinary non-fish categories require no scientific identity. Ambiguous items remain pending.

Decision recorded for every listed candidate:

- **Category decision:** APPROVED
- **Dataset Approver Decision:** APPROVED
- **Reviewer:** Edgar Rivera
- **Review date:** 2026-08-14
- **Rejection reason / notes:** Not applicable

## After decisions are returned

A later task will record independent category and Dataset Approver fields, set accepted rows to `APPROVED_FOR_ACQUISITION`, and reject others with reasons. Only then may it acquire originals into the external workspace, compute SHA-256/pHash, compare against positive and OOD binaries, establish groups, propose partitions, validate, and add real OOD metadata rows.

The gate was recorded on 2026-08-14. Controlled acquisition and metadata ingestion subsequently proceeded for exactly these 14 items.
