// app/src/main/java/com/example/chatbox/domain/usecase/SendMessageUseCase.kt
package com.example.chatbox.domain.usecase

import com.example.chatbox.domain.model.Message
import com.example.chatbox.domain.repository.ChatRepository

class SendMessageUseCase(
    private val repository: ChatRepository
) {
    /**
     * 调用仓库发送在线消息，返回 AI 回复。
     * 实际 UI 一般不会直接用这个返回值，而是通过 getHistory() 的 Flow 更新列表。
     */
    suspend operator fun invoke(userText: String): Message {
        return repository.sendOnlineMessage(userText)
    }
}
