package com.codewithmohamed.quranwordbyword

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import java.io.File

class OfflineReaderTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private fun screenshot(name: String) {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val directory=File(context.getExternalFilesDir(null),"screenshots").apply { mkdirs() }
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(directory,"$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        bitmap.recycle()
    }
    private fun openMenu() { compose.onNodeWithContentDescription("Open navigation menu").performClick() }
    private fun waitPage(page: Int) {
        compose.waitUntil(45000) { compose.onAllNodesWithText("Page $page / 960").fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(45000) { compose.onAllNodesWithContentDescription("Bookmark current page").fetchSemanticsNodes()
            .any { it.config.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled).not() } }
    }
    @Test fun bundledReaderNavigationAndPersistenceWorkWithoutNetwork() {
        waitPage(1)
        openMenu(); compose.onNodeWithText("Continue Reading — Page 1").performClick(); waitPage(1)
        screenshot("01-reader")
        compose.onAllNodesWithText("Choose PDF").assertCountEquals(0)
        openMenu(); screenshot("02-drawer")
        compose.onNodeWithText("Juz (Para)").performClick()
        compose.onNodeWithText("Juz 2").performClick(); waitPage(33)
        openMenu(); compose.onNodeWithText("Surah").performClick()
        compose.onNodeWithTag("chapter-list").performScrollToNode(hasText("Ar-Rahmaan"))
        screenshot("03-surahs")
        compose.onNodeWithText("Ar-Rahmaan").performClick(); waitPage(848)
        compose.onNodeWithContentDescription("Bookmark current page").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithContentDescription("Remove bookmark").fetchSemanticsNodes().isNotEmpty() }
        compose.activityRule.scenario.recreate()
        compose.waitUntil(45000) { compose.onAllNodesWithText("Page 848 / 960").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Remove bookmark").assertExists()
        openMenu(); compose.onNodeWithText("Bookmarks").performClick()
        compose.onNodeWithText("Page 848").assertExists(); screenshot("04-bookmarks")
        compose.onNodeWithContentDescription("Back to reader").performClick()
        openMenu(); compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Dark mode").performClick()
        screenshot("05-settings-dark")
        compose.onNodeWithContentDescription("Keep screen awake while reading").performClick()
    }
}
