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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException



class ChatRepositoryImpl(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val api: ChatApiService
) : ChatRepository {

    // =====================================================
    // 1. 历史消息（有“记忆”的基础）
    // =====================================================

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

    // =====================================================
    // 2. 纯文本对话（glm-4.5，带完整上下文记忆）
    // =====================================================

    override suspend fun sendOnlineMessage(
        userText: String,
        conversationId: Long
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            // (1) 先写入用户消息
            messageDao.insertMessage(
                MessageEntity(
                    text = userText,
                    isUser = true,
                    timestamp = now,
                    conversationId = conversationId
                )
            )

            // (2) 从数据库取出整个会话历史，作为上下文
            val historyEntities = messageDao.getMessagesByConversation(conversationId)
            val historyMessages = historyEntities.map { entity ->
                ChatMessage(
                    role = if (entity.isUser) "user" else "assistant",
                    content = entity.text
                )
            }

            val request = ChatRequest(
                // model 默认 "glm-4.5"
                messages = historyMessages
            )

            val response = api.sendChat(request)
            val replyText = response.choices.firstOrNull()?.message?.content
                ?: "AI 没有返回内容"

            val replyTime = System.currentTimeMillis()

            // (3) 写入 AI 回复
            messageDao.insertMessage(
                MessageEntity(
                    text = replyText,
                    isUser = false,
                    timestamp = replyTime,
                    conversationId = conversationId
                )
            )

            // (4) 更新会话标题 & 更新时间
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

    // =====================================================
    // 3. 文生图：cogview-4-250304（同步）
    // =====================================================

    override suspend fun generateImageFromText(
        prompt: String,
        conversationId: Long
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            // 把“生成图片”的请求也记录下来
            messageDao.insertMessage(
                MessageEntity(
                    text = "[图片请求] $prompt",
                    isUser = true,
                    timestamp = now,
                    conversationId = conversationId
                )
            )

            val request = ImageRequest(
                prompt = prompt      // model 默认 "cogview-4-250304"
            )

            val response = api.generateImage(request)
            val url = response.data.firstOrNull()?.url
                ?: "图片生成失败（接口未返回 URL）"

            val replyTime = System.currentTimeMillis()
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
        } catch (e: HttpException) {
            Log.e("ChatRepositoryImpl", "generateImage HTTP error: ${e.code()} ${e.message()}", e)
            Result.failure(e)
        } catch (e: IOException) {
            Log.e("ChatRepositoryImpl", "generateImage Network error: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("ChatRepositoryImpl", "generateImage error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =====================================================
    // 4. 文生视频：cogvideox-3（异步任务 + 轮询）
    // =====================================================

    override suspend fun generateVideoFromText(
        prompt: String,
        conversationId: Long
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            // 把“视频请求”记录下来
            messageDao.insertMessage(
                MessageEntity(
                    text = "[视频请求] $prompt",
                    isUser = true,
                    timestamp = now,
                    conversationId = conversationId
                )
            )

            // (1) 创建异步视频任务
            val createRequest = VideoRequest(
                prompt = prompt      // model 默认 "cogvideox-3"
            )

            val task = api.generateVideoTask(createRequest)
            val taskId = task.id
            Log.d(
                "ChatRepositoryImpl",
                "video task created, id=$taskId, status=${task.task_status}"
            )

            // (2) 轮询异步结果：最多 30 次，每次间隔 3 秒（总等待约 90 秒）
            val maxPollCount = 30
            val pollIntervalMillis = 3_000L

            var finalUrl: String? = null
            var finalCover: String? = null
            var finalStatus: String? = null

            for (i in 0 until maxPollCount) {
                delay(pollIntervalMillis)

                val result = api.getAsyncResult(taskId)
                finalStatus = result.task_status
                Log.d(
                    "ChatRepositoryImpl",
                    "video async result poll #$i status=$finalStatus, requestId=${result.request_id}"
                )

                if (result.task_status == "SUCCESS") {
                    val first = result.video_result?.firstOrNull()
                    finalUrl = first?.url
                    finalCover = first?.cover_image_url
                    break
                } else if (result.task_status == "FAILED") {
                    throw RuntimeException("视频生成失败（任务状态 FAILED）")
                }
            }

            if (finalUrl == null) {
                throw RuntimeException(
                    "视频生成超时或未返回 URL（taskId=$taskId, status=$finalStatus）"
                )
            }

            val replyTime = System.currentTimeMillis()
            val replyText = buildString {
                append("视频已生成：$finalUrl")
                if (!finalCover.isNullOrBlank()) {
                    append("\n封面：$finalCover")
                }
            }

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
        } catch (e: HttpException) {
            Log.e(
                "ChatRepositoryImpl",
                "generateVideo HTTP error: ${e.code()} ${e.message()}",
                e
            )
            Result.failure(e)
        } catch (e: IOException) {
            Log.e("ChatRepositoryImpl", "generateVideo Network error: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("ChatRepositoryImpl", "generateVideo error: ${e.message}", e)
            Result.failure(e)
        }
    }


    // =====================================================
    // 5. 更新会话标题 & 更新时间（共用逻辑）
    // =====================================================

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
