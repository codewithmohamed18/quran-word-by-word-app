# Verification

## Automated

`python scripts/prepare_quran.py` checks the original PDF checksum, all 30 Juz outline destinations, source count (962) and output count (960). Qur’an page content is copied unchanged.

`./gradlew testDebugUnitTest lintDebug assembleDebug` checks page limits, invalid input, bitmap memory allocation and Android API/resource compatibility.

## Phone/emulator acceptance

- Fresh install in airplane mode: opens page 1 without picker or download; count is 960.
- ☰ opens the gold/cream drawer; tapping outside or Back closes it.
- Juz opens the blue chapter panel; test Juz 1 (page 1), 2 (33), 16 (481), 30 (929).
- Bookmark several pages; list bookmarks; remove one; restart and confirm saved bookmarks and last-read page.
- Go to 960; invalid page numbers stay in the entry dialog. Previous/Next stop at document boundaries.
- Pinch, drag, double tap, Fit page, rotate and rapidly navigate. Displayed page and counter must match.
- Continue reading, Instructions and About work from the drawer. System bars do not overlap controls on Android 15.
- First-launch low-storage failure gives a recoverable restart message.

A passing build is not a replacement for phone testing of gestures and layout.
