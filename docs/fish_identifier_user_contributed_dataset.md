# Fish Identifier user-contributed training dataset architecture

**Date:** 2026-08-14
**Status:** PLANNING ONLY — NO COLLECTION AUTHORIZED

## 1. Purpose and strategic boundary

PescaPR's long-term 39-class classifier should be built primarily from voluntarily contributed,
Puerto Rico-specific angler photographs, supplemented where useful by PescaPR-owned, explicitly
permissioned partner, public-domain, and compatible open-license images. This strategy does not
replace the frozen 39-class catalog, the technical micro-POC, or existing dataset governance.

Three grants must remain separate:

1. **Personal app use:** capture, save, sync, and view a user's own photo under the existing catch
   or private-journal behavior.
2. **ML contribution:** an affirmative, versioned grant for dataset preparation, model development,
   training, validation, testing, improvement, and operation.
3. **Public display:** a separate optional grant for Guía Oficial, community, marketing, or other
   public display. ML consent must not imply public-display or marketing permission.

The Fish Identifier remains intended as a free PescaPR feature. Contribution is optional and
declining must not reduce ordinary feature access.

## 2. Existing architecture and reuse boundary

Current components provide patterns, not sufficient ML consent:

- `IdentificadorScreen.kt` and `MapaPescapr.kt` already capture/select photos; the future flow can
  reuse their entry points conceptually without making camera/storage permission into consent.
- Catch photos are saved locally as JPEGs through `ImageStorage.kt`; `RecordPesca.fishId` already
  supports a canonical species ID. `CatchLog.isPrivate` defaults true, and catch/environmental
  coordinates demonstrate why exact location must not be copied into training metadata by default.
- `SpotPhotoRepository` uses Firebase Auth (including anonymous accounts), Firebase Storage,
  Firestore submission records, upload rollback, pending withdrawal, and admin approve/reject
  operations. `PhotoSubmissionStatus` provides a useful moderation pattern.
- Community spot photos are compressed and uploaded under spot-specific paths, and approval makes
  them displayable. That workflow does **not** grant ML rights and must not be repurposed silently.
- `AdminPhotoModerationDialog` demonstrates a small admin review surface, but training review needs
  consent, label, privacy, duplicate, grouping, and audit controls that it does not currently have.
- `OfficialGuideRepository`, bundled `oficial_guide.json`, and `FichaPez.id` remain the
  authoritative catalog/machine-label source. User-entered common names are never training truth.

Anonymous authentication may be adequate for app access, but counsel/product must decide whether
it provides enough durable identity and withdrawal verification for contribution consent.

## 3. Contribution principles

1. Contribution is voluntary and separately opted in.
2. Camera, storage, catch logging, community upload, or account creation is not ML consent.
3. ML consent uses an affirmative, unbundled control; no preselection or dark pattern.
4. The contributor retains ownership unless a future counsel-approved agreement says otherwise;
   no copyright transfer is currently intended.
5. PescaPR needs a sufficiently broad, non-exclusive license for approved ML purposes.
6. Public display and any future marketing use are independent grants, defaulting false.
7. The contributor-selected `FichaPez.id` is a provisional assertion, not ground truth.
8. Only rights-, privacy-, quality-, label-, and dataset-approved samples become training-eligible.
9. The exact consent text/version, locale, UI version, and acceptance event are retained.
10. Provenance must remain auditable from submission through dataset snapshot and training run.

## 4. Future user workflow

### Capture and personal use

The user captures/selects a photo and may save it in the existing personal catch workflow. That
personal object remains governed by existing private/sync behavior and is not copied into training
storage automatically.

### Provisional manual identification

The user selects a Guía Oficial entry. The contribution record stores the exact canonical
`FichaPez.id` plus catalog version; display strings are convenience metadata only.

### Separate contribution invitation

After a photo and canonical selection exist, PescaPR may invite the user to help improve its free
Fish Identifier, clearly disclosing ML uses. Conceptual choices are `Contribute photo` and
`No thanks`. Final Spanish/English copy and control design require legal review. Declining ends the
contribution path without changing the personal photo.

### Separate optional display choices

If product needs public display, request it independently. Store separate booleans/grant records
for ML training, public/community display, and any future marketing use. Withdrawing one grant
must not be interpreted as withdrawing or granting another.

### Submission and review

