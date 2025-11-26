// app/src/main/java/com/example/chatbox/domain/repository/ChatRepository.kt
package com.example.chatbox.domain.repository

import com.example.chatbox.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    /** 按时间顺序返回历史记录流 */
    fun getHistory(): Flow<List<Message>>

    /** 插入一条消息（用户或 AI） */
    suspend fun insertMessage(message: Message)

    /** 清空所有历史记录 */
    suspend fun clearHistory()

    /** 删除一条消息（目前实现里先空着） */
    suspend fun deleteMessage(message: Message)

    /** 发送在线消息（调用大模型），返回 AI 回复 */
    suspend fun sendOnlineMessage(userText: String): Message
}
