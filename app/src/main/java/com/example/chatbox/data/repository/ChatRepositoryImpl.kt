package com.example.chatbox.data.repository

import com.example.chatbox.data.local.db.MessageDao
import com.example.chatbox.data.model.MessageEntity
import com.example.chatbox.domain.model.Message
import com.example.chatbox.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 现在只做本地 Room 存储，
 * AI 调用 / 多用户后面再加。
 */
class ChatRepositoryImpl(
    private val messageDao: MessageDao
) : ChatRepository {

    override fun observeMessages(): Flow<List<Message>> {
        return messageDao.observeMessages()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun addLocalMessage(text: String, isUser: Boolean) {
        val entity = MessageEntity(
            text = text,
            isUser = isUser,
            timestamp = System.currentTimeMillis()
        )
        messageDao.insertMessage(entity)
    }

    // Entity -> Domain 映射
    private fun MessageEntity.toDomain(): Message =
        Message(
            id = id,
            text = text,
            isUser = isUser,
            timestamp = timestamp
        )
}
