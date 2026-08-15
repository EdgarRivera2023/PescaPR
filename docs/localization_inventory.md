# PescaPR localization inventory

**Scope:** v2.4.1 final audit
**Status:** All identified static default-English production UI is resource-backed; Spanish translation and locale-quality work remain open.

## Current resource structure

- `app/src/main/res/values/strings.xml` contains the default application strings extracted so far.
- No localized `values-*` directory existed before this slice.
- `app/src/main/res/values-es/strings.xml` now exists as a valid empty Spanish resource container.
- Gradle currently has no `generateLocaleConfig`, `resConfigs`, or per-app language configuration.
- No `values-en` directory is needed; the default `values` resources remain the English/default source.

No resource key, Gradle setting, dependency, locale behavior, or visible wording was changed.

## Remaining migration inventory

The codebase contains substantial static UI text directly in Compose/Kotlin. The next extraction
work should be grouped by screen rather than performed as an unreviewed global replacement:

| Area | Representative files | Remaining work |
|---|---|---|
| App shell/navigation | `MainActivity.kt` | **Batch 1 extracted:** drawer labels, plan labels, About/menu labels, toolbar and shell icon descriptions now use default English resources. |
| Map/spots/community | `MapaPescapr.kt`, map-related components | **Batches 2 and final cleanup extracted:** map/spot/photo UI, catch-record entry/detail labels, and environmental/morphology wrappers. Dynamic record values and domain data remain runtime content. |
| Fish Identifier | `ui/identificador/IdentificadorScreen.kt` and related code | **Batch 4 extracted:** standalone capture-validation instructions/actions, result labels/templates, and report confirmation/actions. Guía Oficial data, Gemini output, and matching fallback diagnostics remain dynamic. |
| Guía Oficial | `ui/guia/GuiaOficialScreen.kt` | **Batch 5 extracted:** guide/search shell, developer CRUD controls and fields, image-editor actions, detail labels, photo carousel description, and photo-moderation labels/messages. Species names, aliases, regulations, characteristics, and other fields remain domain data. |
| Private Journal/records | `ui/records/RecordsScreen.kt` and related UI | **Batch 3 extracted:** journal title/sync controls, empty state, catch-count label, AI Insights title/close action, and edit-catch fields/actions. Dynamic species, locations, dates, measurements, and Gemini output remain data. |
| Admin/debug | `ui/AdminScreen.kt` | **Batch 7 extracted:** report/log dialogs, buttons, confirmations, share/clear actions, and save feedback. Log contents remain dynamic diagnostics. |
| Pro/paywall | `ui/components/PaywallScreen.kt` | **Batch 7 extracted:** subscription feature titles/descriptions, plan labels, cancellation copy, and subscribe/free-plan actions. Store-provided price text remains dynamic. |
| Coastal morphology and cards | `CoastalMorphology*`, `ProSwellCard.kt`, `WaterTempCard.kt`, `TideGauge.kt`, `MapaPescapr.kt` | **Batch 6 extracted:** swell/planner/paywall labels, water-temperature and trend labels, golden-tide planner wrappers, and morphology metadata labels/empty state. Dynamic measurements, feature metadata, and typed trend/window values remain runtime data. |
| About/global utilities | `ui/about/AboutDialog.kt`, shared actions | **Final cleanup extracted:** About shell, version wrapper, release-notes heading, and historical release-note bodies now use resources. |
| Shared/error/reporting | `BugReportLogger.kt` and UI call sites | Batch 7 extracted Admin wrappers; raw logs/diagnostics remain dynamic. |

This is a component-level inventory, not an exhaustive occurrence list. v2.4.1 still has feature
batches remaining after the app-shell batch; v2.4.2 should translate the corresponding Spanish keys;
v2.4.3 should migrate remaining Compose callers to `stringResource()` or formatted/plural resources.

### v2.4.1 Batch 1 resource keys

`app_name_pro`, `plan_pro_active`, `plan_free`, `nav_map`, `nav_identifier`, `nav_official_guide`,
`nav_records`, `nav_admin`, `action_about`, `content_desc_about`, `content_desc_app_logo`,
`content_desc_main_menu`, and formatted `debug_pro_tier_set` were extracted from `MainActivity.kt`.
The Spanish file remains intentionally empty until v2.4.2.

### v2.4.1 Batch 2 resource keys

Map and fishing-spot shell text in `MapaPescapr.kt` now uses default resources for offline and marker
messages, community/local spot tabs, new-spot fields/actions, weather/vitals labels, and spot-photo
submission status/actions. Formatted resources preserve the existing error and photo-count wording;
catch-record labels were completed in the final cleanup. No Spanish entries were added.

### v2.4.1 Batch 6 resource keys

Environmental and coastal-morphology static UI now uses default resources for swell metrics, Pro
feature/paywall copy, water-temperature/trend labels, planner and golden-tide templates, and
morphology metadata labels. Numeric values, units/conversions, tide times, trend/window enums, and
dataset bilingual fields remain untouched.

### v2.4.1 Batch 5 resource keys

Guía Oficial static UI now uses default resources for the guide title/search state, developer controls,
CRUD dialog labels and save wrappers, image editing actions, detail-section labels, photo-count and
carousel descriptions, and the embedded photo-moderation shell. Dynamic bilingual species content,
Firestore values, image URLs, and admin-entered values remain outside Android resources.

### v2.4.1 Batch 4 resource keys

