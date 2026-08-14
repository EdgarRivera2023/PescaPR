# Fish Identifier Pilot — Grouping Review

**Review date:** 2026-08-14
**Rows reviewed:** 22
**Result:** No evidence-supported group change and no controlling-group leakage.

## Findings

- All SHA-256 values are unique; no positive/OOD pHash pair is within distance 10.
- The two JaredMcKenzie gear images are distinct source items but share a creator, adjacent source identifiers, similar object/session context, and pHash distance 8. Their shared source/session groups and common TRAIN assignment are required and retained.
- FDA credits two images: `pilot-b1-wc-152817751` (TEST Coryphaena) and `pilot-b1-wc-152817762` (TRAIN Alectis). They are separate numbered source items, species, and documented sessions. This supports distinct asset/session groups, but the common collection is disclosed as a locked-test source-style risk rather than silently merged without session evidence.
- Every other photographer appears once. No source page, source item, hash, pHash evidence, named session, or individual connects those singleton groups.
- Every original retains its own derivative group because no acquired row is established as a crop, resize, mirror, or re-encode of another acquired row.
- Fish-like positives and OOD fish/shark records have individual-fish groups. Non-fish OOD scenes/objects deliberately leave `individualFishGroupId` blank.
- Public-source creators are recorded as photographers/authors, not fabricated as PescaPR contributors.
- Edgar's first-composition moves and the four revised TEST proposals were applied as whole-group
  overrides. The revised TEST rows have four distinct source/session components, and none shares a
  derivative, source, session, individual, exact hash, or reviewed pHash relationship with a row
  left in TRAIN or VALIDATION.

## Row-level decision inventory

| Rows | Grouping decision |
|---|---|
| `pilot-ood-b1-wc-181803762`, `pilot-ood-b1-wc-181803763` | Shared source/session family; separate derivative groups; same TRAIN component. |
| `pilot-b1-wc-152817751`, `pilot-b1-wc-152817762` | Distinct asset/session/individual groups retained; common FDA collection documented as a source-style relationship. |
| Remaining 18 rows | Evidence-supported singleton derivative/source/session groups retained; fish-individual group populated only where applicable. |

The grouping validator confirms that no derivative, source, session, or individual group crosses TRAIN, VALIDATION, and TEST. Any future provenance or binary evidence connecting singleton rows requires regrouping and a new snapshot proposal; published snapshots must never be edited silently.

The FDA rows are now VALIDATION and TRAIN respectively after Edgar rejected the first TEST
composition. Revised TEST consists of `pilot-b1-wc-1734232`, `pilot-b1-wc-107176216`,
`pilot-ood-b1-wc-12104762`, and `pilot-ood-b1-wc-92100670`; all remain UNLOCKED pending review.