Create a contributed copy/object only after valid consent. It enters quarantine, receives hashes
and privacy/quality flags, and is reviewed. The user's species choice remains provisional until an
authorized reviewer assigns the approved canonical ID. Only a final dataset approval makes the
sanitized asset eligible for an export snapshot.

## 5. Consent/version contract

A stable version such as `fish_training_consent_v1` should bind:

- `consentRecordId`, `consentVersion`, locale, rendered-text checksum, UI/app version;
- contributor account/identity reference, acceptance timestamp, and evidence/audit event;
- ownership/authority declaration and third-party-content disclosures;
- `mlTrainingAllowed`, `publicDisplayAllowed`, and `marketingUseAllowed` as independent grants;
- intended license scope and attribution preference;
- withdrawal timestamp/status and the policy version applied to the request;
- submission IDs covered by the grant—avoid ambiguous account-wide blanket consent.

Consent text must be immutable by version. Wording changes create a new version plus a legal
decision about whether old submissions remain eligible or need renewed consent.

## 6. Contribution lifecycle

| State | Meaning | Permitted actor/transition |
|---|---|---|
| `DRAFT` | Local/incomplete intent; no upload or ML grant relied upon | User may discard or submit |
| `SUBMITTED` | Consent evidence and quarantined object received | System atomically records intake |
| `RIGHTS_PENDING` | Ownership, grant, or third-party rights need review | Rights reviewer decides/escalates |
| `PRIVACY_PENDING` | EXIF/sensitive visible content review incomplete | Privacy reviewer/system flags |
| `LABEL_PENDING` | Contributor label awaits canonical review | Label reviewer approves/corrects/escalates |
| `SPECIALIST_PENDING` | Confusion risk requires qualified review | Specialist resolves or rejects |
| `QUALITY_PENDING` | Usefulness, corruption, manipulation, or context unresolved | Dataset reviewer decides |
| `DATASET_REVIEW` | Independent gates passed; final ingestion decision pending | Dataset Approver only |
| `APPROVED` | Sanitized asset is eligible for future snapshots | Dataset Approver |
| `REJECTED` | Failed rights, label, privacy, quality, source, or abuse gate | Reviewer records category/reason |
| `WITHDRAWAL_REQUESTED` | Verified removal/exclusion workflow pending | User/support plus authorized processor |
| `WITHDRAWN` | Excluded from future eligible snapshots under approved policy | Authorized processor records actions |
| `EXCLUDED` | Previously approved image is no longer eligible | Admin/system, with reason and audit event |
| `ARCHIVED` | Minimal non-training audit record retained under policy | Retention process only |

No single mutable status should erase history. Append-only review/audit events record transitions,
actors, timestamps, prior/new values, and reasons.

## 7. Label and moderation governance

The contributor's `selectedFichaPezId` is a candidate label. Reviewers compare the image with
Guía Oficial and source/capture evidence, then set `approvedFichaPezId` independently.

- Clear normal-risk species: one competent label review.
- Known confusion groups or ambiguous life stages: two reviews or enhanced review.
- Defined high-risk cases: qualified specialist review.
- Disagreement or insufficient diagnostic visibility: pending or rejected, never majority-guessed.

Rights approval is independent from label approval. For a small team, one person may hold multiple
roles, but the discoverer/uploader must not be the sole rights, label, and final Dataset Approver.

A future Admin/Master tool should show the image; contributor and proposed canonical labels; Guía
comparison; scientific/common names; consent/grant state; ownership declaration; privacy and abuse
flags; hashes/near duplicates; related session/individual groups; similar approved samples; and
complete review history. Actions include correct/approve label, request specialist review, resolve
privacy, mark duplicate/correlation, reject with typed reason, and approve for training.

## 8. Logical data entities

### `training_photo_submission`

- identity: `submissionId`, `userId`, created/updated timestamps, app/schema version;
- objects: private-photo reference if permitted, quarantine object ID/path, sanitized asset ID;
- labels: `selectedFichaPezId`, `approvedFichaPezId`, catalog/manifest version;
- grants: `consentRecordId`, consent version/checksum, independent ML/display/marketing flags,
  ownership confirmed, attribution preference;
