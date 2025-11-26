package com.example.chatbox.domain.usecase

import com.example.chatbox.domain.repository.ChatRepository

class SendMessageUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(userId: String, content: String) =
        repository.sendMessage(userId, content)
}
