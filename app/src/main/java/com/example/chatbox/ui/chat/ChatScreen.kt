package com.example.chatbox.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chatbox.domain.model.Message
import com.example.chatbox.ui.theme.ChatboxTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: Long,
    onBackToConversations: () -> Unit,
    viewModel: ChatViewModel = ChatViewModel(conversationId = conversationId)
) {
    val uiState by viewModel.uiState.collectAsState()
    var isDark by rememberSaveable { mutableStateOf(false) }

    ChatboxTheme(darkTheme = isDark) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "对话 #$conversationId") },
                    navigationIcon = {
                        TextButton(onClick = onBackToConversations) {
                            Text("会话")
                        }
                    },
                    actions = {
                        TextButton(onClick = { isDark = !isDark }) {
                            Text(
                                text = if (isDark) "☀️" else "🌙",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.messages.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "开始和 AI 聊天吧～",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        items(uiState.messages) { message ->
                            MessageBubble(message = message)
                        }
                    }
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }

                Divider()

                ChatInputBar(
                    text = uiState.inputText,
                    onTextChange = viewModel::onInputChange,
                    onSendClick = viewModel::onSendClick,
                    isSending = uiState.isSending
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("请输入消息") }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onSendClick,
            enabled = !isSending && text.isNotBlank()
        ) {
            Text(
                text = if (isSending) "发送中..." else "发送",
                textAlign = TextAlign.Center
            )
        }
    }
}
