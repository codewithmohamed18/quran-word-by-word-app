# Qur’an Word by Word

An offline Android PDF reader for a merged Arabic–English Qur’an, including a 960-page document. Android 8.0 or later. No account, internet permission, advertising, analytics or broad storage permission. The original PDF content is displayed unchanged.

## Install on your phone

1. Open this repository’s **Actions → Build Android APK**.
2. Open a successful run and download **quran-word-by-word-apk** from Artifacts (GitHub login required for this private repository).
3. Extract the ZIP and install `app-debug.apk`. If prompted, allow installation from the app opening the APK.
4. Open **Qur’an Word by Word → Open PDF**. Choose your downloaded merged PDF.
5. The app copies and validates it on the phone. Once imported, reading works in airplane mode, even if the original document provider becomes unavailable.

The debug APK is for personal testing. A Play Store release requires a separate release signing configuration. Android Studio debug builds and CI debug builds may use different signing keys; uninstall an older differently signed build before installing (uninstalling clears local reading data).

## Reading

- Pinch between 1× and 5×, drag to pan, or double tap to zoom / fit. **Fit** restores the whole page.
- **Previous / Next** navigate pages; tap the page counter to jump to any valid PDF page.
- Tap **☆** to bookmark and **★** to remove a bookmark. **Saved** lists bookmarks in page order.
- Reopening the app continues from the last successfully displayed page, including after a restart or rotation.
- Reading position, bookmarks and Juz mappings belong to the PDF’s SHA-256 fingerprint. Selecting the same PDF again restores its data; a different PDF has separate data.

### 30 Juz shortcuts

**30 Juz** lists all 30 shortcuts. On first use, an unset shortcut asks for its start page (prefilled with the current page). Navigate to the Juz’s first page and save its actual PDF page number. Once configured, the shortcut jumps there immediately. Use **30 Juz → Edit starts** to change it.

**30 Juz → Set up / edit → Use source PDF layout** configures all 30 at once after you confirm the layout. The linked all-in-one source was inspected: its outline starts Juz 1 at page 2 and Juz 2 at page 34, then continues in 32-page blocks through Juz 30 at page 930; that file has 962 pages including a cover and final page. For a 960-page merge containing just those 30 blocks in order, the preset starts at 1, 33, 65, …, 929. For the 962-page original, it starts at 2, 34, 66, …, 930. The app never applies either preset solely from page count: confirm that your merge matches, or set starts individually. All locations remain editable.

Numbering is the PDF’s one-based page position, including any covers, rather than printed verse/page numbers. Configured starts must increase with Juz number.

Source supplied by the user: https://haameem7.wordpress.com/2023/01/07/quran-arabic-english-word-by-word-translation-juz-pdf/
The PDF is selected locally and is not bundled or uploaded to GitHub.

## Android Studio

Open this folder, let Gradle sync, select JDK 17 and install Android SDK 35 / Build Tools 35.0.0 when prompted. Run the **app** configuration on a phone or emulator. `local.properties` is local-only; Android Studio creates it for your SDK location.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Windows: `gradlew.bat testDebugUnitTest lintDebug assembleDebug`.
Output: `app/build/outputs/apk/debug/app-debug.apk`.

The official Gradle 8.13 wrapper JAR is checked in. The Gradle distribution is SHA-256 verified. Android Gradle Plugin 8.9.2, Java 17, compile/target SDK 35, min SDK 26. Runtime uses platform APIs only; JUnit is used for unit tests.

## GitHub Actions

`.github/workflows/android.yml` runs on pushes to `main`, pull requests and manual dispatch. It installs SDK components, runs unit tests and Android lint, builds the debug APK, and uploads the APK and verification reports. Artifacts are retained for 30 days. If no run appears, check that Actions is enabled under repository settings.

## Architecture and data

- Storage Access Framework (`ACTION_OPEN_DOCUMENT`) with persisted read access where supported. A verified private copy guarantees offline access and supplies a seekable file to `PdfRenderer`.
- Copy, SHA-256 calculation, validation and rendering use one background executor. Atomic replacement preserves the previous document if copying or validation fails.
- Only the current page bitmap is held, capped at six million pixels. Old pages are recycled; the full 960-page PDF is never loaded as bitmaps. Rapid navigation discards stale render results.
- SharedPreferences stores progress, bookmarks, Juz starts and the selected URI. App data backup is disabled. No data leaves the phone. Clearing app data or uninstalling removes the imported copy and reading data.
- Import requires enough free space for the new PDF in addition to the previous copy. Password-protected, corrupt or unavailable PDFs produce a recoverable message.
- This is a page-image PDF reader: selectable text, OCR, audio, verse search and screen-reader transcription of scanned content are outside the scope.

## Verify on a device

See [TESTING.md](TESTING.md) for the acceptance checklist and automated coverage.
