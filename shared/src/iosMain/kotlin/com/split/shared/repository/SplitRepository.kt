package com.split.shared.repository

import com.split.shared.models.Item
import com.split.shared.models.Person
import com.split.shared.models.Receipt
import com.split.shared.models.Split
import platform.Foundation.NSUUID

actual class SplitRepository {
    private val splitsMap = mutableMapOf<String, Split>()

    actual suspend fun createSplit(split: Split): Split {
        val newSplit = split.copy(id = NSUUID().UUIDString)
        splitsMap[newSplit.id] = newSplit
        return newSplit
    }

    actual suspend fun getSplit(id: String): Split? = splitsMap[id]

    actual suspend fun getAllSplits(): List<Split> = splitsMap.values.toList()

    actual suspend fun updateSplit(split: Split): Split {
        splitsMap[split.id] = split
        return split
    }

    actual suspend fun deleteSplit(id: String) {
        splitsMap.remove(id)
    }

    actual suspend fun addPerson(splitId: String, person: Person) {
        splitsMap[splitId]?.let { split ->
            val updatedPeople = split.people + person
            splitsMap[splitId] = split.copy(people = updatedPeople)
        }
    }

    actual suspend fun removePerson(splitId: String, personId: String) {
        splitsMap[splitId]?.let { split ->
            val updatedPeople = split.people.filter { it.id != personId }
            splitsMap[splitId] = split.copy(people = updatedPeople)
        }
    }

    actual suspend fun addReceipt(splitId: String, receipt: Receipt) {
        splitsMap[splitId]?.let { split ->
            val updatedReceipts = split.receipts + receipt
            splitsMap[splitId] = split.copy(receipts = updatedReceipts)
        }
    }

    actual suspend fun addItem(splitId: String, item: Item) {
        splitsMap[splitId]?.let { split ->
            val updatedItems = split.items + item
            splitsMap[splitId] = split.copy(items = updatedItems)
        }
    }

    actual suspend fun updateItems(splitId: String, items: List<Item>) {
        splitsMap[splitId]?.let { split ->
            splitsMap[splitId] = split.copy(items = items)
        }
    }
}
