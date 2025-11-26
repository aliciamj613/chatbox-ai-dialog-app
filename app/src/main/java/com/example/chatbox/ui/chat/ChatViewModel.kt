package com.example.chatbox.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbox.domain.model.Message
import com.example.chatbox.domain.usecase.GetHistoryUseCase
import com.example.chatbox.domain.usecase.SendMessageUseCase
import com.example.chatbox.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val conversationId: Long,
    private val getHistoryUseCase: GetHistoryUseCase = AppModule.provideGetHistoryUseCase(),
    private val sendMessageUseCase: SendMessageUseCase = AppModule.provideSendMessageUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    init {
        // 这里先把 conversationId 传给用例（下一步我们会改 UseCase & Repository 让它们按会话取数据）
        viewModelScope.launch {
            try {
                getHistoryUseCase(conversationId).collectLatest { msgs ->
                    _uiState.value = _uiState.value.copy(
                        messages = msgs,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "加载历史消息失败：${e.message ?: "未知错误"}"
                )
            }
        }
    }

    fun onInputChange(newText: String) {
        _uiState.value = _uiState.value.copy(inputText = newText)
    }

    fun onSendClick() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        _uiState.value = _uiState.value.copy(
            inputText = "",
            isSending = true,
            error = null
        )

        viewModelScope.launch {
            val result = sendMessageUseCase(text, conversationId)
            _uiState.value = _uiState.value.copy(
                isSending = false,
                error = result.exceptionOrNull()?.message
            )
        }
    }
}