- lifecycle: typed status, status reason, exclusion/withdrawal state;
- privacy/quality: EXIF sanitation state, privacy flags, quality/context annotations;
- identity controls: SHA-256, pHash, derivative/source/session/individual/contributor group IDs;
- review summary: required review level, reviewer IDs, decisions/timestamps, final approver;
- provenance: original filename only when needed, capture/acquisition timestamp rounded/minimized,
  source type, evidence references, snapshot inclusion pointers.

### `contributor_consent`

Versioned affirmative-consent evidence, independent grants, text checksum, identity reference,
ownership declarations, disclosures, locale/UI version, acceptance and withdrawal events. Private
evidence is access-restricted and never exported into training metadata or Git.

### `training_photo_review`

Append-only review events containing review type, actor/role, input label, proposed/approved label,
decision, reason codes, privacy/quality flags, timestamp, and supersession link.

### `training_asset`

Sanitized approved derivative identity, checksum, transformation recipe/version, parent submission,
eligibility state, grouping IDs, annotations, attribution obligations, and snapshot membership.

### `dataset_snapshot_membership` / `model_training_run`

Immutable snapshot ID/checksum and member asset IDs; training run binds snapshot, split, model,
preprocessing, manifest, code/environment, prediction artifacts, and exported-model checksums.

These are logical contracts only; no Firestore collection or migration is created by this plan.

## 9. Storage separation and privacy sanitation

Use distinct access domains and object identities:

1. **Personal/private photo:** existing local/private catch behavior; not dataset storage.
2. **Quarantined contributed original:** immutable original bytes, restricted reviewer access,
   never public, with consent/evidence reference.
3. **Sanitized approved training derivative:** EXIF removed, deterministic transformation record,
   privacy issues resolved, content-addressed/checksummed.
4. **Public-display derivative:** created only under separate display permission, with independent
   revocation and moderation behavior.
5. **ML export workspace:** controlled external dataset snapshots; metadata and sanitized manifests
   may be versioned in Git, but training binaries and PII remain outside normal Git/app stores.

Before training eligibility, strip EXIF GPS and unnecessary device/owner metadata. Do not retain
exact fishing coordinates unless a separately approved purpose and disclosure require them; use
coarse/non-identifying context if useful. Flag visible faces, minors, plates, boat registrations,
documents/screens, addresses, or other identifiers. Depending on approved policy, reject, crop,
redact into a traceable derivative, or obtain releases. Automated detection may assist later but
does not replace human review and is not required for the first foundation slice.

## 10. Duplicate, correlation, and quality controls

Apply the existing dataset framework: SHA-256 exact duplicate detection, 64-bit pHash review,
derivative/source/session/individual-fish/contributor grouping, and group-safe partitioning.
Multiple photos of one catch may be accepted, but count conservatively as one independent group
and never cross TRAIN/VALIDATION/TEST controlling boundaries.

Reviewer/system annotations may include pose, whole/head/tail visibility, occlusion, blur,
lighting, day/night, underwater, caught/held, boat, shore, cooler/container, juvenile/adult when
confident, damage/cleaning, and camera/compression characteristics. Users should not be burdened
with filling most of these fields.

## 11. Abuse and rejection taxonomy

Typed rejection/exclusion reasons should cover:

- `SPAM_OR_RATE_LIMIT`, `UNRELATED_CONTENT`, `NON_FISH_OR_UNSUPPORTED`;
- `INTENTIONALLY_OR_INCORRECTLY_LABELED`, `LABEL_UNRESOLVED`;
- `EXACT_DUPLICATE`, `NEAR_DUPLICATE`, `CORRELATED_SEQUENCE`;
- `THIRD_PARTY_COPYRIGHT`, `OWNERSHIP_UNPROVEN`, `CONSENT_INVALID`;
- `PRIVACY_FACE_OR_MINOR`, `PRIVACY_IDENTIFIER`, `SENSITIVE_CONTENT`;
- `SCREENSHOT_OR_REPRODUCTION`, `GENERATED_OR_AI_IMAGE`, `MANIPULATED_IMAGE`;
- `LOW_QUALITY`, `TARGET_NOT_VISIBLE`, `SEVERELY_CLEANED_OR_DAMAGED`;
- `MALICIOUS_OR_ILLEGAL_CONTENT`, `SECURITY_QUARANTINE`.

Use upload size/type limits, account/device rate controls, malware/decoder safety, duplicate checks,
review queues, and role-based access. A contributor assertion never overrides rights or safety
review.

