# Fish Identifier Pilot Rights-Review SOP

**Status:** Operational draft for pilot preparation

**Legal status:** Policy/design only. Source allowlists and this SOP require legal approval before image acquisition or contributor collection.

## 1. Purpose and boundary

This SOP decides whether a discovered candidate has sufficiently documented rights for commercial PescaPR machine-learning development. It does not decide species identity, technical quality, duplicate grouping, or partition placement. Rights approval is necessary but never sufficient for ingestion.

`datasets/fish_identifier/pilot_candidate_review.csv` is the pre-ingestion queue. It contains candidate/review facts only; no candidate becomes approved dataset metadata until rights, label/category, expert (when required), quality, and dataset approvals are complete.

Allowed rights results are exactly:

- `APPROVED`: evidence supports the intended commercial ML, derivative, storage, and evaluation use.
- `PENDING`: evidence or required review is missing, ambiguous, or awaiting legal/rights-holder confirmation.
- `REJECTED`: source/terms prohibit use, evidence is insufficient after review, or permission was denied.

Blank means PENDING. A reviewer must never infer approval from downloadability or continued app/site availability.

## 2. Rights-review procedure

### A. Discovery intake

The Discovery Reviewer records:

- stable `candidateId` and `workflowStatus=DISCOVERED`;
- source tier/type, source name, item ID, and canonical item/page URL;
- proposed creator, scientific species/frozen ID or OOD category;
- displayed license/rights statement and its link;
- no image bytes in approved storage and no final metadata row.

Search engines may locate a source page but are never recorded as the source. If the original item cannot be found, reject or leave pending.

### B. Establish creator and rights holder

The Rights Reviewer identifies the photographer/creator, uploader, commissioning organization, and rights holder where possible. Government, museum, Wikimedia, or cloud hosting does not prove that the host created or owns the media. Contractor, partner, contest, user-submitted, and credited third-party material require their own rights evidence.

### C. Record exact governing terms

Capture:

- exact license name and version;
- license/terms URL and source item URL;
- whether commercial use is allowed;
- whether copying, resizing, cropping, augmentation, transformation, feature extraction, and ML training are allowed;
- attribution, change-notice, share-alike, endorsement, trademark, privacy/publicity, or other obligations;
- platform/API terms that may restrict ML despite an image-level license;
- permission/release reference for private contributions;
- review date and named/role-based reviewer.

### D. Capture evidence

Minimum evidence is a durable item identifier plus a dated copy/reference of the item-specific rights statement and governing license/terms. Store private screenshots, permission messages, signed forms, and contributor identity in controlled evidence storage, not Git. Git metadata contains only an opaque evidence reference and sanitized facts.

Evidence must establish the chain from the candidate item to the applicable terms. A generic site footer is insufficient when individual items vary. A license keyword copied by an aggregator requires verification at the upstream publisher.

### E. Review third-party restrictions

Record unresolved recognizable people/minors, private property, trademarks/logos, sensitive location/EXIF data, culturally sensitive material, or third-party works/watermarks. `thirdPartyRightsWarning` must be blank or affirmatively resolved before approval. Rights approval cannot override privacy, publicity, safety, or ethical restrictions.

### F. Decide and hand off

The Rights Reviewer sets `rightsStatus` to APPROVED, PENDING, or REJECTED and records reviewer/date/reason. REJECTED candidates receive a rejection state/reason. APPROVED candidates move to label review but remain outside canonical metadata and partitions. The Dataset Approver later confirms all independent gates before `INGESTED`.

## 3. Evidence standards

### Sufficient when complete

- PescaPR ownership supported by original/custody and chain-of-title records.
- Signed/versioned contributor permission explicitly covering commercial ML and derivatives, plus required privacy/likeness records.
- Item-specific federal creator/public-domain evidence with no third-party credit/restriction.
- Smithsonian item explicitly designated CC0 on the item record.
- Wikimedia original file page showing an allowed license, creator/source/provenance, and no unresolved warning, with license obligations captured.
- Direct rights-holder permission identifying the exact work and intended use.

