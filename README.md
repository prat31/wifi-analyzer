# WiFi Analyzer

A modern Android application to scan and analyze WiFi networks, built with Jetpack Compose and Material 3.

## Features
- **Scan WiFi Networks**: View a list of available networks with signal strength and frequency.
- **Detailed Metrics**: View detailed information like SSID, BSSID, Signal Quality, Channel Width, and Capabilities.
- **Modern UI**: Clean Material 3 interface with Dark Mode support.
- **Privacy Focused**: Requests only minimal location permissions required for scanning.

## Building the App

This project uses Gradle with Kotlin DSL.

1.  **Open in Android Studio**:
    *   Open Android Studio (latest Hedgehog or Iguana recommended).
    *   Select "Open" and navigate to the project root.
    *   Wait for Gradle Sync to complete.

2.  **Build via Command Line**:
    ```bash
    ./gradlew assembleDebug
    ```
    The APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

## Testing

### Unit Tests
Run unit tests (if available):
```bash
./gradlew testDebugUnitTest
```

### Running on Emulator

#### Prerequisites
*   Android Studio installed.
*   An Android Virtual Device (AVD) created via Device Manager.

#### Steps
1.  **Launch Emulator**: Open Device Manager in Android Studio and start your AVD.
2.  **Run App**: Click the "Run" (Green Play) button in Android Studio, selecting the Emulator as the target.
3.  **Simulate WiFi**:
    *   **Note**: Standard Android Emulators generally **do not** support real WiFi scanning. They simulate a connection to "AndroidWifi".
    *   To see the UI in action, the app will scan and likely show an empty list or just the simulated connection.
    *   **Recommendation**: To test the actual scanning logic, use a physical Android device.

#### Cross-Platform Emulator Notes
*   **macOS/Windows/Linux**: The steps are identical within Android Studio.
*   **Genymotion**: If you use Genymotion, it has better WiFi simulation capabilities than the standard AVD.

## Permissions
The app requires `ACCESS_FINE_LOCATION` to scan for WiFi networks (an Android OS requirement). It prompts for this permission on first launch.
