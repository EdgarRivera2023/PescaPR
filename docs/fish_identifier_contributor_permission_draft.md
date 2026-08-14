# Fish Identifier Contributor Permission Draft

> **PROVISIONAL — REQUIRES LEGAL REVIEW BEFORE USE**

This document is an engineering and product-policy draft. It is not legal advice, has not been approved for use, and must not be shown to contributors or integrated into PescaPR until qualified counsel approves the language, collection flow, recordkeeping, and applicable privacy disclosures.

## 1. Purpose

PescaPR may later invite people to voluntarily contribute original fish photographs to improve an on-device fish-identification system. This draft separates that contribution from ordinary catch logs, profile photos, reports, or other PescaPR uploads. A normal PescaPR upload is **not** an ML-training contribution and grants no pilot-dataset permission merely because it exists in the app.

The intended permission is non-exclusive and limited to developing, maintaining, evaluating, and distributing PescaPR fish-identification technology and the supporting dataset workflow. Copyright remains with the contributor or existing rights holder; PescaPR is not requesting a copyright transfer.

## 2. Provisional contributor-facing consent

### Voluntary Fish-Identifier Photo Contribution

By selecting “I agree” and submitting a photograph through the dedicated Fish Identifier contribution process, I confirm and agree that:

1. **My authority.** I created and own the submitted photograph, or I have sufficient authority from the rights holder to grant the permissions below. I have not submitted an image copied from social media, search results, another website, another photographer, or another person without permission.
2. **Non-exclusive permission.** I give PescaPR a non-exclusive, worldwide, royalty-free permission to store, reproduce, analyze, resize, crop, rotate, normalize, annotate, transform, extract technical features from, and otherwise process the photograph for PescaPR fish-identification dataset and model-development purposes. I keep my copyright and may continue using or licensing my photograph.
3. **Commercial ML use.** The permission includes using the photograph to develop, train, fine-tune, validate, test, evaluate, improve, maintain, and troubleshoot machine-learning models and related dataset tools for PescaPR, including commercial versions of the PescaPR application.
4. **Learned model parameters.** I understand that trained models may contain learned statistical parameters influenced by many training images. The model is not intended to store or reproduce my photograph, but PescaPR cannot promise that a model will have no measurable statistical influence from it.
5. **Removal requests and existing models.** I may request removal of the original contributed photograph from future dataset versions and future training, subject to identity/authority verification and the final removal policy. I understand that models already trained, evaluated, released, or distributed before a valid removal request may continue to be used and may not be technically capable of removing the influence of one photograph. This paragraph is provisional and depends on the final legal and product policy.
6. **Attribution.** I understand that individual attribution may not be technically practical inside training batches, model parameters, model outputs, or on-device model files. PescaPR will preserve required attribution records where the agreed permission or law requires them. I choose the attribution preference presented in the contribution form, recognizing that the final legally approved workflow controls.
7. **People and protected/private content.** I confirm that I have handled recognizable people, minors, private property, confidential information, location information, and other protected content appropriately. If another recognizable person appears, I have authority to permit the proposed use or I have disclosed that fact for review. I will not submit unlawful, private, or sensitive material.
8. **Accurate information.** The source, creator, species suggestion, capture context, and other information I provide are accurate to the best of my knowledge. I understand that PescaPR reviewers—not my common-name label alone—decide whether an image and species label enter the dataset.
9. **Voluntary submission.** My contribution is optional. Declining does not prevent normal PescaPR use, and ordinary app photos are not automatically included in ML training.
10. **No endorsement or compensation promise.** My contribution does not imply that PescaPR endorses me, and no payment, credit placement, publication, or model inclusion is promised unless separately agreed.

Suggested affirmative control, subject to legal review:

> [ ] I have read and agree to the Voluntary Fish-Identifier Photo Contribution terms, confirm that I own the photograph or have authority to grant this permission, and voluntarily permit the described commercial machine-learning uses.

Consent must not be bundled with general terms, preselected, inferred from upload, or required to use unrelated app features.

## 3. Rights representations and records

The future workflow should record:

