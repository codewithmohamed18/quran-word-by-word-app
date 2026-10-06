import XCTest

final class ReaderUITests: XCTestCase {
    func testOfflineReaderAndArabicPageNavigation() {
        let app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.webViews.firstMatch.waitForExistence(timeout: 30))
        let next = app.buttons["Next page"]
        XCTAssertTrue(next.waitForExistence(timeout: 30))
        let first = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Page 1 / 960")).firstMatch
        XCTAssertTrue(first.waitForExistence(timeout: 30))
        app.webViews.firstMatch.swipeRight()
        XCTAssertTrue(app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Page 2 / 960")).firstMatch.waitForExistence(timeout: 30))
        app.webViews.firstMatch.swipeLeft()
        XCTAssertTrue(first.waitForExistence(timeout: 30))
        next.tap()
        XCTAssertTrue(app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Page 2 / 960")).firstMatch.waitForExistence(timeout: 30))
        app.buttons["Previous page"].tap()
        XCTAssertTrue(first.waitForExistence(timeout: 30))
        app.buttons["Open menu"].tap()
        app.webViews.firstMatch.swipeUp()
        let settings = app.buttons.matching(NSPredicate(format: "label ENDSWITH %@", "Settings")).firstMatch
        XCTAssertTrue(settings.waitForExistence(timeout: 15))
        settings.tap()
        let search = app.searchFields["Search settings"].waitForExistence(timeout: 5) ? app.searchFields["Search settings"] : app.textFields["Search settings"]
        XCTAssertTrue(search.waitForExistence(timeout: 15))
        search.tap()
        search.typeText("blur")
        XCTAssertTrue(app.staticTexts.matching(NSPredicate(format: "label CONTAINS %@", "Mode 1 page clarity")).firstMatch.waitForExistence(timeout: 15))
        app.buttons["Back to reader"].tap()
        app.buttons["Reading modes"].tap()
        let tajweed = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "2. Colour Tajweed with meaning")).firstMatch
        XCTAssertTrue(tajweed.waitForExistence(timeout: 15))
        tajweed.tap()
        XCTAssertTrue(app.buttons["Reading modes"].waitForExistence(timeout: 15))
        app.buttons["Reading modes"].tap()
        let plain = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "3. Plain Qur’an with colour-coded Tajweed")).firstMatch
        XCTAssertTrue(plain.waitForExistence(timeout: 15))
        plain.tap()
        let mushafMenu = app.buttons["Open reading menu"]
        XCTAssertTrue(mushafMenu.waitForExistence(timeout: 30))
        XCTAssertTrue(mushafMenu.isHittable)
        XCTAssertTrue(app.buttons["Bookmark in focus view"].isHittable)
        // Restore the toolbar with a central tap, then test the actual mode 3 swipe.
        app.webViews.firstMatch.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
        let plainPage = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Page 4 / 850")).firstMatch
        XCTAssertTrue(plainPage.waitForExistence(timeout: 15))
        app.webViews.firstMatch.swipeRight()
        XCTAssertTrue(app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Page 5 / 850")).firstMatch.waitForExistence(timeout: 30))
        app.webViews.firstMatch.swipeLeft()
        XCTAssertTrue(plainPage.waitForExistence(timeout: 30))
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = "Offline word-by-word reader"
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
