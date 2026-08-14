# Guía Oficial Reference Image Audit

**Audit date:** 2026-08-14
**Roadmap scope:** FI-A.4 only
**Catalog:** Frozen classifier catalog v1, 39 bundled Guía Oficial species, 124 Firebase image references

## Scope and decision rule

This audit evaluates visual-reference usefulness and legal reuse separately. Public accessibility, Firebase hosting, inclusion in PescaPR, and possession of a download token are not evidence of copyright ownership, permission, an open license, or ML training rights. No external dataset was acquired and no project image was changed.

Categories: `VERIFIED_PUBLIC_DOMAIN`, `VERIFIED_OPEN_LICENSE`, `VERIFIED_OWNED_BY_PESCAPR`, `VERIFIED_PERMISSION_GRANTED`, `UNKNOWN`, and `RESTRICTED_NOT_SUITABLE_FOR_TRAINING`.

## Method

- Parsed `oficial_guide.json` and derived Firebase object paths.
- Searched source, docs, comments, import/migration files, and relevant Git history for provenance and rights evidence.
- Temporarily fetched every existing reference outside the repository; tokens are not reproduced.
- Checked reachability, decoding, format, size, dimensions, aspect ratio, SHA-256, and 64-bit difference hashes.
- Reviewed contact sheets for near-duplicates, illustrations, watermarks, framing, context, and obvious label concerns.
- Visual taxonomy findings are screening observations, not expert certification.

## Overall findings

| Finding | Result |
|---|---:|
| Species audited | 39 |
| Image references audited | 124 |
| Reachable / broken | 124 / 0 |
| Decoded format | 124 JPEG |
| Resolution range | 247×135 to 2896×2048 |
| Below 400 px width or 250 px height | 14 |
| Below 640 px width or 400 px height | 58 |
| At least 1000×750 | 10 |
| Exact duplicate groups | 2 |
| Verified usable licensing/training rights | 0 |
| Unknown provenance/training rights | 124 |
| Currently approved for training or validation | 0 |

## Provenance and licensing

No local record ties any object to an original source, creator, acquisition date, license, permission, release, or PescaPR ownership. Git history introduces exported Firebase URLs but no provenance ledger. The admin flow JPEG-compresses images into `fichas/{UUID}.jpg` and records no rights metadata.

All 124 are `UNKNOWN` and, under current evidence, `RESTRICTED_NOT_SUITABLE_FOR_TRAINING`. This conservative decision is not a claim that every original is impossible to clear. At least 22 contain a visible credit, logo, website, artist/agency mark, or watermark. #57 visibly carries Alamy watermarks and must not be used without a documented license expressly covering the intended use.

The files can be visual catalog references under the application's existing risk posture, but that does not establish permission for dataset redistribution, model training, validation, publication, or derived-model distribution.

## Technical quality

The collection mixes underwater and handled photographs, isolated specimens, cutouts, drawings, and annotated graphics. Variation is uncontrolled: 1–5 images per class, 58 below 640 px width or 400 px height, uneven backgrounds/life stages, and illustrations unlike user field photos. Every species is `INSUFFICIENT_VARIATION`. Promising photographs are only `POTENTIAL_SEED_DATA` after rights and labels are cleared; none is currently approved.

## Duplicate and leakage findings

- #119 (`Sphyraena guachancho`) and #123 (`Sphyraena picudilla`) are byte-identical under different object names.
- #120 (`Sphyraena guachancho`) and #124 (`Sphyraena picudilla`) are byte-identical under different object names.
- #87 and #89 (`Mycteroperca venenosa`) are a manually identified same-fish cutout with changed background/canvas treatment.
- No other pair met the conservative difference-hash distance threshold of 5 bits.

Source-family members must stay in one future dataset partition. The barracuda pairs must not enter supervised data until an expert resolves the label conflict.

## Known labeling concerns

The cross-species `Sphyraena guachancho` / `Sphyraena picudilla` pairs are concrete conflicts. Both require expert review. Closely related snapper, grouper, amberjack, mackerel, barracuda, and boxfish classes also need expert confirmation; no other image is declared mislabeled solely from appearance.

## Per-species readiness summary

Every row includes `RIGHTS_UNKNOWN`, `NOT_USABLE_FOR_TRAINING`, and `INSUFFICIENT_VARIATION`.

