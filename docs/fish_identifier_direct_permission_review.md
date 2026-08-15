# Fish Identifier direct image-permission review package

**Status:** PROVISIONAL — LEGAL/PRODUCT REVIEW ONLY  
**Scope:** case-by-case permission from an identified image rights holder  
**Date:** 2026-08-15

## 1. Purpose

This package defines the narrow permission PescaPR appears to need before using a specific known
image or bounded image group for Fish Identifier ML development. It is a governance checklist and
review draft, not legal advice, an approval, or an instruction to contact anyone.

The immediate repository use case is the five recorded *Haemulon plumieri* permission opportunities
in `datasets/fish_identifier/experiments/micro_poc_v1/permission_candidates.csv`. Those records remain
`PERMISSION_CANDIDATE` and count as zero toward the micro-POC gate.

## 2. What this package is not

This is not the FI-CONTRIB user agreement, privacy notice, Terms of Service, upload consent screen,
mass contributor license, marketing release, public-display release, or copyright assignment. It
does not authorize collection, acquisition, storage, or outreach. It does not make a public webpage,
institutional policy, or ordinary app use into ML permission.

## 3. Proposed narrow ML permission matrix

| Use | Fish Identifier ML need | Proposed treatment |
|---|---|---|
| Obtain/download a copy of the exact approved image | Required | Explicitly request for the identified item only. |
| Controlled storage and necessary backup | Required | Permit restricted development storage, integrity hashes, provenance, and controlled backups. No public publication. |
| Technical preprocessing | Required | Permit orientation correction, resize, crop, format conversion, normalization, quality-preserving derivatives, and appropriate privacy redaction. |
| Training/fine-tuning | Required | Permit use of the image and permitted derivatives for PescaPR Fish Identifier training and fine-tuning. |
| Validation/development evaluation | Required | Permit validation, model selection, calibration, confusion/error analysis, OOD/rejection analysis, and robustness evaluation. |
| Controlled dataset snapshots | Required | Permit restricted internal snapshots and reproducibility records containing the image/derivative reference; no open dataset release. |
| Future Fish Identifier improvement | Required, bounded | Permit retraining, successor architectures, and later improvements for PescaPR Fish Identifier or closely related fish-recognition work only. |
| Distribution of trained model weights | Legal decision required | Ask counsel whether the grant must expressly cover models influenced by the image being embedded in PescaPR, distributed through Google Play, installed on devices, and updated over time. This is distinct from distributing the source image. |
| Public display in Guía Oficial, galleries, social media, or community areas | Separate/optional | Not requested by this package. Requires a separate grant. |
| Marketing, advertisements, Play Store graphics, or promotional video | Separate/optional | Not requested. Requires an independent marketing permission. |
| Public redistribution of the original image | Not currently requested | No open dataset, source-image download, or public image gallery is implied. |
| Copyright ownership transfer | Not requested | The proposed model is a permission/license, not an assignment, unless counsel identifies a specific reason otherwise. |

Technical ML derivatives are requested only to make the permitted ML work possible; they are not a
request to publish derivative artwork or to exploit the image in unrelated products.

## 4. Rights-holder authority and institutional review

Before relying on permission, record evidence that the grantor can authorize the exact work:

- photographer/copyright owner, or institution and authorized representative;
- authority basis and any employee/contractor or collection-policy limitation;
- exact source page and durable source identifier;
- original rights notice, license terms, or dated permission evidence;
- date granted, grantor name/title where appropriate, and permission version;
- exact image IDs, filenames, collection IDs, or bounded group covered;
- species/candidate mapping and source/session grouping reference.

For an institution, counsel/reviewer must confirm whether it owns the image rights, whether an outside
photographer is credited, whether institutional policy permits commercial ML use, whether another
rights holder must consent, and whether noncommercial, no-derivatives, collection, or downstream-use
terms apply. Institutional hosting alone is not authority.

## 5. Item-level traceability and evidence record

Permission must not say only “photos from our website.” The future sanitized record should contain:

```text
permissionRecordId
permissionVersion
sourceOwnerOrEntity
authorityBasis
grantorNameOrRoleReference
dateGranted
coveredSourcePageAndItemIds
coveredFilenamesOrBoundedGroupIds
candidateIdsAndCanonicalSpeciesMappings
contentHashAfterApprovedAcquisition
allowedUses
excludedUses
modelDistributionTreatment
attributionRequirement
expirationIfAny
revocationTermsIfAny
reviewerAndApproverStatus
privateEvidenceLocationReference
notes
```

