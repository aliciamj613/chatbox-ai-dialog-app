import SwiftUI

struct PasswordField: View {
    let title: String
    @Binding var text: String

    @State private var isSecure = true

    var body: some View {
        HStack {
            Group {
                if isSecure {
                    SecureField(title, text: $text)
                } else {
                    TextField(title, text: $text)
                        .textInputAutocapitalization(.never)
                }
            }
            .textFieldStyle(.roundedBorder)

            Button {
                isSecure.toggle()
            } label: {
                Image(systemName: isSecure ? "eye.slash" : "eye")
            }
        }
    }
}