| FichaPez.id | Scientific name | Common name | Images | Additional technical flags |
|---|---|---|---:|---|
| `0SCZ4miCcNiVY684bCwg` | Acanthocybium solandri | Peto | 4 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, POOR_QUALITY_PRESENT |
| `osXhShrxuuFLdr0ftgmb` | Albula vulpes | macaco, conejo, piojo, ratón, macabí | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE |
| `5SkWhUQgG6JuanpfSLUT` | Alectis ciliaris | corcobado, pámpano, coronado, sol | 4 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE |
| `ptq705ot5CnYod63Xs8E` | Anisotremus surinamensis | Chopa negra, Cachicata negra | 5 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE |
| `cEgkcDR0JUI8GdxEx5LA` | Caranx lugubris | jurel negro | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE |
| `PtbnNsBSRDwJzqGwvmv9` | Centropomus undecimalis | róbalo | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE |
| `wk7kjNwc7FzD9WT3c3Ui` | Cephalopholis cruentata | Cabrilla, cherna enjambre | 5 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, POOR_QUALITY_PRESENT, LABEL_REVIEW_NEEDED |
| `1s69lkvuYEYoQkL6esVp` | Cephalopholis fulva | Mero cabrilla, mero mantequilla, cherna cabrilla, guajiro, gativirí, negrita, fino | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, POOR_QUALITY_PRESENT, LABEL_REVIEW_NEEDED |
| `AYGYpLjkS9LEv7AXVAuk` | Coryphaena hippurus | Dorado | 5 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, POOR_QUALITY_PRESENT |
| `CIfxxdN70JcakCqA0IxY` | Epinephelus adscensionis | Cabra mora, cabrilla | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, LABEL_REVIEW_NEEDED |
| `pFJ38O9TeYjWUt2n8XRS` | Epinephelus guttatus | Mero cabrilla, mero cherna, mero colorado | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, LABEL_REVIEW_NEEDED |
| `TvWu2VyCwDofI4RfOmmU` | Epinephelus itajara | Mero batata, mero grande, mero sapo, judío | 2 | GOOD_REFERENCE_ONLY, LABEL_REVIEW_NEEDED |
| `SQ7eid3h0Fk2ToVnnqm6` | Epinephelus mystacinus | Guasa, mero listado, cherna | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, LABEL_REVIEW_NEEDED |
| `pS0UEezomaklOqZsflTt` | Epinephelus striatus | Mero cherna | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, LABEL_REVIEW_NEEDED |
| `fZog3T6cou99saWzsQyE` | Etelis oculatus | Cartucho, cachucho | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE |
| `RO2iuTVLAX11dy3aNgdf` | Haemulon plumieri | boquicolorao, cachicata, blanca, ronco, cicí, ronco, blanco, ronco grande | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE |
| `0hjc5oWRBLg9dyujatSy` | Lactophrys bicaudalis | Chapín moteado | 1 | GOOD_REFERENCE_ONLY, LABEL_REVIEW_NEEDED |
| `bUROGweaABz6GRUedycl` | Lactophrys polygonia | Chapín panal | 2 | GOOD_REFERENCE_ONLY, LABEL_REVIEW_NEEDED |
| `Hjr9sFSdUEW1RVpR09mV` | Lactophrys quadricornis | Chapín veteado, Chapín toro | 2 | GOOD_REFERENCE_ONLY, POOR_QUALITY_PRESENT, LABEL_REVIEW_NEEDED |
| `IBd1JsryE7jTh1tpyCN8` | Lactophrys trigonus | Chapín jorobado, gallina | 1 | GOOD_REFERENCE_ONLY, LABEL_REVIEW_NEEDED |
| `qDlhElFdSz5UOHDkU8Pe` | Lactophrys triqueter | Chapín liso | 1 | GOOD_REFERENCE_ONLY, LABEL_REVIEW_NEEDED |
| `eBZEv2F3RUvtST6fx0cK` | Lutjanus analis | Sama, pargo criollo | 4 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, LABEL_REVIEW_NEEDED |
| `3qAJ1d8NdETc7HAsvJtg` | Lutjanus buccanella | Alinegra, negrita | 2 | GOOD_REFERENCE_ONLY, POOR_QUALITY_PRESENT, LABEL_REVIEW_NEEDED |
| `J4JKaRrOWzuHfxq9ihKM` | Lutjanus cyanopterus | Pargo cubera | 4 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, POOR_QUALITY_PRESENT, LABEL_REVIEW_NEEDED |
| `NZjA3AcJ6gb2ddsUNOPw` | Lutjanus jocu | pargo perro, pargo sama, pargo dientón, pargo colorado, jocú | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, LABEL_REVIEW_NEEDED |
| `VL21Dl6MaY4SDJkmvIIz` | Lutjanus synagris | Arrayado, rayado, manchego | 4 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, LABEL_REVIEW_NEEDED |
| `giJGGBQxEBmzjTMhZsPg` | Lutjanus vivanus | Chillo ojo amarillo, pargo colorado | 2 | GOOD_REFERENCE_ONLY, LABEL_REVIEW_NEEDED |
| `MjsvzQbyRzyWqtjGYSBM` | Megalops atlanticus | sábalo, tarpón | 5 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE |
| `WSIwTi77Bdy2KEUtE26k` | Mycteroperca venenosa | Guajil, guajil colirrubio, mero pinto | 4 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, LABEL_REVIEW_NEEDED |
| `XTLHUX6xHya0BOisyR6E` | Ocyurus chrysurus | Colirrubia, rabirrubia | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, LABEL_REVIEW_NEEDED |
| `WaSqNZuItzfXizCKyei7` | Pristipomoides aquilonaris | Muniama | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, LABEL_REVIEW_NEEDED |
| `oH2T6KbHxVuRYK0EgI5D` | Rhomboplites aurorubens | Rubia, besugo, buchona, chilla rubia, pargo cunaro, tunaro, sardo | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, POOR_QUALITY_PRESENT, LABEL_REVIEW_NEEDED |
| `V39KoLAZkg0MBjiWaa46` | Scomberomorus cavalla | Carite, sierra, cavalla | 5 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, POOR_QUALITY_PRESENT, LABEL_REVIEW_NEEDED |
| `OF8bIJWNGjtMOJnLeVgf` | Scomberomorus regalis | Sierra alasana, pintado, pelicán | 4 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, POOR_QUALITY_PRESENT, LABEL_REVIEW_NEEDED |
| `u1JpMvcENOy98cd31Za5` | Seriola dumerili | Medregal | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, LABEL_REVIEW_NEEDED |
| `iOXBIJjjwRw2FELFnSe1` | Seriola rivoliana | medregal, escolar | 3 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, LABEL_REVIEW_NEEDED |
| `ejX0Cx9YYxsmOQTJb8kK` | Sphyraena barracuda | picúa, barracuda | 4 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, POOR_QUALITY_PRESENT, LABEL_REVIEW_NEEDED |
| `PM6TqnpdmFQMMDQFoQAy` | Sphyraena guachancho | guaguanche, picuílla, picúa parda | 2 | GOOD_REFERENCE_ONLY, POOR_QUALITY_PRESENT, LABEL_REVIEW_NEEDED, CROSS_SPECIES_DUPLICATE_CONFLICT |
| `Ya1VhjdpdBABqWClLAnW` | Sphyraena picudilla | picudilla, picúa lanceta | 4 | POTENTIAL_SEED_DATA_AFTER_CLEARANCE, POOR_QUALITY_PRESENT, LABEL_REVIEW_NEEDED, CROSS_SPECIES_DUPLICATE_CONFLICT |

## Complete image inventory

Firebase access tokens are redacted. Metadata does not establish rights.

### Acanthocybium solandri — Peto

- FichaPez.id: `0SCZ4miCcNiVY684bCwg`
- Image count: 4
- #1 (image 1): `51538549-846d-4ca5-a299-8a66341ba0db.jpg`; object `fichas/51538549-846d-4ca5-a299-8a66341ba0db.jpg`; 640×427, 16862 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F51538549-846d-4ca5-a299-8a66341ba0db.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #2 (image 2): `8c2d4def-d5d8-4754-be58-0b821ab97379.jpg`; object `fichas/8c2d4def-d5d8-4754-be58-0b821ab97379.jpg`; 739×415, 40052 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F8c2d4def-d5d8-4754-be58-0b821ab97379.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #3 (image 3): `11d7ef8b-dc70-4b42-ac43-ea539700beab.jpg`; object `fichas/11d7ef8b-dc70-4b42-ac43-ea539700beab.jpg`; 700×466, 54538 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F11d7ef8b-dc70-4b42-ac43-ea539700beab.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #4 (image 4): `e2d8c882-c822-4836-8d79-bba78f94dd06.jpg`; object `fichas/e2d8c882-c822-4836-8d79-bba78f94dd06.jpg`; 520×147, 10958 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fe2d8c882-c822-4836-8d79-bba78f94dd06.jpg?alt=media&token=[REDACTED]`; low resolution.

### Albula vulpes — macaco, conejo, piojo, ratón, macabí

- FichaPez.id: `osXhShrxuuFLdr0ftgmb`
- Image count: 3
- #5 (image 1): `6a0190f7-cc0c-4cde-ab17-b30740f22653.jpg`; object `fichas/6a0190f7-cc0c-4cde-ab17-b30740f22653.jpg`; 640×260, 25438 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F6a0190f7-cc0c-4cde-ab17-b30740f22653.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image; visible credit/logo/watermark.
- #6 (image 2): `d93d1b01-17ff-4669-8298-77284094cbce.jpg`; object `fichas/d93d1b01-17ff-4669-8298-77284094cbce.jpg`; 1600×1200, 119959 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fd93d1b01-17ff-4669-8298-77284094cbce.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.
- #7 (image 3): `7b9d4a4e-6863-4d2e-b1ed-24654a20ba26.jpg`; object `fichas/7b9d4a4e-6863-4d2e-b1ed-24654a20ba26.jpg`; 1000×350, 58164 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F7b9d4a4e-6863-4d2e-b1ed-24654a20ba26.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.

### Alectis ciliaris — corcobado, pámpano, coronado, sol

