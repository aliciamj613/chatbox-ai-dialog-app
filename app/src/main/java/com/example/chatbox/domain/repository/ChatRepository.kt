package com.example.chatbox.domain.repository

import com.example.chatbox.domain.model.Message

interface ChatRepository {
    suspend fun sendMessage(
        userId: String,
        text: String
    ): List<Message>

    suspend fun getHistory(
        userId: String
    ): List<Message>
}
