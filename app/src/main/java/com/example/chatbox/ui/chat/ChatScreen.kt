package com.example.chatbox.ui.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
                            Text(if (isDark) "☀️" else "🌙")
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

                // 小提示：当前对话是“有记忆”的
                Text(
                    text = "已开启上下文记忆：AI 会参考本会话所有历史消息。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )

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
                                Text("开始和 AI 聊天吧～")
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                ChatInputBar(
                    text = uiState.inputText,
                    onTextChange = viewModel::onInputChange,
                    onSendClick = viewModel::onSendClick,
                    onGenerateImageClick = viewModel::onGenerateImageClick,
                    onGenerateVideoClick = viewModel::onGenerateVideoClick,
                    isSending = uiState.isSending
                )
            }
        }
    }
}

// =============== 消息气泡：根据内容判断文本 / 图片 / 视频 ===============

@Composable
private fun MessageBubble(message: Message) {
    val bgColor = if (message.isUser)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant

    val contentColor = if (message.isUser)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val text = message.text
    val imagePrefix = "图片已生成："
    val videoPrefix = "视频已生成："

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
        Surface(
            color = bgColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            when {
                text.startsWith(imagePrefix) -> {
                    val url = text.removePrefix(imagePrefix).trim()
                    ImageMessageContent(url = url)
                }

                text.startsWith(videoPrefix) -> {
                    // 只取前一行作为主视频链接
                    val raw = text.removePrefix(videoPrefix).trim()
                    val url = raw.lines().firstOrNull()?.trim().orEmpty()
                    VideoMessageContent(url = url)
                }

                else -> {
                    Text(
                        text = text,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// =============== 图片消息内容 ===============

@Composable
private fun ImageMessageContent(url: String) {
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = "图片已生成：",
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(Modifier.height(4.dp))
        AsyncImage(
            model = url,
            contentDescription = "生成图片",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 260.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

// =============== 视频消息内容（点击跳转外部播放器） ===============

@Composable
private fun VideoMessageContent(url: String) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .clickable(enabled = url.isNotBlank()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                } catch (_: Exception) {
                }
            }
            .padding(10.dp)
    ) {
        Text(
            text = "视频已生成，点击播放",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = url,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =============== 底部输入栏（不变） ===============

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onGenerateImageClick: () -> Unit,
    onGenerateVideoClick: () -> Unit,
    isSending: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("请输入内容…") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSendClick,
                enabled = !isSending && text.isNotBlank()
            ) {
                Text(if (isSending) "发送中…" else "发送")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = onGenerateImageClick,
                enabled = !isSending && text.isNotBlank()
            ) {
                Text("✨ 文生图")
            }

            TextButton(
                onClick = onGenerateVideoClick,
                enabled = !isSending && text.isNotBlank()
            ) {
                Text("🎬 文生视频")
            }
        }
    }
}
