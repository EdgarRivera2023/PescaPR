# Implementation Plan - Module 1: Pro Surf Casting Wave & Swell Metrics

This plan aims to finalize and professionalize the implementation of **Module 1** as outlined in the [TODO_PRO_MIGRATION.md](file:///C:/Users/edgar/AndroidStudioProjects/PescaPR/TODO_PRO_MIGRATION.md). While much of the code is already present in `MainActivity.kt`, it requires structural organization, performance optimization, and verification to meet production standards.

## User Review Required

> [!IMPORTANT]
> I will be refactoring `MainActivity.kt` by extracting data models and network interfaces into a proper package structure (`data`, `network`, `ui`). This is a significant structural change but necessary for the long-term health of the project as we add more modules.

## Proposed Changes

### 1. Structural Refactoring
I will move code out of the 1300+ line `MainActivity.kt` into dedicated files:

#### [NEW] [SwellModels.kt](file:///C:/Users/edgar/AndroidStudioProjects/PescaPR/app/src/main/java/com/bradmir/pescapr/data/SwellModels.kt)
*   `SwellResponse`, `HourlySwell`, `ProSwellMetrics`.

#### [NEW] [MarineWeatherService.kt](file:///C:/Users/edgar/AndroidStudioProjects/PescaPR/app/src/main/java/com/bradmir/pescapr/network/MarineWeatherService.kt)
*   `MarineWeatherService` Retrofit interface.

#### [NEW] [ProSwellCard.kt](file:///C:/Users/edgar/AndroidStudioProjects/PescaPR/app/src/main/java/com/bradmir/pescapr/ui/components/ProSwellCard.kt)
*   `ProSwellCard` and `SwellInfoItem` composables.

### 2. Performance Optimization in [MainActivity.kt](file:///C:/Users/edgar/AndroidStudioProjects/PescaPR/app/src/main/java/com/bradmir/pescapr/MainActivity.kt)
*   **Parallel Probing**: Instead of a sequential `for` loop that calls `marineService.getSwellData` one by one (which can take several seconds if the first points fail), I will implement a parallel search using `async/awaitAll`. This will fetch all 13 probe points simultaneously and pick the first successful result.
*   **Cache Result**: Ensure swell data is cached for the current session when switching between spots to avoid redundant network calls.

### 3. Verification & Branding
*   **Update Version**: Change `versionName` to `1.9.0` in `build.gradle.kts` to reflect the completion of Module 1.
*   **Pro UI Polish**: Add a "PRO" badge or distinct styling to the `ProSwellCard` to reinforce the value of the subscription.

### 4. Progress Tracking
*   Mark tasks `v1.9.0` through `v1.9.5` as completed in `TODO_PRO_MIGRATION.md`.

## Verification Plan

### Automated Tests
*   Run `app:assembleDebug` to ensure refactoring didn't break the build.

### Manual Verification
1.  **Swell Data Loading**: Select a coastal spot and verify that the "Signos Vitales" section shows swell metrics (height, period, direction).
2.  **Parallel Loading Speed**: Observe that the swell metrics load faster than the current sequential implementation.
3.  **Pro Gate**: Toggle `isUserPro` in code and verify that the `ProFeatureTeaser` shows up correctly for non-pro users.
