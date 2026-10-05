# Qur’an Word by Word — 3.2

A Kotlin / Jetpack Compose / Material 3 Android reader with the complete 960-page Arabic–English word-by-word Qur’an bundled. Opens directly to the last-read page. No picker, account, ads, phone download or internet permission. Android 8.0+ (minSdk 26).

## Install

Open this repository’s **Actions → Build Android APK**, select a successful run and download **quran-word-by-word-apk**. Extract and install `app-debug.apk`. The APK is approximately 110 MB; allow additional space for installation and the private PDF copy. The first launch copies the included PDF locally and works in airplane mode.

This version uses a checked-in **testing-only debug key** so future CI debug APKs can update one another while retaining bookmarks/progress. The debug key has the standard public Android debug passwords and is not a secret or a release identity. Older version 1/2 APKs used ephemeral keys, so uninstall those once if Android rejects this update; uninstalling clears local reading data. Never use the test key for a Play Store/production release; configure a private release signing key separately.

## Read and navigate

- Opens in immersive full screen: Android bars, toolbar and page counter are hidden. Single tap shows/hides controls. Pages fit the available space and stay centered without cropping or stretching. The surrounding reading area follows the selected light/dark theme. Cream/gold Material 3 controls remain available.
- Hamburger drawer: Continue Reading, Juz (Para), Surah, Bookmarks, Go to Page, Settings, About.
- 30 Juz and all 114 Surahs in Qur’anic order. Surah rows include Arabic and transliterated names. Some short Surahs share the same PDF page.
- Pinch to zoom up to 6×; double tap to zoom/fit. At fit size, swipe right for the next page and left for the previous page. Next is the left arrow; Previous is the right arrow, following Arabic reading order. At zoom, dragging pans the page.
- Previous/Next controls and page counter jump dialog. Reading menu includes Fit page and Hide controls; a single tap toggles controls.
- Bookmarks and last successfully displayed page persist with Preferences DataStore. Bookmarks have their own screen and can be removed from it.
- Settings: device/light/dark themes and optional keep-screen-awake while the reader is open. PDF page colours remain original in dark mode.

Original interface inspired by the reference’s navigation structure; no copied branding, logos, advertising or third-party UI artwork.

## Correct page mappings

Edit **`app/src/main/assets/navigation.json`** and rebuild. This is the single configurable source for all Juz and Surah destinations. Numbers are one-based PDF page positions, including starts partway down a page. Surah entries contain `number`, `arabic`, `name`, `page`; Juz entries use the same shape. Keep all 30 / 114 entries in order, within pages 1–960. Duplicate Surah page numbers are expected for short chapters.

Examples: At-Tawba starts on 297, Taa-Haa on 497, Ar-Rahmaan on 848, An-Naas on 960. Page starts were checked from the actual scanned edition, using the Surah-opening ribbons / first verses, rather than a standard 604-page Mushaf index. Arabic/transliterated name metadata: https://api.alquran.cloud/v1/surah (downloaded during development; not accessed by the app).

## Bundled edition

User source: https://haameem7.wordpress.com/2023/01/07/quran-arabic-english-word-by-word-translation-juz-pdf/

Build source: https://archive.org/download/quran-arabic-english-word-by-word-translation/quran-arabic-english-word-by-word-translation.pdf

Source SHA-256: `4f6c1a532dc9fe4bec3f09f72ffbb9be66753536b19e268850281d05874adc82`.

The original has 962 pages; its 30 Juz start at pages 2, 34, …, 930. `scripts/prepare_quran.py` verifies its checksum, page count and every Juz outline, and copies original pages 2–961 unchanged into `app/src/main/assets/quran-960.pdf`. The bundled 960 pages exclude the outer cover/final page. Juz starts are 1, 33, …, 929. The generated asset is excluded from git to keep source manageable; **the final APK embeds the entire PDF**, not a remote link.

## Build in Android Studio

Prepare the bundled asset once:

```sh
python -m pip install pypdf==6.1.1
python scripts/prepare_quran.py
```

Or use a verified local original: `python scripts/prepare_quran.py --source /path/to/original-962-page.pdf`.

Open this folder in Android Studio; use JDK 17, SDK 35 and Build Tools 35.0.0. Run **app**.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Windows: `gradlew.bat testDebugUnitTest lintDebug assembleDebug`.
APK: `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions

Every push (all branches), pull request and manual dispatch builds the debug APK. The workflow installs tools, verifies/prepares the PDF asset, compiles, runs unit tests/lint and uploads the APK. It then runs an Android emulator with Wi-Fi/mobile data disabled to test reader/Juz/Surah navigation and DataStore persistence, and uploads screenshots/reports. An APK upload alone does not mean device tests passed: check the overall run result. APK artifacts are retained for 30 days.

## Architecture

- `NavigationConfig`: validated JSON metadata; easy to extend with verse/audio indexes.
- `ReaderStore`: DataStore persistence for bookmarks, position and theme/awake settings; migrates version 2 bundled-reader preferences on first run.
- `PdfEngine`: platform `PdfRenderer`, all native calls serialized on one worker. Bounded preview bitmap; re-renders the visible viewport at higher quality when zoomed, retaining source page detail.
- `ReaderViewModel`: reader state and actions independent of UI.
- `QuranApp`: Compose Material 3 navigation, lists and settings; `PdfPageView` is a Kotlin gesture/rendering view hosted by Compose.

Original source is scanned. Zoom re-renders at screen resolution but cannot invent detail absent in the scan. Text selection, audio, ayah search, Tajweed metadata, favourites and reading statistics are future features, not currently displayed as working controls. No network permission or analytics. See [TESTING.md](TESTING.md).
