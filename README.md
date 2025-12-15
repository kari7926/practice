# Smart Speaker Tester

Smart Speaker Tester is a Jetpack Compose (Material 3) Android app for running automated command-and-listen workflows against a smart speaker. It is visually inspired by iPhone UI patterns (large titles, grouped sections, segmented controls) and uses a single-activity architecture with Navigation Compose.

## Architecture overview
- **UI**: Compose screens under `ui/` (Home, Commands Preview, Running Test, Results) with Navigation Compose driving a single activity.
- **State management**: `SmartSpeakerViewModel` holds immutable `SmartSpeakerUiState` exposed as `StateFlow`, backed by a coroutine-driven state machine (`TestState` enum: Idle → Speaking → Listening → Paused → Completed/Stopped/Error).
- **Parsing**: `parsing/` contains `CommandParser` implementations for CSV (`CsvCommandParser`) and XLSX (`XlsxCommandParser`) that skip empty rows and headers containing "command".
- **Audio/TTS**: `tts/DefaultTtsController` wraps `TextToSpeech` with en-US female/male voice selection; `audio/ReplyEndDetector` listens via `AudioRecord` to detect reply end using RMS energy and silence duration.
- **Domain**: `domain/RangeValidator` enforces custom start/end ranges; `TestRunOptions` represents test selection.
- **Data**: `data/TestCommand` defines command entries; `UiLog` and `TestSummary` live near the ViewModel.

## Build & run
Prerequisites: Android Studio Koala or later with Android SDK 34 and JDK 17.

1. Clone the repository and open in Android Studio.
2. Let Android Studio download dependencies and configure the SDK.
3. Connect a device or start an emulator (API 26+).
4. Run the **app** configuration.

### Command-line
```bash
./gradlew assembleDebug
./gradlew assembleRelease
```
APK outputs:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### Release signing
1. Generate a keystore (run once):
   ```bash
   keytool -genkeypair -v -keystore release.keystore -alias smartspeaker -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Add credentials to `~/.gradle/gradle.properties` (do not commit):
   ```properties
   RELEASE_STORE_FILE=/absolute/path/to/release.keystore
   RELEASE_STORE_PASSWORD=yourStorePassword
   RELEASE_KEY_ALIAS=smartspeaker
   RELEASE_KEY_PASSWORD=yourKeyPassword
   ```
3. Wire signing in `app/build.gradle.kts` using the above properties. The provided Gradle file already reads these properties if present.
4. Build the release APK: `./gradlew assembleRelease`.

## Features
- Storage Access Framework file picker accepting `.xlsx` and `.csv`.
- Header detection ("command" / "commands") and empty-row skipping.
- TTS voice selection (female default, male fallback when available), en-US locale.
- Automated loop: speak command → delay → listen for reply end via RMS/silence thresholds → proceed, logging timeouts as "uncertain end".
- Pause/Resume/Stop/Skip controls, keep-screen-on during tests, friendly error handling.
- Test options: All or custom start/end indices with validation.
- Results summary and recent log view.

## Testing
Run JVM unit tests:
```bash
./gradlew test
```
Includes range validation, parser correctness, and state machine progression tests.
