# Implementation Plan - Paywall Screen UI Component (v2.1.4)

This plan outlines the design and implementation of `PaywallScreen.kt` using Jetpack Compose and Material 3 to present PescaPR Pro features and trigger Google Play Billing subscription flows.

## User Review Required

> [!NOTE]
> **State Hoisting**: The `PaywallScreen` composable will remain completely stateless, accepting `ProductDetails?`, `isLoading: Boolean`, `onSubscribeClicked: () -> Unit`, and `onDismiss: () -> Unit`.
> **Fallback Pricing**: If `ProductDetails` is null (e.g. when testing without Play Store sandbox connection), it will display a fallback pricing label (e.g., "$4.99 / mes").

## Proposed Changes

### UI Layer

#### [NEW] [PaywallScreen.kt](file:///C:/Users/edgar/AndroidStudioProjects/PescaPR/app/src/main/java/com/bradmir/pescapr/ui/components/PaywallScreen.kt)
- Design a Material 3 composable `PaywallScreen` (and `PaywallDialog` wrapper).
- Header:
    - Crown/Star icon + "PRO" Badge.
    - Title: "Desbloquea el Potencial Máximo de PescaPR".
- Feature list items:
    - 🌊 **Métricas de Oleaje & Swell**: Datos en tiempo real optimizados para costa.
    - 🧠 **AI Catch Pattern Matcher**: Análisis inteligente con Gemini para predecir momentos óptimos.
    - 📍 **Red de Pines de la Comunidad**: Acceso a los spots compartidos por la comunidad.
    - 🗺️ **Morfología Costera & Estructuras**: Capas avanzadas de mapas marinos.
    - 📶 **Sincronización & Modo Offline**: Acceso a tus récords sin señal.
- Pricing & CTA:
    - Formatted price derived from `ProductDetails` (or fallback "$4.99 / mes").
    - Primary Button: "Suscribirme Ahora". Shows `CircularProgressIndicator` if `isLoading`.
    - Text Button: "Continuar con versión gratuita".

## Verification Plan

### Automated Tests
- Build debug APK using `gradle_build("app:assembleDebug")`.

### Manual Verification
1. Inspect the layout and typography in Android Studio Preview.
2. Confirm state hoisting: verify that clicking "Suscribirme Ahora" triggers `onSubscribeClicked()` and clicking "Continuar..." triggers `onDismiss()`.
