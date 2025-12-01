import Foundation

final class ChatStore {
    // 内部快照，相当于三张表
    private struct Snapshot: Codable {
        var users: [User] = []
        var conversations: [Conversation] = []
        var messages: [Message] = []
    }

    private var snapshot: Snapshot
    private let fileURL: URL
    private let queue = DispatchQueue(label: "chatbox.store.queue")

    init() {
        let fm = FileManager.default
        let dir = fm.urls(for: .documentDirectory, in: .userDomainMask).first!
        self.fileURL = dir.appendingPathComponent("chatbox_store.json")

        if let data = try? Data(contentsOf: fileURL),
           let snap = try? JSONDecoder().decode(Snapshot.self, from: data) {
            self.snapshot = snap
        } else {
            self.snapshot = Snapshot()
        }
    }

    private func persist() {
        queue.async {
            do {
                let encoder = JSONEncoder()
                encoder.dateEncodingStrategy = .iso8601
                let data = try encoder.encode(self.snapshot)
                try data.write(to: self.fileURL, options: [.atomic])
            } catch {
                print("Persist error: \(error)")
            }
        }
    }

    // MARK: - ID 生成

    private func newId() -> Int64 {
        Int64(Date().timeIntervalSince1970 * 1000) + Int64(Int.random(in: 0..<1000))
    }

    // MARK: - 用户相关

    func user(by id: Int64) -> User? {
        snapshot.users.first { $0.id == id }
    }

    func login(name: String, password: String) -> User? {
        snapshot.users.first { $0.name == name && $0.password == password }
    }

    enum StoreError: Error, LocalizedError {
        case userAlreadyExists
        case userNotFound

        var errorDescription: String? {
            switch self {
            case .userAlreadyExists: return "用户已存在"
            case .userNotFound: return "用户不存在"
            }
        }
    }

    func register(name: String, password: String) throws -> User {
        if snapshot.users.contains(where: { $0.name == name }) {
            throw StoreError.userAlreadyExists
        }
        let user = User(id: newId(), name: name, password: password)
        snapshot.users.append(user)
        persist()
        return user
    }

    func resetPassword(name: String, newPassword: String) throws {
        guard let idx = snapshot.users.firstIndex(where: { $0.name == name }) else {
            throw StoreError.userNotFound
        }
        snapshot.users[idx].password = newPassword
        persist()
    }

    // MARK: - 会话相关

    func conversations(for userId: Int64) -> [Conversation] {
        snapshot.conversations
            .filter { $0.userId == userId }
            .sorted { $0.updatedAt > $1.updatedAt }
    }

    func createConversation(for userId: Int64) -> Conversation {
        let now = Date()
        let conv = Conversation(
            id: newId(),
            userId: userId,
            title: "新会话",
            createdAt: now,
            updatedAt: now
        )
        snapshot.conversations.append(conv)
        persist()
        return conv
    }

    func conversation(by id: Int64) -> Conversation? {
        snapshot.conversations.first { $0.id == id }
    }

    func updateConversation(_ conv: Conversation) {
        if let idx = snapshot.conversations.firstIndex(where: { $0.id == conv.id }) {
            snapshot.conversations[idx] = conv
            persist()
        }
    }

    func deleteConversation(id: Int64) {
        snapshot.conversations.removeAll { $0.id == id }
        snapshot.messages.removeAll { $0.conversationId == id }
        persist()
    }

    // MARK: - 消息相关

    func messages(for conversationId: Int64) -> [Message] {
        snapshot.messages
            .filter { $0.conversationId == conversationId }
            .sorted { $0.timestamp < $1.timestamp }
    }

    @discardableResult
    func addMessage(text: String, isUser: Bool, conversationId: Int64) -> Message {
        let msg = Message(
            id: newId(),
            text: text,
            isUser: isUser,
            timestamp: Date(),
            conversationId: conversationId
        )
        snapshot.messages.append(msg)
        persist()
        return msg
    }

    func clearConversationMessages(conversationId: Int64) {
        snapshot.messages.removeAll { $0.conversationId == conversationId }
        persist()
    }
}
