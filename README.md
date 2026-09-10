# SAND WORKS — Industrial Sand Loading & Wage Distribution Android App

**SAND WORKS** is an offline-first, single-user Android application engineered for sand mining sites and tractor carting operations. It replaces manual paper notebooks with automated trip tracking, whole-rupee wage distribution calculations, worker attendance, daily reconciliations, and cryptographic backup/restore.

---

## Key Features

### 1. Offline-First Architecture (Single User)
- Pure local persistence via Android **Room SQLite**.
- Designed for remote quarry and riverbed environments with zero network connectivity.
- No role-based restrictions—one unified operator console manages the entire site.

### 2. High-Precision Money Engine
- Strict whole-rupee math ensuring total loader shares + driver trip incentives equal the gross trip rate down to 0 paise deviation.
- Supports full-share loaders and half-share loaders.
- Any integer remainder is distributed fairly using a deterministic round-robin algorithm.

### 3. Fleet & Trip Logging
- Rapid trip entry with visual tractor cards (`Tractor 1`, `Tractor 2`, `Tractor 3`).
- Multi-select labourers with instant live wage preview.
- "Save & Add Next Trip" flow for continuous loading cycles.
- Single-loader warnings to prevent accidental fat-finger allocation.

### 4. Workstations & Operations
- **Driver Workstation**: Tracks driver shifts, daily trip counts, daily base allowances, and accumulated trip earnings.
- **Labourer Workstation & Leaderboard**: Real-time ranking of top loaders by trips and earnings across daily and custom date ranges.
- **Attendance Center**: Quick attendance logging with one-tap "Auto-Mark Loaded Workers as Present".

### 5. Daily Reconciliation & Closure
- Compare gross earnings against distributed wages and remaining balances.
- Formal "Close Day" state machine locking daily ledgers against retroactive edits without an audit trail.

### 6. Local Data Health & Backup
- Built-in SQLite database integrity diagnostics.
- One-click JSON backup generation and restore.

---

## Tech Stack
- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose (Material 3)
- **Database**: Room SQLite (KSP)
- **Architecture**: MVVM with Kotlin Coroutines & `StateFlow`
- **Build System**: Gradle 9 with Kotlin DSL (`build.gradle.kts`)

---

## Build & Installation

### Option 1: Download from GitHub Releases
1. Navigate to the [Releases](https://github.com/devara1983ntr/sandworks-android/releases) section.
2. Download `app-debug.apk` onto your Android phone.
3. Tap the file to install and open.

### Option 2: Build Locally from Source
Ensure Java 17 and Android SDK are installed, then run:
```bash
# Clone the repository
git clone https://github.com/devara1983ntr/sandworks-android.git
cd sandworks-android

# Build debug APK
./gradlew assembleDebug

# The APK will be generated at:
# app/build/outputs/apk/debug/app-debug.apk
```
