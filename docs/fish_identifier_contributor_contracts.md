# Fish Identifier contributor domain contracts

**Status:** IMPLEMENTED DOMAIN FOUNDATION — COLLECTION DISABLED
**Legal status:** Engineering contracts only. Consent language and operational policy still require
qualified legal, privacy, and product approval before implementation.

## Scope

The contracts under `com.bradmir.pescapr.data.contribution` describe future contributed training
data without connecting to UI, Firebase, Storage, Firestore, repositories, networking, or image
capture. They encode safety boundaries so later persistence cannot treat an ordinary personal or
public-display photo as training data.

## Rights and consent

`ContributionConsent` identifies the exact consent record and version accepted by a contributor.
It records acceptance time, ownership confirmation, ML permission, and optional public-display and
marketing grants independently. Public-display or marketing permission never implies ML permission.
A withdrawal timestamp makes the consent withdrawn regardless of its earlier grants.

No legal text is embedded in code. A future legally approved implementation must bind
`consentVersion` to immutable reviewed text and evidence.

## Lifecycle

`ContributionStatus` preserves distinct draft, submission, rights, privacy, label, quality,
specialist, dataset, approval, rejection, withdrawal, exclusion, and archive stages.
`ContributionStatusTransitions` permits the ordinary review flow and deliberate removal from an
approved state. It rejects silent restoration from `WITHDRAWN`, `EXCLUDED`, `REJECTED`, or
`ARCHIVED` to active review/approval. Any future restoration procedure requires a new explicitly
designed policy and audit event.

## Submission and training asset separation

`ContributionSubmission` represents a quarantined user contribution. Its contributor-selected
`provisionalFichaPezId` is separate from the reviewed `approvedFichaPezId`. It includes consent
references, typed review states, privacy findings, provenance/grouping IDs, and withdrawal or
exclusion facts.

`TrainingAsset` represents a sanitized, reviewed derivative. It points back to its submission but
uses a separate object reference and ID. It carries the approved canonical fish ID, SHA-256,
optional 64-bit perceptual hash, correlation groups, approval time, and current eligibility state.
Dataset export should consume only validated `TrainingAsset` records, never raw submissions.

## Privacy and review audit

Typed flags cover EXIF GPS, faces, possible minors, license plates, boat registrations, documents,
and other identifying content. Findings remain blocking while `OPEN`; the contract does not imply
automated detection.

`ContributionReviewRecord` captures reviewer, type, decision, time, reason, notes, and proposed
canonical-label corrections. `ContributionLifecycleEvent` is an immutable event-shaped record for
append-only history. Neither contract creates reviewers, roles, permissions, or an event store.

## Eligibility rules

`FishTrainingContributionRules` returns explicit validation errors and grants eligibility only when:

- the submission references the exact consent ID and version;
- ownership is confirmed and ML training is explicitly allowed;
- neither consent nor submission is withdrawn or excluded;
- the submission is `APPROVED`;
- rights, privacy, label, and quality reviews are approved;
- no privacy finding remains open;
- the approved `FichaPez.id` exists in the supplied frozen catalog;
- the sanitized asset points to the same submission and approved label;
- SHA-256 and required grouping IDs are valid; and
- the asset itself is `ELIGIBLE` without exclusion metadata.

Public-display and marketing grants are intentionally absent from these requirements.

## Provenance chain

The contracts support this traceable chain:

`ContributionConsent` → `ContributionSubmission` → review records and lifecycle events →
`TrainingAsset` → `DatasetSnapshotMembership` → `ModelTrainingRunProvenance`.

Snapshot membership records the content hash, canonical ID, partition, schema version, and
eligibility state at inclusion. A training run records the exact snapshot, model/preprocessing
versions, classifier-manifest checksum, timestamps, and output artifact identity.

## Deliberately unimplemented safety boundary

Collection remains impossible through this slice because there is:

- no UI or consent screen;
- no repository or use case invoking these contracts;
- no network, Firebase, Storage, or Firestore adapter;
- no collection/schema/security-rule change;
- no object upload or dataset export implementation; and
- no runtime feature flag or entry point to enable.

This structural absence is safer than a dormant runtime toggle that could accidentally expose an
unfinished path. Production collection remains blocked by `FI-CONTRIB.1` legal/privacy/product
approval and the later isolated backend/security milestone.

## Tests and fixtures

Sanitized in-test fixtures cover a valid private ML contribution, a public-display-only photo, a
withdrawn contribution, label-pending data, and an unresolved privacy finding. Tests also enforce
canonical catalog membership, rights separation, invalid-state reporting, transition restrictions,
content identity, and grouping requirements. No real person, image, consent, or backend record is
used. No serialization library is present in this project, so no persistence format is frozen here;
data-class value preservation is tested without introducing a dependency.

## Moderation command and result contracts

The pure `ContributionModerationEngine` accepts explicit command types rather than a generic status
update. Commands cover submission, rights/privacy/label/quality decisions, label correction,
specialist escalation, dataset approval, rejection, exclusion, and two-stage withdrawal. Each
command carries an ID, submission ID, actor, timestamp, expected lifecycle state, expected aggregate
revision, and optional typed reason/notes.

An accepted result returns a new immutable aggregate plus the review records and lifecycle events
generated by that command. A rejected result returns typed command errors and, where applicable,
the underlying contribution-validation errors. No caller-owned state or storage is mutated.

Expected status and revision provide optimistic concurrency protection. A stale command, a command
for another submission, an invalid transition, or an operation against a terminal state fails
instead of overwriting later work. Label corrections retain earlier reviews and add a
`LABEL_CORRECTED` event. Rights, privacy, label, and quality approvals update only their own review
field; none silently approves another gate. Privacy approval refuses open findings. Dataset approval
requires a sanitized asset and reuses the complete eligibility validator.

`ModerationReasonCode` supplies a small typed vocabulary for rights, privacy, label, quality,
duplicate/correlation, generated/screenshot/non-fish content, malicious media, withdrawal, and an
extensible `OTHER` case.

## Consent version registry

`ConsentVersionRegistry` resolves immutable consent-version metadata and selects the currently
usable version by locale. Metadata contains lifecycle status, effective/approval/retirement times,
locale, an external content identifier, and SHA-256. It deliberately contains no legal prose.

Consent versions move conceptually through `DRAFT`, `LEGAL_REVIEW`, `APPROVED`, and `RETIRED`.
Only an `APPROVED`, explicitly selectable version with an approval timestamp can be offered for a
new contribution. The default `InMemoryConsentVersionRegistry` is empty; production code therefore
contains no approved or selectable consent fixture. Synthetic approved records exist only in tests.

The in-memory registry rejects duplicate IDs, malformed hashes, non-approved selectable versions,
retired selectable versions, and overlapping current versions for one locale. It does not connect
to files, Room, Firebase, Remote Config, or a network.

Retirement controls new acceptance, not history. A consent accepted while its exact version was
approved and effective remains historically valid after that version is retired. Acceptance after
retirement is invalid. Registry validation also requires the submission's consent locale to match
the registered version.

## Remaining dependencies and next slice

Legal/privacy/product decisions remain required for consent wording/version custody, contracting
entity, minors, withdrawal/deletion, snapshot and trained-model treatment, retention, vendor and
sublicense scope, public display, marketing, and incident/takedown handling.

The next safe slice is to finish FI-CONTRIB.2 with storage/access-boundary interfaces and
authorization-policy contracts backed only by in-memory fakes and tests. It must still avoid schema,
Firebase, uploads, UI, and production consent artifacts until `FI-CONTRIB.1` is approved.
