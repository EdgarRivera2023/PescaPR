# Fish Identifier contributor legal requirements brief

> **DRAFT — REQUIRES LEGAL REVIEW BEFORE IMPLEMENTATION**

This is an engineering requirements brief for qualified counsel. It is not legal advice, final
terms, a privacy notice, or contributor-facing copy. Do not collect photos or present this document
for acceptance. The existing `fish_identifier_contributor_permission_draft.md` is likewise
provisional and should be reconciled with counsel's decisions before implementation.

## 1. Intended transaction

An authenticated/identifiable PescaPR user may voluntarily contribute a specific photograph for
commercial development and operation of PescaPR fish-identification technology. Ordinary camera
use, private catches, community uploads, and general app terms do not create this grant.

The desired structure is a non-exclusive license; copyright transfer is not intended. Counsel must
confirm the correct contracting party, jurisdictions, clickwrap/e-signature requirements, term,
termination, evidence, and consumer-language obligations.

## 2. Rights the agreement may need

**REQUIRES LEGAL REVIEW:** determine whether the specific-photo license must be worldwide,
royalty-free, and sufficiently durable for:

- storage, backup, reproduction, format conversion, and controlled reviewer/vendor access;
- annotation, labeling, deduplication, feature extraction, resizing, cropping, redaction,
  normalization, augmentation, and other dataset transformations;
- ML training, fine-tuning, validation, testing, evaluation, robustness analysis, improvement,
  maintenance, and operation;
- creation, retention, distribution, and commercial use of model weights and mobile model files
  derived from datasets containing the image;
- contractors/cloud infrastructure acting for PescaPR;
- assignment, sublicense, or transfer to a future PescaPR business entity, acquirer, or successor,
  only to the extent necessary and appropriately limited.

Counsel should avoid an unnecessary rights grab. The grant should not silently authorize stock
photo sales, unrelated general-purpose models, advertising profiles, biometric identification, or
public display.

## 3. Required contributor representations

**REQUIRES LEGAL REVIEW:** the contributor may need to affirm that they created the photograph or
hold sufficient authority; the submission does not knowingly infringe copyright, privacy,
publicity, trademark, confidentiality, property, or other rights; required permissions/releases
for recognizable people or protected/private content exist; submitted metadata is truthful to the
best of their knowledge; and the contribution is voluntary.

Counsel must decide warranties, remedies, takedown handling, liability limitations, indemnity (if
any), and how to keep the language proportionate and understandable.

## 4. Separate grants

The approved contract/UX must record independently:

- personal/private app use under existing product terms;
- ML training/model-development permission;
- optional public/community/Guía display permission;
- any future marketing permission.

ML contribution must not imply public display. Public display must not imply ML training. Each
grant needs its own default, withdrawal behavior, evidence, and versioning analysis.

## 5. Model weights and withdrawal

**REQUIRES LEGAL REVIEW:** decide and disclose whether contributors may stop future use; whether
source and derivative images are deleted; treatment of backups and immutable snapshots; whether
already-trained, evaluated, released, or installed models may remain; whether unreleased models
must be retrained; whether model weights are considered derivatives or personal data; and what can
truthfully be promised about machine unlearning.

The agreement must align with an operational process. Engineering will retain snapshot and
training-run lineage so affected future use can be identified.

## 6. Privacy, identity, and minors

Counsel/privacy review must determine:

- lawful basis, privacy notice, data controller/processor roles, vendors, transfers, and security;
- minimum contributor identity needed to prove consent and handle withdrawal/takedowns;
- whether anonymous Firebase accounts provide adequate evidence;
- EXIF/location handling, face/plate/boat-registration/private-document policies;
- retention of consent, audit, checksum, rejection, deletion, and legal-defense records;
- contributor eligibility, age requirements, parental/guardian consent, and incidental minors;
- data-subject/access/deletion request handling and incident notification.

No age threshold is proposed here. Collection remains blocked until approved eligibility rules can
be enforced.

## 7. Attribution and public release

Counsel/product must decide whether contributor attribution is optional, required, centralized, or
impractical in model artifacts; how pseudonymous/no-credit preferences work; and how third-party
license obligations are preserved. Separate approval is required before releasing training images,
dataset metadata, public galleries, examples, marketing, or a public dataset.

## 8. Consent evidence requirements

The approved process should produce an immutable consent-version identifier, rendered-text
checksum, locale, UI/app version, affirmative action, account/identity reference, timestamp,
submission IDs covered, ownership declaration, independent grant values, disclosures, and later
withdrawal/supersession events. Private evidence must be access-restricted and excluded from Git,
model artifacts, and distributable metadata.

## 9. Decisions required before collection

No production collection may begin until counsel/product approve and record:

1. final contributor agreement and privacy notice;
2. contracting party, jurisdiction, eligibility/minor rules, and consent mechanism;
3. license scope, term, vendor/sublicense/transfer needs, attribution, and model-weight treatment;
4. independent public-display/marketing grants;
5. withdrawal, deletion, snapshot, trained-model, backup, and retention policy;
6. people/property/location/privacy review and redaction/rejection policy;
7. takedown, dispute, abuse, security incident, and rights-request processes;
8. exact consent-version evidence and change-management process.

Legal approval should itself be versioned, dated, and linked to the enabled app/backend release.
