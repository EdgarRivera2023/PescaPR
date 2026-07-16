# Implementation Plan - Community Pins Feature

Add a "Community Pins" toggle to the map screen in `MapaPescapr`, allowing users to see public fishing spots shared by others via Firestore.

## Proposed Changes

### [Component Name]

#### [MODIFY] [MainActivity.kt](file:///C:/Users/edgar/StudioProjects/PescaPR/app/src/main/java/com/bradmir/pescapr/MainActivity.kt)

1.  **State Management**:
    *   Add `pinesComunidad` (snapshot-based list) and `verPinesComunidad` (Boolean toggle).
    *   Add a `LaunchedEffect` that attaches a listener to the Firestore collection `"pines_publicos"` when `verPinesComunidad` is true.
2.  **UI Updates**:
    *   Wrap the `GoogleMap` composable in a `Box`.
    *   Add a floating `Row` with "Mis Pines" and "Comunidad" buttons at the top center of the map.
3.  **Conditional Rendering**:
    *   Update the `GoogleMap` content to render either local spots (`misPuntos`) or community spots (`pinesComunidad`) based on the toggle state.
    *   Use a different marker color (Azure) for community pins to distinguish them.
4.  **Firestore Integration**:
    *   Update the spot saving logic to asynchronously push new spots to the `"pines_publicos"` collection in Firestore when they are created.

## Verification Plan

### Manual Verification
1.  **Toggle Test**: Verify that clicking "Comunidad" loads pins from Firestore and "Mis Pines" returns to local data.
2.  **Visual Distinction**: Confirm community pins appear in Azure while local pins use the custom `pin_pescapr` drawable.
3.  **Persistence**: Save a new pin and verify it appears in Firestore (via Firebase Console) and subsequently in the "Comunidad" tab for other instances.
