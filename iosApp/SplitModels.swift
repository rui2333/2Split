import Foundation

// MARK: - Data Models (Mirror of Kotlin shared models)

struct PersonModel: Identifiable {
    let id: String
    let name: String
    let emoji: String
}

struct ReceiptModel: Identifiable {
    let id: String
    let splitId: String
    let imageUri: String
    let uploadedAt: Date
    let isProcessed: Bool
}

struct ItemModel: Identifiable {
    let id: String
    let receiptId: String
    let name: String
    let quantity: Double
    let price: Double

    var total: Double {
        quantity * price
    }
}

struct SplitModel: Identifiable {
    let id: String
    let name: String
    let people: [PersonModel]
    let receipts: [ReceiptModel]
    let items: [ItemModel]
    let createdAt: Date
}

struct SettlementModel: Identifiable {
    let id: String
    let personId: String
    let personName: String
    let amount: Double
}

struct TransactionModel {
    let from: String
    let to: String
    let amount: Double
}

// MARK: - Split Calculator (Mirror of Kotlin SplitCalculator)

class SplitCalculator {
    static func calculateSettlements(
        people: [PersonModel],
        items: [ItemModel],
        splitMethod: SplitMethodType = .even
    ) -> [SettlementModel] {
        let totalAmount = items.reduce(0) { $0 + $1.total }
        let peopleCount = people.count

        switch splitMethod {
        case .even:
            return calculateEvenSplit(people: people, totalAmount: totalAmount)
        case .byItem(let assignments):
            return calculateByItemSplit(people: people, items: items, assignments: assignments)
        case .custom(let ratios):
            return calculateCustomSplit(people: people, totalAmount: totalAmount, ratios: ratios)
        }
    }

    private static func calculateEvenSplit(
        people: [PersonModel],
        totalAmount: Double
    ) -> [SettlementModel] {
        let amountPerPerson = (totalAmount / Double(people.count)).rounded(to: 2)
        let remainder = (totalAmount - (amountPerPerson * Double(people.count))).rounded(to: 2)

        return people.enumerated().map { index, person in
            let amount = index == 0 ? amountPerPerson + remainder : amountPerPerson
            return SettlementModel(
                id: person.id,
                personId: person.id,
                personName: person.name,
                amount: amount
            )
        }
    }

    private static func calculateByItemSplit(
        people: [PersonModel],
        items: [ItemModel],
        assignments: [String: String]
    ) -> [SettlementModel] {
        var personAmounts: [String: Double] = [:]

        for person in people {
            personAmounts[person.id] = 0.0
        }

        for item in items {
            if let assignedPersonId = assignments[item.id] {
                let currentAmount = personAmounts[assignedPersonId] ?? 0.0
                personAmounts[assignedPersonId] = currentAmount + item.total
            }
        }

        return people.map { person in
            SettlementModel(
                id: person.id,
                personId: person.id,
                personName: person.name,
                amount: (personAmounts[person.id] ?? 0.0).rounded(to: 2)
            )
        }
    }

    private static func calculateCustomSplit(
        people: [PersonModel],
        totalAmount: Double,
        ratios: [String: Double]
    ) -> [SettlementModel] {
        let totalRatio = ratios.values.reduce(0, +)

        return people.map { person in
            let ratio = ratios[person.id] ?? 0.0
            let amount = totalRatio > 0
                ? (totalAmount * ratio / totalRatio).rounded(to: 2)
                : 0.0
            return SettlementModel(
                id: person.id,
                personId: person.id,
                personName: person.name,
                amount: amount
            )
        }
    }
}

enum SplitMethodType {
    case even
    case byItem([String: String])
    case custom([String: Double])
}

extension Double {
    func rounded(to places: Int) -> Double {
        let divisor = pow(10.0, Double(places))
        return (self * divisor).rounded() / divisor
    }
}
