# Independent GitHub Pages reader

This branch publishes the same five-mode web reader as the ChatGPT-hosted website. It requires no ChatGPT hosting or account. GitHub Actions reassembles the checksum-verified original APK payload only to extract its reading assets, extracts original scan images, overlays the latest web-only reader, and checks every resulting file against snapshot.json before deployment. No native app source is changed.

Public address: https://codewithmohamed18.github.io/quran-word-by-word-app/

One-time setup: repository Settings → Pages → Build and deployment → Source → GitHub Actions. If the initial workflow failed before setup, rerun the Publish independent Quran web reader workflow.

The workflow runs on pushes to github-pages-web. The main branch and its native releases remain unchanged. Future ChatGPT website edits must also be copied to this branch to update the backup; they do not sync automatically.

Mode 4 uses its pinned, public GitHub assets. Browser settings, bookmarks and downloaded pages are separate between website addresses. Safari Share → Add to Home Screen installs this website independently.

Mode 5 preserves all 561 original JPEG scans. During the build, its original PDF is fetched from the publisher, checked by SHA-256, and extracted without reencoding. Deployed reading pages are hosted on GitHub Pages, including offline downloads. The approved 16-line full-screen, Arabic-area proportions, Arabic-width scrolling, and complete-page layouts are included.
