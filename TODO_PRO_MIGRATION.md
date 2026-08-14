# PescaPR Pro Migration Roadmap

Master to-do list for transitioning PescaPR to a robust freemium model with advanced surf casting features.

## Version 1.9
## Module 1: Pro Surf Casting Wave & Swell Metrics
- v1.9.0 [x] Define `SwellResponse` and nested data classes for Open-Meteo Marine API.
- v1.9.1 [x] Implement `MarineWeatherService` Retrofit interface.
- v1.9.2 [x] Add `fetchSwellData` logic to `MainActivity` with coordinate-based querying.
- v1.9.3 [x] Design and implement `ProSwellCard` Composable.
- v1.9.4 [x] Integrate `ProSwellCard` into the Map's `ModalBottomSheet`.
- v1.9.5 [x] Implement UI state handling for "Pro Only" data visualization.

## Version 2.0
## Module 2: Private Catch Journal & AI Pattern Matcher
- v2.0.0 [x] Define `CatchLog` data model with environmental metadata.
- v2.0.1 [x] Configure Firestore Security Rules for `users/{userId}/private_logs`.
- v2.0.2 [x] Implement `PrivateJournalRepository` for local/remote sync.
- v2.0.3 [x] Design the Private Journal UI (List and Detail views).
- v2.0.4 [x] Develop the Gemini AI prompt pipeline for pattern matching.
- v2.0.5 [x] Add "AI Insights" dashboard to the Journal section.

## Version 2.1
## Module 3: Community Pins Network (Pro Tier Advantage)
- v2.1.0 [x] Update `PuntoPesca` model and Firestore documents with `userId`.
- v2.1.1 [x] Implement role-based query filtering logic in `MapaPescapr`.
- v2.1.2 [x] Integrate Google Play Billing Library (v7+).
- v2.1.3 [x] Create `SubscriptionManager` to handle Pro entitlement flags.
- v2.1.4 [x] Design the "Upgrade to Pro" paywall UI.
- v2.1.5 [x] Implement privacy-first pin sharing (static pins only, no live tracking).
- v2.1.6 [x] Implement `SpotPhotoRepository` for Firebase Storage/Firestore integration.
- v2.1.7 [x] Create `ApprovedSpotPhoto` model and metadata for `PuntoPesca`.
- v2.1.8 [x] Develop Admin Moderation UI for approving/rejecting user-submitted photos.

## Version 2.2
## Module 4: Water Temperature Trends (Pro Exclusive)
- v2.2.0 [x] Update `WeatherResponse` to isolate water temperature from ambient metrics.
- v2.2.1 [x] Implement a 7-day thermal trend algorithm (warming/cooling).
- v2.2.2 [x] Create a `WaterTempCard` restricted to Pro users.
- v2.2.3 [x] Add visual indicators (icons/colors) for thermal trends.

## Version 2.3
## Module 5: Coastal Morphology Map Layers & Offline Mode
- v2.3.0 [x] Establish a GeoJSON schema and manually digitize visual data from Catastro/JP
  into map polygons and polylines (e.g., reefs, drop-offs).
- v2.3.1 [x] Curate the dataset with specialized metadata (e.g., Target: Mutton Snapper,
  Gear: 45 lb braided main line, Bottom: Rock/Sand).
- v2.3.2 [x] Keep the Maps API key in ignored local.properties, inject it into the Android
  manifest, and use schema-constrained org.json parsing with Maps Compose overlays without
  the unused direct Maps SDK Utility Library dependency.
- v2.3.3 [x] Implement CoastalMorphologyLayer to render proprietary local GeoJSON from
  res/raw as non-clickable Maps Compose Polygon and Polyline overlays over the satellite
  map for Pro users.
- v2.3.4 [x] Implement non-intercepting interaction design for structure metadata, ensuring
  GeoJSON feature taps do not block interaction with fishing spot markers.
