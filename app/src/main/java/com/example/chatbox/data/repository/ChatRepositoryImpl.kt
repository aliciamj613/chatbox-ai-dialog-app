package com.example.chatbox.data.repository

import android.util.Log
import com.example.chatbox.data.local.db.ConversationDao
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
    private val conversationDao: ConversationDao,
    private val api: ChatApiService
) : ChatRepository {

    /** 按会话 ID 监听消息历史，映射成 Domain 模型 */
    override fun getHistory(conversationId: Long): Flow<List<Message>> {
        return messageDao.observeMessages(conversationId).map { entities ->
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

    /** 发送一条用户消息，调用智谱 API 拿回复，并把两条消息都写入数据库 */
    override suspend fun sendOnlineMessage(
        userText: String,
        conversationId: Long
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            // 1. 先把用户消息写入数据库
            val userEntity = MessageEntity(
                text = userText,
                isUser = true,
                timestamp = now,
                conversationId = conversationId
            )
            messageDao.insertMessage(userEntity)

            // 2. 拉取该会话的完整历史，构造大模型上下文
            val historyEntities = messageDao.getMessagesByConversation(conversationId)
            val historyMessages = historyEntities.map { entity ->
                ChatMessage(
                    role = if (entity.isUser) "user" else "assistant",
                    content = entity.text
                )
            }

            // 3. 调用智谱 API
            val request = ChatRequest(
                messages = historyMessages
            )
            val response = api.sendChat(request)

            val replyText = response.choices.firstOrNull()?.message?.content
                ?: "AI 没有返回内容"

            val replyTime = System.currentTimeMillis()

            // 4. 把 AI 回复写入数据库
            val assistantEntity = MessageEntity(
                text = replyText,
                isUser = false,
                timestamp = replyTime,
                conversationId = conversationId
            )
            messageDao.insertMessage(assistantEntity)

            // 5. 自动更新会话标题 & updatedAt
            val conversation = conversationDao.getConversationById(conversationId)
            if (conversation != null) {
                val shouldUpdateTitle =
                    conversation.title.isBlank() ||
                            conversation.title == "新的对话" ||
                            conversation.title.startsWith("对话 #")

                val newTitle = if (userText.isNotBlank() && shouldUpdateTitle) {
                    val trimmed = userText.trim()
                    if (trimmed.length <= 18) trimmed else trimmed.substring(0, 18) + "…"
                } else {
                    conversation.title
                }

                conversationDao.updateConversation(
                    conversation.copy(
                        title = newTitle,
                        updatedAt = replyTime
                    )
                )
            }

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