The hash is recorded only after a separately authorized acquisition. Private permission messages,
signatures, identity evidence, and contact details remain outside Git and outside distributable
metadata; Git may retain only an opaque evidence reference and sanitized facts.

## 6. Provisional review-only outreach template

**PROVISIONAL — NOT APPROVED FOR OUTREACH**

Subject: PescaPR Fish Identifier — permission to use specific photograph(s) for ML development

Hello [name/entity],

PescaPR is reviewing the specific photograph(s) identified below for its offline Fish Identifier,
which helps users compare fish photographs with the PescaPR Guía Oficial catalog:

`[exact source page / item identifier / filename or bounded collection]`

We are asking whether you have authority to grant PescaPR a non-exclusive permission to obtain and
store a controlled copy, make technical ML-preparation derivatives, and use the photograph(s) and
those derivatives to train, fine-tune, validate, evaluate, calibrate, improve, and test PescaPR’s
Fish Identifier and closely related fish-recognition models. This would include restricted internal
dataset/provenance records and, subject to legal terms, operation and distribution of resulting
PescaPR model weights in the app. It would not authorize unrelated AI products.

Copyright ownership would remain with you. This request does **not** ask for public display,
advertising, social-media use, promotional material, or public redistribution of the original
photograph(s). Those uses would require separate permission.

Please do not grant or decline based on this draft until PescaPR provides a counsel-approved version.
If approved, the final request would identify the exact covered items, required attribution, any
limits, expiration or revocation terms, and a way to accept or decline.

Sincerely,  
`[PescaPR authorized contact — to be supplied only after approval]`

This template deliberately does not promise deletion, untraining, continued model use, payment,
attribution format, or any particular withdrawal result.

## 7. Restricted and noncommercial licenses

The existing rights policy remains unchanged. A license containing **NC/noncommercial** restrictions
is not casually compatible with PescaPR’s commercial/freemium product. **ND/no-derivatives**, unclear
AI/ML clauses, field-of-use limits, editorial-only terms, downstream-distribution limits, or
all-rights-reserved status require legal review and cannot be treated as approved by default. The
repository’s CC BY-NC FishBase finding remains rejected under the current policy.

## 8. Legal decisions required: withdrawal, retention, and downstream use

Counsel/product must decide, rather than this package assuming:

- whether and how a permission may be revoked;
- whether future training use stops after revocation;
- whether controlled source copies and derivatives must be deleted;
- treatment of immutable historical snapshots and audit/provenance records;
- treatment of models already trained, released, or installed;
- whether retraining or unlearning is required or feasible;
- retention periods for source images, derivatives, hashes, permission evidence, reviews, snapshot
  membership, and model-run records;
- legal-preservation and backup exceptions;
- whether trained-model distribution requires additional express wording;
- attribution, publicity/privacy, people/minors, property, location, and cross-border issues.

No retention duration or retroactive deletion/untraining promise is defined here.

## 9. Vendors, subprocessors, and sublicensing

Future use may involve controlled cloud storage, training infrastructure, contractors, or ML service
providers. Before any grant is accepted, legal/product must decide whether those processors are
allowed, what access and geographic transfer limits apply, whether subprocessors must be named, and
whether the permission permits that processing. PescaPR must not assume broad sublicensing or vendor
reuse from a direct photographer permission.

## 10. Legal/product approval checklist and stop gate

Before any outreach or acquisition, an authorized reviewer must approve:

- grant scope and exact model-distribution treatment;
- source-image and derivative storage/backup rules;
- item-level authority and evidence requirements;
- attribution and privacy/likeness handling;
- NC/ND/AI restrictions and institutional terms;
- withdrawal, retention, snapshot, backup, and trained-model treatment;
- vendor/subprocessor and transfer scope;
- final outreach language and acceptance evidence;
- separation from FI-CONTRIB consent and collection flows.

**NO PERMISSION OUTREACH MAY BEGIN UNDER THIS PACKAGE UNTIL THE PROVISIONAL LANGUAGE AND RIGHTS SCOPE ARE REVIEWED AND APPROVED BY THE APPROPRIATE LEGAL/PRODUCT AUTHORITY.**

FI-CONTRIB.1 remains blocking. This direct-permission review package does not authorize contributor
collection, upload UI, persistence, or any operational collection path.