- FichaPez.id: `5SkWhUQgG6JuanpfSLUT`
- Image count: 4
- #8 (image 1): `b54afd8a-9b90-4edf-b31c-9dc3e31f4639.jpg`; object `fichas/b54afd8a-9b90-4edf-b31c-9dc3e31f4639.jpg`; 800×355, 31324 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fb54afd8a-9b90-4edf-b31c-9dc3e31f4639.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #9 (image 2): `b1668f70-6704-4739-8fab-efcd02ab3694.jpg`; object `fichas/b1668f70-6704-4739-8fab-efcd02ab3694.jpg`; 500×333, 30525 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fb1668f70-6704-4739-8fab-efcd02ab3694.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.
- #10 (image 3): `ad476edb-ba50-426c-8e6e-ff80334df21f.jpg`; object `fichas/ad476edb-ba50-426c-8e6e-ff80334df21f.jpg`; 640×435, 35913 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fad476edb-ba50-426c-8e6e-ff80334df21f.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #11 (image 4): `5ff56bb6-b5a8-4a15-925a-9e1dcdde3cd9.jpg`; object `fichas/5ff56bb6-b5a8-4a15-925a-9e1dcdde3cd9.jpg`; 1000×1000, 184540 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F5ff56bb6-b5a8-4a15-925a-9e1dcdde3cd9.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Anisotremus surinamensis — Chopa negra, Cachicata negra

- FichaPez.id: `ptq705ot5CnYod63Xs8E`
- Image count: 5
- #12 (image 1): `e4e2ceee-fe56-44df-9286-d3aaac5621fa.jpg`; object `fichas/e4e2ceee-fe56-44df-9286-d3aaac5621fa.jpg`; 640×436, 40301 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fe4e2ceee-fe56-44df-9286-d3aaac5621fa.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #13 (image 2): `f77161c9-4845-4c31-a60e-22965cee0beb.jpg`; object `fichas/f77161c9-4845-4c31-a60e-22965cee0beb.jpg`; 640×436, 39973 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Ff77161c9-4845-4c31-a60e-22965cee0beb.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #14 (image 3): `4cb87ccd-48e3-4fad-a344-96359692877d.jpg`; object `fichas/4cb87ccd-48e3-4fad-a344-96359692877d.jpg`; 640×436, 36461 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F4cb87ccd-48e3-4fad-a344-96359692877d.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #15 (image 4): `b500b890-3902-49f0-97f7-44a5b2f40061.jpg`; object `fichas/b500b890-3902-49f0-97f7-44a5b2f40061.jpg`; 640×436, 63343 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fb500b890-3902-49f0-97f7-44a5b2f40061.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #16 (image 5): `c3d0e53b-b743-4886-8f48-e2cf7387010b.jpg`; object `fichas/c3d0e53b-b743-4886-8f48-e2cf7387010b.jpg`; 640×436, 60376 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fc3d0e53b-b743-4886-8f48-e2cf7387010b.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Caranx lugubris — jurel negro

- FichaPez.id: `cEgkcDR0JUI8GdxEx5LA`
- Image count: 3
- #17 (image 1): `f097e39c-555a-41b6-b39f-bf3b0d620131.jpg`; object `fichas/f097e39c-555a-41b6-b39f-bf3b0d620131.jpg`; 640×426, 35663 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Ff097e39c-555a-41b6-b39f-bf3b0d620131.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #18 (image 2): `572e648f-cfd7-45e1-8fad-caa477eaa8d3.jpg`; object `fichas/572e648f-cfd7-45e1-8fad-caa477eaa8d3.jpg`; 597×422, 60514 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F572e648f-cfd7-45e1-8fad-caa477eaa8d3.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.
- #19 (image 3): `8bf8f55f-9f83-4296-8d76-87a10bf3a17c.jpg`; object `fichas/8bf8f55f-9f83-4296-8d76-87a10bf3a17c.jpg`; 914×449, 34983 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F8bf8f55f-9f83-4296-8d76-87a10bf3a17c.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Centropomus undecimalis — róbalo

- FichaPez.id: `PtbnNsBSRDwJzqGwvmv9`
- Image count: 3
- #20 (image 1): `fbeb3291-387d-44b3-8c47-9e5f86979025.jpg`; object `fichas/fbeb3291-387d-44b3-8c47-9e5f86979025.jpg`; 369×595, 41204 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Ffbeb3291-387d-44b3-8c47-9e5f86979025.jpg?alt=media&token=[REDACTED]`; low resolution.
- #21 (image 2): `982478b2-b0f6-45c5-a477-5146b36bacc0.jpg`; object `fichas/982478b2-b0f6-45c5-a477-5146b36bacc0.jpg`; 679×452, 42023 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F982478b2-b0f6-45c5-a477-5146b36bacc0.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #22 (image 3): `b0e68d25-9637-44e4-8a47-a3d994e02992.jpg`; object `fichas/b0e68d25-9637-44e4-8a47-a3d994e02992.jpg`; 640×436, 54647 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fb0e68d25-9637-44e4-8a47-a3d994e02992.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Cephalopholis cruentata — Cabrilla, cherna enjambre

- FichaPez.id: `wk7kjNwc7FzD9WT3c3Ui`
- Image count: 5
- #23 (image 1): `dc76d90c-951a-432b-a971-1e907174cbbb.jpg`; object `fichas/dc76d90c-951a-432b-a971-1e907174cbbb.jpg`; 784×391, 59583 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fdc76d90c-951a-432b-a971-1e907174cbbb.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #24 (image 2): `4d0242c2-4985-4602-a10c-31373b0291a6.jpg`; object `fichas/4d0242c2-4985-4602-a10c-31373b0291a6.jpg`; 570×347, 37511 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F4d0242c2-4985-4602-a10c-31373b0291a6.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #25 (image 3): `27e5c4c8-d9c5-4704-97bc-ec0a96b32d7f.jpg`; object `fichas/27e5c4c8-d9c5-4704-97bc-ec0a96b32d7f.jpg`; 600×484, 67898 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F27e5c4c8-d9c5-4704-97bc-ec0a96b32d7f.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #26 (image 4): `deab50ac-770e-417a-86ba-ca478569ae90.jpg`; object `fichas/deab50ac-770e-417a-86ba-ca478569ae90.jpg`; 250×187, 24698 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fdeab50ac-770e-417a-86ba-ca478569ae90.jpg?alt=media&token=[REDACTED]`; low resolution.
- #27 (image 5): `6d1a43b6-7f07-443a-96b3-cf111e8b3938.jpg`; object `fichas/6d1a43b6-7f07-443a-96b3-cf111e8b3938.jpg`; 1977×959, 183261 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F6d1a43b6-7f07-443a-96b3-cf111e8b3938.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Cephalopholis fulva — Mero cabrilla, mero mantequilla, cherna cabrilla, guajiro, gativirí, negrita, fino

