# 2Split - Kotlin Multiplatform Mobile App

A mobile application built with Kotlin Multiplatform (KMM) supporting iOS and Android platforms.

## Project Structure

- **shared/** - Shared Kotlin Multiplatform code
  - `commonMain/` - Common code for both platforms
  - `androidMain/` - Android-specific implementations
  - `iosMain/` - iOS-specific implementations

- **androidApp/** - Android application built with Kotlin

- **iosApp/** - iOS application built with Swift

## Prerequisites

- Kotlin 1.9.21
- Android SDK 34
- Xcode 14+
- macOS 12+
- JDK 11+

## Building

### Android
```bash
./gradlew :androidApp:build
```

### iOS
```bash
cd iosApp
pod install
open 2Split.xcworkspace
```

## Technologies

- **Kotlin Multiplatform** - Shared code between iOS and Android
- **Kotlin** - Android native code
- **Swift** - iOS native code
- **Gradle** - Build system

## License

MIT