- contributor account or verified contact reference, separated from distributable metadata;
- affirmative consent text version, locale, timestamp, and interface version;
- submitted original checksum and internal image ID;
- contributor's ownership/authority declaration;
- recognizable-person/minor/property disclosures and release references;
- attribution preference and approved attribution text;
- species suggestion and capture/session information;
- withdrawal/removal requests and resulting actions;
- any separately negotiated permission or compensation.

The contributor declaration does not replace PescaPR rights, privacy, quality, duplicate, or species review. Pending submissions remain quarantined.

## 4. Intended ML uses

The permission is intended to cover only reasonably related PescaPR fish-identification work:

- dataset curation, deduplication, labeling, and expert review;
- deterministic preprocessing and augmentation;
- feature extraction and analytical quality checks;
- model training, fine-tuning, validation, testing, and evaluation;
- failure analysis, security/robustness testing, and model improvement;
- storage in controlled dataset snapshots and internal review systems;
- distribution and operation of trained PescaPR model artifacts, including commercial app distribution.

The draft does not authorize unrelated biometric identification, advertising profiles, sale of contributor images as stock photography, public release of private evidence, or general-purpose model training. Any materially different use requires a new approved basis and notice/consent where applicable.

## 5. Withdrawal and removal considerations

A final policy must distinguish:

- removal of raw/quarantine/approved image files from controlled active storage;
- exclusion from future snapshot creation and future training runs;
- retention of minimal consent, checksum, audit, legal-defense, and deletion records;
- already-frozen research/evaluation snapshots;
- models already trained but not released;
- models already released or installed on user devices;
- backups and disaster-recovery retention;
- legal preservation obligations.

Product behavior should avoid promising complete “unlearning” unless it can be reliably performed and verified. The provisional position is to stop new use after a valid removal request where feasible while allowing continued use of previously trained/released models. Counsel must approve that balance and the applicable time frames.

## 6. Privacy and security considerations

- Collect the minimum contributor identity needed for consent and audit.
- Keep identity, signed permissions, likeness releases, and contact details outside Git and outside distributable dataset metadata.
- Use anonymized `contributorGroupId` values in dataset records.
- Strip or separately control EXIF location/device identifiers unless demonstrably needed and disclosed.
- Publish no precise fishing location, face, license plate, contact detail, or private evidence through dataset reports.
- Define access, retention, deletion, incident response, and cross-border processing before collection.
- Provide a contact path for rights, privacy, and removal requests.

## 7. Attribution considerations

The dedicated form should offer only choices that PescaPR can honor, such as named credit in a centralized attribution ledger, pseudonymous credit, or no requested public credit. Required attribution must be preserved even if a contributor selects no display preference. Dataset/model-output attribution practicality, CC BY mixing, and app attribution delivery require legal/product review.

## 8. Unresolved legal questions

Counsel must resolve at minimum:

- correct contracting party, governing law, age of consent, and treatment of minors;
- whether a clickwrap, electronic signature, or separate release is required;
- exact license term, termination/withdrawal mechanism, sublicensing, contractor/cloud-processor access, and transfer during a business transaction;
- whether continued use of already-trained or distributed models after withdrawal is enforceable and adequately disclosed;
- copyright, moral rights, database rights, publicity/privacy, property, trademark, and location-data issues in relevant jurisdictions;
- contributor warranties, indemnity, liability limits, dispute handling, and takedown/counter-notice process;
- whether and how images, derived features, snapshots, and audit evidence may be retained;
- privacy notice, lawful basis, international transfers, security controls, and data-subject request process;
- attribution obligations and whether public dataset/model release is ever permitted;
- treatment of incidental people, minors, boats, markings, and third-party content;
- whether model parameters could be treated as derivatives or personal data in any applicable context.

## 9. Legal-review gate

Do not collect contributor images under this draft. Before use, counsel must approve the exact contributor-facing text, affirmative-control design, privacy notice, versioned consent evidence, withdrawal process, data retention, reviewer access, and incident/takedown workflow. Approval should be recorded by document version and date; later wording changes require a new version and migration/consent analysis.
