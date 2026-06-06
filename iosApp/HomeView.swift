import SwiftUI

struct HomeView: View {
    @StateObject private var viewModel = SplitViewModel()
    @State private var showCreateSplit = false
    @State private var splitName = "Split a bill"
    @State private var people = [
        PersonModel(id: UUID().uuidString, name: "You", emoji: "👤"),
        PersonModel(id: UUID().uuidString, name: "Sam", emoji: "👤")
    ]

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Header
                VStack(alignment: .leading, spacing: 8) {
                    Text(splitName)
                        .font(.system(size: 24, weight: .bold))
                    Text("Two people, one bill.")
                        .font(.system(size: 16, weight: .regular))
                        .foregroundColor(.gray)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(16)

                ScrollView {
                    VStack(spacing: 32) {
                        // People avatars
                        HStack(spacing: 0) {
                            ForEach(Array(people.enumerated()), id: \.element.id) { index, person in
                                VStack(spacing: 8) {
                                    Circle()
                                        .fill(Color.green)
                                        .frame(width: 60, height: 60)
                                        .overlay(
                                            Text(String(person.name.prefix(1)))
                                                .font(.system(size: 24, weight: .bold))
                                                .foregroundColor(.white)
                                        )
                                    Text(person.name)
                                        .font(.system(size: 12, weight: .regular))
                                }

                                if index < people.count - 1 {
                                    Text("&")
                                        .padding(.horizontal, 16)
                                        .font(.system(size: 20))
                                }
                            }
                            Spacer()
                        }
                        .frame(maxWidth: .infinity)

                        // Receipt drop area
                        VStack(spacing: 12) {
                            Text("📄")
                                .font(.system(size: 40))
                            Text("drag a receipt here")
                                .font(.system(size: 14, weight: .regular))
                                .foregroundColor(.gray)
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 150)
                        .background(Color(UIColor(red: 0.98, green: 0.98, blue: 0.98, alpha: 1)))
                        .border(Color.gray.opacity(0.3), width: 2)
                        .cornerRadius(8)
                    }
                    .padding(16)
                }

                Spacer()

                // Button
                VStack(spacing: 8) {
                    NavigationLink(destination: ReviewItemsView(viewModel: viewModel)) {
                        HStack {
                            Text("+ Add receipts")
                                .font(.system(size: 16, weight: .semibold))
                                .frame(maxWidth: .infinity)
                        }
                        .frame(height: 50)
                        .background(Color.green)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                    }

                    Text("You can add more than one.")
                        .font(.system(size: 12, weight: .regular))
                        .foregroundColor(.gray)
                }
                .padding(16)
            }
            .navigationTitle("")
            .navigationBarHidden(true)
        }
        .environmentObject(viewModel)
    }
}

#Preview {
    HomeView()
}