- FichaPez.id: `1s69lkvuYEYoQkL6esVp`
- Image count: 3
- #28 (image 1): `7cef24b8-40b2-4b50-992f-5e3f5651ac9f.jpg`; object `fichas/7cef24b8-40b2-4b50-992f-5e3f5651ac9f.jpg`; 640×436, 48543 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F7cef24b8-40b2-4b50-992f-5e3f5651ac9f.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.
- #29 (image 2): `d848dbfc-674e-4689-8503-46563c80faa3.jpg`; object `fichas/d848dbfc-674e-4689-8503-46563c80faa3.jpg`; 691×444, 99660 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fd848dbfc-674e-4689-8503-46563c80faa3.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #30 (image 3): `80b7ebba-cad3-4ab6-b641-1bc7e19b6005.jpg`; object `fichas/80b7ebba-cad3-4ab6-b641-1bc7e19b6005.jpg`; 350×169, 16004 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F80b7ebba-cad3-4ab6-b641-1bc7e19b6005.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image; low resolution.

### Coryphaena hippurus — Dorado

- FichaPez.id: `AYGYpLjkS9LEv7AXVAuk`
- Image count: 5
- #31 (image 1): `8b13b1d4-e570-40bf-99e7-95387cecd34e.jpg`; object `fichas/8b13b1d4-e570-40bf-99e7-95387cecd34e.jpg`; 684×448, 28158 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F8b13b1d4-e570-40bf-99e7-95387cecd34e.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #32 (image 2): `192ca6fb-f82a-4d6e-8646-765e27111074.jpg`; object `fichas/192ca6fb-f82a-4d6e-8646-765e27111074.jpg`; 650×232, 21421 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F192ca6fb-f82a-4d6e-8646-765e27111074.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image; low resolution.
- #33 (image 3): `8c05dd31-bb6a-4ee6-b7e6-bbb063860ccc.jpg`; object `fichas/8c05dd31-bb6a-4ee6-b7e6-bbb063860ccc.jpg`; 1200×900, 64718 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F8c05dd31-bb6a-4ee6-b7e6-bbb063860ccc.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image; visible credit/logo/watermark.
- #34 (image 4): `f57e36e2-5aa5-4980-b152-a175225b26e6.jpg`; object `fichas/f57e36e2-5aa5-4980-b152-a175225b26e6.jpg`; 640×436, 19732 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Ff57e36e2-5aa5-4980-b152-a175225b26e6.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #35 (image 5): `277328ac-c1ab-42cb-8da4-f0a205d93196.jpg`; object `fichas/277328ac-c1ab-42cb-8da4-f0a205d93196.jpg`; 500×500, 11990 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F277328ac-c1ab-42cb-8da4-f0a205d93196.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Epinephelus adscensionis — Cabra mora, cabrilla

- FichaPez.id: `CIfxxdN70JcakCqA0IxY`
- Image count: 3
- #36 (image 1): `cf363b81-ad9a-499d-8b6e-ac331050c995.jpg`; object `fichas/cf363b81-ad9a-499d-8b6e-ac331050c995.jpg`; 640×436, 43429 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fcf363b81-ad9a-499d-8b6e-ac331050c995.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #37 (image 2): `360d5340-be18-42ad-a120-26c6d0397f5f.jpg`; object `fichas/360d5340-be18-42ad-a120-26c6d0397f5f.jpg`; 453×305, 43317 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F360d5340-be18-42ad-a120-26c6d0397f5f.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #38 (image 3): `1d2d2bec-1687-46a9-ace7-4e94f3939253.jpg`; object `fichas/1d2d2bec-1687-46a9-ace7-4e94f3939253.jpg`; 2896×1944, 409477 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F1d2d2bec-1687-46a9-ace7-4e94f3939253.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Epinephelus guttatus — Mero cabrilla, mero cherna, mero colorado

- FichaPez.id: `pFJ38O9TeYjWUt2n8XRS`
- Image count: 3
- #39 (image 1): `22203410-ba76-4395-9ca9-bd06ce5c5ed5.jpg`; object `fichas/22203410-ba76-4395-9ca9-bd06ce5c5ed5.jpg`; 837×366, 41925 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F22203410-ba76-4395-9ca9-bd06ce5c5ed5.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #40 (image 2): `e416b28b-e2fd-482c-8404-fba0ef6cfaaf.jpg`; object `fichas/e416b28b-e2fd-482c-8404-fba0ef6cfaaf.jpg`; 640×436, 69776 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fe416b28b-e2fd-482c-8404-fba0ef6cfaaf.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #41 (image 3): `9772c588-f529-47fa-9409-78fb2fc1f808.jpg`; object `fichas/9772c588-f529-47fa-9409-78fb2fc1f808.jpg`; 570×329, 27550 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F9772c588-f529-47fa-9409-78fb2fc1f808.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.

### Epinephelus itajara — Mero batata, mero grande, mero sapo, judío

- FichaPez.id: `TvWu2VyCwDofI4RfOmmU`
- Image count: 2
- #42 (image 1): `1d3fe321-f76e-4f21-afc4-41b041921014.jpg`; object `fichas/1d3fe321-f76e-4f21-afc4-41b041921014.jpg`; 640×436, 64031 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F1d3fe321-f76e-4f21-afc4-41b041921014.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #43 (image 2): `3a47cc4f-334d-40ee-a65d-0a848dc2508f.jpg`; object `fichas/3a47cc4f-334d-40ee-a65d-0a848dc2508f.jpg`; 640×436, 53669 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F3a47cc4f-334d-40ee-a65d-0a848dc2508f.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Epinephelus mystacinus — Guasa, mero listado, cherna

- FichaPez.id: `SQ7eid3h0Fk2ToVnnqm6`
- Image count: 3
- #44 (image 1): `1756c63c-25a2-4fe8-a485-bce1a576091f.jpg`; object `fichas/1756c63c-25a2-4fe8-a485-bce1a576091f.jpg`; 640×436, 44859 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F1756c63c-25a2-4fe8-a485-bce1a576091f.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #45 (image 2): `02d91209-a379-41ff-b06c-d777d5676709.jpg`; object `fichas/02d91209-a379-41ff-b06c-d777d5676709.jpg`; 640×271, 45596 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F02d91209-a379-41ff-b06c-d777d5676709.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #46 (image 3): `84552f63-d690-464c-8cd4-5a3a34b70a91.jpg`; object `fichas/84552f63-d690-464c-8cd4-5a3a34b70a91.jpg`; 960×459, 61527 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F84552f63-d690-464c-8cd4-5a3a34b70a91.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Epinephelus striatus — Mero cherna

- FichaPez.id: `pS0UEezomaklOqZsflTt`
- Image count: 3
- #47 (image 1): `59e11bfb-76ea-44b0-ac33-36fdd2b6dae6.jpg`; object `fichas/59e11bfb-76ea-44b0-ac33-36fdd2b6dae6.jpg`; 800×534, 51404 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F59e11bfb-76ea-44b0-ac33-36fdd2b6dae6.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image.
- #48 (image 2): `f58a67c6-e8fd-4aee-bbdd-ac5499e42e60.jpg`; object `fichas/f58a67c6-e8fd-4aee-bbdd-ac5499e42e60.jpg`; 678×452, 39428 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Ff58a67c6-e8fd-4aee-bbdd-ac5499e42e60.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #49 (image 3): `fe44fd5d-a33d-4fcb-ba4d-a709234e28d7.jpg`; object `fichas/fe44fd5d-a33d-4fcb-ba4d-a709234e28d7.jpg`; 640×354, 59007 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Ffe44fd5d-a33d-4fcb-ba4d-a709234e28d7.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Etelis oculatus — Cartucho, cachucho

