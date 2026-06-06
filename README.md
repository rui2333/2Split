# 2Split - Kotlin Multiplatform Bill Splitting App

A sophisticated mobile application for splitting bills between friends, built with Kotlin Multiplatform (KMM) supporting both iOS and Android platforms.

## Features

- **Create bill splits** - Easily set up a new split with multiple people
- **Upload receipts** - Add receipts via camera or photo library
- **OCR processing** - Automatic extraction of line items from receipts (foundation for future implementation)
- **Review items** - Verify and edit extracted items
- **Flexible splitting** - Choose between:
  - **Even split** - Divide bill equally among participants
  - **By item** - Assign each line item to specific people
  - **Custom split** - Create custom ratios for specific shares
- **Automatic calculations** - Real-time settlement calculations
- **Cross-platform** - Seamless experience on iOS and Android

## Project Structure

```
2Split/
├── shared/                          # Kotlin Multiplatform shared code
│   ├── src/commonMain/
│   │   ├── kotlin/com/split/shared/
│   │   │   ├── models/             # Data models (Person, Split, Item, etc.)
│   │   │   ├── domain/             # Business logic (SplitCalculator)
│   │   │   └── repository/         # Repository interfaces
│   ├── src/androidMain/            # Android-specific implementations
│   └── src/iosMain/                # iOS-specific implementations
│
├── androidApp/                      # Android application (Jetpack Compose)
│   ├── src/main/kotlin/com/split/android/
│   │   ├── MainActivity.kt          # Entry point with navigation
│   │   ├── screens/                 # Jetpack Compose UI screens
│   │   │   ├── HomeScreen.kt        # Create new split
│   │   │   ├── ReviewItemsScreen.kt # Review extracted items
│   │   │   └── SplitMethodScreen.kt # Choose split method
│   │   └── ui/theme/               # Theme configuration
│   └── build.gradle.kts
│
├── iosApp/                          # iOS application (SwiftUI)
│   ├── 2SplitApp.swift              # App entry point
│   ├── SplitModels.swift            # Data models mirroring shared code
│   ├── SplitCalculator.swift        # Business logic calculator
│   ├── SplitViewModel.swift         # MVVM view model
│   ├── HomeView.swift               # Create new split
│   ├── ReviewItemsView.swift        # Review extracted items
│   └── SplitMethodView.swift        # Choose split method
│
├── build.gradle.kts                 # Root build configuration
├── settings.gradle.kts              # Module configuration
├── gradle.properties                # Gradle properties
└── README.md                        # This file
```

## Data Models

### Core Models (shared across platforms)

**Person**
- `id`: Unique identifier
- `name`: Person's name
- `emoji`: Optional emoji for avatar

**Item**
- `id`: Unique identifier
- `receiptId`: Reference to source receipt
- `name`: Item description
- `quantity`: Number of items
- `price`: Price per item

**Split**
- `id`: Unique identifier
- `name`: Split name
- `people`: List of participants
- `receipts`: List of uploaded receipts
- `items`: Line items to be split
- `splitMethod`: How the bill is split
- `createdAt`: Creation timestamp

**Settlement**
- `personId`: Person identifier
- `personName`: Person's name
- `amount`: Amount owed/owed to

## Business Logic

### SplitCalculator

The core calculator for determining who pays what:

**Even Split**: Divides the total bill equally among all participants
```
Total: $77.40 ÷ 2 people = $38.70 each
```

**By Item Split**: Assigns each item to specific people and sums their totals
```
Item assignments: Pizza → You, Salad → Sam, etc.
```

**Custom Split**: Uses custom ratios to divide the bill
```
Example: 60/40 split between two people
```

## Architecture

### Android (Jetpack Compose)
- **UI Layer**: Declarative UIs with Jetpack Compose
- **State Management**: Jetpack ViewModel
- **Navigation**: Compose Navigation
- **Data**: In-memory repository (can be extended with Room/SQLite)

### iOS (SwiftUI)
- **UI Layer**: SwiftUI for declarative UI
- **State Management**: @ObservedObject ViewModel pattern
- **Navigation**: NavigationStack for modern SwiftUI navigation
- **Data**: In-memory storage with SplitViewModel

### Shared Code
- **Models**: Data classes with serialization support
- **Domain Logic**: Pure business logic (SplitCalculator)
- **Repository Pattern**: Interface for data persistence

## Technologies

### Shared (Kotlin Multiplatform)
- **Kotlin**: 1.9.21
- **kotlinx-serialization**: JSON serialization support

### Android
- **Jetpack Compose**: 1.5.4
- **Material3**: 1.1.2
- **Navigation Compose**: 2.7.5
- **Lifecycle ViewModel**: 2.6.2
- **Kotlin Coroutines**: 1.7.3

