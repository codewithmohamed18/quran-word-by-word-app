# Qur’an Word by Word — 2.0

An offline Android reader with the full 960-page Arabic–English Qur’an included. Opens directly into reading; no PDF picker, account, first-run download or Juz setup. Android 8.0+.

## Install

Download **quran-word-by-word-apk** from a successful **Actions → Build Android APK** run, extract it and install `app-debug.apk`. The APK includes the Qur’an and is approximately 100 MB. It copies the included PDF into private app storage once on first launch, so allow roughly 220 MB for installation and reading storage.

The previous and new CI APKs may have different debug signing certificates because GitHub runners generate ephemeral debug keystores. If Android rejects an update due to signatures, uninstall the old test app first (this clears its local bookmarks/progress). Personal testing APK; Play Store distribution needs release signing and appropriate content permissions.

## Interface

- Compact reading toolbar and full-page Qur’an display.
- Tap **☰** for the cream slide-out menu with a gold Arabic header.
- **Juz (chapters)** opens a blue list of all 30 Juz with correct page starts.
- **Bookmarks**, **Continue reading**, **Go to page**, **Fit page**, **Instructions** and **About** are in the menu.
- Tap **☆ / ★** to add or remove a bookmark. Previous / Next and the page counter appear below the page.
- Pinch to zoom, drag to pan, double tap to zoom or fit. Last successfully displayed page restores automatically, including after rotation and restart.

The interface is inspired by the supplied navigation screenshots. The Arabic–English PDF page content is preserved; this does not convert it into the other app’s Tajweed edition. No ads or unrelated links are displayed.

## Included Qur’an

User-provided source: https://haameem7.wordpress.com/2023/01/07/quran-arabic-english-word-by-word-translation-juz-pdf/

Build source: https://archive.org/download/quran-arabic-english-word-by-word-translation/quran-arabic-english-word-by-word-translation.pdf

Source SHA-256: `4f6c1a532dc9fe4bec3f09f72ffbb9be66753536b19e268850281d05874adc82`.

The original has 962 pages. Its outline identifies Juz 1–30 beginning at one-based pages 2, 34, 66, …, 930. `scripts/prepare_quran.py` verifies the checksum, page count and all 30 outline boundaries, then copies original pages 2–961 without changing page content. It excludes the outer cover and final page to produce the requested 960-page edition. Juz starts are then pages 1, 33, 65, …, 929. The resulting PDF also includes 30 outline bookmarks.

The PDF asset is generated at build time and excluded from git. **The resulting APK contains the entire PDF**; the phone does not contact the source website. Source availability is needed only when preparing a fresh build.

## Android Studio

1. Install Python and prepare the asset before the first build:

```sh
python -m pip install pypdf==6.1.1
python scripts/prepare_quran.py
```

Alternatively `python scripts/prepare_quran.py --source /path/to/original-962-page.pdf` uses an existing copy after verifying it.

2. Open this folder in Android Studio. Use JDK 17 and Android SDK 35 / Build Tools 35.0.0.
3. Run the **app** configuration. The build gives a clear error if the bundled PDF is missing.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Windows: `gradlew.bat testDebugUnitTest lintDebug assembleDebug`.
Output: `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions

Runs on pushes to `main`, pull requests and manual dispatch. Installs build tools and Python, prepares the verified PDF asset, runs unit tests and lint, builds the APK, and uploads the APK and reports. APK artifacts remain available for 30 days.

## Implementation

Platform `PdfRenderer` with one background executor and a single current-page bitmap capped at six million pixels. A first-launch atomic copy provides a seekable private file. PDF page content is not OCR’d or re-typeset. Progress and bookmarks use SharedPreferences. No internet or broad storage permission is declared; no analytics. Text selection, verse search, audio and a Surah index are not implemented.

See [TESTING.md](TESTING.md) for verification coverage.
