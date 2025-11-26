package com.example.chatbox.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chatbox.ui.components.InputBar
import com.example.chatbox.ui.components.MessageBubble
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState

@Composable
fun ChatScreen() {
    val viewModel = remember { ChatViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 消息列表
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp)
        ) {
            items(uiState.messages) { msg ->
                MessageBubble(
                    text = msg.text,
                    isUser = msg.isUser
                )
            }
        }

        // 底部输入栏
        InputBar(
            text = uiState.inputText,
            onTextChange = viewModel::onInputChange,
            onSendClick = viewModel::onSend
        )
    }
}
