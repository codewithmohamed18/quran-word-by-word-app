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
        next.tap()
        XCTAssertTrue(app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Page 2 / 960")).firstMatch.waitForExistence(timeout: 30))
        app.buttons["Previous page"].tap()
        XCTAssertTrue(first.waitForExistence(timeout: 30))
        app.buttons["Reading modes"].tap()
        let tajweed = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "2. Colour Tajweed with meaning")).firstMatch
        XCTAssertTrue(tajweed.waitForExistence(timeout: 15))
        tajweed.tap()
        XCTAssertTrue(app.buttons["Reading modes"].waitForExistence(timeout: 15))
        app.buttons["Reading modes"].tap()
        let plain = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "3. Plain Qur’an with colour-coded Tajweed")).firstMatch
        XCTAssertTrue(plain.waitForExistence(timeout: 15))
        plain.tap()
        XCTAssertTrue(app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Page 4 / 850")).firstMatch.waitForExistence(timeout: 30))
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = "Offline word-by-word reader"
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
