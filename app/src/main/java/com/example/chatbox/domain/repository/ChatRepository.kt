package com.example.chatbox.domain.repository

import com.example.chatbox.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    /** 按会话 ID 监听消息历史 */
    fun getHistory(conversationId: Long): Flow<List<Message>>

    /** 发送一条用户消息 + 请求 AI 回复，并把两条消息都写入对应会话 */
    suspend fun sendOnlineMessage(
        userText: String,
        conversationId: Long
    ): Result<Unit>
}
