# CarpetNeed 🧶📏

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android SDK](https://img.shields.io/badge/Android-SDK%2026%2B-brightgreen.svg?style=flat&logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Offline First](https://img.shields.io/badge/Privacy-100%25%20Offline-success.svg?style=flat)](#privacy--offline-guarantee)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**CarpetNeed** is a professional, feature-rich, and completely offline Android application designed to calculate actual carpet purchase requirements based on fixed carpet roll widths. 

Unlike basic room area tools that merely multiply length by width, **CarpetNeed** simulates real-world physical laying constraints. It determines how many full carpet strips are needed, optimizes laying orientation to minimize waste and seams, factors in pattern repeats and seam allowances, and renders an interactive visual cutting plan.

---

## 🚀 Key Features

* **Fixed Roll-Width Physical Consumption Model**: Computes actual roll length needed rather than theoretical room floor area. Supports common standard roll widths (e.g., 4.0 m, 5.0 m, 3.66 m / 12 ft, 15 ft) as well as custom roll widths.
* **Smart Orientation / Best Fit Optimizer**: Automatically evaluates running carpet strips length-wise vs. width-wise and recommends the option that minimizes total carpet purchase area and physical seams. Manual orientation override is also supported.
* **Interactive Visual Cutting Diagram**: Custom Compose Canvas drawing engine renders a scale representation of the room, required carpet strips, seam locations, and color-coded unused width / waste areas.
* **Pattern Repeat & Seam Allowance**: Handles straight-match and half-drop pattern repeat adjustments, seam overlap allowances, and strict pile/pattern direction preservation rules.
* **Complex Room Geometries**: Supports rectangular single rooms, L-shaped room piece decomposition, and secondary room sections (closets, alcoves, entryway bays).
* **Multi-Room Project Aggregation**: Calculate and consolidate total carpet needs across multiple rooms into a unified purchase summary, grouped by roll width.
* **Unit Conversion & Rounding**: Seamlessly switch between Metric (`meters`, `cm`, `mm`, `m²`) and Imperial (`feet`, `inches`, `yards`, `ft²`) units with configurable purchase rounding steps (exact, nearest 0.1m, 0.5m, or 1.0m).
* **Export & Sharing**: Export calculation reports to Plain Text, CSV, and JSON via Android's Storage Access Framework (SAF), or share directly via native Android share sheets.
* **Calculation History**: Stores recent calculations locally for quick access and comparison.
* **Modern Material 3 UI**: Full support for dark/light themes, dynamic colors, and edge-to-edge layouts.
* **100% Offline & Private**: Zero network calls, no permissions required, no cloud backends, no tracking or ads.

---

## 🛠️ Architecture & Tech Stack

CarpetNeed is built following modern Android development practices and clean MVVM architecture:

* **Language**: 100% [Kotlin](https://kotlinlang.org/)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 design system
* **Architecture**: MVVM (Model-View-ViewModel) with Kotlin `StateFlow` and Unidirectional Data Flow (UDF)
* **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
* **Custom Graphics**: Native Compose `Canvas` rendering (`CuttingPlanCanvas`, `RoomDiagramCanvas`)
* **State Management**: Android Jetpack `ViewModel` & Lifecycle runtime
* **Local Persistence**: Key-Value / File storage with local repository abstraction
* **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`) and Version Catalogs (`libs.versions.toml`)

---

## 🧮 Mathematical & Layout Principles

CarpetNeed bridges the gap between floor measurements and retailer ordering rules:

$$\text{Strips Required} = \left\lceil \frac{\text{Coverage Width}}{\text{Roll Width}} \right\rceil$$

$$\text{Base Roll Length} = \text{Strips Required} \times (\text{Coverage Length} + \text{Seam Allowance})$$

The engine cleanly separates:
1. **Geometric Layout Waste**: Unavoidable off-cuts resulting from fixed roll widths vs. room dimensions.
2. **User Planning Allowance**: Additional safety margin percentage or roll length requested by the user.

---

## 📁 Project Structure

```
CarpetNeed/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/carpetneed/
│   │   │   │   ├── calculator/        # Carpet calculation algorithms & orientation engine
│   │   │   │   ├── data/              # History repository, Preferences & SAF Export Manager
│   │   │   │   ├── model/             # Data models (CarpetSettings, CarpetResult, RoomModels, etc.)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/    # Reusable UI cards, inputs & custom Canvas diagrams
│   │   │   │   │   ├── navigation/    # Navigation graph & Screen routes
│   │   │   │   │   ├── screens/       # Home, Cutting Plan, Multi-Room, History, Settings, About
│   │   │   │   │   ├── theme/         # Material 3 Color schemes, Typography & Theme configuration
│   │   │   │   │   └── viewmodel/     # ViewModel & UI State holders
│   │   │   │   └── MainActivity.kt    # Main activity entry point
│   │   │   ├── res/                   # Drawables, mipmaps, strings & XML configuration
│   │   │   └── AndroidManifest.xml    # App manifest configuration
│   │   └── test/                      # Unit tests for carpet calculation engine & orientation algorithms
│   └── build.gradle.kts               # App module build configuration
├── build.gradle.kts                   # Top-level build file
├── gradle.properties                  # Gradle properties & flags
└── settings.gradle.kts                # Subproject settings
```

---

## 🧪 Testing

CarpetNeed includes unit tests covering carpet layout algorithms, piece calculations, orientation optimization, waste percentages, and edge cases.

To run the unit tests:
```bash
./gradlew test
```

---

## 💻 Building & Running

### Prerequisites
* **Android Studio**: Ladybug (2024.2.1+) or newer
* **JDK**: Version 11 or higher (JDK 17 recommended)
* **Android SDK**: Compile SDK 37, Min SDK 26 (Android 8.0+)

### Steps
1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/CarpetNeed.git
   cd CarpetNeed
   ```
2. **Open in Android Studio** and sync Gradle project.
3. **Build the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
4. **Run on Device or Emulator**:
   Select your target Android device in Android Studio and click **Run** (or press `Shift + F10`).

---

## 🔒 Privacy & Offline Guarantee

CarpetNeed respects user privacy:
* 🌐 **Zero Internet Access**: The application does not declare the `android.permission.INTERNET` permission.
* 🛡️ **No Tracking or Telemetry**: No analytics libraries, crash reporters, or advertising SDKs.
* 💾 **100% Local Processing**: All room dimensions, project history, and calculation results remain strictly on your device.

---

## ⚠️ Disclaimer

CarpetNeed provides mathematical estimates based on user-supplied room dimensions and carpet roll parameters. Actual carpet requirements may vary due to room irregularities, installation techniques, carpet pile direction, pattern matching, doorways, and installer practices. Always verify measurements with a qualified carpet installation professional before purchasing materials.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
