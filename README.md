# dolphy.surf — Android plugin dashboard

An independent Android Studio rebuild project with a dark dashboard, editable plugin tiles, local profiles and permission-based hardware utilities. It does not copy proprietary source, assets, branding, or unsafe plugin execution from the original APK.

## Features in this starter build
- Dark Flipper-inspired interface, editable tile names/icons, up/down ordering, custom accent colours.
- Local JSON profiles: import/export/reset.
- Visual plugin note builder and `.dolphyplugin` JSON descriptor import with allowlisted built-in actions.
- Bluetooth paired-device listing and 10-second BLE discovery (requires Android permissions).
- Wi-Fi status diagnostics, NFC tag details and confirmed NDEF text write, USB device enumeration, phone information.
- No root required. Runs on Android 8+ when built; device capabilities vary.

## Building
1. Install Android Studio with Android SDK 35 and JDK 17.
2. Open this directory as a Gradle project and wait for sync.
3. Use **Build > Build APK(s)**, or `gradle assembleDebug` when Gradle 8.9+ is installed (the Gradle wrapper is not included).
4. Install the generated `app/build/outputs/apk/debug/app-debug.apk` on your own Android device.

This environment lacks an Android SDK/Gradle build toolchain, so an installable APK is not produced or validated here. Test NFC writes with disposable tags; text writes replace existing NDEF data.

## Plugin descriptor example
```json
{"name":"My notes","icon":"◆","type":"note","content":"My custom notes"}
```
Allowed `type`: `note`, `bluetooth`, `wifi`, `nfc`, `usb`, `device`, `plugins`, `appearance`, `profiles`. Existing Dolphy plugin formats may differ; incompatible ones are rejected rather than falsely marked working. Visual Builder currently builds note plugins; it is not an AI code generator or arbitrary Java/Python runtime.

## Security / scope
No DDoS/DoS traffic, spam floods, credential extraction, unauthorized device access, or credit-card cloning. Wireless testing is bounded and uses platform APIs. The GitHub repository must be created separately and this source uploaded; it has not been published.

## Important limitations
The project is an independently authored first-pass rebuild, not a binary patch of the supplied APK or a 1:1 recreation. It has not been compiled against Android SDK in this environment, so runtime and build testing are still required. Android 12+ Bluetooth permissions must be granted. Android 13+ restrictions and hardware-specific NFC capabilities apply. Some Wi-Fi details are redacted by Android unless location permission or location services are active.
