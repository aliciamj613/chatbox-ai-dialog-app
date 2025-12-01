import Foundation
import SwiftUI
import Combine   // ✅ 关键：补上这一行

final class AppState: ObservableObject {
    @Published var currentUser: User? {
        didSet { saveLastUserId() }
    }

    @Published var isDarkMode: Bool = false

    let store: ChatStore
    let repository: ChatRepository

    private let lastUserKey = "chatbox_last_user_id"

    init() {
        self.store = ChatStore()
        let apiClient = ZhipuAPIClient()
        self.repository = ChatRepository(store: store, api: apiClient)

        // 尝试自动登录上次用户
        if let id = UserDefaults.standard.value(forKey: lastUserKey) as? Int64 {
            self.currentUser = store.user(by: id)
        }
    }

    private func saveLastUserId() {
        if let id = currentUser?.id {
            UserDefaults.standard.set(id, forKey: lastUserKey)
        } else {
            UserDefaults.standard.removeObject(forKey: lastUserKey)
        }
    }

    func logout() {
        currentUser = nil
    }
}
