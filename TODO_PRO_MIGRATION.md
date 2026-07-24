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
- v2.0.0 [ ] Define `CatchLog` data model with environmental metadata.
- v2.0.1 [ ] Configure Firestore Security Rules for `users/{userId}/private_logs`.
- v2.0.2 [ ] Implement `PrivateJournalRepository` for local/remote sync.
- v2.0.3 [ ] Design the Private Journal UI (List and Detail views).
- v2.0.4 [ ] Develop the Gemini AI prompt pipeline for pattern matching.
- v2.0.5 [ ] Add "AI Insights" dashboard to the Journal section.

## Version 2.1
## Module 3: Community Pins Network (Pro Tier Advantage)
- v2.1.0 [ ] Update `PuntoPesca` model and Firestore documents with `userId`.
- v2.1.1 [ ] Implement role-based query filtering logic in `MapaPescapr`.
- v2.1.2 [ ] Integrate Google Play Billing Library (v7+).
- v2.1.3 [ ] Create `SubscriptionManager` to handle Pro entitlement flags.
- v2.1.4 [ ] Design the "Upgrade to Pro" paywall UI.
- v2.1.5 [ ] Implement privacy-first pin sharing (static pins only, no live tracking).

## Version 2.2
## Module 4: Water Temperature Trends (Pro Exclusive)
- v2.2.0 [ ] Update `WeatherResponse` to isolate water temperature from ambient metrics.
- v2.2.1 [ ] Implement a 7-day thermal trend algorithm (warming/cooling).
- v2.2.2 [ ] Create a `WaterTempCard` restricted to Pro users.
- v2.2.3 [ ] Add visual indicators (icons/colors) for thermal trends.

## Version 2.3
## Module 5: Coastal Morphology Map Layers & Offline Mode
- v2.3.0 [ ] Create and apply custom JSON Map Style for coastal structures.
- v2.3.1 [ ] Implement Room-based caching for DRNA regulations.
- v2.3.2 [ ] Configure `WorkManager` for periodic offline data synchronization.
- v2.3.3 [ ] Research and implement basic tile caching for saved fishing spots.
- v2.3.4 [ ] Add "Offline Mode" status indicator to the UI.

---
**Current Status:** Module 1 Complete. Planning Module 2.
