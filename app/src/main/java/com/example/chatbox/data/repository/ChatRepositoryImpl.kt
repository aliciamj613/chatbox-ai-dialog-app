// app/src/main/java/com/example/chatbox/data/repository/ChatRepositoryImpl.kt
package com.example.chatbox.data.repository

import com.example.chatbox.data.local.db.MessageDao
import com.example.chatbox.data.model.MessageEntity
import com.example.chatbox.domain.model.Message
import com.example.chatbox.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 聊天仓库实现：
 * - 负责把 Room 的 Entity 转成 Domain Model
 * - sendOnlineMessage 先用本地“假 AI 回复”，后续你接入真实 API 再改
 */
class ChatRepositoryImpl(
    private val messageDao: MessageDao
) : ChatRepository {

    // ========= 本地 Room 部分 =========

    override fun getHistory(): Flow<List<Message>> {
        return messageDao.observeMessages()
            .map { entityList: List<MessageEntity> ->
                entityList.map { entity: MessageEntity ->
                    entity.toDomain()
                }
            }
    }

    override suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message.toEntity())
    }

    override suspend fun clearHistory() {
        messageDao.clearAll()
    }

    override suspend fun deleteMessage(message: Message) {
        // 目前没有 deleteById，就先空实现，防止接口不匹配
        // 如果以后在 Dao 里加：
        // @Query("DELETE FROM messages WHERE id = :id")
        // suspend fun deleteById(id: Long)
        //
        // 这里就可以写：
        // messageDao.deleteById(message.id)
    }

    // ========= 在线消息（暂时本地模拟） =========

    override suspend fun sendOnlineMessage(userText: String): Message {
        // 1. 先把用户消息写入数据库
        val userMessage = Message(
            id = 0L,
            text = userText,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )
        messageDao.insertMessage(userMessage.toEntity())

        // 2. 这里先用“假 AI 回复”占位，后面你接真实 API
        val aiMessage = Message(
            id = 0L,
            text = "（模拟 AI 回复）你刚才说：$userText",
            isUser = false,
            timestamp = System.currentTimeMillis()
        )
        messageDao.insertMessage(aiMessage.toEntity())

        // 返回 AI 消息（其实 UI 主要是通过 getHistory Flow 刷新）
        return aiMessage
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