- v2.3.5 [x] Configure a remote fetch mechanism (e.g., Firebase Cloud Storage) for GeoJSON
  files to enable over-the-air structure updates without requiring Play Store app releases.

## Priority Prerequisite Before Module 6
### Fish Identifier Modernization — Zero-Cost On-Device Recognition

This track intentionally interrupts the normal version roadmap and must be completed before
proceeding with the next major planned PescaPR feature. Guía Oficial remains the authoritative
species catalog. The target flow is: photo → deterministic local preprocessing → bundled
on-device classifier → ranked canonical `FichaPez.id` candidates → `OfficialGuideRepository`
→ authoritative Guía Oficial record → existing/reused guide-detail UI.

#### Phase A — Dataset & Classifier Contract Foundation — NEXT
- FI-A.1 [x] Freeze the supported classifier catalog around the existing 39 Guía Oficial species.
- FI-A.2 [x] Use `FichaPez.id` as the canonical classifier label instead of common-name strings.
- FI-A.3 [x] Design a versioned classifier manifest containing the model version, input
  specification, ordered classifier labels, and output-index-to-`FichaPez.id` mapping.
- FI-A.4 [x] Audit the quality, correctness, licensing, and provenance of current Guía Oficial
  reference images.
- FI-A.5 [x] Define labeled training-data requirements per species and identify appropriately
  licensed external training-image sources.
- FI-A.6 [x] Enforce and measure the documented realistic field/catch-photo variation
  requirements during acquisition planning, including different angles, lighting,
  backgrounds, fish sizes, handling conditions, partial fish where appropriate, and
  juvenile/adult variation where relevant; produce a per-species coverage checklist and gap
  report before accepting the dataset for training.
