import SwiftUI

struct SplitMethodView: View {
    @ObservedObject var viewModel: SplitViewModel
    @Environment(\.dismiss) var dismiss
    @State private var selectedMethod: SplitMethod = .even

    enum SplitMethod {
        case even
        case byItem
        case custom
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: { dismiss() }) {
                    Text("< back")
                        .foregroundColor(.black)
                }
                Text("How to split?")
                    .font(.system(size: 16, weight: .semibold))
                Spacer()
            }
            .padding(16)

            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    Text("Pick a way to split")
                        .font(.system(size: 20, weight: .bold))

                    Text("$77.40 total - between 2 people")
                        .font(.system(size: 14, weight: .regular))
                        .foregroundColor(.gray)
                        .padding(.bottom, 8)

                    // Split evenly option
                    SplitOptionView(
                        isSelected: selectedMethod == .even,
                        title: "Split evenly",
                        subtitle: "½ (50/50 - skip the items)",
                        details: "You\n$38.70\n\nSam\n$38.70",
                        action: { selectedMethod = .even }
                    )

                    SplitOptionView(
                        isSelected: selectedMethod == .byItem,
                        title: "Split by item",
                        subtitle: "assign each line to a person",
                        details: nil,
                        action: { selectedMethod = .byItem }
                    )

                    SplitOptionView(
                        isSelected: selectedMethod == .custom,
                        title: "Custom share",
                        subtitle: "drag the split, e.g. 60/40",
                        details: nil,
                        action: { selectedMethod = .custom }
                    )

                    Spacer()
                }
                .padding(16)
            }

            // Continue button
            Button(action: { dismiss() }) {
                HStack {
                    Text("Continue →")
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

struct SplitOptionView: View {
    let isSelected: Bool
    let title: String
    let subtitle: String
    let details: String?
    let action: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                Image(systemName: isSelected ? "circle.fill" : "circle")
                    .font(.system(size: 20))
                    .foregroundColor(isSelected ? .green : .gray)

                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.system(size: 14, weight: .semibold))
                    Text(subtitle)
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(.gray)
                }
                Spacer()
            }

            if let details = details, isSelected {
                Text(details)
                    .font(.system(size: 12, weight: .regular))
                    .padding(8)
                    .background(Color(UIColor(red: 0.98, green: 0.98, blue: 0.98, alpha: 1)))
                    .cornerRadius(8)
            }
        }
        .padding(16)
        .border(isSelected ? Color.green : Color.gray.opacity(0.3), width: 2)
        .cornerRadius(12)
        .onTapGesture(perform: action)
    }
}

#Preview {
    SplitMethodView(viewModel: SplitViewModel())
}
