# Tests and verification

## Automated build checks

`./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest`

Unit tests check page bounds, invalid jump input, bitmap caps and the editable chapter configuration (all 30 Juz / 114 Surahs, names/order/ranges and known starts). The PDF preparation script verifies source SHA-256, original count/outline and final 960-page count. Representative rendered pages were compared with the original and were pixel-identical.

## Offline Android device test

`./gradlew connectedDebugAndroidTest` on an emulator or connected Android device.

GitHub Actions disables emulator Wi-Fi/data before launching the app. `OfflineReaderTest` opens the included reader, checks Juz 2 → 33 and Ar-Rahmaan → 848, creates a bookmark, recreates the Activity, verifies the same page/bookmark, opens the dedicated Bookmarks screen, and changes dark mode/awake settings. Screenshots are retained for reader, drawer, Surah list, bookmarks and settings.

## Manual follow-up

Check on a physical phone: pinch/double-tap/pan and zoom sharpness, swipe direction and boundaries, invalid jump dialog, full-screen controls restore, rotation during first-copy/render, font sizing, and screen-awake behaviour. A bounded bitmap preview/viewport is held; the 960 pages are never all loaded as bitmaps. Inspect representative Surah destinations, especially starts partway down a page and multiple short Surahs on the same page.