### Insufficient by itself

- public URL, downloadable file, search result, cached copy, or image CDN/Storage URL;
- inclusion in the current app or Guía Oficial;
- a filename, watermark, common-name caption, or uploader assertion without provenance;
- “copyright free,” “free image,” “educational use,” or similar informal language;
- a repository paper/code license applied by assumption to embedded images;
- aggregator license metadata without upstream verification;
- absence of a copyright notice.

## 4. Source-specific pilot rules

| Source | Pilot rule | Default outcome |
|---|---|---|
| PescaPR-owned original | Verify creator/chain of title, original custody, people/property/privacy, and intended ML use | PENDING until documented; then APPROVED |
| Permissioned contributor | Use only counsel-approved separate ML permission version with affirmative consent and evidence | PENDING until legal template and item consent pass |
| NOAA/federal media | Verify the specific image was created by a federal employee/agency and is public domain; reject/seek permission for credited partner/contractor/third-party works | Item-level decision |
| Smithsonian | Accept only item records explicitly marked CC0; review third-party restrictions | Item-level decision |
| Wikimedia Commons | Review original file page, creator, provenance, item license, warnings, source terms, and attribution | Tier 2; item-level decision |
| Other public-domain/CC0/CC BY | Verify original rights holder/source and exact terms; CC BY attribution/change obligations must be operational | Tier 2; item-level decision |
| Current Guía images | Rights remain unknown | REJECTED for pilot ingestion |
| iNaturalist | Current commercial AI/ML restriction controls | REJECTED |
| Search/social media | Discovery is not permission; original source/direct permission required | REJECTED as source |
| FishBase | Contributor-specific rights; no blanket approval | REJECTED absent specific permission |
| Research dataset | Dataset/paper availability is insufficient; exact image-level commercial ML rights required | PENDING or REJECTED |
| NC, ND, editorial-only, all-rights-reserved, unclear | Incompatible or insufficient for default pilot | REJECTED absent separate permission |
| Ordinary PescaPR upload | Not a dedicated ML contribution | REJECTED absent separate approved consent |

CC BY-SA remains outside the pilot's default allowlist pending legal review of share-alike/model/dataset obligations.

## 5. Reviewer roles and separation of duties

### Discovery Reviewer

Finds candidate records and captures source metadata. May flag an obvious forbidden source but cannot approve rights or final ingestion. Should not approve its own enhanced rights decision.

### Rights Reviewer

Evaluates rights, terms, creator/holder, third-party restrictions, evidence, and attribution. Must be independent from species-label approval. Escalates novel licenses, ambiguous federal authorship, contributor-language questions, or conflicts to legal review.

### Species Label Reviewer

Verifies scientific identity, visible/source evidence, and exact frozen `FichaPez.id`. Does not approve rights merely because a source is authoritative.

### Expert Species Reviewer

Provides the additional independent label review required for HIGH/VERY_HIGH confusion cases and adjudicates uncertainty. An unresolved disagreement is PENDING/REJECTED, not averaged away.

### Dataset Approver

Checks that source type, rights, label/category, expert review, quality, evidence references, and workflow state all pass before authorizing ingestion. May be the Rights Reviewer or Label Reviewer in a small project only for LOW/MEDIUM routine cases when the other gate was independently completed; cannot be the sole discoverer, rights reviewer, label reviewer, and final approver for one candidate.

Practical pilot separation: at least two people/independent review acts per positive candidate—one rights decision and one label decision. HIGH/VERY_HIGH needs the planned second label review, so one person may hold multiple project roles but must not collapse independent decisions into a single unchecked action.

## 6. Label approval by pilot risk

| Pilot species/risk | Label requirement | Expert status |
|---|---|---|
| *Coryphaena hippurus* — LOW | One competent reviewer when evidence is strong; escalate uncertainty | Not normally required |
| *Alectis ciliaris* — MEDIUM | One competent reviewer for clear adults; second review for juvenile/weak source evidence | Conditional |
| *Scomberomorus cavalla* — HIGH | Two independent label reviews, one competent in mackerel/wahoo distinctions | Required complete |
| *Seriola dumerili* — HIGH | Two independent label reviews, one competent in amberjack distinctions | Required complete |
| *Sphyraena barracuda* — VERY_HIGH | Two independent reviews including a qualified barracuda expert | Required complete |

