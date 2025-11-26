package com.example.chatbox.ui.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val userId: String = "",
    val isLoggingIn: Boolean = false
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onUserIdChange(text: String) {
        _uiState.update { it.copy(userId = text) }
    }

    fun setLogging(logging: Boolean) {
        _uiState.update { it.copy(isLoggingIn = logging) }
    }
}
