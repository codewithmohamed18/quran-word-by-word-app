package com.codewithmohamed.quranwordbyword
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class NavigationConfigTest {
    private fun text()=File("src/main/assets/navigation.json").readText()
    @Test fun bundledMappingContainsAllChaptersAndCorrectKnownStarts() {
        val config=NavigationConfig.parse(text())
        assertEquals(30,config.juz.size); assertEquals(114,config.surahs.size)
        assertEquals(1,config.juz.first().page); assertEquals(929,config.juz.last().page)
        assertEquals(297,config.surahs[8].page); assertEquals(848,config.surahs[54].page)
        assertEquals(960,config.surahs.last().page)
        assertTrue(config.surahs.all {it.arabic.isNotBlank()})
        assertEquals(config.surahs[92].page,config.surahs[93].page)
    }
    @Test(expected=IllegalArgumentException::class) fun invalidPagesAreRejected() {
        NavigationConfig.parse(text().replace("\"page\": 960","\"page\": 961"))
    }
    @Test(expected=IllegalArgumentException::class) fun chapterOrderMustBeValid() {
        NavigationConfig.parse(text().replace("\"number\": 114","\"number\": 115"))
    }
}
