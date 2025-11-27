package com.example.chatbox.data.repository

import android.util.Log
import com.example.chatbox.data.local.db.ConversationDao
import com.example.chatbox.data.local.db.MessageDao
import com.example.chatbox.data.model.MessageEntity
import com.example.chatbox.data.remote.ChatApiService
import com.example.chatbox.data.remote.ChatMessage
import com.example.chatbox.data.remote.ChatRequest
import com.example.chatbox.data.remote.ImageRequest
import com.example.chatbox.data.remote.VideoRequest
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

    // =============== 历史（有“记忆”的基础） ===============

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

    // =============== 1. 纯文本对话（带上下文记忆） ===============

    override suspend fun sendOnlineMessage(
        userText: String,
        conversationId: Long
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            // 1. 先写入当前这条用户消息
            messageDao.insertMessage(
                MessageEntity(
                    text = userText,
                    isUser = true,
                    timestamp = now,
                    conversationId = conversationId
                )
            )

            // 2. 从数据库取出「这个会话」的全部历史消息
            val historyEntities = messageDao.getMessagesByConversation(conversationId)

            // 转成大模型的 message 结构（user / assistant）
            val historyMessages = historyEntities.map { entity ->
                ChatMessage(
                    role = if (entity.isUser) "user" else "assistant",
                    content = entity.text
                )
            }

            // 3. 在最前面加一个 system 提示，明确要求“基于全历史回答”
            val fullMessages = buildList {
                add(
                    ChatMessage(
                        role = "system",
                        content = "你是一个聊天助手，需要结合本次会话中用户与助手的全部历史消息来回答问题。" +
                                "如果用户提到“刚才”“之前”“上面说的”，请参考本会话里之前的内容进行回答。"
                    )
                )
                addAll(historyMessages)
            }

            val request = ChatRequest(
                // 如果 ChatRequest 有其他字段（如 model），这里可以显式写上
                // model = "glm-4",
                messages = fullMessages
            )

            // 4. 调用文本对话接口
            val response = api.sendChat(request)
            val replyText = response.choices.firstOrNull()?.message?.content
                ?: "AI 没有返回内容"

            val replyTime = System.currentTimeMillis()

            // 5. 把 AI 回复写入数据库
            messageDao.insertMessage(
                MessageEntity(
                    text = replyText,
                    isUser = false,
                    timestamp = replyTime,
                    conversationId = conversationId
                )
            )

            // 6. 更新会话标题 & 更新时间（标题自动从首轮用户问题截断）
            updateConversationMetaAfterReply(
                conversationId = conversationId,
                lastUserText = userText,
                updatedAt = replyTime
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

    // =============== 2. 文生图（把图片 URL 写成一条 AI 消息） ===============

    override suspend fun generateImageFromText(
        prompt: String,
        conversationId: Long
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            // 用户的“生成图片”请求也作为一条用户消息写入
            messageDao.insertMessage(
                MessageEntity(
                    text = "[图片请求] $prompt",
                    isUser = true,
                    timestamp = now,
                    conversationId = conversationId
                )
            )

            val request = ImageRequest(
                prompt = prompt
            )

            val response = api.generateImage(request)
            val url = response.data.firstOrNull()?.url
                ?: "图片生成失败（接口未返回 URL）"

            val replyTime = System.currentTimeMillis()

            // 这里先简单用 URL 文本保存，后续你可以根据前缀判断并用 Coil 展示图片
            val replyText = "图片已生成：$url"

            messageDao.insertMessage(
                MessageEntity(
                    text = replyText,
                    isUser = false,
                    timestamp = replyTime,
                    conversationId = conversationId
                )
            )

            updateConversationMetaAfterReply(
                conversationId = conversationId,
                lastUserText = prompt,
                updatedAt = replyTime
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepositoryImpl", "generateImage error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =============== 3. 文生视频（把视频 URL 写成一条 AI 消息） ===============

    override suspend fun generateVideoFromText(
        prompt: String,
        conversationId: Long
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            messageDao.insertMessage(
                MessageEntity(
                    text = "[视频请求] $prompt",
                    isUser = true,
                    timestamp = now,
                    conversationId = conversationId
                )
            )

            val request = VideoRequest(
                prompt = prompt
            )

            val response = api.generateVideo(request)
            val url = response.data.firstOrNull()?.url
                ?: "视频生成失败（接口未返回 URL）"

            val replyTime = System.currentTimeMillis()
            val replyText = "视频已生成：$url"

            messageDao.insertMessage(
                MessageEntity(
                    text = replyText,
                    isUser = false,
                    timestamp = replyTime,
                    conversationId = conversationId
                )
            )

            updateConversationMetaAfterReply(
                conversationId = conversationId,
                lastUserText = prompt,
                updatedAt = replyTime
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepositoryImpl", "generateVideo error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =============== 会话标题 & 更新时间统一更新 ===============

    private suspend fun updateConversationMetaAfterReply(
        conversationId: Long,
        lastUserText: String,
        updatedAt: Long
    ) {
        val conversation = conversationDao.getConversationById(conversationId)
        if (conversation != null) {
            val shouldUpdateTitle =
                conversation.title.isBlank() ||
                        conversation.title == "新的对话" ||
                        conversation.title.startsWith("对话 #")

            val newTitle = if (lastUserText.isNotBlank() && shouldUpdateTitle) {
                val trimmed = lastUserText.trim()
                if (trimmed.length <= 18) trimmed else trimmed.substring(0, 18) + "…"
            } else {
                conversation.title
            }

            conversationDao.updateConversation(
                conversation.copy(
                    title = newTitle,
                    updatedAt = updatedAt
                )
            )
        }
    }
}