- FI-A.7 [x] Apply the documented group-aware train, validation, and test partition strategy to
  the acquired dataset and verify leakage and duplicate-image controls.
  - FI-A.7-FRAMEWORK [x] Establish the repository-local image metadata contract, deterministic
    partitioning policy, grouping hierarchy, exact/perceptual duplicate controls, leakage
    validator, synthetic fixtures, and locked-test/snapshot versioning rules. Subsequent pilot
    execution applied and verified the framework against 22 approved real images.
  - FI-A.7-PILOT [ ] Execute the documented five-species, 125-positive/50-OOD rights-cleared
    pilot through discovery, rights/label approval, ingestion, hashing, grouping, deterministic
    partition proposal, locked-test review, and leakage validation. FI-A.7 completes only after
    the real pilot snapshot has no unresolved validation errors.
    - FI-A.7-PILOT-GOVERNANCE [x] Prepare provisional contributor-permission language, the
      rights-review SOP and role gates, controlled external binary-workspace policy, narrow Git
      protections, and the candidate-review decision schema. Legal approval remains required;
      no images have been acquired.
    - FI-A.7-PILOT-DISCOVERY [x] Complete the first metadata-only discovery pass against approved
      source families: 58 genuine candidate records across the five pilot species, all pending
      item-level rights and visual label review. No image binaries were downloaded or ingested.
    - FI-A.7-PILOT-ADJUDICATION [x] Complete the first item-level rights and visual/metadata label
      review of all 58 discovery candidates plus three narrow Alectis additions. Independent
      approval and HIGH/VERY_HIGH second/expert reviews remain open; zero candidates are currently
      approved for acquisition and no image binaries were retained or ingested.
    - FI-A.7-PILOT-BATCH1-APPROVAL [x] Edgar Rivera independently approved all eight packet
      candidates (five Coryphaena, three clear adult Alectis) on 2026-08-14; the decisions and
      `APPROVED_FOR_ACQUISITION` state are recorded in the candidate queue.
    - FI-A.7-PILOT-BATCH1-INGESTION [x] Acquire exactly the eight approved originals into the
      external controlled workspace; record SHA-256 and 64-bit pHash, real grouping metadata and
      deterministic partition proposals; and validate eight metadata rows with zero errors. The
      broader pilot, OOD, expert reviews, locked-test review, and snapshot validation remain open.
    - FI-A.7-PILOT-OOD-BATCH1-APPROVAL [x] Edgar Rivera independently approved category and
      acquisition decisions for all 14 packet candidates on 2026-08-14; the authoritative queue
      records the decisions without assigning any OOD sample to a frozen classifier class.
    - FI-A.7-PILOT-OOD-INGESTION [x] Acquire all 14 approved OOD originals into the external
      workspace, record real hashes and correlated groups, and validate the combined 8-positive /
      14-OOD metadata with zero errors. Locked-test review/versioning, snapshot validation,
      broader pilot targets, warning dispositions, and required expert reviews remain open.
    - FI-A.7-PILOT-SNAPSHOT-FRAMEWORK [x] Add locked-test approval records, explicit warning
      dispositions, grouping review, and deterministic create/verify tooling with synthetic tests.
      Real snapshot creation fails closed while TEST approval or lock state is incomplete.
    - FI-A.7-PILOT-TESTSET-APPROVAL [x] Edgar Rivera independently approved the four revised
      `testset-v1` rows on 2026-08-14 after the first unrepresentative composition was reassigned.
      Exactly those four rows are locked; the historical decisions and overrides remain preserved.
    - FI-A.7-PILOT-SNAPSHOT-V1 [x] Create and verify immutable `pilot-snapshot-v1` for 22 real
      rows (8 positive / 14 OOD; TRAIN 13 / VALIDATION 5 / TEST 4), including checksummed
      `testset-v1`, warning dispositions, partition overrides, and sanitized repository manifests.
      FI-A.7 is complete with zero unresolved structural errors. The broader pilot remains open.
    - FI-A.7-PILOT-EXPERIMENT-SUBSET [ ] Expand to an experiment-quality five-species development
      subset. The 2026-08-14 source/governance checkpoint documents an exact 242-image shortfall
      to the 50-per-species minimum, direct-source limitations, and the pending qualified review
      queues for Scomberomorus, Seriola, and Sphyraena. No new candidates were approved or acquired;
      specialist decisions and source-diverse item-level discovery are the next operational gates.
- FI-A.8 [ ] Build and validate the documented hybrid unsupported-fish/non-fish rejection
  strategy, including an OOD corpus and evidence-based confidence/margin thresholds; require a
  new catalog/model version if evaluation justifies an explicit unknown/other output class.
  - FI-A.8-FRAMEWORK [x] Define typed accept/ambiguous/reject/invalid states, model-agnostic score
    inputs, supported/OOD and confusion-slice metrics, calibration and locked-test discipline,
    versioned evaluation templates, and dependency-free threshold-sweep tooling with synthetic
    tests. No numeric production threshold is selected. FI-A.8 remains open until a trained model
    produces validation/OOD predictions, a policy is empirically selected, and a frozen locked-test
    checkpoint is evaluated.
- FI-A.9 [ ] Define accuracy targets and evaluation criteria before Android model integration.
- FI-A.10 [ ] Treat the current approximately 124 reference-image URLs (about 1–5 per species)
  only as reference/seed material; they are not sufficient by themselves, and a substantially
  larger labeled dataset will almost certainly be required for a robust field-photo classifier.

#### Proof-of-Concept Bridge — Five-Class Experiment
- FI-POC.1 [x] Select five lower-review-risk experiment classes and freeze the separate
  `fish-identifier-poc-v1` class manifest using canonical `FichaPez.id` outputs. The POC is
  explicitly non-production, preserves the immutable 39-class manifest, and does not weaken the
  specialist gates in the parallel five-species production pilot.
