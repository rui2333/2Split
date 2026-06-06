import SwiftUI

struct ReviewItemsView: View {
    @ObservedObject var viewModel: SplitViewModel
    @Environment(\.dismiss) var dismiss

    private let items = [
        ItemModel(id: UUID().uuidString, receiptId: "", name: "Margherita pizza", quantity: 1, price: 18.00),
        ItemModel(id: UUID().uuidString, receiptId: "", name: "Caesar salad", quantity: 2, price: 12.50),
        ItemModel(id: UUID().uuidString, receiptId: "", name: "Garlic bread", quantity: 1, price: 7.00),
        ItemModel(id: UUID().uuidString, receiptId: "", name: "Sparkling water", quantity: 2, price: 6.00),
        ItemModel(id: UUID().uuidString, receiptId: "", name: "Tiramisu", quantity: 1, price: 9.50),
        ItemModel(id: UUID().uuidString, receiptId: "", name: "House red (glass)", quantity: 2, price: 11.00)
    ]

    private var subtotal: Double {
        items.reduce(0) { $0 + $1.total }
    }

    private let taxAndTip = 13.40
    private var total: Double {
        subtotal + taxAndTip
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Header
                HStack {
                    Button(action: { dismiss() }) {
                        Text("< back")
                            .foregroundColor(.black)
                    }
                    Text("Check the items")
                        .font(.system(size: 16, weight: .semibold))
                    Spacer()
                }
                .padding(16)

                // Items list
                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        ForEach(items) { item in
                            HStack {
                                Text("\(Int(item.quantity))x \(item.name)")
                                    .font(.system(size: 14, weight: .regular))
                                Spacer()
                                Text(String(format: "$%.2f", item.total))
                                    .font(.system(size: 14, weight: .semibold))
                            }
                            Divider()
                        }

                        // Totals
                        VStack(spacing: 8) {
                            HStack {
                                Text("Subtotal")
                                Spacer()
                                Text(String(format: "$%.2f", subtotal))
                            }
                            .font(.system(size: 14, weight: .regular))

                            HStack {
                                Text("Tax + tip")
                                Spacer()
                                Text(String(format: "$%.2f", taxAndTip))
                            }
                            .font(.system(size: 14, weight: .regular))

                            Divider()
                                .padding(.vertical, 8)

                            HStack {
                                Text("Total")
                                    .font(.system(size: 16, weight: .bold))
                                Spacer()
                                Text(String(format: "$%.2f", total))
                                    .font(.system(size: 16, weight: .bold))
                            }
                        }
                        .padding(.top, 16)
                    }
                    .padding(16)
                }

                // Continue button
                NavigationLink(destination: SplitMethodView(viewModel: viewModel)) {
                    HStack {
                        Text("Looks good — assign →")
                            .font(.system(size: 16, weight: .semibold))
                            .frame(maxWidth: .infinity)
                    }
                    .frame(height: 50)
                    .background(Color.green)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                }
                .padding(16)
            }
            .navigationBarHidden(true)
        }
    }
}

#Preview {
    ReviewItemsView(viewModel: SplitViewModel())
}
