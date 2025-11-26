// app/src/main/java/com/example/chatbox/domain/usecase/GetHistoryUseCase.kt
package com.example.chatbox.domain.usecase

import com.example.chatbox.domain.model.Message
import com.example.chatbox.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class GetHistoryUseCase(
    private val repository: ChatRepository
) {
    operator fun invoke(): Flow<List<Message>> = repository.getHistory()
}
