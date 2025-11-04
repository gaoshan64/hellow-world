# GPS Compass Android App

This project contains a simple Android application that shows live GPS data with a compass interface. It is written in Kotlin and uses the Android Jetpack libraries.

## Features

- Compass dial that renders the current travel direction (0°–359°) calculated from the latest two GPS fixes.
- Heading value shown both in the center of the compass and as text below the dial.
- Live altitude and speed readings with unit conversions.
- Timestamp that displays how many seconds ago the latest GPS fix was received.
- Settings screen that lets you choose altitude units (meters/feet), speed units (km/h, mph, m/s, knots), and switch between popular languages (system default, English, 简体中文, Español, Français, हिन्दी).
- Runtime location permission handling and graceful feedback while waiting for GPS.

## Project structure

The repository follows the standard Android Gradle layout:

```
hellow-world/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/java/com/example/gpstracker/
│   ├── src/main/res/
│   └── ...
├── build.gradle.kts
├── gradle.properties
└── settings.gradle.kts
```

## Building the APK

1. Open the project in **Android Studio Flamingo (or newer)**.
2. Let Android Studio download the Android Gradle Plugin and synchronize the project. The IDE will generate any missing Gradle wrapper files automatically.
3. Connect an Android device or start an emulator.
4. Use **Build ▸ Make Project** to compile, then **Build ▸ Build Bundle(s) / APK(s) ▸ Build APK(s)** to produce an installable APK located under `app/build/outputs/apk/`.

Alternatively, once the Gradle wrapper has been generated, you can assemble a debug APK from the command line:

```bash
./gradlew assembleDebug
```

The resulting APK can be found at `app/build/outputs/apk/debug/app-debug.apk`.

## Permissions

The app requests `ACCESS_FINE_LOCATION` at runtime. You must grant this permission so that the compass heading, altitude, and speed can update using real GPS data.
