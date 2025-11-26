// app/src/main/java/com/example/chatbox/ui/chat/ChatScreen.kt
package com.example.chatbox.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = ChatViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Chat") }
            )
        },
        bottomBar = {
            InputBar(
                text = uiState.inputText,
                isSending = uiState.isSending,
                onTextChange = viewModel::onInputChange,
                onSendClick = viewModel::onSendClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            MessageList(
                messages = uiState.messages,
                modifier = Modifier.fillMaxSize()
            )

            uiState.error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageList(
    messages: List<com.example.chatbox.domain.model.Message>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(messages) { msg ->
            val align = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
            val containerColor =
                if (msg.isUser) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.secondaryContainer

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
            ) {
                Surface(
                    color = containerColor,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = msg.text,
                        modifier = Modifier.padding(8.dp),
                        textAlign = if (msg.isUser) TextAlign.End else TextAlign.Start
                    )
                }
            }
        }
    }
}

@Composable
private fun InputBar(
    text: String,
    isSending: Boolean,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit
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
            placeholder = { Text("输入消息...") },
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onSendClick,
            enabled = !isSending && text.isNotBlank()
        ) {
            Text(if (isSending) "发送中..." else "发送")
        }
    }
}
