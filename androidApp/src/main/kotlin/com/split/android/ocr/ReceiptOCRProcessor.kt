package com.split.android.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.split.shared.models.Item
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.math.abs

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
            val result = recognizer.process(image).await()
            val rawText = result.text

            image.close()

            parseReceiptText(rawText)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseReceiptText(text: String): List<Item> {
        val lines = text.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val items = mutableListOf<Item>()
        var currentLine = ""

        for (line in lines) {
            // Skip common receipt headers/footers
            if (isReceiptMetadata(line)) continue

            // Try to parse line item
            val parsed = parseLineItem(line)
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
            }
        }

        return items
    }

    private fun parseLineItem(line: String): ExtractedLine? {
        // Pattern: "Item Name    $12.50" or "2x Item Name    $25.00"
        // Try to extract quantity, name, and price

        val tokens = line.split(Regex("\\s{2,}|\\t")).map { it.trim() }.filter { it.isNotEmpty() }

        if (tokens.isEmpty()) return null

        var quantity = 1.0
        var price = 0.0
        var nameTokens = tokens.toMutableList()

        // Check first token for quantity (e.g., "2x" or "2")
        val firstToken = tokens.firstOrNull()
        if (firstToken != null) {
            val qtyMatch = Regex("^(\\d+)x?$").find(firstToken)
            if (qtyMatch != null) {
                quantity = qtyMatch.groupValues[1].toDoubleOrNull() ?: 1.0
                nameTokens.removeAt(0)
            }
        }

        // Check last token for price (e.g., "$12.50")
        val lastToken = tokens.lastOrNull()
        if (lastToken != null) {
            val priceMatch = Regex("^\\$?([0-9]+\\.?[0-9]{0,2})$").find(lastToken)
            if (priceMatch != null) {
                price = priceMatch.groupValues[1].toDoubleOrNull() ?: 0.0
                if (lastToken != tokens.first()) {
                    nameTokens.removeAt(nameTokens.size - 1)
                }
            }
        }

        val itemName = nameTokens.joinToString(" ")

        // Only return if we have a name and price
        if (itemName.isNotEmpty() && price > 0) {
            return ExtractedLine(itemName, quantity, price)
        }

        return null
    }

    private fun isReceiptMetadata(line: String): Boolean {
        val lowerLine = line.lowercase()
        val metadata = listOf(
            "receipt", "invoice", "subtotal", "tax", "total", "tip", "amount",
            "thank you", "welcome", "date", "time", "phone", "address",
            "store", "merchant", "card", "cash", "payment", "change",
            "---", "===", "***", "signature", "customer copy", "merchant copy"
        )

        return metadata.any { lowerLine.contains(it) } || line.length < 2
    }
}
