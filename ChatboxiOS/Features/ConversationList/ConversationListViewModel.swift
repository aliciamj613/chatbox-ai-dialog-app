import Foundation
import Combine

final class ConversationListViewModel: ObservableObject {
    @Published var conversations: [Conversation] = []

    // 加载当前用户的会话列表
    func load(for user: User, store: ChatStore) {
        conversations = store.conversations(for: user.id)
    }

    // 新建会话
    func createConversation(for user: User, store: ChatStore) {
        _ = store.createConversation(for: user.id)
        load(for: user, store: store)
    }

    // 删除会话
    func deleteConversation(_ conv: Conversation, store: ChatStore, user: User) {
        store.deleteConversation(id: conv.id)
        load(for: user, store: store)
    }
}