- FichaPez.id: `fZog3T6cou99saWzsQyE`
- Image count: 3
- #50 (image 1): `329cdc5f-827e-4d4e-8643-9bfc6a4afded.jpg`; object `fichas/329cdc5f-827e-4d4e-8643-9bfc6a4afded.jpg`; 640×480, 42915 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F329cdc5f-827e-4d4e-8643-9bfc6a4afded.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image.
- #51 (image 2): `41461fda-8d8e-4676-8f5e-5277e7813286.jpg`; object `fichas/41461fda-8d8e-4676-8f5e-5277e7813286.jpg`; 640×436, 74321 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F41461fda-8d8e-4676-8f5e-5277e7813286.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.
- #52 (image 3): `2ecfac5d-0927-4fad-92d2-b5a7574c48c5.jpg`; object `fichas/2ecfac5d-0927-4fad-92d2-b5a7574c48c5.jpg`; 640×436, 17014 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F2ecfac5d-0927-4fad-92d2-b5a7574c48c5.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Haemulon plumieri — boquicolorao, cachicata, blanca, ronco, cicí, ronco, blanco, ronco grande

- FichaPez.id: `RO2iuTVLAX11dy3aNgdf`
- Image count: 3
- #53 (image 1): `e9f7d0b5-9bf5-4be4-9806-be72bdc7ba2b.jpg`; object `fichas/e9f7d0b5-9bf5-4be4-9806-be72bdc7ba2b.jpg`; 570×323, 33262 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fe9f7d0b5-9bf5-4be4-9806-be72bdc7ba2b.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #54 (image 2): `52bf6862-92db-4c2a-af2a-72416bfb922c.jpg`; object `fichas/52bf6862-92db-4c2a-af2a-72416bfb922c.jpg`; 739×415, 37657 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F52bf6862-92db-4c2a-af2a-72416bfb922c.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #55 (image 3): `25c57227-09c5-4a06-9814-2298e515ee3a.jpg`; object `fichas/25c57227-09c5-4a06-9814-2298e515ee3a.jpg`; 2048×1279, 427564 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F25c57227-09c5-4a06-9814-2298e515ee3a.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Lactophrys bicaudalis — Chapín moteado

- FichaPez.id: `0hjc5oWRBLg9dyujatSy`
- Image count: 1
- #56 (image 1): `77a6b1bf-2c3f-44b8-8c88-334cfb5c3e57.jpg`; object `fichas/77a6b1bf-2c3f-44b8-8c88-334cfb5c3e57.jpg`; 640×436, 47246 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F77a6b1bf-2c3f-44b8-8c88-334cfb5c3e57.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.

### Lactophrys polygonia — Chapín panal

- FichaPez.id: `bUROGweaABz6GRUedycl`
- Image count: 2
- #57 (image 1): `095aba62-bd91-47c8-8939-218384cb913e.jpg`; object `fichas/095aba62-bd91-47c8-8939-218384cb913e.jpg`; 646×475, 59075 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F095aba62-bd91-47c8-8939-218384cb913e.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.
- #58 (image 2): `d891154a-7d6b-4da5-b974-199db9beea7e.jpg`; object `fichas/d891154a-7d6b-4da5-b974-199db9beea7e.jpg`; 588×390, 19787 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fd891154a-7d6b-4da5-b974-199db9beea7e.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Lactophrys quadricornis — Chapín veteado, Chapín toro

- FichaPez.id: `Hjr9sFSdUEW1RVpR09mV`
- Image count: 2
- #59 (image 1): `a85102de-c812-48fd-becb-0ff6232e6430.jpg`; object `fichas/a85102de-c812-48fd-becb-0ff6232e6430.jpg`; 250×135, 11297 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fa85102de-c812-48fd-becb-0ff6232e6430.jpg?alt=media&token=[REDACTED]`; low resolution.
- #60 (image 2): `5703b44e-f52c-4227-994b-f81f512edbe6.jpg`; object `fichas/5703b44e-f52c-4227-994b-f81f512edbe6.jpg`; 547×365, 25530 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F5703b44e-f52c-4227-994b-f81f512edbe6.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Lactophrys trigonus — Chapín jorobado, gallina

- FichaPez.id: `IBd1JsryE7jTh1tpyCN8`
- Image count: 1
- #61 (image 1): `2eab957f-5098-43ac-b17a-09aebf6e5db9.jpg`; object `fichas/2eab957f-5098-43ac-b17a-09aebf6e5db9.jpg`; 570×309, 29775 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F2eab957f-5098-43ac-b17a-09aebf6e5db9.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Lactophrys triqueter — Chapín liso

- FichaPez.id: `qDlhElFdSz5UOHDkU8Pe`
- Image count: 1
- #62 (image 1): `c469f88d-3675-40b2-a9fc-f938b53e88de.jpg`; object `fichas/c469f88d-3675-40b2-a9fc-f938b53e88de.jpg`; 1000×750, 108909 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fc469f88d-3675-40b2-a9fc-f938b53e88de.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Lutjanus analis — Sama, pargo criollo

- FichaPez.id: `eBZEv2F3RUvtST6fx0cK`
- Image count: 4
- #63 (image 1): `8f708da9-12fd-4235-89f4-057ab45f32f8.jpg`; object `fichas/8f708da9-12fd-4235-89f4-057ab45f32f8.jpg`; 850×566, 64676 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F8f708da9-12fd-4235-89f4-057ab45f32f8.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image.
- #64 (image 2): `7739e718-bd53-4ab2-9418-7affe25b433a.jpg`; object `fichas/7739e718-bd53-4ab2-9418-7affe25b433a.jpg`; 640×436, 40060 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F7739e718-bd53-4ab2-9418-7affe25b433a.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.
- #65 (image 3): `98109d75-773b-44fe-8f2c-44f6b02f3cc1.jpg`; object `fichas/98109d75-773b-44fe-8f2c-44f6b02f3cc1.jpg`; 550×258, 32884 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F98109d75-773b-44fe-8f2c-44f6b02f3cc1.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #66 (image 4): `f0948778-72c7-4f9e-9be0-6cd4fa4d35a0.jpg`; object `fichas/f0948778-72c7-4f9e-9be0-6cd4fa4d35a0.jpg`; 570×350, 30350 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Ff0948778-72c7-4f9e-9be0-6cd4fa4d35a0.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Lutjanus buccanella — Alinegra, negrita

- FichaPez.id: `3qAJ1d8NdETc7HAsvJtg`
- Image count: 2
- #67 (image 1): `bbe9b47f-33ac-49e1-a7f8-5f1d5544c5a3.jpg`; object `fichas/bbe9b47f-33ac-49e1-a7f8-5f1d5544c5a3.jpg`; 640×436, 59939 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fbbe9b47f-33ac-49e1-a7f8-5f1d5544c5a3.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #68 (image 2): `bc98999f-3fe2-48c6-a3e7-38122a8cb98b.jpg`; object `fichas/bc98999f-3fe2-48c6-a3e7-38122a8cb98b.jpg`; 450×237, 19286 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fbc98999f-3fe2-48c6-a3e7-38122a8cb98b.jpg?alt=media&token=[REDACTED]`; low resolution.

