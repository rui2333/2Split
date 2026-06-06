import Foundation
import SwiftUI

@MainActor
class SplitViewModel: ObservableObject {
    @Published var currentSplit: SplitModel?
    @Published var allSplits: [SplitModel] = []
    @Published var selectedSplitMethod: SplitMethodType = .even
    @Published var settlements: [SettlementModel] = []

    func createSplit(name: String, people: [PersonModel]) {
        let newSplit = SplitModel(
            id: UUID().uuidString,
            name: name,
            people: people,
            receipts: [],
            items: [],
            createdAt: Date()
        )
        currentSplit = newSplit
        allSplits.append(newSplit)
    }

    func addItemsToCurrentSplit(_ items: [ItemModel]) {
        guard var split = currentSplit else { return }
        split.items.append(contentsOf: items)
        currentSplit = split

        // Recalculate settlements
        updateSettlements()
    }

    func updateSplitMethod(_ method: SplitMethodType) {
        selectedSplitMethod = method
        updateSettlements()
    }

    private func updateSettlements() {
        guard let split = currentSplit else { return }
        settlements = SplitCalculator.calculateSettlements(
            people: split.people,
            items: split.items,
            splitMethod: selectedSplitMethod
        )
    }

    func loadSplit(id: String) {
        currentSplit = allSplits.first { $0.id == id }
        if let split = currentSplit {
            settlements = SplitCalculator.calculateSettlements(
                people: split.people,
                items: split.items,
                splitMethod: selectedSplitMethod
            )
        }
    }

    func deleteSplit(id: String) {
        allSplits.removeAll { $0.id == id }
        if currentSplit?.id == id {
            currentSplit = nil
        }
    }
}
