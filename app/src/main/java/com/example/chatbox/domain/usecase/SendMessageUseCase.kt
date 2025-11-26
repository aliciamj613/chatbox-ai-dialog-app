package com.example.chatbox.domain.usecase

import com.example.chatbox.domain.model.Message
import com.example.chatbox.domain.repository.ChatRepository

class SendMessageUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(
        userId: String,
        text: String
    ): List<Message> {
        return chatRepository.sendMessage(userId, text)
    }
}
