package com.example.chatbox.data.repository

import com.example.chatbox.data.local.db.MessageDao
import com.example.chatbox.data.model.MessageEntity
import com.example.chatbox.data.remote.ChatApiService
import com.example.chatbox.data.remote.ChatMessage
import com.example.chatbox.data.remote.ChatRequest
import com.example.chatbox.domain.model.Message
import com.example.chatbox.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import android.util.Log

class ChatRepositoryImpl(
    private val messageDao: MessageDao,
    private val api: ChatApiService
) : ChatRepository {

    override fun getHistory(): Flow<List<Message>> =
        messageDao.observeMessages()
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message.toEntity())
    }

    override suspend fun clearHistory() {
        messageDao.clearAll()
    }

    override suspend fun deleteMessage(message: Message) {
        // 暂时不用
    }

    override suspend fun sendOnlineMessage(userText: String): Message {
        // 先保存用户消息
        val userMessage = Message(
            id = 0L,
            text = userText,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )
        messageDao.insertMessage(userMessage.toEntity())

        return try {
            // 组装请求体
            val request = ChatRequest(
                messages = listOf(
                    ChatMessage(role = "user", content = userText)
                )
            )

            // 调用智谱 API
            val response = api.sendChat(request)

            val replyText = response.choices.firstOrNull()?.message?.content
                ?: "（智谱 API 没有返回内容）"

            val aiMessage = Message(
                id = 0L,
                text = replyText,
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
            messageDao.insertMessage(aiMessage.toEntity())
            aiMessage
        } catch (e: HttpException) {
            val code = e.code()
            val body = e.response()?.errorBody()?.string()
            Log.e("ChatRepository", "HTTP $code error: $body", e)

            val aiMessage = Message(
                id = 0L,
                text = "请求失败：HTTP $code\n${body ?: ""}",
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
            messageDao.insertMessage(aiMessage.toEntity())
            aiMessage
        }
    }
}

/** Entity -> Domain */
private fun MessageEntity.toDomain(): Message =
    Message(
        id = id,
        text = text,
        isUser = isUser,
        timestamp = timestamp
    )

/** Domain -> Entity */
private fun Message.toEntity(): MessageEntity =
    MessageEntity(
        id = id,
        text = text,
        isUser = isUser,
        timestamp = timestamp
    )
