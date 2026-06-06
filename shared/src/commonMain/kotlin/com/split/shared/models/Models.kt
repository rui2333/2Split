package com.split.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class Person(
    val id: String,
    val name: String,
    val emoji: String = ""
)

@Serializable
data class Receipt(
    val id: String,
    val splitId: String,
    val imageUri: String,
    val uploadedAt: Long = System.currentTimeMillis(),
    val isProcessed: Boolean = false
)

@Serializable
data class Item(
    val id: String,
    val receiptId: String,
    val name: String,
    val quantity: Double,
    val price: Double
) {
    fun total(): Double = quantity * price
}

@Serializable
data class Split(
    val id: String,
    val name: String,
    val people: List<Person>,
    val receipts: List<Receipt> = emptyList(),
    val items: List<Item> = emptyList(),
    val splitMethod: SplitMethod = SplitMethod.EVEN,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
sealed class SplitMethod {
    object EVEN : SplitMethod()

    @Serializable
    data class BY_ITEM(val assignments: Map<String, String>) : SplitMethod() // itemId -> personId

    @Serializable
    data class CUSTOM(val ratios: Map<String, Double>) : SplitMethod() // personId -> ratio
}

@Serializable
data class Settlement(
    val personId: String,
    val personName: String,
    val amount: Double
)

@Serializable
data class Transaction(
    val from: String,  // personId
    val to: String,    // personId
    val amount: Double
)
