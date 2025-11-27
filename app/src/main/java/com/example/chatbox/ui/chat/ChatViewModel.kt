package com.example.chatbox.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbox.di.AppModule
import com.example.chatbox.domain.model.Message
import com.example.chatbox.domain.repository.ChatRepository
import com.example.chatbox.domain.usecase.GetHistoryUseCase
import com.example.chatbox.domain.usecase.SendMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
    private val sendMessageUseCase: SendMessageUseCase = AppModule.provideSendMessageUseCase(),
    private val repository: ChatRepository = AppModule.chatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    init {
        // 订阅当前会话的消息历史（有“记忆”的所有内容）
        viewModelScope.launch {
            getHistoryUseCase(conversationId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text, error = null) }
    }

    /** 纯文本对话 */
    fun onSendClick() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            val result = sendMessageUseCase(text, conversationId)
            _uiState.update { state ->
                state.copy(
                    isSending = false,
                    inputText = if (result.isSuccess) "" else state.inputText,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    /** 文生图 */
    fun onGenerateImageClick() {
        val prompt = _uiState.value.inputText.trim()
        if (prompt.isBlank() || _uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            val result = repository.generateImageFromText(prompt, conversationId)
            _uiState.update { state ->
                state.copy(
                    isSending = false,
                    inputText = if (result.isSuccess) "" else state.inputText,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    /** 文生视频 */
    fun onGenerateVideoClick() {
        val prompt = _uiState.value.inputText.trim()
        if (prompt.isBlank() || _uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            val result = repository.generateVideoFromText(prompt, conversationId)
            _uiState.update { state ->
                state.copy(
                    isSending = false,
                    inputText = if (result.isSuccess) "" else state.inputText,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }
}
