# Fish Identifier dataset-unblock decision review

**Review date:** 2026-08-15  
**Scope:** repository evidence only; no new discovery, acquisition, outreach, or image handling  
**Decision status:** FI-MICRO-POC.2 remains blocked

## 1. Current blocker summary

PescaPR has architecture, evaluation, and training metadata contracts, but it does not have a
legitimate approved training snapshot. The three-class micro-POC is still the smallest credible first
training experiment, but its required gate remains **10 accepted independent originals per class**
(12–15 preferred). No micro-POC candidate is currently approved for acquisition.

The blockers are not one generic shortage:

- **Rights:** five useful *Haemulon plumieri* rescue records and all preliminary *Ocyurus*/*Lactophrys*
  queues lack item-level reusable training permission or final rights adjudication.
- **Label/specialist:** *Ocyurus chrysurus* and *Lactophrys triqueter* require enhanced review;
  the *Haemulon* queue has three unresolved label records, but they do not currently add an
  independent rights-qualified group.
- **Independence/grouping:** correlated alternates are retained conservatively and do not count as
  separate groups.
- **Source volume:** the one-time *Haemulon* public/open rescue produced six useful records but zero
  new qualified groups. The POC v2 census likewise found no five-class path to its 30-original gate.
- **Quality:** some records are illustrations, plated food, or too low resolution and cannot be
  rescued by rights or label approval.
- **Legal FI-CONTRIB:** future PescaPR-contributed imagery could solve source volume, but FI-CONTRIB.1
  still blocks operational collection and the provisional permission draft cannot be used.

## 2. Micro-POC gap table

Counts below distinguish preliminary independence-adjusted supply from actually approved groups.
Only approved rights, label, quality, and grouping decisions count toward the experiment gate.

| Class | Preliminary independent groups | Approved groups | Minimum / preferred | Numerical gap to minimum | Main blocker and best existing path |
|---|---:|---:|---:|---:|---|
| *Haemulon plumieri* | 7 qualified | 0 acquired/approved | 10 / 12–15 | **3** (5–8 preferred) | Rights. Obtain explicit permission for at least three already-recorded independent field/source groups, then complete rights, label, quality, and grouping approval. |
| *Ocyurus chrysurus* | 15 adjusted candidates | 0 | 10 / 12–15 | 0 numerically; **10 approval-ready groups still required** | Rights and item-level review. Adjudicate the existing 17-record queue before any new search; enhanced review must confirm clear adult labels and grouping. |
| *Lactophrys triqueter* | 13 adjusted candidates | 0 | 10 / 12–15 | 0 numerically; **10 approval-ready groups still required** | Rights, quality/context, and grouping. Adjudicate the existing 21-record queue; exclude the 25 iNaturalist-origin records and resolve enhanced boxfish review. |

The tracker’s 15 and 13 figures are feasibility counts, not approved training data. Thus the
micro-POC cannot proceed merely because two classes clear a preliminary numerical count.

### Haemulon remaining records

The five rescue records with usable field/underwater context are independently grouped but rights
pending: `microhae-rescue-nccos-sybert-2019`, `microhae-rescue-ufifas-hook-line-2025`,
`microhae-rescue-mexfish-bomeisler-2025`, `microhae-rescue-mexfish-golder-2022`, and
`microhae-rescue-mexfish-kimberly-2014`. The SCDNR record is all-rights-reserved and the former
FishBase item is CC BY-NC, so neither counts under the approved policy. Three additional
*Haemulon* records are label-pending or quality/rights blocked; resolving them does not presently
close the three-group gap because they are not independent rights-qualified groups.

## 3. Existing permission-only opportunities

The repository records exactly five *Haemulon* permission routes. They remain zero toward the gate:

| Recorded opportunity | Evidence in repository | Potential groups | Current state | Decision relevance |
|---|---|---:|---|---|
| Mexican Fish contributor network | 13+ named-photographer/location/date records | 10+ | Permission candidate | Strongest route; could exceed the three-group gap if authority and scope are confirmed. |
| UF/IFAS Extension Pasco County | 3 recent hook-and-line field images | 2–3 | Permission candidate | Useful field context; creator/rightsholder and grant must be confirmed. |
| Smithsonian STRI Shorefishes contributors | 20 species-gallery images | Multiple | Written permission required | Potentially useful diversity, but institutional terms do not grant commercial ML reuse. |
| Florida Museum Discover Fishes photographers | 4+ named-photographer records | 4+ | No compatible open grant found | Potential supplemental groups if direct rights are obtained. |
| Reef Life Survey photographers | 6 records across locations/contributors | Multiple | Direct confirmation required | Potential natural-scene diversity; no count until permission and grouping are documented. |

Any direct grant would need, at minimum, explicit authority from the rights holder and a legally
approved scope covering non-exclusive storage, reproduction, technical preprocessing/derivatives,
training, fine-tuning, validation, testing, evaluation, model improvement, controlled snapshot use,
and distribution/operation of PescaPR model artifacts. Attribution, people/property/privacy issues,
withdrawal and retention treatment must be recorded. Public display and marketing are separate and
must not be inferred from ML permission. The repository contains no private contact information and
no outreach has occurred.

## 4. Existing specialist and review opportunities

The broader five-species queue contains already-discovered reviews that should precede new search:

- *Scomberomorus cavalla*: 5 candidates await qualified second review.
- *Seriola dumerili*: 13 candidates await qualified amberjack second review.
- *Sphyraena barracuda*: 13 candidates await two reviews including a qualified barracuda expert.

Resolving these reviews could convert existing records into reviewable candidates, but it cannot
close the broader 50-per-species development target by itself: acquired positives remain 5, 3, 0, 0,
and 0, with the documented **242-image** shortfall. It also does not close the micro-*Haemulon*
rights gap.

For the micro-POC, *Ocyurus* and *Lactophrys triqueter* have enhanced-review requirements, but their
queues are still preliminary rights/label records rather than a specialist-only near-ready batch.
The most valuable review work is therefore existing item-level adjudication after rights evidence is
resolved, not another discovery strategy.

## 5. Ranked unblock actions

1. **Make a legal/product decision to pursue direct permission for at least three already-recorded
   independent *Haemulon* groups**, prioritizing the Mexican Fish field records and the UF/IFAS/other
   named opportunities that can document authority. Use only an approved direct-permission instrument;
   do not use the provisional contributor draft.
2. For any permission that is obtained, complete item-level rights evidence, enhanced label review,
   quality review, conservative source/session grouping, and independent Dataset Approver review.
3. In parallel, adjudicate the existing *Ocyurus* and *Lactophrys triqueter* queues from the POC v2
   shortlist. Treat their 15/13 counts as upper-bound feasibility evidence, not approvals.
4. Recompute approved independent groups. Only after the micro gate is met should a small approved
   acquisition packet and TRAIN/VALIDATION snapshot be considered.
5. Continue the already-known broader specialist queues separately; do not let them obscure the
   smaller micro-POC route or imply that the 242-image shortfall is closed.

## 6. Stop/go gates

- **GO to approved micro acquisition preparation:** all three classes have at least 10 approved,
  independent, quality-accepted originals, with rights evidence and required label reviews complete.
- **Haemulon-specific GO:** at least three new independent permission records convert the class from
  7 to at least 10, then pass all remaining review and grouping gates.
- **NO_GO:** if the existing *Haemulon* permission routes cannot produce three approved independent
  groups, or if *Ocyurus*/*Lactophrys* remain below 10 approved groups, the micro-POC remains blocked.
  The 10-group minimum must not be lowered.
- **Broader subset:** remains not ready until its own acquisition, specialist, diversity, and
  50-per-species evidence gates are met; its 242-image shortfall remains unchanged.

## 7. Discovery and FI-CONTRIB decision

The repository evidence does **not** justify another broad discovery campaign now. The documented
next move is to resolve existing permission routes and existing review queues first. If those fail,
a new discovery or contributor-data strategy would require a separately approved decision; this
review does not initiate it.

FI-CONTRIB remains a possible long-term source of rights-cleared field photos, but FI-CONTRIB.1 still
blocks operational collection, consent UI, upload, persistence, and production use. No contributor
path may be used to bypass that legal/privacy gate.

