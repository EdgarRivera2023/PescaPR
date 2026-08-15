# PescaPR localization inventory

**Scope:** v2.4.0 foundation review  
**Status:** Spanish resource directory established; broad extraction and translation remain open

## Current resource structure

- `app/src/main/res/values/strings.xml` currently contains only the existing `app_name` resource.
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
| App shell/navigation | `MainActivity.kt` | Drawer labels, plan labels, about/menu labels, toolbar and icon descriptions. |
| Map/spots/community | `MapaPescapr.kt`, map-related components | Spot/community labels, offline/error toasts, photo-review messages, dialogs, units and dynamic interpolation. |
| Fish Identifier | `ui/identificador/IdentificadorScreen.kt` and related code | User instructions, states, errors, action labels, accessibility text; classifier IDs and scores must remain data. |
| Guía Oficial | `ui/guia/GuiaOficialScreen.kt` | Admin actions, editing/photo-review labels, dialogs and accessibility text. Species names, aliases, regulations and descriptive fields remain domain data. |
| Private Journal/records | `ui/records/RecordsScreen.kt` and related UI | Section labels, empty states, controls, validation/errors. |
| Admin/debug | `ui/AdminScreen.kt` | Report/log dialogs, buttons, confirmations and Toast text. |
| Pro/paywall | `ui/components/PaywallScreen.kt` | Feature titles, subscription copy, billing actions and cancellation text. Product/legal review may be needed for final translations. |
| Coastal morphology and cards | `CoastalMorphology*`, `ProSwellCard.kt`, `WaterTempCard.kt`, `TideGauge.kt` | Labels, units, explanations, warnings, content descriptions and formatted values. |
| Shared/error/reporting | `BugReportLogger.kt` and UI call sites | User-facing errors and messages must be separated from log/internal text. |

This is a component-level inventory, not an exhaustive occurrence list. v2.4.1 should enumerate
resource keys while preserving existing wording; v2.4.2 should translate the corresponding Spanish
keys; v2.4.3 should migrate Compose callers to `stringResource()` or formatted/plural resources.

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
- **Capitalization and sentence fragments:** Compose labels currently rely on Spanish capitalization
  and concatenated fragments; each future resource should be a complete, translator-reviewable unit.
- **Billing/legal copy:** subscription wording and cancellation claims require product/legal review in
  addition to translation.

## Later locale configuration

`values-es` is the standard Android Spanish locale for this foundation. Per-app language configuration,
`generateLocaleConfig`, a language selector, automatic language switching, and regional `es-rPR`
variants are intentionally deferred to v2.4.4 or a separately documented decision.

