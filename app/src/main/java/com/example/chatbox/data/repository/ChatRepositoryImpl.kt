package com.example.chatbox.data.repository

import android.util.Log
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
import java.io.IOException

class ChatRepositoryImpl(
    private val messageDao: MessageDao,
    private val api: ChatApiService
) : ChatRepository {

    override fun getHistory(conversationId: Long): Flow<List<Message>> {
        // 按会话 ID 订阅消息列表
        return messageDao
            .observeMessagesByConversation(conversationId)
            .map { entities ->
                entities.map { entity ->
                    Message(
                        id = entity.id,
                        text = entity.text,
                        isUser = entity.isUser,
                        timestamp = entity.timestamp
                    )
                }
            }
    }

    override suspend fun sendOnlineMessage(
        userText: String,
        conversationId: Long
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            // 1. 先把用户消息写进对应会话
            messageDao.insertMessage(
                MessageEntity(
                    // id 自增，用默认 0 即可
                    text = userText,
                    isUser = true,
                    timestamp = now,
                    // ✅ 关键：写入当前会话 ID
                    conversationId = conversationId
                )
            )

            // 2. 取出当前会话的全部历史，用于构造对话上下文
            val history = messageDao.getMessagesByConversation(conversationId)

            val apiMessages = history.map { entity ->
                ChatMessage(
                    role = if (entity.isUser) "user" else "assistant",
                    content = entity.text
                )
            }

            // 3. 调用智谱 API（这里使用你 ApiModels 里的 ChatRequest / ChatResponse 定义）
            val request = ChatRequest(
                model = "glm-4-air", // 如果你实际用的是别的模型，在这里改
                messages = apiMessages
            )

            val response = api.sendChat(request)

            val firstChoice = response.choices.firstOrNull()
            val replyText = firstChoice?.message?.content
                ?: "对不起，我暂时无法回答这个问题。"

            val replyTime = System.currentTimeMillis()

            // 4. 再把 AI 回复写回同一个会话
            messageDao.insertMessage(
                MessageEntity(
                    text = replyText,
                    isUser = false,
                    timestamp = replyTime,
                    conversationId = conversationId
                )
            )

            Result.success(Unit)
        } catch (e: HttpException) {
            Log.e("ChatRepositoryImpl", "HTTP error: ${e.code()} ${e.message()}", e)
            Result.failure(e)
        } catch (e: IOException) {
            Log.e("ChatRepositoryImpl", "Network error: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("ChatRepositoryImpl", "Unexpected error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
