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
- v2.3.5 [ ] Configure a remote fetch mechanism (e.g., Firebase Cloud Storage) for GeoJSON
  files to enable over-the-air structure updates without requiring Play Store app releases.

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
**Current Status:** Module 5 partially complete (v2.3.0 through v2.3.4 done).

 
