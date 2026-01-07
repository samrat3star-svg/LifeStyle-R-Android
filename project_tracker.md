# 🚀 Project Tracker: LifeStyle-R Android

## 📋 Project Overview
- **Goal**: Android application for intermittent fasting tracking and client management.
- **Tech Stack**: Kotlin, XML, Clean Architecture, MVVM+MVI, Hilt, Coroutines, Flow, Firebase Firestore, WorkManager/AlarmManager.
- **Current Version**: 1.0.1 Stable

## ✅ Completed Modules

### 1. 🏗️ Architecture & Setup
- [x] **Clean Architecture Layers**: Data, Domain, Presentation layers established.
- [x] **Dependency Injection**: Hilt set up with `AppModule` and `FirebaseModule`.
- [x] **Navigation**: Navigation Component configured with `AuthActivity` and `MainActivity` flows.
- [x] **Design System**: Material 3 theming with DayNight adaptive support.

### 2. 🔐 Authentication & Users
- [x] **Login Flow**: 
    - Inputs: Registration Code + Password.
    - Initial Password = Mobile Number.
- [x] **Security**: 
    - Force Password Change logic on first login logic defined.
    - Token-based auth structure ready.
- [x] **Repository**: `AuthRepository` with local (Preferences) and remote (Firestore/Mock) data sources.
- [x] **UI**: `LoginFragment` mapped to ViewModel with StateFlow/MVI.

### 3. ⏱️ Fasting Tracking (Core Feature)
- [x] **Dashboard UI**: `FastingDashboardFragment` displaying timer and status.
- [x] **Logic Implementation**: 
    - `PollingManager`: **(CRITICAL FIX)** Centralized manager for background sync.
    - **Hybrid Strategy**: Uses `WorkManager` for intervals ≥ 15 mins, and `AlarmManager` for high-frequency (< 15 mins) precision.
    - **Rescue Mechanism**: Auto-resets timers if they drift into the past.
- [x] **Background Work**: `FastingPollingWorker` and `FastingPollingReceiver`.

### 4. ⚙️ Settings & Configuration
- [x] **Settings UI**: `SettingsFragment` to toggle polling and alarms.
- [x] **Local Storage**: `PreferenceManager` to persist user preferences.
- [x] **Real-time Updates**: Changing settings immediately reconfigures the `PollingManager`.
- [x] **Default-On Logic**: Alarms and Sync (Timer) are ON by default for new installs.
- [x] **Adaptive UI**: Full DayNight support for correct readability in Dark mode.

### 5. 🛠️ Release & Updates
- [x] **In-App Update System**: Version-agnostic update checks using GitHub Releases.
- [x] **Hardened Installation**: Uses `FileProvider` to resolve "Parsing Error" and "Open With" prompts.
- [x] **Smart Cleanup**: Automatically purges old APK files from device storage.
- [x] **Version Detection**: Handles "1.0" vs "1.0.1" semver comparisons correctly.

### 6. 🗄️ Database & Backend
- [x] **Schema Design**: Comprehensive Firestore map for `users`, `fasting_sessions`, and `form_submissions`.
- [x] **Migration Strategy**: Guide and scripts created for migrating legacy Google Sheets data to Firebase.
- [x] **Firestore Setup**: Dependencies added and `google-services.json` workflow documented.

## 🚧 In Progress / Next Steps
- [ ] **Patient Management**: Implement CRUD for Clients/Patients (Currently placeholders).
- [ ] **Appointments**: Implement Scheduler UI and Logic.
- [ ] **Profile**: Complete User Profile editing and viewing.
- [ ] **Registration**: Build out the `RegisterFragment` (currently placeholder).
- [ ] **Sync Verification**: End-to-end testing of data sync between devices.

## 🐛 Recent Fixes & Improvements
- **2026-01-08 (PM)**: **Finalized v1.0.1 (Optimized Release)**.
    - *Performance*: Implemented "Full Sync" strategy to pre-fetch all data (Settings, Stats, Breaks) in a single API call, reducing tab navigation latency to zero.
    - *Caching*: Optimized Fragments to use silent in-memory caching, eliminating redundant loading spinners.
    - *UI Polish*: Restored original high-contrast Light theme for Measurements and Breaks screens.
    - *Navigation*: Centered headings in history fragments for improved aesthetic balance.
- **2026-01-08 (AM)**: **Released v1.0.1 Stable**.
    - *UI Fix*: Implemented stable DayNight theme support via `values-night` resources.
    - *Readability*: Ensured all dialogs are readable in both Light and Dark modes.
    - *Defaults*: Set Alarms and Sync Timer to be enabled by default for better user engagement.
    - *Stability*: Resolved startup crashes caused by global theme overrides.
    - *Update Hardening*: Integrated `FileProvider` and smart APK cleanup.
- **2026-01-03**: **Fixed Stuck Polling Timer**. 
    - *Issue*: Timer would get stuck in "Checking..." state.
    - *Fix*: Created `PollingManager` to use `AlarmManager.setExactAndAllowWhileIdle` for short intervals, ensuring reliable execution even in Doze mode. Added "Force" update on settings save.
- **2026-01-05**: **Fixed Auto-Sync Race Condition & Settings Crash**.
    - *Auto-Sync Issue*: Competition between `AlarmManager`, background `Worker`, and foreground `ViewModel` timer caused resets and missed syncs.
    - *Worker Fix*: Corrected critical API action bug in `FastingPollingWorker` (`"login"` -> `"getClientSettings"`).
    - *Crash Fix*: Updated `FastingDashboardFragment` to use `viewLifecycleOwner.lifecycleScope` and check for null binding, ensuring updates stop when the view is destroyed.
- **2026-01-05 (PM)**: **Eliminated "Infinite Spinner" & Race Conditions**.
    - *Fix*: completely removed the specific `loadingOverlay` layout. Implemented a synchronous "Instant Lock" in `FastingDashboardViewModel`.
- **2026-01-06 (AM)**: **Fixed Sync Deadlock Regression**.
    - *Issue*: A safety check accidentally blocked the main `loadSettings` task.
    - *Fix*: Removed the self-blocking guard from `loadSettings`.

## 📂 Documentation
- `DATABASE_SCHEMA_MAP.md`: Full Firestore schema.
- `FIREBASE_SETUP_GUIDE.md` & `INSTRUCTIONS.md`: Setup guides.
- `MIGRATION_GUIDE.md`: Sheets to Firebase migration steps.
