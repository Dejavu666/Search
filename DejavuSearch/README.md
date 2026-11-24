# DEJAVU Search 0.1.0

A lightweight Android search app for TECNO LJ9 (and other Android devices). It sends Google Search requests to Chrome with configurable language, region, and SafeSearch preference.

## Features
- Search through Google in Google Chrome.
- Optional `hl=en` (English interface/language hint).
- Optional `gl=us` (United States country hint).
- SafeSearch preference selector:
  - Google default (no `safe` parameter)
  - Filter (`safe=active`)
  - Blur (`safe=moderate`)
  - Off requested (`safe=off`)
- Shows exactly which preferences the app intends to send.
- No INTERNET permission; no search history or analytics stored by this app.

## Important limitation
The app can set request parameters, but it cannot guarantee Google's enforcement outcome. Google may override or lock SafeSearch due to account, device, network, policy, or regional/legal requirements. The status panel deliberately reports the **requested preference**, not a verified Google-side state. The app does not disguise the device's physical location or claim to bypass regional restrictions.

`gl=us` is a search-country hint, not a VPN or proof that the request originates in the United States. `hl=en` is a language hint, not a change to the Google account's country.

## Build
1. Install Android Studio with Android SDK Platform 35.
2. Open this folder in Android Studio.
3. Allow Gradle sync to download dependencies.
4. Connect the TECNO LJ9 with USB debugging enabled, or use an emulator.
5. Select the `app` run configuration and press Run.

To build an APK from Android Studio: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

## Technical
- Kotlin + Jetpack Compose + Material 3
- minSdk 23, targetSdk 35
- package: `id.dejavu.search`
- Chrome package target: `com.android.chrome`
