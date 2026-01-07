# 🚀 Project Tracker: LifeStyle-R Android

## 📋 Project Overview
- **Goal**: Android application for intermittent fasting tracking and client management.
- **Tech Stack**: Kotlin, XML, Clean Architecture, MVVM+MVI, Hilt, Coroutines, Flow, Firebase Firestore, WorkManager/AlarmManager.
- **Current Version**: Development Phase

## ✅ Completed Modules

### 1. 🏗️ Architecture & Setup
- [x] **Clean Architecture Layers**: Data, Domain, Presentation layers established.
- [x] **Dependency Injection**: Hilt set up with `AppModule` and `FirebaseModule`.
- [x] **Navigation**: Navigation Component configured with `AuthActivity` and `MainActivity` flows.
- [x] **Design System**: Material 3 theming and styling applied.

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

### 5. 🗄️ Database & Backend
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
- **2026-01-03**: **Fixed Stuck Polling Timer**. 
    - *Issue*: Timer would get stuck in "Checking..." state.
    - *Fix*: Created `PollingManager` to use `AlarmManager.setExactAndAllowWhileIdle` for short intervals, ensuring reliable execution even in Doze mode. Added "Force" update on settings save.
- **2026-01-05**: **Fixed Auto-Sync Race Condition & Settings Crash**.
    - *Auto-Sync Issue*: Competition between `AlarmManager`, background `Worker`, and foreground `ViewModel` timer caused resets and missed syncs.
    - *Auto-Sync Fix*: Temporarily disabled background `AlarmManager` to isolate foreground logic. Implemented strict 1-minute ticking info-level logs to verify `updatePollingStatus` loop.
    - *Worker Fix*: Corrected critical API action bug in `FastingPollingWorker` (`"login"` -> `"getClientSettings"`).
    - *Crash Issue*: App crashed when navigating to Settings while sync was pending due to `FastingDashboardFragment` updating a destroyed view.
    - *Crash Fix*: Updated `FastingDashboardFragment` to use `viewLifecycleOwner.lifecycleScope` and check for null binding, ensuring updates stop when the view is destroyed.
- **2026-01-05 (PM)**: **Eliminated "Infinite Spinner" & Race Conditions**.
    - *Issue*: Loading overlay persisted too long or blocked valid input; Buttons could be "double-clicked" causing data corruption.
    - *Fix*: completely removed the specific `loadingOverlay` layout. Implemented a synchronous "Instant Lock" in `FastingDashboardViewModel` to block polling interference.
    - *Status*: Verified Overlay is gone. Buttons now use "Instant Lock" (disabled state) without visual dimming.
- **2026-01-06 (AM)**: **Fixed Sync Deadlock Regression**.
    - *Issue*: A safety check added to prevent button spam accidentally blocked the main `loadSettings` task, stopping all sheet updates.
    - *Fix*: Removed the self-blocking guard from `loadSettings` and restricted it only to the background polling loop.
    - *Status*: Verified data synchronization is restored.

## 📂 Documentation
- `DATABASE_SCHEMA_MAP.md`: Full Firestore schema.
- `FIREBASE_SETUP_GUIDE.md` & `INSTRUCTIONS.md`: Setup guides.
- `MIGRATION_GUIDE.md`: Sheets to Firebase migration steps.
