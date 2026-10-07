<div align="center">

<img src="docs/images/banner.svg" alt="Tajweed and Meaning — a quiet space for the Quran" width="100%">

### Read • Understand • Reflect

A Qur’an reader with word-by-word study, colour-coded Tajweed and a quiet full-page reading experience.

**[Download Android v1.9](https://github.com/codewithmohamed18/quran-word-by-word-app/releases/tag/v1.9)** · **[Open GitHub website](https://codewithmohamed18.github.io/quran-word-by-word-app/)** · **[Open ChatGPT website](https://tajweed-meaning-ipad.lazify.chatgpt.site/)**

![Modes](https://img.shields.io/badge/Reading-5_modes-207965?style=flat-square)
![Android](https://img.shields.io/badge/Android-v1.9-c7a45c?style=flat-square)
![Offline](https://img.shields.io/badge/Offline-Bundled_%2B_downloadable_pages-207965?style=flat-square)

</div>

## A look inside

<table><tr>
<td align="center"><img src="docs/images/mode5.jpg" width="260" alt="16-line colour-coded Tajweed filling the reading screen"><br><strong>16-line Tajweed</strong><br>More Arabic, less empty space.</td>
<td align="center"><img src="docs/images/mode1.jpg" width="260" alt="Arabic-English word-by-word reading"><br><strong>Word-by-word study</strong><br>Read and understand together.</td>
<td align="center"><img src="docs/images/settings.jpg" width="260" alt="Settings organized by reading mode"><br><strong>Make it yours</strong><br>Settings organized by mode.</td>
</tr></table>

*Real screenshots of the reader packaged in v1.9. Android uses the device’s native safe areas around the reading screen.*

## Five ways to read

| Mode | What you’ll find |
| --- | --- |
| **1 · Word by word** | Original Arabic–English scans with study enlargement and reading controls. |
| **2 · Tajweed & meaning** | Indo-Pak Arabic styling, colour-coded text, Hilali–Khan meaning and ayah bookmarks. |
| **3 · 13-line Mushaf** | Colour-coded Tajweed scans with a quiet full-page view. |
| **4 · Study the Noble Qur’an** | All three volumes in one reading collection. |
| **5 · 16-line Tajweed** | All 561 source pages, searchable Surah navigation and six page layouts. |

Mode 5 preserves the original Arabic, ayah numbers and printed Tajweed colours. Choose **16-line full screen**, **Fit Arabic area** or **Arabic width and scroll**. Choose **Original complete page** to see every printed margin note. Covers and reference pages remain available.

## Start reading

### Android v1.9

1. Open the [v1.9 release](https://github.com/codewithmohamed18/quran-word-by-word-app/releases/tag/v1.9).
2. Download **Tajweed-and-Meaning-v1.9.apk**.
3. Open it and allow installation from that source when Android asks.
4. Open **Tajweed & Meaning 1.9** and choose a reading mode.

Android 8 or newer is required. Modes **1, 2, 3 and 5 are bundled** for offline reading. Mode 4 uses its online source pages and the reader’s download option. The APK is large because it includes the original high-quality scans.

v1.9 installs as a **separate app** from the older adaptive reader. Existing apps and their bookmarks stay intact. Bookmarks do not automatically transfer between apps or websites; use the backup tools where available.

### iPhone and iPad

Open either website in **Safari**, then tap **Share → Add to Home Screen → Add**. No IPA installation, Apple signing, payment or account is needed. Public IPA and iOS simulator downloads have been removed; use the web reader on these devices.

### Laptop, desktop and other browsers

Open either website. Keyboard navigation is supported: **right arrow advances**, **left arrow goes back**, following the reader’s Arabic page order.

## Read comfortably

- **Surah and Juz navigation** to jump to your reading.
- **Page bookmarks** and **Mode 2 ayah bookmarks** to return to your place.
- **Per-mode layouts and magnification** to suit your screen.
- **Zoom and pan** in both directions on enlarged pages.
- **Quiet full-page reading**, with controls available when you tap.
- **Organized settings** for Mode 1, 2, 3, 4, 5 and General.
- **Saved reading setups**, settings search, backup and restore.

## Offline reading

In the Android v1.9 APK, bundled modes are available without downloading them again. To save Mode 4 pages, connect first and use **Menu → Downloads & offline reading**.

On either website, use that menu while online to save a page, Surah, Juz or full reading mode where offered. Wait for the download to finish before disconnecting. Scanned collections are large; smaller downloads help when storage is limited.

Browser storage can be cleared or evicted. Keep a backup of important bookmarks. The two websites and the APK each store their own reading data.

## Share with someone

Send this link by WhatsApp, email or text:

**https://codewithmohamed18.github.io/quran-word-by-word-app/**

For Android installation, send the [public v1.9 download page](https://github.com/codewithmohamed18/quran-word-by-word-app/releases/tag/v1.9).

The GitHub and ChatGPT websites are hosted independently. Updates are published separately. This Android release packages the approved web reader; it does not change either website.

## Sources and care for the text

Source scans are displayed without rewriting their printed Arabic or translations. Mode 2’s meaning uses the Muhammad Muhsin Khan and Muhammad Taqi-ud-Din al-Hilali translation.

- [Word-by-word Juz collection](https://haameem7.wordpress.com/2023/01/07/quran-arabic-english-word-by-word-translation-juz-pdf/)
- [Study the Noble Qur’an · three volumes](https://www.kalamullah.com/study-the-noble-quran.html)
- [16-line colour-coded Tajweed · original PDF](https://quranpdf.wordpress.com/wp-content/uploads/2014/12/quran-16-lines-tajwedi-hammad-company1.pdf)

Printed editions use their own colour conventions. Mode 5 includes its original reference pages. Display settings cannot recover detail absent from the scans. Report a content or navigation problem with the mode, Surah and page number so it can be checked against the source.

## For contributors

| Location | Purpose |
| --- | --- |
| `android-v19/` | Android v1.9 wrapper for the approved five-mode reader. |
| `scripts/build-v19.py` | Build and verify the pinned web snapshot for the APK. |
| `scripts/build-v19-android.sh` | Compile, align and sign the APK. |
| `android/` | Earlier native Android source. |
| `ios/` | Archived native iOS development source; no public iOS downloads. |
| [`github-pages-web` branch](https://github.com/codewithmohamed18/quran-word-by-word-app/tree/github-pages-web) | Published web snapshot and Pages build. |
| `payload/`, `payload-manifest.json` | Verified original supplied APK archive. |

### Build Android v1.9

Install JDK 17, Python 3, Android SDK platform 35 and build-tools 35.0.0. Set `ANDROID_HOME`, then run:

```sh
pip install PyMuPDF==1.26.6 Pillow==11.3.0
python scripts/build-v19.py
bash scripts/build-v19-android.sh
```

Output: `dist/Tajweed-and-Meaning-v1.9.apk`. The build pins the approved GitHub web commit, verifies every source file, and bundles the original scans. Mode 5 JPEGs are extracted without reencoding. The checked-in development signing certificate provides installable builds; it is not a production store certificate.

## Feedback

Have a suggestion or found a problem? [Open an issue](https://github.com/codewithmohamed18/quran-word-by-word-app/issues). Include your device, reading mode, page number and a screenshot when helpful.

<div align="center">

**May this make it easier to return to the Qur’an, one page at a time.**

</div>