- FI-POC.2 [ ] Acquire, approve, hash, group, and validate 30–50 independent originals for each
  POC class (150 minimum; 200–250 preferred), plus 50–100 development OOD originals, then freeze a
  group-safe POC TRAIN/VALIDATION snapshot. Validate snook and bonefish source feasibility before
  binary acquisition; do not use or alter production `testset-v1`.
  - FI-POC.2-DISCOVERY [x] Complete metadata-only feasibility for all five POC v1 classes. POC v1
    is NO_GO under the current approved-source policy: snook yields four plausible independent
    candidates and bonefish only two independence-adjusted groups, while no class establishes the
    35–40-candidate buffer. Preserve v1 unchanged and perform a metadata-first POC v2 replacement
    evaluation before any binary acquisition.
  - FI-POC.2-REVISION [ ] Evaluate and freeze a separate POC v2 only after replacement candidates
    demonstrate a credible rights- and independence-adjusted path to at least 30 originals.
    - FI-POC.2-REVISION-FEASIBILITY [x] Complete metadata-first review of the initial five-class
      replacement shortlist. Result: NO_GO. Permitted-source volume collapses to 6–15 independent groups
      per class, no candidate reaches the 35–45 discovery buffer, and no exact five-class set yet
      has a credible path to 30 accepted originals per class. Preserve POC v1 and do not freeze
      POC v2; the next action is a new shortlist plus targeted permission/source checks for the
      strongest leads, `Ocyurus chrysurus` and `Haemulon plumieri`.
- FI-POC.3 [ ] Run the first Phase B training/export experiment against the frozen POC dataset,
  emit canonical-ID predictions for the existing evaluation tooling, and use validation/OOD—not
  locked production TEST—to exercise FI-A.8 threshold-policy analysis.

#### Technical Micro-POC Bridge — Three-Class Architecture Proof
- FI-MICRO-POC.1 [x] Freeze `fish-identifier-micro-poc-v1` as an
  `EXPERIMENT_ONLY_NOT_PRODUCTION` three-class contract using canonical `FichaPez.id` outputs for
  `Haemulon plumieri`, `Ocyurus chrysurus`, and `Lactophrys triqueter`. Its experiment-only gate is
  10 accepted independent originals per class (12–15 preferred) and does not reduce any five-class,
  Stage 1, or production requirement.
- FI-MICRO-POC.2 [ ] Reuse the POC v2 feasibility queue to adjudicate and independently approve,
  acquire, hash, group, and validate 10–15 originals per micro-POC class. Stop without lowering the
  gate if any class cannot reach 10; reuse only unlocked development OOD and never production
  `testset-v1`.
  - FI-MICRO-POC.2-ADJUDICATION [ ] **BLOCKED at the required stop gate:** exact item-page rights,
    label/quality, and conservative grouping review still produces only 7 qualified independent
    `Haemulon plumieri` groups. The final public/open-source rescue added six useful independent
    records but zero rights-approved groups; the former FishBase pending item was confirmed as
    prohibited CC BY-NC. No approval packet was issued and the 10-per-class threshold was not lowered.
    Progress now requires explicit direct permission for at least three independent groups or future
    rights-cleared PescaPR-contributed imagery.
    - FI-MICRO-POC.2-HAEMULON-RESCUE [x] Complete the one-time narrow source rescue, preserve
      conservative grouping, and record five permission-only opportunities. Public/open supply did
      not close the three-group gap; do not begin another discovery strategy automatically.
- FI-MICRO-POC.3 [ ] Train the first small transfer-learning model, export a mobile-compatible
  artifact, verify canonical-ID output mapping, and emit real validation/development-OOD
  predictions into FI-A.8 tooling. Treat results strictly as architecture evidence, not production
  accuracy.

#### FI-CONTRIB — User-Contributed Training Dataset — PARALLEL LONG-TERM TRACK

PescaPR will build the production 39-class dataset primarily from explicit, voluntary,
rights-reviewed user contributions of real Puerto Rico angler photos, supplemented by PescaPR-owned,
permissioned partner, public-domain, and compatible open-license sources. Personal photo use, ML
training permission, and optional public display are separate grants. This track complements—but
does not replace—the micro-POC, FI-A.8, production dataset gates, or canonical `FichaPez.id` contract.