## 12. Withdrawal/deletion decisions requiring legal review

Engineering must preserve traceability and support exclusion, but legal/product must decide before
collection:

- whether and how contributors withdraw future ML permission;
- identity/authority verification for requests;
- deletion timing for quarantine originals, approved derivatives, backups, and public copies;
- whether frozen snapshots are rebuilt, tombstoned, or retained but barred from future runs;
- whether already-trained/unreleased or released model weights may remain in use;
- whether future models are prohibited from using withdrawn assets;
- retention period and lawful basis for consent evidence, checksums, decisions, and deletion logs;
- treatment of installed/offline models and legal preservation obligations.

Do not promise machine unlearning or total deletion until the policy and technical guarantees are
approved. Snapshot membership and training-run lineage must make future exclusion/retraining
decisions technically possible.

## 13. Children and contributor eligibility

Legal/privacy review must define contributor eligibility, age requirements, parental/guardian
consent if applicable, treatment of incidental minors in images, and whether anonymous accounts
can form the required grant. This plan does not invent an age threshold. Collection must remain
disabled until these decisions and enforcement mechanisms exist.

## 14. Provenance and metrics

For every approved image, the system must prove contributor/source, consent version, acceptance
time, ownership declaration, canonical approved label, reviewers, eligibility, sanitation recipe,
hashes/groups, snapshot memberships, and model runs that consumed those snapshots. Withdrawal or
exclusion events propagate prospectively through this lineage without rewriting history.

Admin/product metrics should include submitted/approved/rejected/withdrawn counts; approval rate;
approved images and independent groups per `FichaPez.id`; contributor, session, field-context,
pose, lighting, life-stage, and device diversity; duplicate/correlation rates; review latency and
backlog; privacy/rights/quality rejection reasons; species below Stage 1/production targets; and
snapshot/model utilization. Metrics must minimize identity/location exposure and are not yet an
analytics implementation.

## 15. Relationship to current ML tracks

- **Technical micro-POC:** uses a tiny rights-reviewed public-source dataset now to prove training,
  export, canonical mapping, and FI-A.8 mechanics. It remains separate and non-production.
- **User-contributed dataset:** becomes the primary long-term acquisition strategy for authentic
  Puerto Rico angler photographs.
- **Production 39-class classifier:** waits for sufficiently reviewed, diverse data and may combine
  user contributions, PescaPR-owned images, permissioned partners, and compatible open/public data.

Existing public-source work remains useful for engineering, reference, supplementation, and
coverage gaps; it simply is not the primary production-volume strategy.

## 16. Rollout phases and collection gates

### Contributor Phase 1 — Foundation

Counsel-approved rights/privacy/age/withdrawal decisions; immutable consent/version contract;
logical schemas/statuses; threat model; retention/access/storage design. **Blocks all collection.**

### Contributor Phase 2 — Backend and capture opt-in

Create isolated backend/storage contracts and security rules, then implement explicit opt-in and
separate display grants. Run security/privacy tests before enabling uploads. No silent migration of
existing photos.

### Contributor Phase 3 — Moderation

Implement role-separated rights, privacy, label, specialist, quality, duplicate, and Dataset
Approver workflows with audit history and withdrawal intake.

### Contributor Phase 4 — Dataset export

Export only eligible sanitized assets into immutable group-safe dataset snapshots compatible with
existing validator, snapshot, metadata, and test-set discipline.

### Contributor Phase 5 — ML feedback loop

Bind training runs to snapshots, evaluate without TEST leakage, and surface per-species/diversity
coverage gaps back to contribution campaigns without pressuring users or exposing locations.

## 17. Smallest safe implementation slice

After legal/product decisions—not before—implement repository-only/backend-neutral contracts first:

1. versioned consent/grant schema with independent ML/display/marketing fields;
2. typed lifecycle/rejection/review enums and append-only audit-event schema;
3. submission/training-asset logical schemas using canonical `FichaPez.id`;
4. storage-boundary and access-control specification plus privacy/retention test cases;
5. validation fixtures proving no submission becomes training-eligible without consent, rights,
   label, privacy, quality, and Dataset Approver gates.

Do not create UI, upload endpoints, Firestore collections/rules, or accept photos in this slice.
It creates a reviewable contract before irreversible collection behavior.