### Lutjanus cyanopterus — Pargo cubera

- FichaPez.id: `J4JKaRrOWzuHfxq9ihKM`
- Image count: 4
- #69 (image 1): `d3191dc0-5c73-4f4f-b06a-48ee6bcb43ed.jpg`; object `fichas/d3191dc0-5c73-4f4f-b06a-48ee6bcb43ed.jpg`; 1024×576, 74178 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fd3191dc0-5c73-4f4f-b06a-48ee6bcb43ed.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image; visible credit/logo/watermark.
- #70 (image 2): `a4dba736-01cf-414e-a411-9210dd9e92a4.jpg`; object `fichas/a4dba736-01cf-414e-a411-9210dd9e92a4.jpg`; 450×213, 18842 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fa4dba736-01cf-414e-a411-9210dd9e92a4.jpg?alt=media&token=[REDACTED]`; low resolution.
- #71 (image 3): `ba7838b7-b4ff-4474-82b6-29d80df2de5e.jpg`; object `fichas/ba7838b7-b4ff-4474-82b6-29d80df2de5e.jpg`; 1920×1440, 279609 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fba7838b7-b4ff-4474-82b6-29d80df2de5e.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #72 (image 4): `d6e1ffd8-eb76-48c8-b7ee-b45104582100.jpg`; object `fichas/d6e1ffd8-eb76-48c8-b7ee-b45104582100.jpg`; 1024×683, 105198 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fd6e1ffd8-eb76-48c8-b7ee-b45104582100.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Lutjanus jocu — pargo perro, pargo sama, pargo dientón, pargo colorado, jocú

- FichaPez.id: `NZjA3AcJ6gb2ddsUNOPw`
- Image count: 3
- #73 (image 1): `5787bc3f-9a43-486b-b689-c003bb7ed2b4.jpg`; object `fichas/5787bc3f-9a43-486b-b689-c003bb7ed2b4.jpg`; 480×320, 23308 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F5787bc3f-9a43-486b-b689-c003bb7ed2b4.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #74 (image 2): `febdee8e-e3ac-4fa5-b110-3d8bb5f433c9.jpg`; object `fichas/febdee8e-e3ac-4fa5-b110-3d8bb5f433c9.jpg`; 640×436, 46321 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Ffebdee8e-e3ac-4fa5-b110-3d8bb5f433c9.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.
- #75 (image 3): `0c1b7c7c-eaf1-4d37-bf25-a214453da1a9.jpg`; object `fichas/0c1b7c7c-eaf1-4d37-bf25-a214453da1a9.jpg`; 640×359, 66103 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F0c1b7c7c-eaf1-4d37-bf25-a214453da1a9.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Lutjanus synagris — Arrayado, rayado, manchego

- FichaPez.id: `VL21Dl6MaY4SDJkmvIIz`
- Image count: 4
- #76 (image 1): `582c45fe-1ee8-457d-b509-2f3439852608.jpg`; object `fichas/582c45fe-1ee8-457d-b509-2f3439852608.jpg`; 550×292, 25797 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F582c45fe-1ee8-457d-b509-2f3439852608.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #77 (image 2): `ebb7f8f3-ba36-461b-952a-c0ef93f484ba.jpg`; object `fichas/ebb7f8f3-ba36-461b-952a-c0ef93f484ba.jpg`; 540×370, 62377 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Febb7f8f3-ba36-461b-952a-c0ef93f484ba.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #78 (image 3): `f900509d-ca75-41a2-9aad-4b1e296454d1.jpg`; object `fichas/f900509d-ca75-41a2-9aad-4b1e296454d1.jpg`; 540×370, 26399 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Ff900509d-ca75-41a2-9aad-4b1e296454d1.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #79 (image 4): `36d7294a-3d7f-47ba-b007-97f281fccabf.jpg`; object `fichas/36d7294a-3d7f-47ba-b007-97f281fccabf.jpg`; 1024×683, 76215 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F36d7294a-3d7f-47ba-b007-97f281fccabf.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Lutjanus vivanus — Chillo ojo amarillo, pargo colorado

- FichaPez.id: `giJGGBQxEBmzjTMhZsPg`
- Image count: 2
- #80 (image 1): `0ac5421d-7f4e-4d5d-a78d-f5cfbf72ab59.jpg`; object `fichas/0ac5421d-7f4e-4d5d-a78d-f5cfbf72ab59.jpg`; 2048×2048, 644174 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F0ac5421d-7f4e-4d5d-a78d-f5cfbf72ab59.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #81 (image 2): `31a957af-55f1-46d6-88ed-50af9fc54861.jpg`; object `fichas/31a957af-55f1-46d6-88ed-50af9fc54861.jpg`; 640×307, 49783 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F31a957af-55f1-46d6-88ed-50af9fc54861.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Megalops atlanticus — sábalo, tarpón

- FichaPez.id: `MjsvzQbyRzyWqtjGYSBM`
- Image count: 5
- #82 (image 1): `ff950dbd-817d-4831-a1f1-faab81aa28c7.jpg`; object `fichas/ff950dbd-817d-4831-a1f1-faab81aa28c7.jpg`; 586×341, 32948 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fff950dbd-817d-4831-a1f1-faab81aa28c7.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #83 (image 2): `5f0ab6ce-8ad2-44a4-b35a-6f1447dca5d2.jpg`; object `fichas/5f0ab6ce-8ad2-44a4-b35a-6f1447dca5d2.jpg`; 548×364, 20275 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F5f0ab6ce-8ad2-44a4-b35a-6f1447dca5d2.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #84 (image 3): `e03c2d60-8f20-402e-a87e-5bdab60e8ff1.jpg`; object `fichas/e03c2d60-8f20-402e-a87e-5bdab60e8ff1.jpg`; 570×279, 22637 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fe03c2d60-8f20-402e-a87e-5bdab60e8ff1.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #85 (image 4): `99e2bac1-7b6f-4ffc-a10d-35107326171f.jpg`; object `fichas/99e2bac1-7b6f-4ffc-a10d-35107326171f.jpg`; 512×256, 13820 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F99e2bac1-7b6f-4ffc-a10d-35107326171f.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image.
- #86 (image 5): `5b3e5949-c3be-4281-958d-9c0d5b530879.jpg`; object `fichas/5b3e5949-c3be-4281-958d-9c0d5b530879.jpg`; 640×349, 24900 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F5b3e5949-c3be-4281-958d-9c0d5b530879.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image.

### Mycteroperca venenosa — Guajil, guajil colirrubio, mero pinto

