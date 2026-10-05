package com.codewithmohamed.quranwordbyword;

/** Page numbers exposed to readers are always one based. */
public final class ReaderRules {
    private ReaderRules() {}
    public static int clamp(int page, int count) {
        if (count < 1) throw new IllegalArgumentException("Empty PDF");
        return Math.max(1, Math.min(page, count));
    }
    public static int parsePage(String text, int count) {
        int page;
        try { page = Integer.parseInt(text.trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Enter a whole page number"); }
        if (page < 1 || page > count) throw new IllegalArgumentException("Enter a page from 1 to " + count);
        return page;
    }
    public static boolean validJuzMap(int[] starts, int count) {
        if (starts.length != 30) return false;
        int previous = 0;
        for (int start : starts) {
            // Zero is an explicitly unconfigured shortcut, never an estimated location.
            if (start == 0) continue;
            if (start <= previous || start > count) return false;
            previous = start;
        }
        return true;
    }
    public static int[] renderSize(int width, int height, int screenWidth) {
        double scale = Math.min(2400.0 / width, Math.max(1600.0, screenWidth * 2.5) / width);
        scale = Math.min(scale, Math.sqrt(6000000.0 / ((double) width * height)));
        return new int[] { Math.max(1, (int) (width * scale)), Math.max(1, (int) (height * scale)) };
    }
}
