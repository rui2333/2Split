import SwiftUI
import PhotosUI

struct ReceiptUploadState {
    let id: String
    let name: String
    var isProcessing: Bool = false
    var items: [ItemModel] = []
}

struct UploadReceiptsView: View {
    @ObservedObject var viewModel: SplitViewModel
    @Environment(\.dismiss) var dismiss
    @State private var receipts: [ReceiptUploadState] = []
    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var showPhotoPicker = false

    private let ocrProcessor = ReceiptOCRProcessor()

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: { dismiss() }) {
                    Text("< back")
                        .foregroundColor(.black)
                }
                Text("Receipts (\(receipts.count))")
                    .font(.system(size: 16, weight: .semibold))
                Spacer()
            }
            .padding(16)

            // Content
            if receipts.isEmpty {
                VStack(spacing: 16) {
                    Text("📸")
                        .font(.system(size: 48))
                    Text("No receipts yet")
                        .font(.system(size: 18, weight: .bold))
                    Text("Add receipts to get started")
                        .font(.system(size: 14, weight: .regular))
                        .foregroundColor(.gray)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                ScrollView {
                    VStack(spacing: 0) {
                        ForEach(Array(receipts.enumerated()), id: \.element.id) { index, receipt in
                            ReceiptUploadItemView(receipt: receipt)
                            if index < receipts.count - 1 {
                                Divider()
                            }
                        }
                    }
                    .padding(16)
                }
            }

            Spacer()

            // Buttons
            VStack(spacing: 12) {
                HStack(spacing: 12) {
                    Button(action: { }) {
                        HStack {
                            Text("📷 Camera")
                                .font(.system(size: 16, weight: .semibold))
                                .frame(maxWidth: .infinity)
                        }
                        .frame(height: 50)
                        .background(Color(red: 0, green: 0.55, blue: 0.55))
                        .foregroundColor(.white)
                        .cornerRadius(8)
                    }

                    PhotosPicker(
                        selection: $selectedPhotoItem,
                        matching: .images,
                        photoLibrary: .shared()
                    ) {
                        HStack {
                            Text("📁 Library")
                                .font(.system(size: 16, weight: .semibold))
                                .frame(maxWidth: .infinity)
                        }
                        .frame(height: 50)
                        .background(Color(red: 0, green: 0.55, blue: 0.55))
                        .foregroundColor(.white)
                        .cornerRadius(8)
                    }
                    .onChange(of: selectedPhotoItem) { _ in
                        handlePhotoSelection()
                    }
                }

                if !receipts.isEmpty {
                    NavigationLink(destination: ReviewItemsView(viewModel: viewModel)) {
                        HStack {
                            Text("Scan receipts →")
                                .font(.system(size: 16, weight: .semibold))
                                .frame(maxWidth: .infinity)
                        }
                        .frame(height: 50)
                        .background(Color.green)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                    }
                }
            }
            .padding(16)
        }
        .navigationBarHidden(true)
    }

    private func handlePhotoSelection() {
        guard let selectedPhotoItem = selectedPhotoItem else { return }

        Task {
            if let data = try await selectedPhotoItem.loadTransferable(type: Data.self),
               let uiImage = UIImage(data: data) {
                let receiptId = UUID().uuidString
                let receiptName = "Receipt \(receipts.count + 1)"

                var newReceipt = ReceiptUploadState(
                    id: receiptId,
                    name: receiptName,
                    isProcessing: true
                )
                receipts.append(newReceipt)

                // Process OCR
                ocrProcessor.processReceipt(image: uiImage) { result in
                    DispatchQueue.main.async {
                        if let index = receipts.firstIndex(where: { $0.id == receiptId }) {
                            switch result {
                            case .success(let items):
                                receipts[index].items = items
                                receipts[index].isProcessing = false
                            case .failure:
                                receipts[index].isProcessing = false
                            }
                        }
                    }
                }
            }
        }
    }
}

struct ReceiptUploadItemView: View {
    let receipt: ReceiptUploadState

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(receipt.name)
                        .font(.system(size: 14, weight: .semibold))

                    if receipt.isProcessing {
                        HStack(spacing: 8) {
                            ProgressView()
                                .scaleEffect(0.8, anchor: .center)
                            Text("Reading your receipts...")
                                .font(.system(size: 12, weight: .regular))
                                .foregroundColor(.gray)
                        }
                    } else if !receipt.items.isEmpty {
                        Text("\(receipt.items.count) items found")
                            .font(.system(size: 12, weight: .regular))
                            .foregroundColor(.green)
                    }
                }
                Spacer()
                Text("✓")
                    .font(.system(size: 20))
                    .foregroundColor(.green)
            }
        }
        .padding(.vertical, 12)
    }
}

#Preview {
    UploadReceiptsView(viewModel: SplitViewModel())
}
