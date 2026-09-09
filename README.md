# LG webOS TV Remote Control Android App — Phase 1

## Overview
This is a native Android application for discovering, pairing with, and controlling LG Smart TVs running webOS. Development follows a strict phased approach.

**Phase 1 Focus:** Local network TV discovery, SSAP WebSocket communication, pairing/registration flow, encrypted client-key storage, auto-reconnection, and connection state management.

---

## Phase 1 Scope & Architecture

```
+-------------------------------------------------------+
|                    Android App                        |
|                                                       |
|  +-------------------------------------------------+  |
|  | Compose UI (ConnectTvScreen / Device Cards)     |  |
|  +------------------------+------------------------+  |
|                           |                           |
|  +------------------------v------------------------+  |
|  | ConnectTvViewModel                              |  |
|  +------------------------+------------------------+  |
|                           |                           |
|  +------------------------v------------------------+  |
|  | TvConnectionRepository / Implementation         |  |
|  +------------+-----------------------+------------+  |
|               |                       |               |
|  +------------v------------+ +--------v------------+  |
|  | TvDiscoveryService      | | SsApWebSocketClient |  |
|  | (SSDP + Subnet Scan)    | | (ws:3000 / wss:3001)|  |
|  +-------------------------+ +--------+------------+  |
|                                       |               |
|  +------------------------------------v------------+  |
|  | PairedTvStorage (EncryptedSharedPreferences)    |  |
|  +-------------------------------------------------+  |
+---------------------------+---------------------------+
                            |
                     Local Wi-Fi / LAN
                            |
                            v
+-------------------------------------------------------+
|                    LG webOS TV                        |
|                                                       |
|  - SSDP / UPnP Discovery Service                      |
|  - SSAP WebSocket Service (ws://IP:3000, wss://:3001) |
|  - Prompt / PIN Pairing & Client-Key Verification     |
+-------------------------------------------------------+
```

---

## Key Features Implemented in Phase 1

1. **Automatic Device Discovery**:
   - Primary: SSDP UPnP UDP multicast query on `239.255.255.250:1900` (`urn:lge-com:service:webos-second-screen:1`).
   - Fallback: Local Wi-Fi subnet port scan on port 3000.
   - Deduplication by IP and MAC address.

2. **Dedicated SSAP WebSocket Layer (`SsApWebSocketClient`)**:
   - Opens WebSocket connections to `ws://TV_IP:3000` or `wss://TV_IP:3001` (webOS 4.0+ TLS fallback).
   - Sends registration payload containing full LG webOS permissions manifest.
   - Handles PROMPT and PIN pairing requests returned by webOS.
   - Listens for `client-key` emission upon user approval on TV screen.

3. **Encrypted Client-Key Persistence (`PairedTvStorage`)**:
   - Uses `androidx.security.crypto.EncryptedSharedPreferences` backed by Android Keystore (AES256 GCM).
   - Stores TV ID, TV Name, IP address, Port, MAC address, Client Key, and last connection timestamp.
   - Fallback to isolated private preferences with error logging if keystore initialization fails.

4. **Auto-Reconnection & DHCP IP Recovery**:
   - Automatically reconnects to the previously paired TV on app launch using the stored client-key.
   - Identifies TV by MAC address during discovery to update IP address if DHCP changes it.

5. **Explicit Sealed Connection State (`TvConnectionState`)**:
   - `Disconnected`
   - `Discovering`
   - `Connecting(device)`
   - `WaitingForPairing(device, pairingType)`
   - `Connected(device)`
   - `Reconnecting(device, attempt)`
   - `ConnectionFailed(device, errorMessage)`
   - `PairingRequired(device)`

6. **Phase 1 UI**:
   - Clean dark theme Compose interface displaying available LG TVs, status banner, confirmation modal, PIN entry modal, and manual refresh.

---

## LG webOS Communication & Pairing Flow

```
Android App                                            LG webOS TV
    |                                                      |
    |---- SSDP M-SEARCH (239.255.255.250:1900) ----------->|
    |<--- SSDP Response (IP, Name, Location XML) ---------|
    |                                                      |
    |---- Open WebSocket (ws://TV_IP:3000) --------------->|
    |<--- WebSocket Open ----------------------------------|
    |                                                      |
    |---- TX: register payload (clientKey / null) -------->|
    |                                                      |
    |  [First-time Pairing Path]                           |
    |<--- RX: response (pairingType: PROMPT) --------------|
    |     (TV displays "Allow connection?" prompt)        |
    |     [User accepts on TV screen]                      |
    |<--- RX: type: "registered", payload: client-key -----|
    |     (App securely saves client-key)                  |
    |                                                      |
    |  [Reconnection Path]                                 |
    |---- TX: register payload (with saved client-key) --->|
    |<--- RX: type: "registered" --------------------------|
    |     (Connected immediately without TV prompt)        |
```

---

## Required Permissions (`AndroidManifest.xml`)

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_MULTICAST_STATE" />
```

`android:usesCleartextTraffic="true"` is enabled specifically for local network WebSocket (`ws://TV_IP:3000`) communication with LG webOS TVs.

---

## Physical TV Test Procedure (16-Step Verification Checklist)

1. Connect Android phone and LG webOS TV to the exact same Wi-Fi network.
2. Enable **LG Connect Apps** / **Mobile Connection** in LG TV Network Settings.
3. Build and launch the app: `./gradlew installDebug` or run from Android Studio.
4. Verify the screen displays "Searching for LG TVs..." or "Available Devices".
5. Confirm your LG webOS TV appears in the list with its name and IP address.
6. Tap on the discovered TV card.
7. Tap **Connect** in the confirmation dialog.
8. Observe the TV screen: a pairing prompt ("Allow app to connect?") or PIN code should appear.
9. Accept the request on your TV screen (or enter PIN in the app if prompted).
10. Verify the app status updates to **Connected to [TV Name]** and displays the "Paired" badge.
11. Force close the Android app.
12. Relaunch the Android app.
13. Verify the app recognizes the previously paired TV and reconnects automatically without showing a prompt on the TV.
14. Turn off Wi-Fi on the phone or turn off TV power; verify the app transitions gracefully to `ConnectionFailed` or `Disconnected` state with helpful text.
15. Turn Wi-Fi / TV back on and tap **Refresh**; verify discovery and reconnection succeed.
16. Run unit tests to verify automated logic: `./gradlew test` (or `gradlew app:testDebugUnitTest`).

---

## Troubleshooting

- **TV Not Found**:
  - Ensure TV is turned on and connected to the same Wi-Fi router.
  - Disable AP Isolation / Guest Network mode on router.
  - Ensure "LG Connect Apps" option is ON in LG TV Settings -> Network.
- **Pairing Rejected**:
  - Clear paired device from app and re-try pairing.
  - In LG TV settings, clear remembered mobile devices under Settings -> Connection -> Mobile Connection.
- **Cleartext Traffic Error**:
  - Verify `android:usesCleartextTraffic="true"` is set in `AndroidManifest.xml`.

---

## Future Roadmap

- **Phase 2**: Basic Remote Control (D-pad, OK, Back, Home, Vol+/Vol-, Mute, Ch+/Ch-, Power).
- **Phase 3**: Advanced Controls (Numeric pad, Media playback controls, Input source switcher).
- **Phase 4**: Pointer & Touchpad (WebSocket pointer input, trackpad gestures).
- **Phase 5**: On-screen Keyboard & Text Search Sync.
- **Phase 6**: App Launcher (Discover installed webOS apps and deep-link launcher).
- **Phase 7**: Multi-TV Management, Saved Profiles, and Wake-on-LAN.
