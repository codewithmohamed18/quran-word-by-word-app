# Tajweed & Meaning — Safari edition

Web port of https://github.com/codewithmohamed18/quran-word-by-word-app (v1.8.5).

Open in Safari and choose Share → Add to Home Screen. All three modes retain the original bundled Arabic, translation and scanned page imagery. Scans are extracted directly from the source PDF and displayed with the same page crop bounds in a browser canvas, avoiding image recompression. Source resolution limits still apply.

Pages opened are cached automatically. Menu → Save for offline reading downloads all 1810 scanned pages; cached app assets include all 604 Tajweed text pages. Downloads are resumable. Safari storage can be evicted or cleared, so offline retention is not guaranteed. Bookmarks and settings are local to this web origin and do not transfer from the Android app automatically; use its backup/restore feature.

Native PDF quality and volume-key controls are omitted. Screen wake lock depends on browser support. Browser pinch zoom replaces native zoom.

The original third-party licence and source notices are included in dist/SOURCE-LICENSE.txt.

## Web reader 2.0

- Quick Arabic/meaning sizes and scanned-page magnification, with night controls and full-page focus.
- Saved ayahs in mode 2, alongside the existing per-mode page bookmarks; included in backup/restore.
- Share links to a page or ayah. Remember the scroll position when reopening the same saved page.
- Offline downloads for the current Surah, Juz, reading mode or all scans; visible progress, pause/resume, bounded parallel downloads, retries and storage feedback. Existing scan caches are retained during updates.
- Prefetch the next scanned page, discard stale rendering requests, modal keyboard focus, Escape support and reduced-motion defaults.

Feedback informing these priorities:
- https://apps.apple.com/us/app/quran-by-quran-com-%D9%82%D8%B1%D8%A2%D9%86/id1118663303?platform=iphone&see-all=reviews
- https://apps.apple.com/us/app/the-clear-quran/id6446804552?platform=iphone&see-all=reviews
- https://quran.com/product-updates/reading-bookmark-easily-track-your-quran-progress

These are selected reading improvements, not a claim that every requested feature from other apps is implemented. Arabic text, translations, scan files and Tajweed markings remain sourced from the existing bundled assets.

## Web reader 3.0 — Reading Mode 4

The live web app adds **Study the Noble Qur’an — Word for Word** (Darussalam, full colour, 2012), with all 1,797 pages from the three complete volumes. The downloadable PDF preserves every original page, including covers, introductions and contents, and adds volume/Juz outline bookmarks. Sharper matching juz scans provide the web reading pages, retaining all printed content and colours while trimming only blank outer margins. Web images are encoded as WebP at quality 90, up to 1,500 pixels wide.

Mode 4 has its own last-read page, bookmarks, Surah/Juz navigation, combined-page and volume jumps, fit-width scrolling, zoom, share links and backup/restore. Offline saving supports this mode, a Surah, a Juz, or all scanned modes. Saving every mode requires substantially more storage than reader 2.0; the app shows the current estimate. Existing offline page caches are retained during updates.

Menu → **Study Qur’an · complete PDF** downloads the combined book. Its 16 MiB pieces are fetched sequentially and checked against SHA-256 hashes, then assembled as one PDF for saving to Files. PDF pieces are not included in automatic app-shell caching.

### Rebuild with Mode 4

```sh
python scripts/fetch-study.py /tmp/quran-study-source
QURAN_STUDY_SOURCE=/tmp/quran-study-source python scripts/build-web.py
```

Dependencies: PyMuPDF (`fitz`), Pillow, Python 3.10+, and curl. `config/study-sources.json` records verified URLs, page counts and SHA-256 hashes; changed or truncated sources fail verification. `scripts/build-study.py` checks all 114 Surah starts, all 30 Juz starts, page counts and volume boundaries. The web integration is applied only after native bundled files are copied into web output. Android and iOS sources are unchanged.

Requested source: https://www.kalamullah.com/study-the-noble-quran.html
Complete-volume mirror: https://www.islamicauthenticlibrary.org/
High-resolution juz scans: https://www.emaanlibrary.com/book/the-noble-quran-word-for-word-arabic-english-color/

The printed edition’s colours identify grammatical categories; they are not a replacement for the separate Tajweed mode’s recitation markings.
