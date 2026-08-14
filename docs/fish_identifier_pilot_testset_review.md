# Fish Identifier Pilot — `testset-v1` Review Packet

**Prepared:** 2026-08-14
**First composition review:** REJECTED / REASSIGNED by Edgar Rivera on 2026-08-14
**Revised composition approval:** APPROVED FOR LOCKED TEST by Edgar Rivera, 2026-08-14
**Reviewer requested:** Edgar Rivera, independent Dataset Approver

## Purpose

These four rows were assigned TEST by the stable `pescapr-partition-v1` source-group hash. The assignment is reproducible but not evidence that the composition is representative. Edgar must independently choose one allowed decision per row before any row is locked or an immutable snapshot is created.

| Image ID | Type | Species/OOD | Source item | Source/session group | License / rights | Quality and variation | Related material outside TEST | Review assessment | Decision |
|---|---|---|---|---|---|---|---|---|---|
| `pilot-b1-wc-152817751` | POSITIVE | *Coryphaena hippurus* | [Wikimedia/FDA item 152817751](https://commons.wikimedia.org/wiki/?curid=152817751) | `cory-fda-210` / `session-cory-fda-210` | Public domain / APPROVED | Adult, full left lateral, post-catch controlled reference | FDA is also the credited source for TRAIN image `pilot-b1-wc-152817762`, but it is a different species, source item, numbered asset, and recorded session | Technically clear but controlled-reference bias and the broader FDA collection's TRAIN presence weaken a realistic locked-test role | **MOVE_TO_VALIDATION — Edgar Rivera, 2026-08-14** |
| `pilot-b1-wc-25501633` | POSITIVE | *Alectis ciliaris* | [Wikimedia item 25501633](https://commons.wikimedia.org/wiki/?curid=25501633) | `alectis-mendez-sealife-bray` / `session-alectis-mendez-sealife-bray` | CC BY 2.0 / APPROVED | Adult, full right-lateral oblique, aquarium/naturalistic background | No related photographer, source item, session, or individual appears in TRAIN/VALIDATION | Independent but aquarium context is less representative of PescaPR catch photos | **MOVE_TO_VALIDATION — Edgar Rivera, 2026-08-14** |
| `pilot-ood-b1-wc-33200360` | OOD | `boat` | [Wikimedia item 33200360](https://commons.wikimedia.org/wiki/?curid=33200360) | `source-ood-wc-33200360` / `session-ood-wc-33200360` | CC BY 4.0 / APPROVED | Boat/water scene, no supported fish | No related photographer, source/session group, or binary similarity appears outside TEST | Two boat-only OOD rows made TEST too narrow | **MOVE_TO_TRAIN — Edgar Rivera, 2026-08-14** |
| `pilot-ood-b1-wc-37313734` | OOD | `boat` | [Wikimedia item 37313734](https://commons.wikimedia.org/wiki/?curid=37313734) | `source-ood-wc-37313734` / `session-ood-wc-37313734` | CC BY 2.0 / APPROVED | Coastal two-boat scene, no supported fish | No related photographer, source/session group, or binary similarity appears outside TEST | Two boat-only OOD rows made TEST too narrow | **MOVE_TO_TRAIN — Edgar Rivera, 2026-08-14** |

Current composition is 2 positive and 2 OOD, but neither positive is a typical angler field/catch photo and both OOD rows are boats. Approval is therefore a process-integrity decision for this small pilot, not a claim that `testset-v1` is representative enough for model accuracy reporting.

Edgar rejected this composition for locking and directed the four moves above. Those historical decisions are recorded in canonical approval and partition-override metadata.

## Revised TEST proposal — human decisions pending

| Image ID | Type | Species/OOD | Source item | Source/session group | Field/reference context | Why it improves representativeness | Related-group leakage assessment | Human decision |
|---|---|---|---|---|---|---|---|---|
| `pilot-b1-wc-1734232` | POSITIVE | *Coryphaena hippurus* | [Wikimedia item 1734232](https://commons.wikimedia.org/wiki/?curid=1734232) | `cory-jeff-weiss-2005-catch` / `session-cory-jeff-weiss-2005-catch` | Adult large male held after capture on a boat in daylight; person visible | Closely resembles a consumer angler catch submission and replaces controlled-reference bias | Unique SHA/source/session/individual and photographer; no pHash candidate or controlling group outside TEST | **APPROVE_FOR_LOCKED_TEST** |
| `pilot-b1-wc-107176216` | POSITIVE | *Alectis ciliaris* | [Wikimedia item 107176216](https://commons.wikimedia.org/wiki/?curid=107176216) | `alectis-noaa-reef1701` / `session-alectis-noaa-reef1701` | Adult full fish in natural underwater conditions | Adds a different supported species, source, and natural context without using the rejected aquarium/controlled Alectis images | Unique SHA/source/session/individual and photographer; no pHash candidate or controlling group outside TEST | **APPROVE_FOR_LOCKED_TEST** |
| `pilot-ood-b1-wc-12104762` | OOD | `shark_ray` | [Wikimedia/NOAA item 12104762](https://commons.wikimedia.org/wiki/?curid=12104762) | `source-ood-wc-12104762` / `session-ood-wc-12104762` | Nurse shark under a natural reef ledge | Supplies fish-like OOD pressure close to classifier morphology rather than an easy scene-only negative | Unique SHA/source/session/individual; no related group or pHash candidate outside TEST | **APPROVE_FOR_LOCKED_TEST** |
| `pilot-ood-b1-wc-92100670` | OOD | `beach_water` | [Wikimedia item 92100670](https://commons.wikimedia.org/wiki/?curid=92100670) | `source-ood-wc-92100670` / `session-ood-wc-92100670` | Santa Isabel, Puerto Rico beach/water scene | Adds locally relevant non-fish context and category diversity instead of a second boat | Unique SHA/source/session; `individualFishGroupId` is not applicable; no pHash candidate outside TEST | **APPROVE_FOR_LOCKED_TEST** |

The revised set remains a four-image pipeline benchmark, not a statistical accuracy set. It is materially closer to intended use because it combines an angler catch, another supported species in natural conditions, a fish-like unsupported animal, and a Puerto Rico non-fish scene.

## Allowed decisions

For every row, record exactly one:

- `APPROVE_FOR_LOCKED_TEST`
- `MOVE_TO_VALIDATION`
- `MOVE_TO_TRAIN`
- `REJECT_FROM_SNAPSHOT`

Record the same decision in `datasets/fish_identifier/pilot_testset_approvals.csv` with reviewer and review date.

## Reviewer checks

Edgar should confirm the source item and displayed image, rights/category/species status, source/session independence, intended variation value, and whether the combined four-row composition is acceptable for a **pipeline pilot** locked test. Moving a row requires rerunning group-level leakage validation and produces a new proposed composition before locking.

Edgar Rivera approved all four revised rows for locked TEST on 2026-08-14. The historical first-composition decisions remain preserved above. Canonical approval metadata and the locked snapshot artifacts are authoritative for publication state.
