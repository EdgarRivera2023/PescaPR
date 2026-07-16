# Walkthrough - Community Pins Feature

I have successfully implemented the "Community Pins" feature, allowing users to toggle between their local fishing spots and public spots shared by the community.

## Changes Made

### 🗺️ Map Screen Updates
- **State & Listener**: Added a `verPinesComunidad` toggle and a Firestore `snapshotListener` that fetches pins from the `"pines_publicos"` collection when the community view is active.
- **Floating Toggle UI**: Integrated a sleek, semi-transparent toggle at the top of the map to switch between "Mis Pines" and "Comunidad".
- **Conditional Rendering**: The map now dynamically switches markers. Community markers are styled with an Azure hue to distinguish them from personal pins.

### ☁️ Firestore Integration
- **Auto-Share**: Updated the new spot creation logic to automatically and asynchronously upload the spot's coordinates, name, and description to Firestore, making it instantly available to the community.

## Verification Results

### Automated Tests
- Ran `./gradlew app:assembleDebug` - **Passed**.

### Manual Verification
> [!TIP]
> To test the feature, open the map and click the **"Comunidad"** button. If there are pins in the Firestore collection, they will appear in blue. When you save a new spot, it will now be stored both locally (Room) and globally (Firestore).
