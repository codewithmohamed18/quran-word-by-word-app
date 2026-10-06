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
