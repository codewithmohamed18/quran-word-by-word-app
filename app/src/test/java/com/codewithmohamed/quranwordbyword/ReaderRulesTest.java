package com.codewithmohamed.quranwordbyword;
import org.junit.Test;
import static org.junit.Assert.*;

public class ReaderRulesTest {
    @Test public void limitsWorkFor960PageDocument() {
        assertEquals(1, ReaderRules.clamp(0, 960));
        assertEquals(960, ReaderRules.clamp(961, 960));
        assertEquals(487, ReaderRules.clamp(487, 960));
    }
    @Test public void smallerReplacementClampsSavedPage() {
        assertEquals(25, ReaderRules.clamp(960, 25));
    }
    @Test public void jumpUsesOneBasedPages() {
        assertEquals(960, ReaderRules.parsePage(" 960 ", 960));
        for (String invalid : new String[]{"0", "961", "-1", "1.5", "", "999999999999"}) {
            try { ReaderRules.parsePage(invalid, 960); fail(invalid); }
            catch (IllegalArgumentException expected) { }
        }
    }
    @Test public void emptyDocumentIsRejected() {
        try { ReaderRules.clamp(1, 0); fail(); }
        catch (IllegalArgumentException expected) { }
    }
    @Test public void juzLocationsAreNeverEstimated() {
        int[] map = new int[30];
        assertTrue(ReaderRules.validJuzMap(map, 960));
        map[0] = 1; map[2] = 70; map[29] = 925;
        assertTrue(ReaderRules.validJuzMap(map, 960));
        map[1] = 75; assertFalse(ReaderRules.validJuzMap(map, 960));
        map[1] = 1; assertFalse(ReaderRules.validJuzMap(map, 960));
        map[1] = 33; map[29] = 961; assertFalse(ReaderRules.validJuzMap(map, 960));
    }
    @Test public void bitmapMemoryIsBoundedForLargePages() {
        for (int[] page : new int[][]{{595,842},{10000,20000},{10,10000},{842,595}}) {
            int[] size = ReaderRules.renderSize(page[0], page[1], 1440);
            assertTrue(size[0] > 0 && size[1] > 0);
            assertTrue((long) size[0] * size[1] <= 6000000);
            assertTrue(size[0] <= 2400);
        }
    }
}
