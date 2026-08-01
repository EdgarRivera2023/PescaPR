# Implementation Plan - Preview for MainTabsScreen

The goal is to create a `@Preview` for the `MainTabsScreen` composable in `MainActivity.kt`. Since `MainTabsScreen` has many direct dependencies (Room Database, Firebase, Subscription Manager), I will extract a stateless `MainTabsContent` composable to facilitate previewing.

## User Review Required

> [!IMPORTANT]
> To enable previewing, `MainTabsScreen` will be refactored to separate the state/logic from the UI structure. This involves extracting a new `MainTabsContent` composable.

## Proposed Changes

### [PescaPR App](file:///C:/Users/edgar/AndroidStudioProjects/PescaPR/app/src/main/java/com/bradmir/pescapr/MainActivity.kt)

#### [MODIFY] [MainActivity.kt](file:///C:/Users/edgar/AndroidStudioProjects/PescaPR/app/src/main/java/com/bradmir/pescapr/MainActivity.kt)

1.  **Extract `MainTabsContent`**:
    *   Create a new `@Composable` function `MainTabsContent` that takes all necessary UI states and callbacks, including the content for each tab.
    *   Move the `Column`, `Row` (header), `TabRow`, `HorizontalPager`, and `VersionNote` logic into this new composable.
2.  **Refactor `MainTabsScreen`**:
    *   Update `MainTabsScreen` to handle state and dependency initialization.
    *   Call `MainTabsContent` with the initialized states and provide the actual tab screens as content.
3.  **Add `@Preview`**:
    *   Add `MainTabsScreenPreview` at the end of `MainActivity.kt`.
    *   Use `PescaPRTheme` for the preview.
    *   Provide mock data for `MainTabsContent` in the preview.

## Verification Plan

### Automated Tests
*   `analyze_current_file` to ensure no syntax errors.
*   `render_compose_preview` to verify the preview renders correctly.

### Manual Verification
*   Check that the app still runs as expected after the refactoring.
