package com.example.chatbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// 用一个简单的数据类表示一条消息
data class Message(
    val sender: Sender,
    val text: String
)

enum class Sender { User, Bot }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    // 输入框的内容（先只是 UI，用不到也没关系）
    var input by remember { mutableStateOf("") }

    // 临时写死的几条对话，用来演示布局
    val messages = listOf(
        Message(Sender.User, "Hi, I'm the user."),
        Message(Sender.Bot, "Hello, I'm your AI assistant."),
        Message(Sender.User, "This is a static chat UI for week 1.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chatbox") }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Type a message") },
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { /* 现在先什么都不做，只是 UI */ }
                ) {
                    Text("Send")
                }
            }
        }
    ) { innerPadding ->
        // 中间的消息列表
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                if (msg.sender == Sender.User) {
                    UserBubble(msg.text)
                } else {
                    BotBubble(msg.text)
                }
            }
        }
    }
}

@Composable
fun UserBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = text,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun BotBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                modifier = Modifier.padding(10.dp),
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