### iOS
- **SwiftUI**: Modern iOS UI framework
- **Foundation**: Core APIs

## Setup & Building

### Prerequisites
- Kotlin 1.9.21
- Android SDK 34 (minSdk 21)
- Xcode 14+ (for iOS)
- macOS 12+
- JDK 11+
- Gradle 7.6+

### Android Build
```bash
# Build debug APK
./gradlew :androidApp:assembleDebug

# Build release APK
./gradlew :androidApp:assembleRelease

# Run on connected device/emulator
./gradlew :androidApp:installDebug
```

### iOS Build
```bash
cd iosApp

# Open in Xcode
open -a Xcode .

# Or build from command line
xcodebuild -scheme 2Split -configuration Debug
```

## Future Enhancements

### Phase 2: Receipt OCR
- Integrate ML Kit Text Recognition (Android)
- Integrate Vision Framework (iOS)
- Automatic item extraction from receipt images
- Item quantity and price parsing

### Phase 3: Advanced Features
- Cloud sync with Firebase/CloudKit
- Multiple bill history and archive
- Payment tracking and reminders
- Tip splitting customization
- Expense categorization
- Photo management and storage

### Phase 4: Social Features
- Group management
- Payment settlements with push notifications
- Activity history and audit logs
- Recurring splits
- Currency support

### Phase 5: Payments Integration
- Connect payment providers (Stripe, PayPal)
- Direct payment settlement
- Payment request sharing
- Transaction history

## File Structure by Responsibility

### Models (Data Layer)
- `shared/src/commonMain/kotlin/com/split/shared/models/Models.kt`
  - Person, Item, Receipt, Split, Settlement, Transaction
  - SplitMethod sealed class for splitting strategies

### Business Logic (Domain Layer)
- `shared/src/commonMain/kotlin/com/split/shared/domain/SplitCalculator.kt`
  - Even split calculation
  - Item-by-item split calculation
  - Custom ratio split calculation
  - Settlement to transaction conversion

### Data Persistence (Repository Layer)
- `shared/src/*/kotlin/com/split/shared/repository/SplitRepository.kt`
  - Create, read, update, delete operations
  - Android implementation (in-memory)
  - iOS implementation (in-memory)

### Android UI
- `androidApp/src/main/kotlin/com/split/android/`
  - MainActivity: Navigation setup
  - screens/: HomeScreen, ReviewItemsScreen, SplitMethodScreen
  - ui/theme/: Color scheme and Material3 theming

### iOS UI
- `iosApp/`
  - 2SplitApp.swift: App entry point
  - HomeView, ReviewItemsView, SplitMethodView: Screen UIs
  - SplitViewModel: State management
  - SplitModels.swift & SplitCalculator.swift: Shared logic in Swift

## Code Sharing Strategy

1. **Models**: Mirrored in both Kotlin (commonMain) and Swift
   - Allows type-safe access in native code
   - Serialization handled per platform

2. **Business Logic**: Replicated in both
   - Ensures identical calculations across platforms
   - Easier than FFI for complex algorithms
   - Can be unified in future with Kotlin/Native improvements

3. **UI**: Platform-specific
   - Jetpack Compose for Android
   - SwiftUI for iOS
   - Each leverages native UI capabilities

## Configuration Files

### `gradle.properties`
- Android SDK versions (compileSdk: 34, minSdk: 21)
- Kotlin code style settings

### `build.gradle.kts` (root)
- Plugin versions: Kotlin 1.9.21, Android 8.1.3
- kotlinx-serialization plugin

### `settings.gradle.kts`
- Project name: "2Split"
- Modules: ":shared", ":androidApp"

### `shared/build.gradle.kts`
- Kotlin multiplatform configuration
- Dependencies: Kotlin stdlib, kotlinx-serialization
- iOS targets: iosX64, iosArm64, iosSimulatorArm64

### `androidApp/build.gradle.kts`
- Jetpack Compose dependencies
- Material3 theme
- Navigation Compose
- Kotlin Coroutines

## Testing

### Unit Tests (Shared)
```bash
./gradlew :shared:test
```

### Android Instrumentation Tests
```bash
./gradlew :androidApp:connectedAndroidTest
```

### iOS Unit Tests
```bash
xcodebuild test -scheme 2Split
```

## Contributing

When adding new features:

1. **Models**: Add to `shared/src/commonMain/kotlin/...`
2. **Logic**: Add to `shared/src/commonMain/kotlin/.../domain/`
3. **Android UI**: Add to `androidApp/src/main/kotlin/.../screens/`
4. **iOS UI**: Add to `iosApp/`
5. **Tests**: Add corresponding test files

## License

MIT License - See LICENSE file for details

## Author

Built with Kotlin Multiplatform Mobile
