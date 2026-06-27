package com.split.android.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.split.shared.models.Item
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ExtractedLine(
    val text: String,
    val quantity: Double = 1.0,
    val price: Double = 0.0
)

class ReceiptOCRProcessor {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun processReceipt(bitmap: Bitmap): List<Item> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = suspendCancellableCoroutine<String> { continuation ->
                recognizer.process(image)
                    .addOnSuccessListener { text ->
                        continuation.resume(text.text)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }

            parseReceiptText(result)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseReceiptText(text: String): List<Item> {
        val lines = text.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() && it.length > 1 }

        val items = mutableListOf<Item>()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // Skip common receipt headers/footers
            if (isReceiptMetadata(line)) {
                i++
                continue
            }

            // Try to parse current line (and potentially next line if price is separate)
            val parsed = parseLineItem(line, if (i + 1 < lines.size) lines[i + 1] else null)
            if (parsed != null) {
                items.add(
                    Item(
                        id = UUID.randomUUID().toString(),
                        receiptId = "",
                        name = parsed.text,
                        quantity = parsed.quantity,
                        price = parsed.price
                    )
                )
                // If price was found on next line, skip it
                if (parsed.text.isNotEmpty() && parsed.price > 0 && i + 1 < lines.size) {
                    val nextLine = lines[i + 1]
                    if (isPriceOnly(nextLine)) {
                        i++ // Skip next line since we consumed it
                    }
                }
            }
            i++
        }

        return items
    }

    private fun parseLineItem(line: String, nextLine: String? = null): ExtractedLine? {
        // Smart parsing: Look for price first, then extract quantity and name

        var quantity = 1.0
        var price = 0.0
        var itemName = ""

        // First, look for price in current line
        val priceInLine = extractPrice(line)
        if (priceInLine > 0) {
            price = priceInLine
            // Remove price from line to get the name
            itemName = line.replace(Regex("\\$?[0-9]+\\.?[0-9]{0,2}\\s*$"), "").trim()
        } else if (nextLine != null && isPriceOnly(nextLine)) {
            // Price might be on next line
            price = extractPrice(nextLine)
            itemName = line.trim()
        } else {
            // No price found
            itemName = line.trim()
        }

        if (itemName.isEmpty()) return null

        // Extract quantity from the beginning of item name
        val qtyMatch = Regex("^(\\d+)\\s*x\\s+(.+)$").find(itemName)
        if (qtyMatch != null) {
            quantity = qtyMatch.groupValues[1].toDoubleOrNull() ?: 1.0
            itemName = qtyMatch.groupValues[2].trim()
        }

        // Only return if we have a valid name and price
        if (itemName.isNotEmpty() && price > 0) {
            // Clean up common words at the end
            itemName = itemName.replace(Regex("\\s+(qty|x)\\s*$", RegexOption.IGNORE_CASE), "").trim()
            return ExtractedLine(itemName, quantity, price)
        }

        return null
    }

    private fun extractPrice(line: String): Double {
        // Look for currency patterns: $12.50, 12.50, $12, 12, etc.
        val pricePatterns = listOf(
            Regex("\\$([0-9]+\\.[0-9]{2})"),  // $12.50
            Regex("\\$([0-9]+)(?=[^0-9]|$)"), // $12
            Regex("([0-9]+\\.[0-9]{2})(?=[^0-9]|$)"), // 12.50
            Regex("\\b([0-9]+)\\s*$")         // 12 at end of line
        )

        for (pattern in pricePatterns) {
            val match = pattern.find(line)
            if (match != null) {
                val priceStr = match.groupValues[1]
                val price = priceStr.toDoubleOrNull() ?: 0.0
                if (price > 0 && price < 10000) { // Reasonable price range
                    return price
                }
            }
        }

        return 0.0
    }

    private fun isPriceOnly(line: String): Boolean {
        // Check if line is just a price (like on a separate line)
        val cleaned = line.trim()
        return Regex("^\\$?[0-9]+(\\.\\d{2})?$").matches(cleaned)
    }

    private fun isReceiptMetadata(line: String): Boolean {
        val lowerLine = line.lowercase()

        // Common metadata patterns
        val metadata = listOf(
            // Financial summaries
            "receipt", "invoice", "subtotal", "sub total", "tax", "total", "tip", "amount",
            "balance", "due", "paid", "remaining",
            // Store/transaction info
            "thank you", "welcome", "date", "time", "phone", "address", "store",
            "merchant", "card", "cash", "payment", "change", "charge",
            // Decorative lines
            "---", "===", "***", "---", "____",
            // Legal/administrative
            "signature", "customer copy", "merchant copy", "void", "refund",
            "register", "transaction", "number",
            // Common separators
            "product", "description", "price", "unit price"
        )

        // Check if line contains metadata keywords
        if (metadata.any { lowerLine.contains(it) }) return true

        // Check if line is mostly numbers (like dates, phone numbers, etc.)
        if (Regex("^[0-9/\\-:.,\\s]+$").matches(line)) return true

        // Check if line is too short or just special characters
        if (line.length < 2 || Regex("^[^a-zA-Z0-9]+$").matches(line)) return true

        return false
    }
}