- FichaPez.id: `WSIwTi77Bdy2KEUtE26k`
- Image count: 4
- #87 (image 1): `55b6db4a-9484-4ca8-a3fd-dc511bb1db3f.jpg`; object `fichas/55b6db4a-9484-4ca8-a3fd-dc511bb1db3f.jpg`; 861×356, 73823 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F55b6db4a-9484-4ca8-a3fd-dc511bb1db3f.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image; manual near-duplicate/source variant.
- #88 (image 2): `3a3cdc1c-645e-4ead-8065-81bbc650689c.jpg`; object `fichas/3a3cdc1c-645e-4ead-8065-81bbc650689c.jpg`; 700×700, 43933 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F3a3cdc1c-645e-4ead-8065-81bbc650689c.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image.
- #89 (image 3): `64464399-ebc4-42ec-b67c-0ef7409ae823.jpg`; object `fichas/64464399-ebc4-42ec-b67c-0ef7409ae823.jpg`; 997×600, 136658 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F64464399-ebc4-42ec-b67c-0ef7409ae823.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image; manual near-duplicate/source variant.
- #90 (image 4): `c622bf6c-0bf8-43e4-b28a-678a46983423.jpg`; object `fichas/c622bf6c-0bf8-43e4-b28a-678a46983423.jpg`; 960×459, 86522 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fc622bf6c-0bf8-43e4-b28a-678a46983423.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Ocyurus chrysurus — Colirrubia, rabirrubia

- FichaPez.id: `XTLHUX6xHya0BOisyR6E`
- Image count: 3
- #91 (image 1): `1948d718-0b89-43a1-a8eb-55dd5a60de80.jpg`; object `fichas/1948d718-0b89-43a1-a8eb-55dd5a60de80.jpg`; 500×406, 40313 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F1948d718-0b89-43a1-a8eb-55dd5a60de80.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #92 (image 2): `fd6101be-2b0c-476f-b69a-9afa45067aad.jpg`; object `fichas/fd6101be-2b0c-476f-b69a-9afa45067aad.jpg`; 640×436, 42805 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Ffd6101be-2b0c-476f-b69a-9afa45067aad.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.
- #93 (image 3): `b14afa85-8c36-4751-8cf8-dc91ec6554ab.jpg`; object `fichas/b14afa85-8c36-4751-8cf8-dc91ec6554ab.jpg`; 960×614, 116004 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fb14afa85-8c36-4751-8cf8-dc91ec6554ab.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Pristipomoides aquilonaris — Muniama

- FichaPez.id: `WaSqNZuItzfXizCKyei7`
- Image count: 3
- #94 (image 1): `adfc1896-c3ff-44ee-bf4d-7142dc8fcd17.jpg`; object `fichas/adfc1896-c3ff-44ee-bf4d-7142dc8fcd17.jpg`; 640×436, 19374 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fadfc1896-c3ff-44ee-bf4d-7142dc8fcd17.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #95 (image 2): `b96860d5-7a6a-43a8-905e-a13a643bd3ee.jpg`; object `fichas/b96860d5-7a6a-43a8-905e-a13a643bd3ee.jpg`; 597×335, 25777 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fb96860d5-7a6a-43a8-905e-a13a643bd3ee.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #96 (image 3): `8721f233-e2f8-4a1b-8725-39d2d59ba429.jpg`; object `fichas/8721f233-e2f8-4a1b-8725-39d2d59ba429.jpg`; 516×387, 15796 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F8721f233-e2f8-4a1b-8725-39d2d59ba429.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image; visible credit/logo/watermark.

### Rhomboplites aurorubens — Rubia, besugo, buchona, chilla rubia, pargo cunaro, tunaro, sardo

- FichaPez.id: `oH2T6KbHxVuRYK0EgI5D`
- Image count: 3
- #97 (image 1): `4d9714e9-8d40-4fc5-809b-612000402c31.jpg`; object `fichas/4d9714e9-8d40-4fc5-809b-612000402c31.jpg`; 247×150, 6492 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F4d9714e9-8d40-4fc5-809b-612000402c31.jpg?alt=media&token=[REDACTED]`; low resolution.
- #98 (image 2): `42e24629-ed37-447f-8c0e-73bc11cc566b.jpg`; object `fichas/42e24629-ed37-447f-8c0e-73bc11cc566b.jpg`; 570×323, 35443 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F42e24629-ed37-447f-8c0e-73bc11cc566b.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #99 (image 3): `d401ee22-d42e-4b64-ba0b-eb6526792816.jpg`; object `fichas/d401ee22-d42e-4b64-ba0b-eb6526792816.jpg`; 447×447, 24846 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fd401ee22-d42e-4b64-ba0b-eb6526792816.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Scomberomorus cavalla — Carite, sierra, cavalla

- FichaPez.id: `V39KoLAZkg0MBjiWaa46`
- Image count: 5
- #100 (image 1): `7edb3084-756b-4544-84d9-f21cc2413914.jpg`; object `fichas/7edb3084-756b-4544-84d9-f21cc2413914.jpg`; 640×436, 18099 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F7edb3084-756b-4544-84d9-f21cc2413914.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image; visible credit/logo/watermark.
- #101 (image 2): `6b97414e-b878-4c13-a71d-c60a5708c6ea.jpg`; object `fichas/6b97414e-b878-4c13-a71d-c60a5708c6ea.jpg`; 640×308, 22336 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F6b97414e-b878-4c13-a71d-c60a5708c6ea.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #102 (image 3): `a2f32bee-e4ce-4bbb-983e-2368e4ece958.jpg`; object `fichas/a2f32bee-e4ce-4bbb-983e-2368e4ece958.jpg`; 960×618, 43222 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fa2f32bee-e4ce-4bbb-983e-2368e4ece958.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #103 (image 4): `d03ca855-662a-4bc3-8ef4-33b7a4f589ef.jpg`; object `fichas/d03ca855-662a-4bc3-8ef4-33b7a4f589ef.jpg`; 600×179, 25107 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fd03ca855-662a-4bc3-8ef4-33b7a4f589ef.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image; low resolution.
- #104 (image 5): `f3063ca0-9182-4457-9b2a-440aa2b84e60.jpg`; object `fichas/f3063ca0-9182-4457-9b2a-440aa2b84e60.jpg`; 640×436, 15689 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Ff3063ca0-9182-4457-9b2a-440aa2b84e60.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.

### Scomberomorus regalis — Sierra alasana, pintado, pelicán

- FichaPez.id: `OF8bIJWNGjtMOJnLeVgf`
- Image count: 4
- #105 (image 1): `cc0df09c-78d1-4fe9-b151-2071a8a0cdff.jpg`; object `fichas/cc0df09c-78d1-4fe9-b151-2071a8a0cdff.jpg`; 550×203, 22986 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fcc0df09c-78d1-4fe9-b151-2071a8a0cdff.jpg?alt=media&token=[REDACTED]`; low resolution.
- #106 (image 2): `90d6b32c-bdbf-4e05-851c-b3bd0f2786cc.jpg`; object `fichas/90d6b32c-bdbf-4e05-851c-b3bd0f2786cc.jpg`; 640×436, 28690 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F90d6b32c-bdbf-4e05-851c-b3bd0f2786cc.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.
- #107 (image 3): `d9501b4d-d549-464e-8c0b-5a18d1a82743.jpg`; object `fichas/d9501b4d-d549-464e-8c0b-5a18d1a82743.jpg`; 1280×853, 102820 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fd9501b4d-d549-464e-8c0b-5a18d1a82743.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #108 (image 4): `389c17c2-208f-4e5e-9ece-692a6c51959d.jpg`; object `fichas/389c17c2-208f-4e5e-9ece-692a6c51959d.jpg`; 640×436, 22561 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F389c17c2-208f-4e5e-9ece-692a6c51959d.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.

