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
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        // Surface screenshot capture needs a real rendered frame after Compose test-clock updates.
        Thread.sleep(350)
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val directory=File(context.getExternalFilesDir(null),"screenshots").apply { mkdirs() }
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(directory,"$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
        bitmap.recycle()
    }
    private fun openMenu() { compose.onNodeWithContentDescription("Open navigation menu").performClick() }
    private fun waitPage(page: Int) {
        compose.waitUntil(45000) { compose.onAllNodesWithText("Page $page / 960").fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(45000) { compose.onAllNodes(hasContentDescription("Bookmark current page") or hasContentDescription("Remove bookmark")).fetchSemanticsNodes()
            .any { it.config.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled).not() } }
    }
    @Test fun bundledReaderNavigationAndPersistenceWorkWithoutNetwork() {
        compose.waitUntil(45000) {
            compose.runOnIdle {
                androidx.lifecycle.ViewModelProvider(compose.activity)[ReaderViewModel::class.java].state.value.displayed
            }
        }
        compose.onAllNodesWithContentDescription("Open navigation menu").assertCountEquals(0)
        screenshot("00-full-screen")
        compose.onNodeWithTag("pdf-reader").performTouchInput { click() }
        waitPage(1)
        compose.onNodeWithTag("pdf-reader").performTouchInput { click() }
        compose.waitUntil(10000) { compose.onAllNodesWithText("Page 1 / 960").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("pdf-reader").performTouchInput { click() }
        waitPage(1)
        openMenu(); compose.onNodeWithText("Continue Reading — Page 1").performClick(); waitPage(1)
        screenshot("01-reader")
        compose.onAllNodesWithText("Choose PDF").assertCountEquals(0)
        openMenu(); screenshot("02-drawer")
        compose.onNodeWithText("Juz (Para)").performClick()
        compose.onNodeWithText("Juz 2").performClick(); waitPage(33)
        compose.onNodeWithContentDescription("Qur’an page 33. Pinch or double tap to zoom. Swipe at fit size to change pages.")
            .performTouchInput { doubleClick() }
        screenshot("06-zoom")
        compose.onNodeWithContentDescription("Reading options").performClick()
        compose.onNodeWithText("Fit page").performClick()
        compose.onNodeWithContentDescription("Qur’an page 33. Pinch or double tap to zoom. Swipe at fit size to change pages.")
            .performTouchInput { swipeRight() }
        waitPage(34)
        compose.onNodeWithTag("pdf-reader").performTouchInput { swipeLeft() }
        waitPage(33)
        compose.onNodeWithContentDescription("Next page").performClick(); waitPage(34)
        val next=compose.onNodeWithContentDescription("Next page").fetchSemanticsNode().boundsInRoot
        val previous=compose.onNodeWithContentDescription("Previous page").fetchSemanticsNode().boundsInRoot
        org.junit.Assert.assertTrue("Next belongs on the left in Arabic reading order",next.center.x<previous.center.x)
        compose.onNodeWithText("Page 34 / 960").performClick()
        compose.onNode(hasSetTextAction()).performTextReplacement("961")
        compose.onNodeWithText("Go",useUnmergedTree=true).performClick()
        compose.onNodeWithText("Enter a whole page number from 1 to 960").assertExists()
        compose.onNode(hasSetTextAction()).performTextReplacement("960")
        compose.onNodeWithText("Go",useUnmergedTree=true).performClick(); waitPage(960)
        compose.onNodeWithContentDescription("Next page").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Previous page").performClick(); waitPage(959)
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
        compose.waitUntil(10000) {
            compose.runOnIdle {
                androidx.lifecycle.ViewModelProvider(compose.activity)[ReaderViewModel::class.java]
                    .state.value.preferences.theme == "dark"
            }
        }
        screenshot("05-settings-dark")
        compose.onNodeWithContentDescription("Back to reader").performClick(); waitPage(848)
        screenshot("07-reader-dark")
        compose.onNodeWithTag("pdf-reader").performTouchInput { click() }
        compose.waitUntil(10000) { compose.onAllNodesWithText("Page 848 / 960").fetchSemanticsNodes().isEmpty() }
        screenshot("08-fullscreen-dark")
        compose.onNodeWithTag("pdf-reader").performTouchInput { click() }; waitPage(848)
        openMenu(); compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithContentDescription("Keep screen awake while reading").performClick()
    }
}
