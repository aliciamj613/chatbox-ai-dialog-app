package com.example.chatbox.ui.conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.chatbox.data.local.db.AppDatabase
import com.example.chatbox.data.local.prefs.UserPreferences
import com.example.chatbox.data.model.ConversationEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    onOpenConversation: (Long) -> Unit,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val conversationDao = remember { db.conversationDao() }
    val messageDao = remember { db.messageDao() }
    val prefs = remember { UserPreferences(context) }

    val scope = rememberCoroutineScope()

    val currentUserId = remember { prefs.getLastUserId() }

    // 如果拿不到当前用户，提示重新登录
    if (currentUserId == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("当前用户信息丢失，请重新登录")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onLogout) {
                    Text("返回登录页")
                }
            }
        }
        return
    }

    val conversations by conversationDao
        .observeConversations(currentUserId)
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的对话") },
                actions = {
                    TextButton(onClick = onToggleTheme) {
                        Text(if (isDarkTheme) "☀️" else "🌙")
                    }
                    TextButton(onClick = onLogout) {
                        Text("退出登录")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        val now = System.currentTimeMillis()
                        val id = conversationDao.insertConversation(
                            ConversationEntity(
                                userId = currentUserId,
                                title = "新的对话",
                                createdAt = now,
                                updatedAt = now
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
                Text("还没有任何对话，点击右下角 + 开始新对话吧～")
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
                                conversationDao.deleteConversationById(conversation.id)
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
                text = if (conversation.title.isBlank()) "未命名对话" else conversation.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "最近更新：${formatUpdatedAt(conversation.updatedAt)}",
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

private fun formatUpdatedAt(timestamp: Long): String {
    if (timestamp <= 0L) return "-"
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return formatter.format(date)
}
