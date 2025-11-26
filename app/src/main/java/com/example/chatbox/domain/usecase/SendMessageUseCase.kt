package com.example.chatbox.domain.usecase

import com.example.chatbox.domain.repository.ChatRepository

class SendMessageUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(text: String, isUser: Boolean) {
        repository.addLocalMessage(text, isUser)
    }
}
