package com.example.chatbox.domain.usecase

import com.example.chatbox.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import com.example.chatbox.domain.model.Message

class GetHistoryUseCase(
    private val repository: ChatRepository
) {
    operator fun invoke(userId: String): Flow<List<Message>> =
        repository.observeMessages(userId)
}
