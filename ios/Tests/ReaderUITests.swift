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
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = "Offline word-by-word reader"
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
