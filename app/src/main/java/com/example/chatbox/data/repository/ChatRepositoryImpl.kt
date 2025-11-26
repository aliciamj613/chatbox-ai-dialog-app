package com.example.chatbox.data.repository

import com.example.chatbox.domain.model.Message
import com.example.chatbox.domain.repository.ChatRepository
import kotlinx.coroutines.delay

class ChatRepositoryImpl : ChatRepository {

    // 暂时用内存 List 模拟历史记录
    private val messages = mutableListOf<Message>()

    override suspend fun sendMessage(userId: String, text: String): List<Message> {
        val currentId = (messages.maxOfOrNull { it.id } ?: 0L) + 1L

        val userMsg = Message(
            id = currentId,
            text = text,
            isUser = true
        )
        messages += userMsg

        // 模拟一下网络延迟
        delay(200)

        val botMsg = Message(
            id = currentId + 1,
            text = "Echo from repo: $text",
            isUser = false
        )
        messages += botMsg

        return messages.toList()
    }

    override suspend fun getHistory(userId: String): List<Message> {
        // 现在就直接返回内存里的 list
        return messages.toList()
    }
}