Rights approval is independent at every risk level. Strong species evidence does not cure weak rights, and strong rights do not cure an uncertain label.

## 7. Pilot decision matrix

| Source permitted | `rightsStatus` | `labelStatus` | Expert requirement | Third-party warning | Candidate result |
|---|---|---|---|---|---|
| Yes | APPROVED | APPROVED | Complete or not required | Resolved/none | Dataset Approver may set `candidateDecision=APPROVED_FOR_INGESTION` |
| Yes | PENDING | Any | Any | Any | Remain PENDING; no ingestion |
| Yes | APPROVED | PENDING | Any | Any | Remain PENDING; no ingestion |
| Yes | APPROVED | APPROVED | Required but incomplete | Any | Remain PENDING; no ingestion |
| Yes | APPROVED | APPROVED | Complete | Unresolved | Remain PENDING or REJECTED; no ingestion |
| No | Any | Any | Any | Any | REJECT_UNSUPPORTED_SOURCE |
| Any | REJECTED | Any | Any | Any | REJECT_RIGHTS |
| Any | Any | REJECTED | Any | Any | REJECT_LABEL |

Only `APPROVED_FOR_INGESTION` may create a new row in `pilot_metadata.csv`, initially `datasetPartition=UNASSIGNED` and `partitionLockStatus=UNLOCKED`. Discovery never writes directly to TRAIN, VALIDATION, or TEST.

## 8. Controlled binary workspace

The normal repository is the control plane, not the image store. The exact external root remains configurable; a conceptual local/external layout is:

```text
PescaPR-Datasets/
  fish_identifier/pilot/
    raw/          # immutable acquired originals
    approved/     # approved snapshot materialization/object references
    quarantine/   # pending/rejected files with restricted access
    evidence/     # licenses, permissions, dated source evidence; private
    snapshots/    # immutable binary snapshots and integrity manifests
    tmp/           # disposable download/hash/contact-sheet work
```

Controls before acquisition:

- choose a root outside the Git repository and record it through a local, untracked configuration mechanism;
- restrict access to reviewers who need the content; evidence/PII access is narrower than image access;
- encrypt storage/backups where appropriate and define retention/deletion/incident procedures;
- preserve original bytes read-only after ingestion; address objects by internal ID/checksum;
- never embed access tokens, signed URLs, contributor identity, or private evidence in Git metadata;
- back up immutable snapshots and evidence separately from disposable caches;
- record sanitized checksums, snapshot manifests, tooling, and reports in Git when safe.

Repository content may include CSV schemas/metadata, tools, checksums, sanitized reports, and snapshot manifests. It must not contain raw/quarantine binaries, PII, private permission evidence, credentials, or large mutable datasets.

## 9. Git/storage enforcement

Narrow `.gitignore` rules protect only the designated repository-local emergency/temporary pilot paths: `raw`, `quarantine`, `private_evidence`, `download_cache`, `tmp`, generated contact sheets/duplicate-analysis caches, and common image extensions under `approved`. Canonical CSVs, fixtures, docs, scripts, and sanitized reports remain visible to Git.

Ignore rules are a last defense, not authorization to use the repository as binary storage. The external controlled workspace remains the required destination.

## 10. Review audit and changes

Every SOP/license allowlist change receives a document version/date and prospective applicability review. Previously approved images must be re-reviewed when source terms, provenance, permission scope, or third-party facts materially change. Preserve rejection and removal audit records without retaining unnecessary binary/PII content.

## 11. Next operational action

Obtain legal approval for contributor language and the pilot source/license policy; assign people to the operational roles; approve the external workspace and evidence controls. Then begin metadata-only discovery by adding real candidate facts—no image bytes—to `pilot_candidate_review.csv` with `workflowStatus=DISCOVERED` and `rightsStatus=PENDING`.
