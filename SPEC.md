# Zo Computer Android — PIMPED Edition Spec

## What It Is

A fully-native Android client for the Zo Computer platform, wrapping brodiblanco's live Zo instance. Every feature the web can do, plus hardware-level control no web app can touch.

## Stack

- **Package:** `com.bxthre3.zocomputer`
- **Min SDK:** 26 (Android 8.0) | **Target SDK:** 35 (Android 15)
- **UI:** Jetpack Compose + BX3 Design System v2
- **Architecture:** MVVM + Clean Architecture
- **DI:** Hilt
- **Networking:** Retrofit2 + OkHttp4 + KotlinX Serialization
- **Local DB:** Room
- **Background:** WorkManager + Foreground Service
- **Biometrics:** AndroidX Biometric API

## Feature Spec

### 1. Authentication
- Biometric unlock (fingerprint / face) with fallback to handle + password
- Secure token storage via EncryptedSharedPreferences
- Session token refresh with silent retry
- Auto-logout on token expiry

### 2. WebView Core
- Full-screen Chromium WebView loading `https://brodiblanco.zo.computer`
- WebView JavaScript interface bridge for native ←→ JS communication
- WebViewCache via OKHTTP interceptor (no stale cached pages)
- Pull-to-refresh + swipe navigation
- Downloads intercepted and handled by native DownloadManager
- In-app URL filtering: external links open in Chrome Custom Tab

### 3. AgentOS Panel (Native Overlay)
Slide-up bottom sheet over WebView showing:
- 19-agent roster with live status (ACTIVE/IDLE/DND)
- Active task count badge
- Quick actions: 3 preset task creation buttons
- Agent detail cards: name, role, department, completion rate, last seen

### 4. Native Notifications
- Firebase Cloud Messaging (FCM) push token registration
- Notification channel: AgentOS Alerts (high priority), System (default)
- Notification types: P1 escalations, task assignments, agent completions
- Tap notification → deep link to relevant WebView route

### 5. Hardware Access
- Camera: QR code scanning for agent/business card verification
- Microphone: Voice-to-text for hands-free agent commands
- Bluetooth: Discover + connect to nearby Irrig8 field sensors (BLE 4.0)
- NFC: Read/write NDEF tags for sensor pairing
- Sensors: Accelerometer for device orientation, battery state
- Vibrate: Haptic feedback on agent status changes

### 6. System Integration
- Foreground service with persistent notification: "Zo Computer running"
- Background sync every 15 min via WorkManager (respects battery optimization)
- App Shortcuts: 4 dynamic shortcuts (Dashboard, Agents, Tasks, Settings)
- Widget: 4x2 AgentOS status widget showing active/idle/pending counts
- Screen pinning: Lock device to WebView-only mode (kiosk)
- Edge-to-edge display with proper system bar insets

### 7. Security
- No root detection bypass — detect and warn
- Screenshot protection: `FLAG_SECURE`
- Certificate pinning for API calls
- ProGuard/R8 obfuscation on release
- Debug detection: disable FCM + analytics in debug builds

### 8. Settings
- Handle display + avatar
- Biometric toggle
- Notification preferences (per type)
- Cache size display + one-tap clear
- About: version, build, agent uptime
- Dark/Light/System theme toggle

## Screens

1. **SplashScreen** — Logo animation, biometric prompt if enrolled
2. **LoginScreen** — Handle + password + biometric fallback
3. **MainScreen** — WebView + AgentOS bottom sheet + nav
4. **SettingsScreen** — All preferences
5. **AgentDetailScreen** — Full agent profile from AgentOS API

## API Endpoints

```
GET  /api/agentos/status         → AgentOS health + metrics
GET  /api/agentos/agents         → All 19 agents
GET  /api/agentos/tasks          → Active tasks
POST /api/agentos/tasks           → Create task
GET  /api/agentos/org            → Org chart
GET  /api/ota/zocomputer         → OTA update manifest
```

## Design

- BX3 Design System v2 with AgentOS theme variant
- Status colors: ACTIVE=#22C55E, IDLE=#EAB308, DND=#EF4444
- Font: System default (Roboto)
- Dark mode: Pure black (#000000) background, white text
