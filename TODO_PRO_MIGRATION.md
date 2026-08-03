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

## Version 2.2
## Module 4: Water Temperature Trends (Pro Exclusive)
- v2.2.0 [x] Update `WeatherResponse` to isolate water temperature from ambient metrics.
- v2.2.1 [x] Implement a 7-day thermal trend algorithm (warming/cooling).
- v2.2.2 [x] Create a `WaterTempCard` restricted to Pro users.
- v2.2.3 [x] Add visual indicators (icons/colors) for thermal trends.

## Version 2.3
## Module 5: Coastal Morphology Map Layers & Offline Mode
- v2.3.0 [x] Create and apply custom JSON Map Style for coastal structures.
- v2.3.1 [x] Implement OfficialGuideRepository as a lightweight local data layer to parse and serve the "Oficial guide" directly from the device.
- v2.3.2 [x] Design the UI screen to display the Official Guide data, strictly observing state hoisted from the ViewModel.
- v2.3.3 [x] Implement dynamic UrlTileProvider mapping for Pro Catastro tiles and default satellite fallback for Free tier.
- v2.3.4 [x] Remove redundant UI toggle buttons; tie layer visibility and map type exclusively to the isPro entitlement state.
- v2.3.5 [x] Add "Offline Mode" status indicator to the UI.

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
- v2.6.4 [ ] Embed Privacy Policy and TOS as zero-cost static HTML/Markdown assets, accessible via the Settings screen.

## Parallel Track (Non-Code / Marketing)
## App Store Optimization (ASO)
- ASO.1 [ ] Conduct keyword research for local Puerto Rico surf fishing terminology.
- ASO.2 [ ] Revamp the Google Play Store description to naturally integrate the new keywords.
- ASO.3 [ ] Design feature-focused screenshots highlighting Pro tier tools (e.g., AI Insights, Swell Metrics).
- ASO.4 [ ] Add engaging, descriptive captions to all Play Store images.

---
**Current Status:** Module 5 complete (v2.3.0 to v2.3.5 done). Ready for Module 6 (Bilingual Localization / v2.4).
