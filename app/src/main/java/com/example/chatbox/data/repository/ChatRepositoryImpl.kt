package com.example.chatbox.data.repository

import com.example.chatbox.data.local.db.MessageDao
import com.example.chatbox.data.model.toDomain
import com.example.chatbox.data.model.toEntity
import com.example.chatbox.domain.model.Message
import com.example.chatbox.domain.repository.ChatRepository
import com.example.chatbox.util.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(
    private val messageDao: MessageDao
) : ChatRepository {

    override fun observeMessages(userId: String): Flow<List<Message>> =
        messageDao.observeMessages(userId).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun clearMessages(userId: String) {
        messageDao.clearMessages(userId)
    }

    override suspend fun sendMessage(userId: String, content: String): Result<Unit> = try {
        val now = System.currentTimeMillis()

        val userMsg = Message(
            userId = userId,
            role = "user",
            content = content,
            timestamp = now
        )

        // 伪造一个 AI 回复，先跑通
        delay(500)
        val aiMsg = Message(
            userId = userId,
            role = "assistant",
            content = "AI: $content",
            timestamp = now + 1
        )

        messageDao.insertMessages(listOf(userMsg.toEntity(), aiMsg.toEntity()))
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
