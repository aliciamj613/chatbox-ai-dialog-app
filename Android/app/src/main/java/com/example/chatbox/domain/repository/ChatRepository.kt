package com.example.chatbox.domain.repository

import com.example.chatbox.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    /** 按会话 ID 监听消息历史（带“记忆”的基础） */
    fun getHistory(conversationId: Long): Flow<List<Message>>

    /** 纯文本对话（已经是“有记忆”的，会带上完整历史消息） */
    suspend fun sendOnlineMessage(
        userText: String,
        conversationId: Long
    ): Result<Unit>

    /** 文生图：生成图片 URL，并作为一条 AI 消息写入该会话 */
    suspend fun generateImageFromText(
        prompt: String,
        conversationId: Long
    ): Result<Unit>

    /** 文生视频：生成视频 URL，并作为一条 AI 消息写入该会话 */
    suspend fun generateVideoFromText(
        prompt: String,
        conversationId: Long
    ): Result<Unit>
}
