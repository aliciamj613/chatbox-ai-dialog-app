package com.example.chatbox.ui.conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.chatbox.data.local.db.AppDatabase
import com.example.chatbox.data.model.ConversationEntity
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    onOpenConversation: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val dao = db.conversationDao()

    // 监听会话列表
    val conversationsFlow = dao.observeConversations()
    val conversations by conversationsFlow.collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的对话") },
                actions = {
                    // 用文字按钮实现“退出”
                    IconButton(onClick = onLogout) {
                        Text("退出")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        val now = System.currentTimeMillis()
                        val newId = dao.insertConversation(
                            ConversationEntity(
                                title = "新的对话",
                                createdAt = now,
                                updatedAt = now
                            )
                        )
                        // 创建完会话后，直接进入该会话
                        onOpenConversation(newId)
                    }
                }
            ) {
                // 用文字代替图标
                Text("+")
            }
        }
    ) { innerPadding ->
        if (conversations.isEmpty()) {
            // 空列表提示
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "还没有任何对话，点击右下角 + 新建一个吧～",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(conversations) { conv ->
                    ConversationItem(
                        conversation = conv,
                        onClick = { onOpenConversation(conv.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: ConversationEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = conversation.title,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "最近更新：${conversation.updatedAt}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
