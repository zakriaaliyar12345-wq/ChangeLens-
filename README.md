# ChangeLens

A visual time machine for documenting and comparing how real-world things change.

## Features

- Create projects to document renovations, plant growth, restoration, DIY projects, and more
- Capture photos over time with captions, dates, and notes
- Compare before/after with a draggable slider or transparency overlay
- Add annotations and markers to photos
- Export progress reports as self-contained HTML
- Share comparison images via native Android sharing
- Fully offline-first — all data stored locally
- Multilingual: English, Persian (fa-IR), Dari (fa-AF) with RTL support
- Material 3 theming with dark mode

## Build

```bash
./gradlew assembleDebug   # Debug APK
./gradlew assembleRelease # Signed release APK
```

## Test

```bash
./gradlew testDebugUnitTest          # 44 unit tests
./gradlew connectedDebugAndroidTest # 6 instrumentation tests
```

## Architecture

- Java + XML layouts (no Kotlin, no Compose)
- Room database with LiveData and ViewModel
- RecyclerView with DiffUtil
- SharedPreferences for settings
- FileProvider for sharing
- JobScheduler for background maintenance

## License

MIT
