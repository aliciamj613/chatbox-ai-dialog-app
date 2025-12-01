import Foundation

final class ChatRepository {
    private let store: ChatStore
    private let api: ZhipuAPIClient

    init(store: ChatStore, api: ZhipuAPIClient) {
        self.store = store
        self.api = api
    }

    // 历史消息
    func history(for conversationId: Int64) -> [Message] {
        store.messages(for: conversationId)
    }

    // 发送文本消息（在线 + 记忆）
    func sendOnlineMessage(text: String, conversationId: Int64) async throws {
        // 1. 插入用户消息
        _ = store.addMessage(text: text, isUser: true, conversationId: conversationId)

        // 2. 取所有消息作为上下文
        let all = store.messages(for: conversationId)
        // ✅ 只拿最后 12 条，避免太长
        let lastMessages = Array(all.suffix(12))

        let payloadMsgs = lastMessages.map { msg in
            ChatMessagePayload(
                role: msg.isUser ? "user" : "assistant",
                content: msg.text
            )
        }


        // 3. 调用智谱
        let reply = try await api.chat(messages: payloadMsgs)

        // 4. 插入助手消息
        let assistant = store.addMessage(
            text: reply,
            isUser: false,
            conversationId: conversationId
        )

        // 5. 更新会话标题 & 时间
        if var conv = store.conversation(by: conversationId) {
            let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
            if conv.title == "新会话" || conv.title.isEmpty {
                let title: String
                if trimmed.isEmpty {
                    title = "新会话"
                } else if trimmed.count <= 18 {
                    title = trimmed
                } else {
                    title = String(trimmed.prefix(18)) + "…"
                }
                conv.title = title
            }
            conv.updatedAt = assistant.timestamp
            store.updateConversation(conv)
        }
    }

    // 文生图
    func generateImageFromText(prompt: String, conversationId: Int64) async throws {
        _ = store.addMessage(
            text: "[图片请求] \(prompt)",
            isUser: true,
            conversationId: conversationId
        )

        let url = try await api.generateImage(prompt: prompt)
        let reply = "图片已生成：\(url)"
        let assistant = store.addMessage(
            text: reply,
            isUser: false,
            conversationId: conversationId
        )

        if var conv = store.conversation(by: conversationId) {
            conv.updatedAt = assistant.timestamp
            if conv.title == "新会话" || conv.title.isEmpty {
                let trimmed = prompt.trimmingCharacters(in: .whitespacesAndNewlines)
                let title: String
                if trimmed.isEmpty {
                    title = "新会话"
                } else if trimmed.count <= 18 {
                    title = trimmed
                } else {
                    title = String(trimmed.prefix(18)) + "…"
                }
                conv.title = title
            }
            store.updateConversation(conv)
        }
    }

    // 文生视频
    func generateVideoFromText(prompt: String, conversationId: Int64) async throws {
        _ = store.addMessage(
            text: "[视频请求] \(prompt)",
            isUser: true,
            conversationId: conversationId
        )

        let taskId = try await api.createVideoTask(prompt: prompt)
        let (url, cover) = try await api.pollVideoResult(taskId: taskId)

        var reply = "视频已生成：\(url)"
        if let cover = cover, !cover.isEmpty {
            reply += "\n封面：\(cover)"
        }

        let assistant = store.addMessage(
            text: reply,
            isUser: false,
            conversationId: conversationId
        )

        if var conv = store.conversation(by: conversationId) {
            conv.updatedAt = assistant.timestamp
            if conv.title == "新会话" || conv.title.isEmpty {
                let trimmed = prompt.trimmingCharacters(in: .whitespacesAndNewlines)
                let title: String
                if trimmed.isEmpty {
                    title = "新会话"
                } else if trimmed.count <= 18 {
                    title = trimmed
                } else {
                    title = String(trimmed.prefix(18)) + "…"
                }
                conv.title = title
            }
            store.updateConversation(conv)
        }
    }
}
