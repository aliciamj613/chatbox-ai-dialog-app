package com.example.chatbox.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chatbox.ui.components.InputBar
import com.example.chatbox.ui.components.MessageBubble
import com.example.chatbox.ui.components.TopBar

@Composable
fun ChatScreen(
    viewModel: ChatViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = { TopBar(title = "ChatBox", onClear = { /* TODO: 清空逻辑可后加 */ }) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                items(state.messages) { msg ->
                    MessageBubble(
                        text = msg.content,
                        isUser = msg.role == "user"
                    )
                }
            }

            InputBar(
                text = state.input,
                onTextChange = viewModel::onInputChange,
                onSendClick = viewModel::onSend,
                onImageClick = { /* TODO 多模态入口 */ },
                enabled = !state.isSending
            )
        }
    }
}
