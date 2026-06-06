package com.split.shared.repository

import com.split.shared.models.Item
import com.split.shared.models.Person
import com.split.shared.models.Receipt
import com.split.shared.models.Split

expect class SplitRepository {
    suspend fun createSplit(split: Split): Split
    suspend fun getSplit(id: String): Split?
    suspend fun getAllSplits(): List<Split>
    suspend fun updateSplit(split: Split): Split
    suspend fun deleteSplit(id: String)
    suspend fun addPerson(splitId: String, person: Person)
    suspend fun removePerson(splitId: String, personId: String)
    suspend fun addReceipt(splitId: String, receipt: Receipt)
    suspend fun addItem(splitId: String, item: Item)
    suspend fun updateItems(splitId: String, items: List<Item>)
}
