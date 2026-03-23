# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-03-23

### Added
- TV discovery via mDNS (NSD) with saved device persistence
- TV connection via AndroidRemoteTv (PIN pairing flow)
- Remote control pad: D-pad, OK, Back, Home, Power, Volume +/-
- Haptic feedback on all button presses
- Multi-language support (12 locales: EN, ES, FR, PT, ZH, VI, JA, RU, DE, Klingon, Latin, Esperanto)
- Pro version with Google Play Billing (lifetime + monthly subscription)
- AdMob banner ads (hidden for Pro users)
- Firebase Crashlytics integration

### Architecture
- Clean Architecture: `domain/` → `data/` → `presentation/`
- Dependency Injection with Hilt (`@Binds` + `@Provides`)
- MVVM with ViewModel + StateFlow + Jetpack Compose
- SOLID principles applied throughout
- No magic strings — centralized constants (`AppConstants`, `BillingConstants`, `PrefsKeys`)
- Debug-only logging with `BuildConfig.DEBUG` guards
- ProGuard/R8 enabled for release builds
- Semantic versioning from v0.1.0
