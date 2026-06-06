import UIKit
import Vision

class ReceiptOCRProcessor {
    typealias ProcessingCompletion = (Result<[ItemModel], Error>) -> Void

    enum OCRError: Error {
        case processingFailed
        case noTextFound
    }

    func processReceipt(image: UIImage, completion: @escaping ProcessingCompletion) {
        guard let cgImage = image.cgImage else {
            completion(.failure(OCRError.processingFailed))
            return
        }

        let requestHandler = VNImageRequestHandler(cgImage: cgImage, options: [:])
        let request = VNRecognizeTextRequest { [weak self] request, error in
            if let error = error {
                completion(.failure(error))
                return
            }

            guard let results = request.results as? [VNRecognizedTextObservation] else {
                completion(.failure(OCRError.noTextFound))
                return
            }

            let extractedText = results.compactMap { $0.topCandidates(1).first?.string }.joined(separator: "\n")
            let items = self?.parseReceiptText(extractedText) ?? []
            completion(.success(items))
        }

        request.recognitionLanguages = ["en"]
        request.usesLanguageCorrection = true

        do {
            try requestHandler.perform([request])
        } catch {
            completion(.failure(error))
        }
    }

    private func parseReceiptText(_ text: String) -> [ItemModel] {
        let lines = text.split(separator: "\n")
            .map { String($0).trimmingCharacters(in: .whitespaces) }
            .filter { !$0.isEmpty }

        var items: [ItemModel] = []

        for line in lines {
            // Skip metadata lines
            if isReceiptMetadata(line) {
                continue
            }

            // Try to parse as line item
            if let parsed = parseLineItem(line) {
                items.append(
                    ItemModel(
                        id: UUID().uuidString,
                        receiptId: "",
                        name: parsed.name,
                        quantity: parsed.quantity,
                        price: parsed.price
                    )
                )
            }
        }

        return items
    }

    private func parseLineItem(_ line: String) -> (name: String, quantity: Double, price: Double)? {
        // Split by multiple spaces or tabs
        let tokens = line.components(separatedBy: .whitespaces)
            .filter { !$0.isEmpty }

        guard tokens.count >= 2 else { return nil }

        var quantity: Double = 1.0
        var price: Double = 0.0
        var nameTokens = tokens

        // Check first token for quantity (e.g., "2x" or "2")
        if let firstToken = tokens.first {
            if let qtyMatch = firstToken.range(of: "^(\\d+)x?$", options: .regularExpression) {
                let qtyString = String(firstToken[qtyMatch]).replacingOccurrences(of: "x", with: "")
                quantity = Double(qtyString) ?? 1.0
                nameTokens.removeFirst()
            }
        }

        // Check last token for price (e.g., "$12.50")
        if let lastToken = tokens.last {
            let priceString = lastToken.replacingOccurrences(of: "$", with: "")
            if let parsedPrice = Double(priceString), parsedPrice > 0 {
                price = parsedPrice
                if !nameTokens.isEmpty && nameTokens.last != tokens.last {
                    nameTokens.removeLast()
                }
            }
        }

        let itemName = nameTokens.joined(separator: " ")

        // Only return if we have a name and price
        if !itemName.isEmpty && price > 0 {
            return (itemName, quantity, price)
        }

        return nil
    }

    private func isReceiptMetadata(_ line: String) -> Bool {
        let lowerLine = line.lowercased()
        let metadata = [
            "receipt", "invoice", "subtotal", "tax", "total", "tip", "amount",
            "thank you", "welcome", "date", "time", "phone", "address",
            "store", "merchant", "card", "cash", "payment", "change",
            "---", "===", "***", "signature", "customer copy", "merchant copy"
        ]

        return metadata.contains { lowerLine.contains($0) } || line.count < 2
    }
}
