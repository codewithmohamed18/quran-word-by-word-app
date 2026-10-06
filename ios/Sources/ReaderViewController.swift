import UIKit
import WebKit

/// The supplied HTML/JavaScript is unchanged. This bridge implements its Android API on iOS.
final class ReaderViewController: UIViewController, WKScriptMessageHandler, WKNavigationDelegate {
    private var web: WKWebView!
    private let renderer = PDFRenderer()
    private var immersive = false
    private var dark = false
    override var prefersStatusBarHidden: Bool { immersive }
    override var preferredStatusBarStyle: UIStatusBarStyle { dark ? .lightContent : .darkContent }

    override func viewDidLoad() {
        super.viewDidLoad()
        let controller = WKUserContentController()
        controller.add(self, name: "native")
        let bridge = """
        (() => {
          const send = (method,value) => window.webkit.messageHandlers.native.postMessage({method,value});
          window.Android = {
            pdfPage:n=>send('word',n), plainPage:n=>send('plain',n),
            awake:v=>send('awake',v), fullscreen:v=>send('fullscreen',v),
            volumeKeys:v=>{}
          };
          document.addEventListener('DOMContentLoaded',()=>{
            const update=()=>send('theme',document.body.classList.contains('dark'));
            new MutationObserver(update).observe(document.body,{attributes:true,attributeFilter:['class']});
            update();
          });
        })();
        """
        controller.addUserScript(WKUserScript(source: bridge, injectionTime: .atDocumentStart, forMainFrameOnly: true))
        let configuration = WKWebViewConfiguration()
        configuration.userContentController = controller
        configuration.websiteDataStore = .default()
        web = WKWebView(frame: .zero, configuration: configuration)
        web.navigationDelegate = self
        web.isOpaque = false
        web.backgroundColor = UIColor(red: 1, green: 0.992, blue: 0.969, alpha: 1)
        web.scrollView.contentInsetAdjustmentBehavior = .never
        web.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(web)
        NSLayoutConstraint.activate([
            web.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            web.bottomAnchor.constraint(equalTo: view.safeAreaLayoutGuide.bottomAnchor),
            web.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor),
            web.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor)
        ])
        view.backgroundColor = web.backgroundColor
        guard let root = Bundle.main.resourceURL?.appendingPathComponent("web"),
              FileManager.default.fileExists(atPath: root.appendingPathComponent("index.html").path) else {
            fatalError("Bundled reader is missing. Run scripts/prepare.py before building.")
        }
        web.loadFileURL(root.appendingPathComponent("index.html"), allowingReadAccessTo: root)
    }

    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        guard message.frameInfo.isMainFrame, let body = message.body as? [String: Any],
              let method = body["method"] as? String else { return }
        switch method {
        case "word", "plain":
            guard let page = body["value"] as? Int,
                  (method == "word" ? 1...960 : 4...850).contains(page) else { return }
            let pixels = min(2400, max(1600, Int(view.bounds.width * UIScreen.main.scale * 2)))
            renderer.render(mode: method, number: page, pixelWidth: pixels) { [weak self] result in
                guard let self = self else { return }
                let arguments: [Any]
                switch result {
                case .success(let data): arguments = [page, data.base64EncodedString(), NSNull()]
                case .failure: arguments = [page, NSNull(), "This page could not be rendered. Please try again."]
                }
                guard let json = try? JSONSerialization.data(withJSONObject: arguments),
                      let text = String(data: json, encoding: .utf8) else { return }
                let callback = method == "word" ? "receivePdf" : "receivePlainPdf"
                self.web.evaluateJavaScript("window.\(callback)(...\(text))", completionHandler: nil)
            }
        case "awake": UIApplication.shared.isIdleTimerDisabled = (body["value"] as? Bool) ?? false
        case "fullscreen":
            immersive = (body["value"] as? Bool) ?? false
            setNeedsStatusBarAppearanceUpdate()
        case "theme":
            dark = (body["value"] as? Bool) ?? false
            view.backgroundColor = dark ? UIColor(red: 0.082, green: 0.118, blue: 0.106, alpha: 1) : UIColor(red: 1, green: 0.992, blue: 0.969, alpha: 1)
            web.backgroundColor = view.backgroundColor
            overrideUserInterfaceStyle = dark ? .dark : .light
            setNeedsStatusBarAppearanceUpdate()
        default: break
        }
    }

    func webView(_ webView: WKWebView, decidePolicyFor action: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
        // Offline pages only; remote navigation is never loaded inside the reader.
        let scheme = action.request.url?.scheme
        decisionHandler(scheme == "file" || scheme == "about" || scheme == "data" ? .allow : .cancel)
    }
}
