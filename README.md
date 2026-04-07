# Zo Computer Android App

**Package:** `com.bxthre3.zocomputer`  
**Version:** 1.0.0  
**Target:** brodiblanco.zo.computer

A native Android wrapper for your Zo Computer private workspace.

## Build Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34
- JDK 17

### Build Release APK
```bash
cd /path/to/zo-computer-android
./gradlew :app:assembleRelease
```

APK output: `app/build/outputs/apk/release/app-release.apk`

### Build Debug APK
```bash
./gradlew :app:assembleDebug
```

## Features
- Full-screen WebView of brodiblanco.zo.computer
- Pull-to-refresh
- Back button navigation support
- Material 3 dark theme
- Secure connection (authenticated)

## App Details
| Property | Value |
|----------|-------|
| Application ID | com.bxthre3.zocomputer |
| Min SDK | 28 (Android 9) |
| Target SDK | 34 (Android 14) |
| Compile SDK | 34 |
