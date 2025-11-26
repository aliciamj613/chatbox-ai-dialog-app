package com.example.chatbox.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbox.domain.model.Message
import com.example.chatbox.domain.repository.ChatRepository
import com.example.chatbox.domain.usecase.GetHistoryUseCase
import com.example.chatbox.domain.usecase.SendMessageUseCase
import com.example.chatbox.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val userId: String,
    private val sendMessage: SendMessageUseCase,
    observeHistory: GetHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    init {
        viewModelScope.launch {
            observeHistory(userId).collect { list ->
                _uiState.update { it.copy(messages = list) }
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(input = text, error = null) }
    }

    fun onSend() {
        val content = _uiState.value.input.trim()
        if (content.isEmpty() || _uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            when (val result = sendMessage(userId, content)) {
                is Result.Success -> _uiState.update { it.copy(input = "", isSending = false) }
                is Result.Error ->
                    _uiState.update { it.copy(isSending = false, error = result.throwable.message) }
            }
        }
    }
}
