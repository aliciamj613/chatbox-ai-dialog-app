package com.example.chatbox.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
    viewModel: ChatViewModel = ChatViewModel(conversationId = conversationId)
) {
    val uiState by viewModel.uiState.collectAsState()

    // 🌙 夜间模式本地开关（只影响 ChatScreen）
    var isDark by rememberSaveable { mutableStateOf(false) }

    ChatboxTheme(darkTheme = isDark) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ChatBox") },
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
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(uiState.messages) { message ->
                        MessageBubble(message = message)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }

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
    val isUser = message.isUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = if (isUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.medium,
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Text(
                text = message.text,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .widthIn(max = 260.dp),
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
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            placeholder = { Text("输入消息…") }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onSendClick,
            enabled = text.isNotBlank() && !isSending
        ) {
            Text(if (isSending) "发送中…" else "发送")
        }
    }
}