### Seriola dumerili — Medregal

- FichaPez.id: `u1JpMvcENOy98cd31Za5`
- Image count: 3
- #109 (image 1): `0f8dbd4d-48a7-4cfc-aed6-b927eed456d2.jpg`; object `fichas/0f8dbd4d-48a7-4cfc-aed6-b927eed456d2.jpg`; 671×298, 18093 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F0f8dbd4d-48a7-4cfc-aed6-b927eed456d2.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #110 (image 2): `ebc4f375-f528-46d9-95fa-6822723387ea.jpg`; object `fichas/ebc4f375-f528-46d9-95fa-6822723387ea.jpg`; 850×398, 45333 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Febc4f375-f528-46d9-95fa-6822723387ea.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #111 (image 3): `d6edb40f-38a8-454d-9e37-0c036fa32c27.jpg`; object `fichas/d6edb40f-38a8-454d-9e37-0c036fa32c27.jpg`; 1000×448, 56991 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fd6edb40f-38a8-454d-9e37-0c036fa32c27.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Seriola rivoliana — medregal, escolar

- FichaPez.id: `iOXBIJjjwRw2FELFnSe1`
- Image count: 3
- #112 (image 1): `c438da37-e08c-4632-8777-f731f9f78410.jpg`; object `fichas/c438da37-e08c-4632-8777-f731f9f78410.jpg`; 640×340, 56726 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fc438da37-e08c-4632-8777-f731f9f78410.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #113 (image 2): `5aa4dd31-c849-489d-b9da-ba98d033c30d.jpg`; object `fichas/5aa4dd31-c849-489d-b9da-ba98d033c30d.jpg`; 548×364, 28966 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F5aa4dd31-c849-489d-b9da-ba98d033c30d.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #114 (image 3): `0f5db030-ca32-4a35-bc7b-b9f65b181f68.jpg`; object `fichas/0f5db030-ca32-4a35-bc7b-b9f65b181f68.jpg`; 533×375, 12808 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F0f5db030-ca32-4a35-bc7b-b9f65b181f68.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Sphyraena barracuda — picúa, barracuda

- FichaPez.id: `ejX0Cx9YYxsmOQTJb8kK`
- Image count: 4
- #115 (image 1): `81bbd557-6008-4c28-9248-05fa12a2db64.jpg`; object `fichas/81bbd557-6008-4c28-9248-05fa12a2db64.jpg`; 960×640, 96203 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F81bbd557-6008-4c28-9248-05fa12a2db64.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #116 (image 2): `b0797c56-2059-489d-b849-57f88d98b747.jpg`; object `fichas/b0797c56-2059-489d-b849-57f88d98b747.jpg`; 672×480, 69845 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fb0797c56-2059-489d-b849-57f88d98b747.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.
- #117 (image 3): `6a8f57ee-202e-4871-83cc-bb2c52d5c79f.jpg`; object `fichas/6a8f57ee-202e-4871-83cc-bb2c52d5c79f.jpg`; 600×163, 13095 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F6a8f57ee-202e-4871-83cc-bb2c52d5c79f.jpg?alt=media&token=[REDACTED]`; low resolution.
- #118 (image 4): `811cb194-762c-4ddf-9505-28541c7e231c.jpg`; object `fichas/811cb194-762c-4ddf-9505-28541c7e231c.jpg`; 671×298, 17050 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F811cb194-762c-4ddf-9505-28541c7e231c.jpg?alt=media&token=[REDACTED]`; fish visible; no exceptional issue at contact-sheet scale.

### Sphyraena guachancho — guaguanche, picuílla, picúa parda

- FichaPez.id: `PM6TqnpdmFQMMDQFoQAy`
- Image count: 2
- #119 (image 1): `d045f652-07e2-47e6-aee1-21362bc0af2b.jpg`; object `fichas/d045f652-07e2-47e6-aee1-21362bc0af2b.jpg`; 640×197, 20037 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fd045f652-07e2-47e6-aee1-21362bc0af2b.jpg?alt=media&token=[REDACTED]`; exact cross-species duplicate; low resolution.
- #120 (image 2): `71be3a81-7683-40ba-b122-e4fb56ee3419.jpg`; object `fichas/71be3a81-7683-40ba-b122-e4fb56ee3419.jpg`; 640×436, 63588 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F71be3a81-7683-40ba-b122-e4fb56ee3419.jpg?alt=media&token=[REDACTED]`; exact cross-species duplicate.

### Sphyraena picudilla — picudilla, picúa lanceta

- FichaPez.id: `Ya1VhjdpdBABqWClLAnW`
- Image count: 4
- #121 (image 1): `d60a560c-9bf6-42d0-a16f-388e5abe4181.jpg`; object `fichas/d60a560c-9bf6-42d0-a16f-388e5abe4181.jpg`; 640×480, 14375 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2Fd60a560c-9bf6-42d0-a16f-388e5abe4181.jpg?alt=media&token=[REDACTED]`; drawing/illustration/rendered image; visible credit/logo/watermark.
- #122 (image 2): `78fe77be-9130-4d6d-87c1-2c150cdc2b9c.jpg`; object `fichas/78fe77be-9130-4d6d-87c1-2c150cdc2b9c.jpg`; 964×724, 89750 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F78fe77be-9130-4d6d-87c1-2c150cdc2b9c.jpg?alt=media&token=[REDACTED]`; visible credit/logo/watermark.
- #123 (image 3): `6d9ddbcc-995d-45ca-9aaa-bd65ede89ea8.jpg`; object `fichas/6d9ddbcc-995d-45ca-9aaa-bd65ede89ea8.jpg`; 640×197, 20037 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F6d9ddbcc-995d-45ca-9aaa-bd65ede89ea8.jpg?alt=media&token=[REDACTED]`; exact cross-species duplicate; low resolution.
- #124 (image 4): `344d7148-62c8-437a-9501-32b2b16dc001.jpg`; object `fichas/344d7148-62c8-437a-9501-32b2b16dc001.jpg`; 640×436, 63588 bytes; URL `https://firebasestorage.googleapis.com/v0/b/pescapr-c8a12d85.firebasestorage.app/o/fichas%2F344d7148-62c8-437a-9501-32b2b16dc001.jpg?alt=media&token=[REDACTED]`; exact cross-species duplicate.

## Images that must not be used for training

At present, all 124 must be excluded from training, validation, and test sets because none has documented training rights. #57 has an obvious stock watermark; #119/#123 and #120/#124 have unresolved cross-species labels; #87/#89 would cause same-source leakage. Drawings/rendered images should be excluded from a field-photo classifier unless a future design explicitly justifies and isolates them.

## Images with unresolved rights

All 124. Before reuse beyond visual reference, record original source, creator/rightsholder, exact license or permission, license version, attribution, retrieval date, modification history, permitted ML/derivative/redistribution uses, and evidence. Firebase UUIDs and tokens are not provenance.

## Recommended next action for FI-A.5

Define per-species minimums and an acceptance checklist before sourcing. Require traceable originals, expert-confirmed labels, duplicate/source-family grouping, representative field/catch variation, and a separate unknown/non-fish evaluation set. Treat current files only as visual aids and candidate leads; reassess individual seed use only after rights and label clearance.
