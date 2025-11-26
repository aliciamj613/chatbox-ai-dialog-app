package com.example.chatbox.domain.usecase

import com.example.chatbox.domain.model.Message
import com.example.chatbox.domain.repository.ChatRepository

class GetHistoryUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(
        userId: String
    ): List<Message> {
        return chatRepository.getHistory(userId)
    }
}
