# Tajweed & Meaning — v1.8.5 clearer word pages & organized settings

[Download releases](https://github.com/codewithmohamed18/quran-word-by-word-app/releases). Choose **Tajweed-and-Meaning-v1.8-Adaptive.apk** for Android. The original [v1.7 release](https://github.com/codewithmohamed18/quran-word-by-word-app/releases/tag/v1.7-1) remains available unchanged.

## What changed in v1.8.5

- Mode 1 uses lossless display frames and memory-bounded rendering levels, plus gentle edge clarity that can be disabled.
- Settings have eight ordered, searchable sections, with explicit mode-specific headings.
- Save up to eight named reading setups, restore them with one tap, and include them in offline backups.
- Reset the current mode’s layout without erasing bookmarks or changing other modes’ layouts.

## Existing v1.8.3 Mushaf improvements

- Mode 3 now trims only the unused scanned margins at rendering time, retaining all detected ink, headings, border, side notes and printed page numbers. The PDF itself is unchanged. `config/plain-crops.json` records 850 page-specific bounds; preparation recomputes and verifies every rectangle.
- Mode 3 defaults to full-page focus, with only menu/bookmark corner controls and no floating footer. Tap the page to restore the full toolbar. Settings can enable the footer, secondary controls or standard reading view.
- PDF magnification 100–250%, saved per PDF mode. The gesture setting chooses page turns or horizontal panning. Start at the Arabic right-hand edge. Existing pinch zoom remains available.
- Google Play reader feedback informed this focus on small-screen enlargement and overlays that do not obscure text: https://play.google.com/store/apps/details?id=org.haris.quran

## Existing v1.8.2 improvements

- Full-page focus view has safe floating menu/mode/bookmark and RTL page controls instead of full toolbars. Optional launch-in-focus and an unobstructed-page toggle. System safe areas remain enforced on Android and iOS.
- Reading comfort presets: original v1.7, larger relaxed reading (scrolling PDF/46px Arabic/24px English), quiet evening, and full-page focus.
- Word-by-word paper options now include warm parchment and high-contrast monochrome. Adjustable page contrast, paper warmth and an optional movable line guide. Original appearance remains the default; warmth changes displayed Tajweed colours only when explicitly enabled.
- Swipe page turns can be disabled; buttons remain usable. All changes preserve bundled text/translation and work offline.

## Existing v1.8.1 improvements

- Restores v1.7 full-page PDF fill in all orientations by default. Arabic and English text sizes in mode 2 remain the original readable sizes; no automatic shrinking.
- Four PDF layouts, saved independently for word-by-word and plain: v1.7 full page, original proportions, fit width with vertical scrolling, and crop to fill. Crop can hide edges; use fit width when preserving page shape with larger text matters.
- Standard compact safe-area toolbar; optional larger controls. Tap-to-hide controls can be disabled. External keyboard right arrow advances, left arrow goes back, following Arabic page order.
- Bookmark notes, page progress, page dimmer, Arabic/English line spacing, daily page goal and local reading statistics. Statistics count unique pages per mode/day and foreground reading time, excluding menus/background time.
- Offline copy/paste backup and validated restore for bookmarks, notes, settings, last pages and statistics. Reset display settings keeps reading data. Backups from the separate v1.7 app are not automatically accessible.
- Android version code 19 updates an existing adaptive v1.8 installation; iOS marketing version 1.8.1/build 19.

## Existing v1.8 improvements

- Native safe-area layout excludes Android status bars, display cutouts, navigation bars and the keyboard. Fullscreen retains cutout protection.
- Dedicated Reading modes button, large toolbar targets, responsive phone/tablet and landscape controls. The compact toolbar preserves reading space; pinch zoom remains available.
- Original three offline reading modes, Arabic page direction, themes, bookmarks and navigation retained.
- Lossless PDF cleanup preserves every original compressed image stream. Page sizes/rotation are verified, and first/middle/last page raster comparisons must match exactly.
- Android renders seekable PDF assets directly from the APK through a storage proxy: no extra full-size PDF copies in private storage. The APK itself remains large because it contains two complete high-quality scanned PDFs. Lossless cleanup saves about 1.1 MB; installed storage saves roughly another 185 MB compared with copying both PDFs.

**Installation:** v1.8 uses a separate application ID and installs alongside v1.7. The supplied APK's signing key/source were not provided, so this is not an in-place update. Existing v1.7 bookmarks stay in v1.7; v1.8 starts its own state. Future v1.8 debug builds use the checked-in debug certificate; it is for testing, not a production signing secret.

## Android source and build

Android source is in `android/src/main`. It uses Android's native WebView and PdfRenderer (Android 8 / API 26 or newer), with no network or storage permission. `web-overrides` contains the small responsive additions to the supplied app's own HTML/assets. It retains the original supplied app's title and content.

Install JDK 17, Python 3, Android SDK platform 35 and build-tools 35.0.0, set `ANDROID_HOME`, then run:

```sh
pip install PyMuPDF==1.26.6
python scripts/prepare-v18.py
bash scripts/build-android.sh
```

The output is `dist/Tajweed-and-Meaning-v1.8-Adaptive.apk`. This complete repository can be opened as a folder in Android Studio; the platform build script is the authoritative build and does not require Gradle dependencies.

Every push to main builds Android and iOS in GitHub Actions. Android instrumentation uses actual pointer taps to verify modes/bookmarks, rendered pages from both bundled PDFs, bookmark persistence, minimum touch targets, native safe areas and absence of duplicate PDFs. It runs with Wi-Fi/data disabled on cutout, compact phone, landscape and tablet Android 15 display profiles. Screenshots and test logs are uploaded as `android-checks` artifacts. Emulator checks are representative, not a claim of testing every physical phone.

## iOS

The UIKit/WKWebView port is in `ios/`, with XcodeGen project configuration and native Core Graphics PDF rendering. Its native view already follows safe-area constraints; it receives the same responsive controls and lossless assets in v1.8. The build runs iPhone simulator UI tests.

An **UNSIGNED IPA cannot be installed directly** on an iPhone. Sign locally or configure Apple certificate/provisioning secrets; the optional signed export works only for provisioned devices. Public iPhone distribution requires TestFlight or App Store distribution. The simulator ZIP is for Xcode's simulator only.

## Original supplied app archive

The exact supplied v1.7 APK is stored in `payload/` as binary chunks because of repository size limits. `payload-manifest.json` records its checksum and asset hashes. `scripts/prepare.py` reconstructs and verifies it before extraction. These archived bytes are never changed by the adaptive build. The original APK is included alongside v1.8 in releases.

The repository is currently private. Downloads require repository access until its owner makes it public. No advertisements, login, file picker or online PDF download are added.


### 1.8.4 reading improvements

Swipe right for the next page and left for the previous page in both PDF modes. The master switch is in Settings. The new Horizontal gesture setting chooses page turns (default, also at PDF magnification) or sideways PDF panning. Native pinch zoom pans until zoomed back out; vertical scrolling stays available in either gesture mode.

Mode 1 → Reading comfort → Word-by-word study enlarges the original scan to 200% and enters a safe full-screen view. Use the up/down section controls to move across and down with overlap, then advance to the next page at the end. Choose 50%, 75% or 90% screen steps in Settings. No PDF words are cut, rearranged or rewritten. Choose Original v1.7 to restore the original full-page size. Mode 1 can optionally open in full-page view on future launches.


### 1.8.5 page clarity and settings

Mode 1 uses lossless PNG frames instead of JPEG recompression, with Clear (default), Fast and Maximum rendering levels. Bitmap dimensions are capped by a memory budget on Android and iOS. A smaller three-frame cache bounds retained image data, and stale quality requests cannot replace the selected quality. Optional gentle/strong edge clarity improves edge emphasis without reconstructing or changing the source Arabic/translation. Mode 3 retains its rendering pipeline. The original scanned images are about 765 × 1040 pixels; no rendering setting can recover detail absent from those scans. The PDF image streams remain byte-for-byte unchanged.

Settings now use eight ordered sections, with mode-specific headings and relevant default expansion. Search filters settings and restores the prior section state when cleared. Saved reading setups preserve the selected mode, focus state and settings (up to eight named setups), are stored offline and included in validated backups. Reset this mode’s layout leaves other modes’ layouts, bookmarks and reading history intact.
