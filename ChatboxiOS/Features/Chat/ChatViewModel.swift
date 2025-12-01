import Foundation
import Combine 
@MainActor
final class ChatViewModel: ObservableObject {
    @Published var messages: [Message] = []
    @Published var inputText: String = ""
    @Published var isSending: Bool = false
    @Published var error: String?

    let conversationId: Int64
    private let store: ChatStore
    private let repository: ChatRepository

    init(conversationId: Int64, store: ChatStore, repository: ChatRepository) {
        self.conversationId = conversationId
        self.store = store
        self.repository = repository
        loadHistory()
    }

    func loadHistory() {
        messages = store.messages(for: conversationId)
    }

    func onInputChange(_ text: String) {
        inputText = text
        error = nil
    }

    func sendMessage() {
        let text = inputText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty, !isSending else { return }

        isSending = true
        error = nil

        Task {
            do {
                try await repository.sendOnlineMessage(text: text, conversationId: conversationId)
                inputText = ""
                loadHistory()
                isSending = false
            } catch {
                self.error = error.localizedDescription
                self.isSending = false
            }
        }
    }

    func generateImage() {
        let prompt = inputText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !prompt.isEmpty, !isSending else { return }

        isSending = true
        error = nil

        Task {
            do {
                try await repository.generateImageFromText(prompt: prompt, conversationId: conversationId)
                inputText = ""
                loadHistory()
                isSending = false
            } catch {
                self.error = error.localizedDescription
                self.isSending = false
            }
        }
    }

    func generateVideo() {
        let prompt = inputText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !prompt.isEmpty, !isSending else { return }

        isSending = true
        error = nil

        Task {
            do {
                try await repository.generateVideoFromText(prompt: prompt, conversationId: conversationId)
                inputText = ""
                loadHistory()
                isSending = false
            } catch {
                self.error = error.localizedDescription
                self.isSending = false
            }
        }
    }
}
