package com.example.chatbox.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// 聊天消息表，只保留最核心的四个字段
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long
)
