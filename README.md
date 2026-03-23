# Minimal TV Remote

Android app to control Android TV devices via the Android TV Remote Protocol v2.

## Features

- 📡 **Auto-discovery** — Finds TVs on your network via mDNS (NSD)
- 🔐 **PIN Pairing** — Secure connection using the PIN displayed on your TV
- 🎮 **Remote Control** — D-pad, OK, Back, Home, Power, Volume +/-
- 📳 **Haptic Feedback** — Vibration on every button press
- 🌍 **12 Languages** — EN, ES, FR, PT, ZH, VI, JA, RU, DE, Klingon, Latin, Esperanto
- 💎 **Pro Version** — Remove ads with lifetime purchase or monthly subscription
- 🔥 **Crashlytics** — Crash reporting via Firebase from v0.1.0

## Architecture

```
com.remote.app/
├── domain/          ← Pure Kotlin: models + repository interfaces
├── data/            ← Android implementations (NSD, BillingClient, AndroidRemoteTv)
├── presentation/    ← ViewModel + Jetpack Compose UI
├── di/              ← Hilt modules (@Binds interface → impl)
└── i18n/            ← Localization strings
```

- **Pattern**: MVVM with ViewModel + StateFlow
- **DI**: Dagger Hilt
- **Async**: Coroutines + Flow
- **UI**: Jetpack Compose + Material 3

## Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK 34

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-user/real-remote-app.git
   cd real-remote-app
   ```

2. **Add Firebase config**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a project (or use an existing one)
   - Add an Android app with package name `com.remote.app`
   - Download `google-services.json` and place it in `app/`

3. **Open in Android Studio**
   - File → Open → select the project root
   - Wait for Gradle sync to complete

4. **Run the app**
   - Select a device or emulator (API 24+)
   - Click ▶️ Run

> **Time to run**: ~3 minutes from clone to first build.

### Build Release

```bash
./gradlew assembleRelease
```

The release build has ProGuard/R8 enabled with minification and resource shrinking.

## Configuration

| Config | Location | Description |
|--------|----------|-------------|
| AdMob IDs | `app/build.gradle.kts` → `buildConfigField` | Test IDs by default, replace for production |
| Billing Product IDs | `domain/BillingConstants.kt` | `pro_lifetime_399`, `pro_monthly_099` |
| App Version | `app/build.gradle.kts` | Semantic versioning (`MAJOR.MINOR.PATCH`) |
| Firebase | `app/google-services.json` | Required for Crashlytics |

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 1.9.24 |
| UI | Jetpack Compose + Material 3 |
| Architecture | Clean Architecture + MVVM |
| DI | Dagger Hilt 2.50 |
| Async | Coroutines + StateFlow |
| TV Protocol | AndroidRemoteTv (protobuf + BouncyCastle) |
| Ads | Google AdMob |
| Billing | Google Play Billing 6.2.0 |
| Crash Reporting | Firebase Crashlytics |
| Build | Gradle Kotlin DSL, AGP 8.2.0 |

## License

All rights reserved.