The standalone Fish Identifier now uses default resources for its title, photo instructions, camera/
gallery/validation actions, result headings and formatted confidence/English-name labels, regulation
headings, and report flow text. Recognition, Gemini prompts/parsing, Firestore access, and dynamic
species/content values were not changed.

### v2.4.1 Batch 3 resource keys

Records/private-journal static labels, synchronization actions and wrappers, empty state, catch-count
display, AI Insights chrome, and edit-catch labels/actions now use default resources. Gemini-generated
insight text and the fallback messages produced by the AI worker remain runtime output.

### v2.4.1 Batch 7 resource keys

Billing/paywall feature copy and actions, Admin/error-log controls and wrappers, About shell/version
text, and shared save/cancel/close actions now use default resources. Google Play price text, log
contents, and raw diagnostics remain outside this batch.

### v2.4.1 final cleanup and audit

Catch-record entry/detail labels, formatted environmental metadata wrappers, validation messages, and
historical About release-note versions/bodies are now resource-backed. The targeted production-source
audit found no meaningful Category A static UI literals remaining. Remaining literals are dynamic
record values, domain/backend data, internal keys/diagnostics, or decorative bullet punctuation.

## Static UI versus dynamic data

Static application chrome belongs in Android resources: navigation labels, buttons, dialogs, errors,
accessibility descriptions, subscription copy, and fixed unit labels. Dynamic values require resource
formatting rather than string concatenation when they are displayed.

Guía Oficial remains the source of truth for domain content. Its Spanish names (`nombreComun`), English
names (`nombreIngles`), scientific names, aliases, regulations, characteristics, and other fish
fields are runtime data and must not be copied into `strings.xml`. Likewise, coastal morphology
records already expose Spanish/English fields and should remain data-driven.

Backend/server text and Firebase error messages require separate consideration: static wrappers can
be localized, while remote text should not be silently treated as a translatable resource.

## Risks to address in v2.4.1–v2.4.3

- **Interpolated strings and units:** map, swell, temperature, tide, score, date, and count text is
  often assembled with `${...}` or `String.format`; replace with formatted resources and locale-aware
  number/date handling rather than translating fragments.
- **Plural-sensitive wording:** counts such as photos, days, spots, and records need plural resources,
  not singular strings with appended numbers.
- **Date/time and number locale:** current `Locale.US`, `°F`, `ft`, `sec`, decimal formatting, and
  date strings may be product decisions rather than translation-only work.
- **Accessibility:** `contentDescription` values are user-visible and need inventory alongside visible
  labels; `null` descriptions may be intentional for decorative icons.
- **Mixed languages:** current UI contains Spanish, English, product names, `PRO`, and technical terms;
  translation policy should define which branded/product terms remain unchanged.
- **Logic keys versus display text:** resource strings must never replace `FichaPez.id`, database or
  Firestore field names, enum/lifecycle values, analytics keys, preference keys, storage paths, or
  other persisted/internal values. Any comparison against a displayed/common-name string should be
  recorded for later cleanup, not changed in this foundation slice.
- **Error text:** exception/backend messages may be dynamic and unstable; localize a stable user-facing
  wrapper and retain the raw diagnostic separately where appropriate.
- **Map/spot dynamic data:** dynamic spot names, notes, coordinates, photo URLs, catch values, backend
  payloads, and exception text remain runtime data; static wrappers are resource-backed.
- **Records locale formatting:** journal dates, weights, lengths, weather values, and the catch-count
  label still need locale-aware number/date and plural review. Raw exception text remains appended to
  a localized synchronization wrapper. Gemini-generated content is not translated by resources.
- **Identifier dynamic output:** species names, Guía Oficial fields, confidence values, Gemini output,
  and matching fallback/error diagnostics remain runtime data. Confidence/number formatting and any
  future local-classifier result presentation need locale review. No display-string logic coupling was
  found in the standalone Identifier screen.
- **Guía Oficial boundary:** bundled/Firestore species content remains runtime data; the known bundled-
  JSON versus Firestore-admin-edit behavior is intentionally preserved. Photo counts and carousel
  descriptions use formatted resources, while raw backend/moderation errors remain dynamic. No
  display-string logic coupling was found in the Guía screen.
- **Environmental locale concerns:** temperatures, wave/tide measurements, compass directions, dates,
  and units still use existing formatting. Morphology `_es`/`_en` field selection remains unchanged,
  and typed trend/window values are only mapped to display resources at the UI boundary. Raw backend
  errors and dynamic explanations remain runtime content.
- **Final v2.4.1 audit — Category A:** none identified in the targeted production UI sweep.
- **Final v2.4.1 audit — Category B:** species/domain values, user-entered records and measurements, Gemini output,
  Firestore/Firebase payloads, Google Play prices, and log contents remain intentionally dynamic.
- **Final v2.4.1 audit — Category C:** IDs, keys, URLs, MIME types, storage paths, enum values, logs,
  and developer diagnostics remain internal.
- **Final v2.4.1 audit — Category D:** date/number/unit formatting, plurals, generated text, and
  morphology bilingual-field selection remain deferred localization-quality work.
- **Capitalization and sentence fragments:** Compose labels currently rely on Spanish capitalization
  and concatenated fragments; each future resource should be a complete, translator-reviewable unit.
- **Billing/legal copy:** subscription wording and cancellation claims require product/legal review in
  addition to translation.

## Later locale configuration

`values-es` is the standard Android Spanish locale for this foundation. Per-app language configuration,
`generateLocaleConfig`, a language selector, automatic language switching, and regional `es-rPR`
variants are intentionally deferred to v2.4.4 or a separately documented decision.
