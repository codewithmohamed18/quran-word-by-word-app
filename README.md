# Tajweed & Meaning — supplied Soft Cream v1.7

This repository replaces the previous Qur’an Word by Word project with the supplied **Tajweed-and-Meaning-v1.7-Soft-Cream.apk** and an iOS port of its complete offline reader.

## Downloads

Open [Releases](https://github.com/codewithmohamed18/quran-word-by-word-app/releases/latest).

- **Android:** install the APK. It is the exact uploaded, signed v1.7 file, not a reconstructed APK. SHA-256: `5550e1c06b7843f8f4bbd27314c743201b4ce2e05e17878fc724e9991070c497`.
- **iOS unsigned IPA:** a real iPhone/iPad build, but it must be signed with an Apple account before installation. Downloading it alone does not install an app.
- **iOS Simulator ZIP:** open using Xcode's Simulator; it does not run on an iPhone.
- **Signed iOS IPA:** generated only when the repository's Apple signing secrets are configured. An Ad Hoc IPA installs only on devices registered in its provisioning profile.

Public iPhone distribution requires TestFlight or the App Store and an Apple Developer account. GitHub cannot bypass Apple's signing and device rules.

This repository must be **public** for anyone to access its downloads. If it is private, the owner and authorized collaborators can access them. Change visibility at Settings → General → Danger Zone → Change repository visibility. No signing keys are committed here.

## Reader

All three supplied modes and their content are retained: original Arabic-English word-by-word pages with Soft Cream theme, colour Tajweed with Hilali–Khan meanings, and the original 13-line colour-coded Tajweed Mushaf. The supplied interface includes Juz/Surah navigation, search, bookmarks, reading history, last page, dark mode, full-screen display, page animations and right-to-left page turns. Content is bundled and reading does not need internet.

The Android download is unmodified. Its application identifier is `com.khalid.tajweedmeaning`; it is a separate app from the former `com.codewithmohamed.quranwordbyword`, so old app bookmarks are not automatically migrated. The original Android native source and signing key were not included with the APK; this repository does not claim to recover them.

The iOS app uses the exact embedded HTML, JavaScript, text, fonts and PDFs, plus a Swift/UIKit/WKWebView bridge and Core Graphics PDF renderer. iOS keeps its own reading state in the persistent WebKit store. The Android volume-button setting has no effect on iOS because iOS does not provide a supported app API to repurpose its volume buttons.

## Build iOS on a Mac

1. Install Xcode and XcodeGen (`brew install xcodegen`).
2. Run `python3 scripts/prepare.py` from the repository root.
3. Run `cd ios && xcodegen generate`.
4. Open `ios/TajweedMeaning.xcodeproj`, choose the TajweedMeaning scheme and an iPhone Simulator, then Run.
5. For your own iPhone, select your Apple development team and a unique bundle identifier in Xcode's Signing & Capabilities.

`scripts/prepare.py` reconstructs the supplied APK from binary parts and checks its full checksum before extracting the offline assets. Parts keep every Git blob comfortably below GitHub's file limits. They are not downloads from third-party servers, and no runtime network fetch is needed.

## GitHub Actions

Every main-branch push verifies the exact Android APK, builds an unsigned iOS device app, runs iPhone Simulator tests, and publishes both platforms to GitHub Releases after checks pass. Test results are attached to the workflow run. The Xcode project is generated from the checked-in `ios/project.yml`.

To enable a signed Ad Hoc iOS IPA, add repository Actions secrets:

| Secret | Value |
| --- | --- |
| `APPLE_CERTIFICATE_P12_BASE64` | Base64-encoded Apple Distribution certificate and private key (.p12) |
| `APPLE_CERTIFICATE_PASSWORD` | Password protecting that .p12 |
| `APPLE_PROVISIONING_PROFILE_BASE64` | Base64-encoded Ad Hoc provisioning profile matching `com.codewithmohamed18.tajweedmeaning` and including the intended devices |

Certificates are imported into a temporary CI keychain and removed afterward. Do not put them in a commit or issue. These credentials are not needed for simulator or unsigned builds.

## Attribution

The APK's `SOURCE-LICENSE.txt`, source acknowledgements, fonts, Qur’an data and translation notices are preserved in the extracted bundle and About screen. The embedded MIT attribution credits Abubakr Elmallah. Content provenance is described in the supplied app's About screen; no new Qur’an text or translation is substituted.