- FI-CONTRIB.0 [x] Define the contributor architecture, consent/version principles, independent
  rights grants, privacy sanitation, typed lifecycle, review/moderation roles, logical data/storage
  boundaries, provenance/snapshot lineage, abuse controls, metrics, rollout phases, and legal
  decision gates. Planning only; no collection is authorized.
- FI-CONTRIB.1 [ ] **BLOCKS ALL PRODUCTION COLLECTION:** obtain qualified legal/privacy/product
  approval for contributor agreement and privacy notice, contracting party, eligibility/minors,
  consent evidence, license/vendor/transfer scope, public-display separation, withdrawal/deletion,
  trained-model/snapshot treatment, retention, takedown, and incident processes.
- FI-CONTRIB.2 [ ] Freeze backend-neutral contracts for versioned consent/grants, canonical-ID
  submission and training-asset schemas, typed lifecycle/rejection/review events, storage/access
  boundaries, privacy flags, and validation fixtures. Do not create upload UI or enable collection.
  - FI-CONTRIB.2-CONTRACTS [x] Implement backend-neutral Kotlin consent/right, lifecycle,
    submission, sanitized training-asset, privacy, review/event, snapshot-membership, and model-run
    provenance contracts with pure eligibility/transition validation and sanitized unit fixtures.
    Collection remains structurally impossible: there is no UI, repository, backend adapter,
    Firebase schema/rule, upload path, or runtime enablement. FI-CONTRIB.1 still blocks production
    collection and the remaining persistence/access contract work keeps FI-CONTRIB.2 open.
  - FI-CONTRIB.2-MODERATION-CONTRACTS [x] Add explicit backend-neutral moderation commands,
    typed results/reasons, optimistic state/revision guards, append-only audit outputs, and a
    consent-version registry abstraction with an empty-by-default in-memory implementation and
    synthetic tests. No production-selectable consent version, persistence, Firebase adapter, UI,
    or collection path exists; FI-CONTRIB.1 remains blocking.
- FI-CONTRIB.3 [ ] Design and implement isolated backend/Storage/Firestore architecture and security
  rules for quarantined originals, private consent evidence, sanitized training derivatives, and
  separately permissioned public-display copies; complete security/privacy testing before uploads.
- FI-CONTRIB.4 [ ] Implement the explicit capture/Guía selection/contribution opt-in flow with
  independent ML and public-display grants. Never migrate or enroll existing personal/community
  photos automatically.
- FI-CONTRIB.5 [ ] Implement role-separated Admin/Master moderation for rights, privacy, canonical
  label correction, specialist escalation, quality, duplicates/correlation, abuse, withdrawal, and
  final Dataset Approver decisions with append-only history.
- FI-CONTRIB.6 [ ] Export only eligible sanitized assets into immutable group-safe snapshots using
  the existing metadata, hash/pHash, leakage-validator, consent provenance, and test-set discipline;
  bind every model run to exact snapshot and consent-policy versions.
- FI-CONTRIB.7 [ ] Add privacy-safe coverage/admin metrics and the ML feedback loop: approved images
  and independent groups per species, contributor/session/field diversity, review/rejection and
  withdrawal counts, Stage 1 gaps, and snapshot/model utilization.

#### Phase B — Model Training & Validation — PLANNED
- FI-B.1 [ ] Evaluate and select an appropriate lightweight mobile image-classification
  architecture without prematurely binding the roadmap to one architecture.
- FI-B.2 [ ] Train or fine-tune the selected architecture using the approved labeled dataset.
- FI-B.3 [ ] Export the validated model to an Android-compatible local inference format,
  expected to be TensorFlow Lite/LiteRT unless evaluation identifies a better fit.
