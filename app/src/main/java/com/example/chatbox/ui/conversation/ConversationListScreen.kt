package com.example.chatbox.ui.conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.chatbox.data.local.db.AppDatabase
import com.example.chatbox.data.model.ConversationEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    onOpenConversation: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val conversationDao = db.conversationDao()
    val messageDao = db.messageDao()

    val conversationsFlow = conversationDao.observeConversations()
    val conversations by conversationsFlow.collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的对话") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text(text = "退出登录")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        val id = conversationDao.insertConversation(
                            ConversationEntity(
                                title = "新的对话",
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                        onOpenConversation(id)
                    }
                }
            ) {
                Text("+")
            }
        }
    ) { innerPadding ->
        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "还没有任何对话，点击右下角 + 开始新对话吧～",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversations) { conversation ->
                    ConversationRow(
                        conversation = conversation,
                        onClick = { onOpenConversation(conversation.id) },
                        onDelete = {
                            scope.launch {
                                // 删除会话
                                conversationDao.deleteConversationById(conversation.id)
                                // 同时清空该会话下的消息
                                messageDao.clearConversation(conversation.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = conversation.title.ifBlank { "未命名对话" },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "最近更新：${conversation.updatedAt}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(onClick = onDelete) {
            Text(
                text = "删除",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
