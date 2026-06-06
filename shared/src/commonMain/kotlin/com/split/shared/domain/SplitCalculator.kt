package com.split.shared.domain

import com.split.shared.models.Item
import com.split.shared.models.Person
import com.split.shared.models.Settlement
import com.split.shared.models.SplitMethod
import com.split.shared.models.Transaction
import kotlin.math.round

object SplitCalculator {
    fun calculateSettlements(
        people: List<Person>,
        items: List<Item>,
        splitMethod: SplitMethod
    ): List<Settlement> {
        val totalAmount = items.sumOf { it.total() }
        val peopleCount = people.size

        return when (splitMethod) {
            is SplitMethod.EVEN -> calculateEvenSplit(people, totalAmount)
            is SplitMethod.BY_ITEM -> calculateByItemSplit(people, items, splitMethod.assignments)
            is SplitMethod.CUSTOM -> calculateCustomSplit(people, totalAmount, splitMethod.ratios)
        }
    }

    private fun calculateEvenSplit(people: List<Person>, totalAmount: Double): List<Settlement> {
        val amountPerPerson = round((totalAmount / people.size) * 100) / 100
        val remainder = round((totalAmount - (amountPerPerson * people.size)) * 100) / 100

        return people.mapIndexed { index, person ->
            val amount = if (index == 0) amountPerPerson + remainder else amountPerPerson
            Settlement(person.id, person.name, amount)
        }
    }

    private fun calculateByItemSplit(
        people: List<Person>,
        items: List<Item>,
        assignments: Map<String, String>
    ): List<Settlement> {
        val personAmounts = mutableMapOf<String, Double>()

        for (person in people) {
            personAmounts[person.id] = 0.0
        }

        for (item in items) {
            val assignedPersonId = assignments[item.id]
            if (assignedPersonId != null && personAmounts.containsKey(assignedPersonId)) {
                val currentAmount = personAmounts[assignedPersonId] ?: 0.0
                personAmounts[assignedPersonId] = currentAmount + item.total()
            }
        }

        return people.map { person ->
            Settlement(person.id, person.name, round((personAmounts[person.id] ?: 0.0) * 100) / 100)
        }
    }

    private fun calculateCustomSplit(
        people: List<Person>,
        totalAmount: Double,
        ratios: Map<String, Double>
    ): List<Settlement> {
        val totalRatio = ratios.values.sum()

        return people.map { person ->
            val ratio = ratios[person.id] ?: 0.0
            val amount = if (totalRatio > 0) round((totalAmount * ratio / totalRatio) * 100) / 100 else 0.0
            Settlement(person.id, person.name, amount)
        }
    }

    fun calculateTransactions(settlements: List<Settlement>): List<Transaction> {
        val debtors = settlements.filter { it.amount > 0 }.toMutableList()
        val creditors = settlements.filter { it.amount < 0 }.map { it.copy(amount = -it.amount) }.toMutableList()
        val transactions = mutableListOf<Transaction>()

        while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
            val debtor = debtors.first()
            val creditor = creditors.first()

            val amount = minOf(debtor.amount, creditor.amount)
            transactions.add(Transaction(debtor.personId, creditor.personId, amount))

            debtors[0] = debtor.copy(amount = debtor.amount - amount)
            creditors[0] = creditor.copy(amount = creditor.amount - amount)

            if (debtors[0].amount == 0.0) debtors.removeAt(0)
            if (creditors[0].amount == 0.0) creditors.removeAt(0)
        }

        return transactions
    }
}