- FI-B.4 [ ] Validate per-class accuracy, with focused confusion analysis for visually similar
  snappers, groupers, amberjacks, mackerels, barracudas, and boxfish.
- FI-B.5 [ ] Evaluate top-1 and top-3 performance.
- FI-B.6 [ ] Establish and test a low-confidence/unknown threshold policy.
- FI-B.7 [ ] Measure model size, inference latency, and memory use on target Android devices.

#### Phase C — Android On-Device Identifier Integration — PLANNED
- FI-C.1 [ ] Add the selected local inference runtime, bundled classifier/model asset, and
  versioned label manifest.
- FI-C.2 [ ] Implement deterministic preprocessing: EXIF correction, resizing,
  crop/letterbox behavior, normalization, and model-specific tensor preparation.
- FI-C.3 [ ] Add a dedicated `FishClassifier` abstraction and Identifier ViewModel/state owner.
- FI-C.4 [ ] Return typed ranked results containing `FichaPez.id` and classifier score.
- FI-C.5 [ ] Resolve names, regulations, characteristics, aliases, and photos exclusively from
  Guía Oficial rather than from model-generated content.
- FI-C.6 [ ] Use `OfficialGuideRepository` as the shared catalog source and support the bundled,
  offline Guía Oficial fallback.
- FI-C.7 [ ] Show the top candidate plus alternatives when appropriate, support a
  low-confidence/unknown result, and allow manual correction or species selection.
- FI-C.8 [ ] Reuse or extract the existing Guía Oficial fish-detail UI for identified records.
- FI-C.9 [ ] Ensure identification works offline except for remote guide photos that have not
  already been cached or bundled.

#### Phase D — Migrate All Fish-Identification Call Sites — PLANNED
- FI-D.1 [ ] Migrate the standalone Fish Identifier to the shared on-device classifier and
  canonical catalog mapping.
- FI-D.2 [ ] Migrate catch-registration identification in `MapaPescapr.kt` to the same classifier
  and catalog mapping logic.
- FI-D.3 [ ] Replace brittle common-name equality matching with canonical `FichaPez.id` in both
  flows.

#### Phase E — Remove Gemini From Fish Identification — PLANNED
- FI-E.1 [ ] After local identification is validated, remove Gemini calls from
  `IdentificadorScreen.kt` and from the catch-registration fish-matching flow.
- FI-E.2 [ ] Remove fish-identification dependence on the Gemini API key and eliminate
  quota/spending and connectivity failure modes from fish identification.
- FI-E.3 [ ] Verify that no fish-identification behavior requires connectivity.
- FI-E.4 [ ] Do not globally remove the Gemini SDK or API key until the independent AI Pattern
  Insights feature in `RecordsScreen.kt` has been explicitly evaluated and either retained
  securely, redesigned locally, replaced, or removed.

### Near-Term Production Security — Signing Artifacts Audit
- SEC-SIGN.1 [ ] Verify whether the release AAB and signing keystore are tracked by Git and
  assess their intended repository status.
- SEC-SIGN.2 [ ] Audit Git history for release-binary and signing-key exposure.
- SEC-SIGN.3 [ ] Review `.gitignore`, local signing configuration, secret storage, and access
  controls for release signing material.
- SEC-SIGN.4 [ ] Confirm Play App Signing configuration and custody of upload/app-signing keys.
- SEC-SIGN.5 [ ] Determine whether key rotation or other remediation is necessary; perform no
  remediation until the audit and recovery plan are approved.

## Version 2.4
## Module 6: Bilingual Localization (English & Spanish)
- v2.4.0 [ ] Create the `values-es` resource directory for Spanish localization.
- v2.4.1 [ ] Extract all hardcoded UI text into the default `res/values/strings.xml` (English).
- v2.4.2 [ ] Populate `res/values-es/strings.xml` with the corresponding Spanish translations.
- v2.4.3 [ ] Refactor MainActivity and Jetpack Compose screens to utilize `stringResource()`.
- v2.4.4 [ ] Enable `generateLocaleConfig = true` in `build.gradle.kts` for Android 13+ per-app language support.

