package com.example.chatbox.domain.usecase

import com.example.chatbox.domain.repository.ChatRepository

class SendMessageUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(
        text: String,
        conversationId: Long
    ): Result<Unit> {
        return repository.sendOnlineMessage(text, conversationId)
    }
}
