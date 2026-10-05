package com.codewithmohamed.quranwordbyword;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.GradientDrawable;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private static final int GREEN = Color.rgb(23, 57, 65);
    private static final int GOLD = Color.rgb(190, 154, 82);
    private FrameLayout drawerLayer;
    private LinearLayout drawer;
    private boolean drawerOpen;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    // Renderer belongs exclusively to worker; UI never opens or closes a PDF page.
    private PdfRenderer renderer;
    private ParcelFileDescriptor descriptor;
    private SharedPreferences prefs;
    private PdfPageView pageView;
    private TextView status, documentName, welcome;
    private ProgressBar spinner;
    private Button previous, next, pageButton, star, juzButton, bookmarksButton, fitButton;
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
        openBundledQuran();
    }
    private File pdfFile() { return new File(getFilesDir(), "bundled-quran-960.pdf"); }
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
        FrameLayout shell = new FrameLayout(this);
        shell.setBackgroundColor(Color.rgb(248, 245, 237));
        shell.setOnApplyWindowInsetsListener((v, insets) -> {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            } else v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        shell.addView(root, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout toolbar = row(root); toolbar.setBackgroundColor(GREEN);
        Button menu = button("☰", this::openDrawer); menu.setTextSize(25); menu.setTextColor(Color.WHITE);
        menu.setBackgroundColor(Color.TRANSPARENT); menu.setContentDescription("Open navigation menu");
        toolbar.addView(menu, new LinearLayout.LayoutParams(dp(56), dp(60)));
        LinearLayout heading = new LinearLayout(this); heading.setOrientation(LinearLayout.VERTICAL);
        toolbar.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        TextView title = label(getString(R.string.app_name), 19, Color.WHITE);
        heading.addView(title, new LinearLayout.LayoutParams(-1, -2));
        documentName = label("ARABIC · ENGLISH · OFFLINE", 10, Color.rgb(232, 210, 164));
        heading.addView(documentName, new LinearLayout.LayoutParams(-1, dp(20)));
        star = button("☆", this::toggleBookmark); star.setTextSize(28); star.setTextColor(Color.rgb(232, 210, 164));
        star.setBackgroundColor(Color.TRANSPARENT); star.setContentDescription("Bookmark this page");
        toolbar.addView(star, new LinearLayout.LayoutParams(dp(56), dp(60)));
        FrameLayout frame = new FrameLayout(this);
        root.addView(frame, new LinearLayout.LayoutParams(-1, 0, 1));
        pageView = new PdfPageView(this); frame.addView(pageView, new FrameLayout.LayoutParams(-1, -1));
        welcome = label("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n\nPreparing your Qur’an…", 22, GREEN);
        welcome.setPadding(dp(24), dp(20), dp(24), dp(20));
        frame.addView(welcome, new FrameLayout.LayoutParams(-1, -1));
        spinner = new ProgressBar(this);
        frame.addView(spinner, new FrameLayout.LayoutParams(dp(48), dp(48), Gravity.CENTER));
        spinner.setVisibility(View.GONE);
        status = label("Your Qur’an is included", 11, GREEN);
        root.addView(status, new LinearLayout.LayoutParams(-1, dp(26)));
        LinearLayout navigation = row(root);
        previous = button("‹ Previous", () -> navigate(page - 1)); item(navigation, previous);
        pageButton = button("Go to page", this::jump); item(navigation, pageButton);
        next = button("Next ›", () -> navigate(page + 1)); item(navigation, next);
        // Menu controls share the same readiness state as the reader controls.
        juzButton = button("Juz (chapters)", this::showJuz);
        bookmarksButton = button("Bookmarks", this::showBookmarks);
        fitButton = button("Fit page", () -> pageView.fit());
        buildDrawer(shell);
        setContentView(shell); shell.requestApplyInsets(); refresh();
    }
    private void buildDrawer(FrameLayout shell) {
        drawerLayer = new FrameLayout(this); drawerLayer.setVisibility(View.GONE);
        shell.addView(drawerLayer, new FrameLayout.LayoutParams(-1, -1));
        View shade = new View(this); shade.setBackgroundColor(0x88000000); shade.setOnClickListener(v -> closeDrawer());
        drawerLayer.addView(shade, new FrameLayout.LayoutParams(-1, -1));
        drawer = new LinearLayout(this); drawer.setOrientation(LinearLayout.VERTICAL);
        drawer.setBackgroundColor(Color.rgb(249, 246, 236));
        drawer.setClickable(true);
        drawerLayer.addView(drawer, new FrameLayout.LayoutParams(Math.min(dp(320), getResources().getDisplayMetrics().widthPixels - dp(40)), -1, Gravity.START));
        ScrollView scroll = new ScrollView(this); drawer.addView(scroll, new LinearLayout.LayoutParams(-1, -1));
        LinearLayout contents = new LinearLayout(this); contents.setOrientation(LinearLayout.VERTICAL); scroll.addView(contents);
        LinearLayout banner = new LinearLayout(this); banner.setOrientation(LinearLayout.VERTICAL); banner.setGravity(Gravity.CENTER);
        banner.setPadding(dp(18), dp(28), dp(18), dp(28));
        banner.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{0xFF6A5129, 0xFFD9BE7E, 0xFF80642F}));
        contents.addView(banner, new LinearLayout.LayoutParams(-1, dp(208)));
        TextView arabic = label("بِسْمِ اللَّهِ\nالرَّحْمَٰنِ الرَّحِيمِ", 29, Color.WHITE); banner.addView(arabic);
        TextView name = label("Qur’an Word by Word", 19, Color.WHITE); name.setPadding(0, dp(16), 0, dp(4)); banner.addView(name);
        banner.addView(label("Arabic & English · 30 Juz", 12, Color.WHITE));
        drawerItem(contents, "▤", juzButton, this::showJuz);
        drawerItem(contents, "★", bookmarksButton, this::showBookmarks);
        drawerItem(contents, "▶", button("Continue reading", () -> {}), () -> navigate(prefs.getInt(documentId + ".last", 1)));
        drawerItem(contents, "↗", button("Go to page", () -> {}), this::jump);
        drawerItem(contents, "⊞", fitButton, () -> pageView.fit());
        drawerItem(contents, "?", button("Instructions", () -> {}), () -> error("Your 960-page Qur’an is already included. Choose any of the 30 Juz from the menu. Pinch to zoom, drag to pan, double tap or use Fit page. Tap ☆ to bookmark. Your last-read page is saved automatically. All reading works offline."));
        drawerItem(contents, "i", button("About", () -> {}), () -> error("Qur’an Word by Word\nArabic–English PDF edition\n960 pages · 30 Juz\n\nSource: haameem7.wordpress.com, Arabic–English word-by-word translation. The original page content is preserved. No account or internet connection is needed to read."));
        TextView footer = label("READ · REFLECT · RETURN", 10, GOLD); footer.setPadding(0, dp(32), 0, dp(20)); contents.addView(footer);
    }
    private void drawerItem(LinearLayout contents, String symbol, Button button, Runnable action) {
        LinearLayout line = new LinearLayout(this); line.setGravity(Gravity.CENTER_VERTICAL); line.setPadding(dp(18), 0, dp(14), 0);
        TextView icon = label(symbol, 22, Color.WHITE);
        GradientDrawable background = new GradientDrawable(); background.setColor(GOLD); background.setCornerRadius(dp(5)); icon.setBackground(background);
        line.addView(icon, new LinearLayout.LayoutParams(dp(32), dp(32)));
        button.setTextColor(GREEN); button.setTextSize(16); button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setPadding(dp(14), 0, 0, 0); button.setBackgroundColor(Color.TRANSPARENT);
        button.setOnClickListener(v -> { closeDrawer(); if (count > 0 && !importing) action.run(); });
        line.addView(button, new LinearLayout.LayoutParams(0, dp(54), 1));
        contents.addView(line, new LinearLayout.LayoutParams(-1, dp(54)));
    }
    private void openDrawer() {
        drawerOpen = true; drawerLayer.setVisibility(View.VISIBLE);
        drawer.setTranslationX(-drawer.getLayoutParams().width); drawer.animate().translationX(0).setDuration(200).start();
    }
    private void closeDrawer() {
        drawerOpen = false; drawer.animate().cancel(); drawerLayer.setVisibility(View.GONE);
    }
    @Override public void onBackPressed() {
        if (drawerOpen) closeDrawer(); else super.onBackPressed();
    }
    private void refresh() {
        boolean ready = count > 0 && !importing;
        previous.setEnabled(ready && page > 1); next.setEnabled(ready && page < count);
        pageButton.setEnabled(ready); juzButton.setEnabled(ready); bookmarksButton.setEnabled(ready);
        star.setEnabled(ready && rendered); fitButton.setEnabled(ready && rendered);
        pageButton.setText(count > 0 ? page + " / " + count : "Go to page");
        boolean marked = bookmarks().contains(Integer.toString(page));
        star.setText(marked ? "★" : "☆");
        star.setContentDescription(marked ? "Remove bookmark for page " + page : "Bookmark page " + page);
    }
    private void openBundledQuran() {
        importing = true; spinner.setVisibility(View.VISIBLE); welcome.setVisibility(View.GONE);
        status.setText("Preparing your included Qur’an…"); refresh();
        worker.execute(() -> {
            File temporary = new File(getFilesDir(), "bundled-copy.tmp");
            try {
                if (!pdfFile().isFile()) {
                    try (InputStream input = getAssets().open("quran-960.pdf");
                         FileOutputStream output = new FileOutputStream(temporary)) {
                        byte[] buffer = new byte[65536]; int n;
                        while ((n = input.read(buffer)) != -1) {
                            if (destroyed) throw new IOException("Preparation interrupted");
                            output.write(buffer, 0, n);
                        }
                        output.getFD().sync();
                    }
                    Files.move(temporary.toPath(), pdfFile().toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                }
                closePdf(); openPdf(pdfFile());
                int total = renderer.getPageCount();
                if (total != 960) throw new IOException("Bundled page count mismatch");
                String id = "bundled_quran_960_v1";
                int last = ReaderRules.clamp(prefs.getInt(id + ".last", 1), total);
                runOnUiThread(() -> {
                    if (destroyed) return;
                    count = total; documentId = id; page = last; importing = false;
                    navigate(page);
                });
            } catch (Exception e) {
                temporary.delete();
                runOnUiThread(() -> {
                    if (destroyed) return;
                    importing = false; spinner.setVisibility(View.GONE);
                    welcome.setText("The included Qur’an could not be prepared. Free some storage and restart the app.");
                    welcome.setVisibility(View.VISIBLE); status.setText("Please restart to retry"); refresh();
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
                    spinner.setVisibility(View.GONE); status.setText("Page could not be rendered. Try another page or restart the app."); refresh();
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
    private void showJuz() {
        Dialog dialog = new Dialog(this);
        LinearLayout panel = new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(12), dp(18), dp(12)); panel.setBackgroundColor(Color.rgb(48, 96, 188));
        LinearLayout heading = new LinearLayout(this); heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = label("Choose your Juz", 21, Color.WHITE);
        heading.addView(title, new LinearLayout.LayoutParams(0, dp(54), 1));
        Button close = button("×", dialog::dismiss); close.setTextSize(28); close.setTextColor(Color.WHITE);
        close.setBackgroundColor(Color.TRANSPARENT); close.setContentDescription("Close Juz list");
        heading.addView(close, new LinearLayout.LayoutParams(dp(48), dp(48))); panel.addView(heading);
        TextView subtitle = label("30 chapters · Arabic & English", 12, 0xFFDCE7FF); panel.addView(subtitle);
        ListView list = new ListView(this); list.setDivider(new android.graphics.drawable.ColorDrawable(0x335FFFFF)); list.setDividerHeight(dp(1));
        String[] labels = new String[30];
        for (int i = 0; i < 30; i++) labels[i] = "Juz " + (i + 1) + "    ·    Page " + (1 + i * 32);
        list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, labels) {
            @Override public View getView(int position, View convert, android.view.ViewGroup parent) {
                TextView row = (TextView) super.getView(position, convert, parent);
                row.setTextColor(Color.WHITE); row.setTextSize(16); row.setMinHeight(dp(52));
                row.setPadding(dp(10), dp(10), dp(10), dp(10)); return row;
            }
        });
        list.setOnItemClickListener((parent, view, position, id) -> { dialog.dismiss(); navigate(1 + position * 32); });
        list.setSelection((page - 1) / 32);
        panel.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        dialog.setContentView(panel); dialog.show();
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(Math.min(dp(380), getResources().getDisplayMetrics().widthPixels - dp(32)),
            (int) (getResources().getDisplayMetrics().heightPixels * .78));
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
