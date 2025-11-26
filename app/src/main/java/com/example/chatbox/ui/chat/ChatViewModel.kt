// app/src/main/java/com/example/chatbox/ui/chat/ChatViewModel.kt
package com.example.chatbox.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbox.di.AppModule
import com.example.chatbox.domain.model.Message
import com.example.chatbox.domain.usecase.GetHistoryUseCase
import com.example.chatbox.domain.usecase.SendMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {

    constructor() : this(
        DefaultChatDependencies.getHistoryUseCase,
        DefaultChatDependencies.sendMessageUseCase
    )

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            getHistoryUseCase().collectLatest { list ->
                _uiState.value = _uiState.value.copy(messages = list)
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun onSendClick() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isSending) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSending = true, error = null)
                sendMessageUseCase(text)  // 实际列表会通过 getHistory Flow 刷新
                _uiState.value = _uiState.value.copy(inputText = "")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "发送失败")
            } finally {
                _uiState.value = _uiState.value.copy(isSending = false)
            }
        }
    }
}

/**
 * 默认依赖获取（配合 AppModule）
 */
object DefaultChatDependencies {

    val getHistoryUseCase: GetHistoryUseCase
        get() = AppModule.provideGetHistoryUseCase()

    val sendMessageUseCase: SendMessageUseCase
        get() = AppModule.provideSendMessageUseCase()
}
