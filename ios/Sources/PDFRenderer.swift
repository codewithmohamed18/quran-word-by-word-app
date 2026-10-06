import UIKit
import CoreGraphics

/// Serialized PDF work avoids concurrent CGPDF access and keeps rendering off the UI thread.
final class PDFRenderer {
    private let queue = DispatchQueue(label: "quran.pdf", qos: .userInitiated)
    private var documents: [String: CGPDFDocument] = [:]
    private var crops: [String:[[Double]]] = [:]
    private enum RenderError: Error { case missingPage }

    func render(mode: String, number: Int, pixelWidth: Int, completion: @escaping (Result<Data, Error>) -> Void) {
        queue.async { [self] in
            let result: Result<Data, Error> = autoreleasepool {
                do { return .success(try image(mode: mode, number: number, pixelWidth: pixelWidth)) }
                catch { return .failure(error) }
            }
            DispatchQueue.main.async { completion(result) }
        }
    }

    private func image(mode: String, number: Int, pixelWidth: Int) throws -> Data {
        guard let root = Bundle.main.resourceURL?.appendingPathComponent("web") else { throw RenderError.missingPage }
        if documents[mode] == nil {
            let name = mode == "word" ? "word-by-word.pdf" : "plain-13line.pdf"
            documents[mode] = CGPDFDocument(root.appendingPathComponent(name) as CFURL)
        }
        guard let document = documents[mode], let page = document.page(at: number) else { throw RenderError.missingPage }
        let bounds = page.getBoxRect(.mediaBox)
        var crop = bounds
        do {
            let cropName = mode == "word" ? "word-crops.json" : "plain-crops.json"
            if crops[mode] == nil {
                crops[mode] = try JSONSerialization.jsonObject(with: Data(contentsOf: root.appendingPathComponent(cropName))) as? [[Double]]
            }
            if let coordinates = crops[mode], coordinates.indices.contains(number - 1), coordinates[number - 1].count == 4 {
                let c = coordinates[number - 1]
                // Supplied crops use top-left PDF coordinates; Core Graphics uses bottom-left.
                crop = CGRect(x: bounds.minX + c[0], y: bounds.maxY - c[3], width: c[2] - c[0], height: c[3] - c[1]).intersection(bounds)
            }
        }
        guard !crop.isNull, crop.width > 0, crop.height > 0 else { throw RenderError.missingPage }
        let scale = CGFloat(pixelWidth) / crop.width
        let format = UIGraphicsImageRendererFormat()
        format.scale = 1
        format.opaque = true
        let size = CGSize(width: CGFloat(pixelWidth), height: ceil(crop.height * scale))
        let renderer = UIGraphicsImageRenderer(size: size, format: format)
        return renderer.jpegData(withCompressionQuality: 0.92) { context in
            context.cgContext.setFillColor(UIColor.white.cgColor)
            context.cgContext.fill(CGRect(origin: .zero, size: size))
            let canvas = context.cgContext
            canvas.translateBy(x: -crop.minX * scale, y: crop.maxY * scale)
            canvas.scaleBy(x: scale, y: -scale)
            canvas.drawPDFPage(page)
        }
    }
}
