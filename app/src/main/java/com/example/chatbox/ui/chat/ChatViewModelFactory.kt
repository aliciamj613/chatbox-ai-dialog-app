package com.example.chatbox.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.chatbox.domain.repository.ChatRepository
import com.example.chatbox.domain.usecase.GetHistoryUseCase
import com.example.chatbox.domain.usecase.SendMessageUseCase

class ChatViewModelFactory(
    private val userId: String,
    private val chatRepository: ChatRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            val sendUseCase = SendMessageUseCase(chatRepository)
            val historyUseCase = GetHistoryUseCase(chatRepository)
            return ChatViewModel(userId, sendUseCase, historyUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
