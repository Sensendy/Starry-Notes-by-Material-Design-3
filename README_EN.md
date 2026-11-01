# Starry Notes

A local-first Markdown notes app built with **Material Design 3**. Everything is stored on your device — no network, no ads, no analytics SDKs.

- App name: Starry Notes
- Package: `com.starryweaver.notes`
- Version: 1.8 (versionCode 9)
- Minimum: Android 8.0 (API 26)

---

## Features

### Notes
- Create / edit / delete notes with debounced auto-save, flushed on exit
- Title, `#tags`, pinning
- Search across title, body and tags
- Tag filtering and sorting (recently updated / created / title)
- Single-column and two-column card layouts, remembered across restarts
- Card menu: pin, share, export as Markdown, move to trash
- A newly created note is discarded automatically if both title and body stay empty

### Markdown
- Three editor modes: edit / split live preview / preview only
- Toolbar: H1–H3, bold, italic, strikethrough, inline code, code block, quote, bullet list, ordered list, task item, link, horizontal rule
- Wraps the selection or prefixes lines; tapping again toggles it off
- Full-screen borderless input, with the toolbar docked above the keyboard
- Lightweight built-in renderer: headings, paragraphs, fenced code blocks, quotes, lists, rules, plus inline bold / italic / strikethrough / code / links

### Trash
- Soft delete, restore, delete forever, empty trash

### Import / Export
- Export everything as JSON; import accepts both a full backup and a bare array, merging by id (newer `updatedAt` wins)
- Export a single note as Markdown with frontmatter (title / tags / created / updated)
- Share a note through the system share sheet; receive text from other apps to create a note
- All done through the system file picker — no storage permissions requested

### Appearance
- Light / dark: follow system, light, dark (three-segment switch)
- 6 built-in accent colors plus a custom color (RGB sliders, full accent palette derived via HSL)
- Dynamic color on Android 12+, preferred over the built-in accents when enabled
- Theme switching plays a circular reveal expanding from the touch point

### Others
- First-launch privacy policy gate (cannot be skipped)
- Multi-page onboarding
- About screen: tech stack, developer, open-source licenses with GitHub links

---

## Tech stack

| Item | Version / notes |
| --- | --- |
| Language | Kotlin 2.4.20 |
| Build | Gradle 9.7.1 + AGP 9.4.1 |
| UI | Jetpack Compose (BOM 2026.09.00) + Material 3 |
| Navigation | Navigation Compose 2.10.0 |
| Serialization | kotlinx.serialization (JSON) |
| Storage | JSON file in app-private storage + SharedPreferences |
| Architecture | MVVM + Repository, manual DI |
| SDK | compileSdk / targetSdk 37, minSdk 26 |

No third-party UI, networking, analytics or ad SDKs.

---

## Building

Requirements:

- JDK 17 or newer
- Android SDK Platform 37 and Build-Tools 37.0.0

```bash
# 1. Point Gradle at your SDK (or let Android Studio create it)
echo "sdk.dir=/path/to/android/sdk" > local.properties

# 2. Build the debug APK
./gradlew :app:assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk

# 3. Run unit tests
./gradlew :app:testDebugUnitTest
```

The first build downloads dependencies and takes a while; later builds are incremental.

---

## Project layout

```
app/src/main/java/com/starryweaver/notes/
├── MainActivity.kt              # Entry point, privacy/onboarding gate, theme wrapper
├── NotesApplication.kt          # App-scoped singletons (repositories, long-lived scope)
├── data/                        # Data layer
│   ├── Note.kt                  # Note model (kotlinx.serialization)
│   ├── NoteRepository.kt        # JSON file persistence, CRUD, trash
│   ├── SettingsRepository.kt    # Settings (theme, sort, layout, onboarding/privacy flags)
│   ├── ImportExport.kt          # JSON / Markdown import-export (pure, unit-tested)
│   └── WelcomeNote.kt           # Seed note created on first install
├── util/
│   ├── MarkdownParser.kt        # Block-level Markdown parser (pure, unit-tested)
│   └── MarkdownEdit.kt          # Toolbar insert/wrap helpers (pure, unit-tested)
└── ui/
    ├── theme/                   # Colors, theme, circular reveal animation
    ├── components/              # Note card, Markdown renderer
    ├── notes/                   # Note list (single/double column, search, filter, sort)
    ├── edit/                    # Editor (three modes, toolbar, full-screen input)
    ├── trash/                   # Trash
    ├── settings/                # Settings and Appearance screens
    ├── about/                   # About screen
    ├── onboarding/              # Onboarding
    ├── privacy/                 # Privacy policy
    └── navigation/              # Routes and bottom navigation
```

Tests: 26 JVM unit tests under `app/src/test/`, covering import/export, Markdown parsing and editing, routes and layout enums.

---

## License

Released under the [Apache License 2.0](LICENSE). Copyright 2026 Nx430.

Per section 4 of Apache-2.0, redistributions must retain a copy of this license;
this project does not ship a NOTICE file.

Bundled open-source components (all Apache License 2.0):

- AndroidX (Core, Lifecycle, Activity, Navigation)
- Jetpack Compose
- Material Components for Android
- Kotlin standard library
- kotlinx.serialization
