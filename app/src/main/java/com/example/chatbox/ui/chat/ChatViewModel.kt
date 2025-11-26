package com.example.chatbox.ui.chat

import androidx.lifecycle.ViewModel
import com.example.chatbox.domain.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false
)

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun onSend() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        val current = _uiState.value
        val newId = (current.messages.maxOfOrNull { it.id } ?: 0L) + 1L

        val userMsg = Message(
            id = newId,
            text = text,
            isUser = true
        )

        val botMsg = Message(
            id = newId + 1,
            text = "Echo: $text",
            isUser = false
        )

        _uiState.value = current.copy(
            messages = current.messages + userMsg + botMsg,
            inputText = ""
        )
    }
}
