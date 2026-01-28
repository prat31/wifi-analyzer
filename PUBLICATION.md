# Publishing to Google Play Store

## 1. Prepare for Release

### Update Version
In `app/build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode = 2 // Increment this
    versionName = "1.1" // Update this
}
```

### Sign the APK/Bundle
You need a keystore to sign your app.
1.  **Generate Keystore** (if you don't have one):
    *   Go to **Build > Generate Signed Bundle / APK**.
    *   Select **Android App Bundle** (recommended for Play Store).
    *   Click **Create new...** under Key store path.
    *   Fill in the details and remember your passwords.

2.  **Configure Signing in Gradle** (Optional but recommended for CI/CD):
    *   Add your signing config to `app/build.gradle.kts` (securely!).

## 2. Build the Bundle
Run the following command to generate a release bundle (`.aab`):
```bash
./gradlew bundleRelease
```
Output: `app/build/outputs/bundle/release/app-release.aab`

## 3. Upload to Play Console

1.  **Create an Account**: Go to [Google Play Console](https://play.google.com/console) and sign up ($25 fee).
2.  **Create App**: Click "Create app", enter the name "WiFi Analyzer", language, and app type.
3.  **Set up Store Listing**:
    *   Upload Screenshots (Phone, Tablet).
    *   Add Short and Full description.
    *   Upload High-res icon (512x512).
4.  **Privacy Policy**: You must provide a URL to a privacy policy since you use Location permissions.
5.  **Releases**:
    *   Go to **Testing > Internal testing** (for initial test) or **Production**.
    *   Click **Create new release**.
    *   Upload the `.aab` file you built in Step 2.
    *   Review and Rollout.

## 4. Privacy Policy Requirement
Since this app uses `ACCESS_FINE_LOCATION`, Google requires a Privacy Policy.
*   **What to say**: "This app uses Location data solely for the purpose of scanning for nearby WiFi networks as required by the Android Operating System. This data is not stored, transmitted, or shared with third parties."
