# Verification

## Automated

`./gradlew testDebugUnitTest lintDebug assembleDebug`

Unit tests cover 960-page bounds, restoring progress into a shorter document, invalid jump input, empty PDFs, partial / ordered / out-of-range Juz maps and the bitmap allocation cap. Lint checks Android API use and resources. GitHub Actions retains test and lint reports alongside the APK.

## Device acceptance checklist

Use a downloaded, unencrypted PDF and Android 8+ (also check Android 15 edge-to-edge). This checklist requires a phone/emulator; passing unit tests is not a substitute.

- Cold launch: all controls remain visible around system bars; Open PDF shows the system picker.
- Cancel selection: existing document remains readable.
- Import the 960-page PDF: page count shows 960; navigate first / last page; invalid jump values stay in the input dialog.
- Pinch / drag / double tap / Fit in portrait and landscape. Rapidly navigate; the counter and displayed page match.
- Bookmark pages 1, 487, 960; remove 487; Saved lists 1 and 960 in order.
- Configure each Juz from its actual PDF start, verify each shortcut and edit a start. Unset shortcuts must ask for a page; no inferred map.
- Navigate to 487; force-stop/relaunch and rotate; confirm restoration to page 487.
- Turn airplane mode on and remove/move the original source PDF; relaunch and verify the imported copy remains usable.
- Import another PDF: its bookmarks / progress / map are independent. Reimport the original identical bytes: original data returns.
- Import a non-PDF renamed `.pdf`, a password-protected PDF or interrupt an import: recoverable error; previous document/data retained. Try a valid PDF afterward.
- Check large PDF import/render responsiveness and memory on a lower-memory phone.

No actual Qur’an PDF is committed as a test fixture; users retain their own copy.
