package com.codewithmohamed.quranwordbyword;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private static final int PICK_PDF = 10;
    private static final int GREEN = Color.rgb(16, 62, 52);
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    // Renderer belongs exclusively to worker; UI never opens or closes a PDF page.
    private PdfRenderer renderer;
    private ParcelFileDescriptor descriptor;
    private SharedPreferences prefs;
    private PdfPageView pageView;
    private TextView status, documentName, welcome;
    private ProgressBar spinner;
    private Button previous, next, pageButton, star, juzButton, bookmarksButton, fitButton, openButton;
    private int page = 1, count;
    private String documentId = "";
    private volatile int generation;
    private volatile boolean destroyed;
    private boolean importing;
    private boolean rendered;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("reader", MODE_PRIVATE);
        buildUi();
        if (pdfFile().isFile()) reopen();
    }
    private File pdfFile() { return new File(getFilesDir(), "selected.pdf"); }
    private int dp(int n) { return (int) (n * getResources().getDisplayMetrics().density + .5f); }
    private TextView label(String text, int size, int color) {
        TextView view = new TextView(this); view.setText(text); view.setTextSize(size);
        view.setTextColor(color); view.setGravity(Gravity.CENTER); return view;
    }
    private Button button(String text, Runnable action) {
        Button view = new Button(this); view.setText(text); view.setTextSize(13); view.setAllCaps(false);
        view.setMinWidth(0); view.setMinimumWidth(0); view.setPadding(dp(4), 0, dp(4), 0);
        view.setOnClickListener(v -> action.run()); return view;
    }
    private LinearLayout row(LinearLayout root) {
        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(row, new LinearLayout.LayoutParams(-1, -2)); return row;
    }
    private void item(LinearLayout row, View view) {
        row.addView(view, new LinearLayout.LayoutParams(0, dp(52), 1));
    }
    private void buildUi() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(246, 243, 235));
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            // Android 15 enforces edge-to-edge: keep every control clear of system bars.
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            } else {
                v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
            return insets;
        });
        TextView title = label(getString(R.string.app_name), 23, Color.WHITE);
        title.setBackgroundColor(GREEN); title.setPadding(dp(8), dp(15), dp(8), dp(15));
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));
        documentName = label("Your Qur’an, always with you", 13, GREEN);
        documentName.setMaxLines(1); documentName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        root.addView(documentName, new LinearLayout.LayoutParams(-1, dp(28)));
        LinearLayout actions = row(root);
        openButton = button("Open PDF", this::pickPdf); item(actions, openButton);
        juzButton = button("30 Juz", this::showJuz); item(actions, juzButton);
        bookmarksButton = button("Saved", this::showBookmarks); item(actions, bookmarksButton);
        fitButton = button("Fit", () -> pageView.fit()); item(actions, fitButton);
        FrameLayout frame = new FrameLayout(this);
        root.addView(frame, new LinearLayout.LayoutParams(-1, 0, 1));
        pageView = new PdfPageView(this); frame.addView(pageView, new FrameLayout.LayoutParams(-1, -1));
        welcome = label("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n\nWelcome\n\nSelect your Arabic–English Qur’an PDF.\nA private copy stays on this phone for offline reading.\n\nPinch to zoom • Drag to pan\nDouble tap to zoom or fit", 19, GREEN);
        welcome.setPadding(dp(24), dp(20), dp(24), dp(20));
        frame.addView(welcome, new FrameLayout.LayoutParams(-1, -1));
        spinner = new ProgressBar(this);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(dp(48), dp(48), Gravity.CENTER);
        frame.addView(spinner, sp); spinner.setVisibility(View.GONE);
        status = label("No PDF selected", 13, GREEN); root.addView(status, new LinearLayout.LayoutParams(-1, dp(30)));
        LinearLayout navigation = row(root);
        previous = button("Previous", () -> navigate(page - 1)); item(navigation, previous);
        pageButton = button("Go to page", this::jump); item(navigation, pageButton);
        next = button("Next", () -> navigate(page + 1)); item(navigation, next);
        star = button("☆", this::toggleBookmark); star.setTextSize(26); item(navigation, star);
        star.setContentDescription("Bookmark this page");
        setContentView(root); root.requestApplyInsets(); refresh();
    }
    private void refresh() {
        boolean ready = count > 0 && !importing;
        previous.setEnabled(ready && page > 1); next.setEnabled(ready && page < count);
        pageButton.setEnabled(ready); juzButton.setEnabled(ready); bookmarksButton.setEnabled(ready);
        star.setEnabled(ready && rendered); fitButton.setEnabled(ready && rendered); openButton.setEnabled(!importing);
        pageButton.setText(count > 0 ? page + " / " + count : "Go to page");
        boolean marked = bookmarks().contains(Integer.toString(page));
        star.setText(marked ? "★" : "☆");
        star.setContentDescription(marked ? "Remove bookmark for page " + page : "Bookmark page " + page);
    }
    private void pickPdf() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE); intent.setType("application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try { startActivityForResult(intent, PICK_PDF); }
        catch (android.content.ActivityNotFoundException e) { error("No document picker is available on this device."); }
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != PICK_PDF || result != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // Some providers do not offer persistent grants. The verified private copy still works offline.
        }
        importPdf(uri);
    }
    private void reopen() {
        importing = true; spinner.setVisibility(View.VISIBLE); welcome.setVisibility(View.GONE);
        status.setText("Opening your Qur’an…"); refresh();
        worker.execute(() -> {
            try {
                closePdf(); openPdf(pdfFile());
                int total = renderer.getPageCount();
                String id = prefs.getString("current_id", "local");
                int last = ReaderRules.clamp(prefs.getInt(id + ".last", 1), total);
                runOnUiThread(() -> {
                    if (destroyed) return;
                    count = total; documentId = id; page = last; importing = false;
                    documentName.setText(prefs.getString("current_name", "Qur’an PDF")); navigate(page);
                });
            } catch (Exception e) { runOnUiThread(() -> openingFailed("The saved PDF could not be opened. Select the PDF again.")); }
        });
    }
    private void importPdf(Uri uri) {
        importing = true; rendered = false; generation++;
        welcome.setVisibility(View.GONE); spinner.setVisibility(View.VISIBLE); status.setText("Saving PDF for offline reading…"); refresh();
        worker.execute(() -> {
            File temporary = new File(getFilesDir(), "import-" + UUID.randomUUID() + ".pdf");
            try {
                String name = "Qur’an PDF";
                try (Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) name = cursor.getString(0);
                } catch (Exception ignored) { /* Display name is optional. */ }
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                try (InputStream input = getContentResolver().openInputStream(uri);
                     FileOutputStream output = new FileOutputStream(temporary)) {
                    if (input == null) throw new IOException("Document unavailable");
                    byte[] buffer = new byte[65536]; int n;
                    while ((n = input.read(buffer)) != -1) {
                        if (destroyed) throw new IOException("Import interrupted");
                        output.write(buffer, 0, n); digest.update(buffer, 0, n);
                    }
                    output.getFD().sync();
                }
                int total;
                // Validate before replacing a working document. Password-protected PDFs are rejected safely.
                try (ParcelFileDescriptor fd = ParcelFileDescriptor.open(temporary, ParcelFileDescriptor.MODE_READ_ONLY);
                     PdfRenderer candidate = new PdfRenderer(fd)) {
                    total = candidate.getPageCount();
                    if (total < 1) throw new IOException("Empty PDF");
                    try (PdfRenderer.Page first = candidate.openPage(0)) {
                        if (first.getWidth() < 1 || first.getHeight() < 1) throw new IOException("Invalid page");
                    }
                }
                if (destroyed) throw new IOException("Import interrupted");
                Files.move(temporary.toPath(), pdfFile().toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                closePdf(); openPdf(pdfFile());
                StringBuilder hex = new StringBuilder();
                for (byte b : digest.digest()) hex.append(String.format(Locale.ROOT, "%02x", b & 255));
                String id = hex.toString(), finalName = name;
                String oldUri = prefs.getString("current_uri", "");
                prefs.edit().putString("current_id", id).putString("current_name", name)
                    .putString("current_uri", uri.toString()).commit();
                if (!oldUri.isEmpty() && !oldUri.equals(uri.toString())) {
                    try { getContentResolver().releasePersistableUriPermission(Uri.parse(oldUri), Intent.FLAG_GRANT_READ_URI_PERMISSION); }
                    catch (SecurityException ignored) { /* Already revoked. */ }
                }
                int last = ReaderRules.clamp(prefs.getInt(id + ".last", 1), total);
                runOnUiThread(() -> {
                    if (destroyed) return;
                    count = total; documentId = id; page = last; importing = false;
                    documentName.setText(finalName); navigate(page);
                    if (total != 960) Toast.makeText(this, "This PDF has " + total + " pages. Shortcuts use its actual page numbers.", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                temporary.delete();
                runOnUiThread(() -> {
                    if (destroyed) return;
                    importing = false;
                    error("Cannot import this PDF. Select a downloaded, unencrypted PDF and check free space. Your existing reading data is kept.");
                    if (count > 0) navigate(page); else openingFailed("Choose a readable PDF to begin.");
                });
            }
        });
    }
    private void openPdf(File file) throws IOException {
        ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        try { renderer = new PdfRenderer(fd); descriptor = fd; }
        catch (RuntimeException e) { fd.close(); throw e; }
    }
    private void closePdf() {
        if (renderer != null) { renderer.close(); renderer = null; }
        if (descriptor != null) { try { descriptor.close(); } catch (IOException ignored) {} descriptor = null; }
    }
    private void openingFailed(String message) {
        if (destroyed) return;
        importing = false; spinner.setVisibility(View.GONE);
        welcome.setText(message); welcome.setVisibility(View.VISIBLE); status.setText("Select PDF to continue"); refresh();
    }
    private void navigate(int target) {
        if (count < 1 || importing) return;
        page = ReaderRules.clamp(target, count); rendered = false;
        int requested = page, token = ++generation, screenWidth = getResources().getDisplayMetrics().widthPixels;
        // Never display the previous page beneath the new page number while rendering.
        pageView.setPage(null); spinner.setVisibility(View.VISIBLE); status.setText("Loading page " + page + "…"); refresh();
        worker.execute(() -> {
            if (token != generation || destroyed) return;
            Bitmap bitmap = null;
            try {
                if (renderer == null) throw new IOException("PDF not open");
                try (PdfRenderer.Page pdfPage = renderer.openPage(requested - 1)) {
                    int[] size = ReaderRules.renderSize(pdfPage.getWidth(), pdfPage.getHeight(), screenWidth);
                    bitmap = Bitmap.createBitmap(size[0], size[1], Bitmap.Config.ARGB_8888); bitmap.eraseColor(Color.WHITE);
                    pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                }
                Bitmap result = bitmap;
                runOnUiThread(() -> {
                    if (destroyed || token != generation) { result.recycle(); return; }
                    pageView.setPage(result); rendered = true; spinner.setVisibility(View.GONE);
                    status.setText("Page " + requested + " • Pinch to zoom");
                    prefs.edit().putInt(documentId + ".last", requested).apply(); refresh();
                });
            } catch (Exception | OutOfMemoryError e) {
                if (bitmap != null) bitmap.recycle();
                runOnUiThread(() -> {
                    if (destroyed || token != generation) return;
                    spinner.setVisibility(View.GONE); status.setText("Page could not be rendered. Try another page or reopen the PDF."); refresh();
                });
            }
        });
    }
    private void jump() { pageDialog("Go to page", page, this::navigate); }
    private interface PageChoice { void choose(int number); }
    private void pageDialog(String title, int initial, PageChoice action) {
        EditText input = new EditText(this); input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setSingleLine(true); input.setText(Integer.toString(initial)); input.selectAll();
        LinearLayout box = new LinearLayout(this); box.setPadding(dp(24), dp(8), dp(24), dp(4));
        box.addView(input, new LinearLayout.LayoutParams(-1, -2));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(title).setMessage("PDF page number, from 1 to " + count)
            .setView(box).setNegativeButton("Cancel", null).setPositiveButton("Go", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try { int n = ReaderRules.parsePage(input.getText().toString(), count); action.choose(n); dialog.dismiss(); }
            catch (IllegalArgumentException e) { input.setError(e.getMessage()); }
        })); dialog.show();
    }
    private Set<String> bookmarks() {
        return new HashSet<>(prefs.getStringSet(documentId + ".bookmarks", Collections.emptySet()));
    }
    private void toggleBookmark() {
        Set<String> saved = bookmarks(); String key = Integer.toString(page);
        boolean added = !saved.remove(key); if (added) saved.add(key);
        prefs.edit().putStringSet(documentId + ".bookmarks", saved).apply(); refresh();
        Toast.makeText(this, added ? "Page bookmarked" : "Bookmark removed", Toast.LENGTH_SHORT).show();
    }
    private void showBookmarks() {
        List<Integer> saved = new ArrayList<>();
        for (String s : bookmarks()) {
            try { int n = Integer.parseInt(s); if (n >= 1 && n <= count) saved.add(n); } catch (NumberFormatException ignored) {}
        }
        Collections.sort(saved);
        if (saved.isEmpty()) { error("No bookmarks yet. Tap ☆ on a page to save it."); return; }
        String[] labels = new String[saved.size()]; for (int i = 0; i < labels.length; i++) labels[i] = "Page " + saved.get(i);
        new AlertDialog.Builder(this).setTitle("Bookmarks").setItems(labels, (d, which) -> navigate(saved.get(which)))
            .setNegativeButton("Close", null).show();
    }
    private int[] juzStarts() {
        int[] starts = new int[30];
        for (int i = 0; i < 30; i++) starts[i] = prefs.getInt(documentId + ".juz." + i, 0);
        return starts;
    }
    private void showJuz() {
        int[] starts = juzStarts(); String[] labels = new String[30];
        for (int i = 0; i < 30; i++) labels[i] = "Juz " + (i + 1) + (starts[i] > 0 ? " • Page " + starts[i] : " • Set start page");
        new AlertDialog.Builder(this).setTitle("30 Juz shortcuts").setItems(labels, (d, which) -> {
            if (starts[which] > 0) navigate(starts[which]); else configureJuz(which);
        }).setNeutralButton("Set up / edit", (d, w) -> juzSetup())
            .setNegativeButton("Close", null).show();
    }
    private void juzSetup() {
        new AlertDialog.Builder(this).setTitle("Set up Juz shortcuts")
            .setItems(new String[]{"Use source PDF layout", "Edit individual starts"}, (d, which) -> {
                if (which == 1) editJuz(); else sourceLayout();
            }).setNegativeButton("Close", null).show();
    }
    private void sourceLayout() {
        if (count != 960 && count != 962) {
            error("The source layouts have 960 pages (Juz only) or 962 pages (original with cover). Set starts individually for this PDF.");
            return;
        }
        int first = count == 960 ? 1 : 2;
        new AlertDialog.Builder(this).setTitle("Use the 32-page Juz layout?")
            .setMessage("The linked source has 32 pages per Juz. For this " + count + "-page file, Juz 1 starts at PDF page " + first +
                ". Use this only if your file contains all 30 Juz in order with no extra pages between them. You can edit any shortcut afterward.")
            .setPositiveButton("Use layout", (d, w) -> {
                SharedPreferences.Editor editor = prefs.edit();
                for (int i = 0; i < 30; i++) editor.putInt(documentId + ".juz." + i, first + i * 32);
                editor.apply(); showJuz();
            }).setNegativeButton("Cancel", null).show();
    }
    private void editJuz() {
        String[] choices = new String[30]; for (int i = 0; i < 30; i++) choices[i] = "Set Juz " + (i + 1) + " start";
        new AlertDialog.Builder(this).setTitle("Match shortcuts to your PDF")
            .setItems(choices, (d, which) -> configureJuz(which)).setNegativeButton("Close", null).show();
    }
    private void configureJuz(int index) {
        int existing = juzStarts()[index];
        pageDialog("Juz " + (index + 1) + " start page", existing > 0 ? existing : page, n -> {
            int[] starts = juzStarts(); starts[index] = n;
            if (!ReaderRules.validJuzMap(starts, count)) { error("Juz starts must increase in order. Check the neighbouring shortcuts."); return; }
            prefs.edit().putInt(documentId + ".juz." + index, n).apply(); navigate(n);
        });
    }
    private void error(String message) {
        if (!destroyed) new AlertDialog.Builder(this).setTitle(getString(R.string.app_name)).setMessage(message).setPositiveButton("OK", null).show();
    }
    @Override protected void onDestroy() {
        destroyed = true; generation++;
        if (pageView != null) pageView.setPage(null);
        worker.execute(this::closePdf); worker.shutdown(); super.onDestroy();
    }
}