## Version 2.5
## Module 7: UX Polish & Onboarding
- v2.5.0 [ ] Implement Jetpack DataStore (Preferences) to manage local boolean flags without Firestore reads.
- v2.5.1 [ ] Create a dynamic Onboarding Tutorial/Walkthrough for first-time app launches, checking the DataStore state.
- v2.5.2 [ ] Add a "Skip" option for returning users.
- v2.5.3 [ ] Implement isSystemInDarkTheme() in Compose to automatically sync the app's theme with the device's display settings.
- v2.5.4 [ ] Add a manual Dark Mode toggle in the app Settings screen, persisting the user's preference in DataStore.

## Version 2.6
## Module 8: Compliance & Retention
- v2.6.0 [ ] Integrate the Google Play In-App Review API.
- v2.6.1 [ ] Add a "Rate Your App" button to the Settings UI.
- v2.6.2 [ ] Implement logic to prompt users for a review automatically after 3 successful log entries (using local counters).
- v2.6.3 [ ] Draft Privacy Policy and Terms of Service.
- v2.6.4 [ ] Embed Privacy Policy and TOS as zero-cost static HTML/Markdown assets, accessible via the Settings UI.

## Version 2.7
## Module 9: In-App Update Management
- v2.7.0 [ ] Integrate Google Play In-App Update SDK dependencies.
- v2.7.1 [ ] Implement a centralized `UpdateManager` to share logic for availability, downloading, and installation states.
- v2.7.2 [ ] Add automatic update check on application launch (Flexible for normal, Immediate for critical/breaking).
- v2.7.3 [ ] Implement Flexible Update UI: Prompt "Update Now" vs "Later" without permanent suppression.
- v2.7.4 [ ] Support Immediate Update flow for mandatory security or API compatibility releases.
- v2.7.5 [ ] Add "Check for Updates" to the About section, displaying the current version and providing manual trigger without app restart.
- v2.7.6 [ ] Handle lifecycle events: cancellation, failure, interruption, and downloaded-but-not-applied states.
- v2.7.7 [ ] Validate complete update lifecycle using Internal test track releases with `versionCode` progression.

## Ongoing Tracks

### Coastal Morphology Dataset Expansion
The morphology dataset is a continuously evolving PescaPR data asset and is not tied to a specific application version. This track represents the expansion and maintenance of the dataset after the initial foundation established in Module 5.

- Continue identifying and digitizing reefs, troughs, drop-offs, sandbars, channels, cuts, points, and other fishing-relevant coastal structures throughout Puerto Rico.
- Expand geographic coverage.
- Validate existing morphology features and correct inaccurate geometry.
- Enrich morphology features with fishing-relevant metadata where appropriate.
- Maintain GeoJSON data quality and consistency as the dataset grows.
- Prioritize expansion based on fishing relevance, user demand, available source data, and field knowledge.

### App Store Optimization (ASO)
- ASO.1 [ ] Conduct keyword research for local Puerto Rico surf fishing terminology.
- ASO.2 [ ] Revamp the Google Play Store description to naturally integrate the new keywords.
- ASO.3 [ ] Design feature-focused screenshots highlighting Pro tier tools (e.g., AI Insights, Swell Metrics).
- ASO.4 [ ] Add engaging, descriptive captions to all Play Store images.

---
**Current Status:** Module 5 complete (v2.3.0 through v2.3.5 done). Fish Identifier
Modernization FI-A.7 is complete with locked `testset-v1` and verified `pilot-snapshot-v1`.
FI-A.8-FRAMEWORK is complete, while empirical FI-A.8 and the broader FI-A.7-PILOT acquisition /
specialist-review track remain open prerequisites before Module 6.

 
