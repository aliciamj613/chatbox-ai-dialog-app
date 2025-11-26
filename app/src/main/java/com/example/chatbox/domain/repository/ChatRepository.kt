package com.example.chatbox.domain.repository

import com.example.chatbox.domain.model.Message
import com.example.chatbox.util.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(userId: String): Flow<List<Message>>
    suspend fun clearMessages(userId: String)
    suspend fun sendMessage(userId: String, content: String): Result<Unit>
}
