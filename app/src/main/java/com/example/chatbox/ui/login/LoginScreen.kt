package com.example.chatbox.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Welcome to ChatBox")

        OutlinedTextField(
            value = state.userId,
            onValueChange = viewModel::onUserIdChange,
            label = { Text("User ID") },
            modifier = Modifier.padding(top = 16.dp)
        )

        Button(
            onClick = {
                if (state.userId.isNotBlank()) {
                    onLoginSuccess(state.userId.trim())
                }
            },
            enabled = state.userId.isNotBlank(),
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Login")
        }
    }
}
