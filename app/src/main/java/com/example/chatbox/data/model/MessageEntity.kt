package com.example.chatbox.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 聊天消息的数据库实体
 * - userId: 属于哪个登录用户
 * - role: "user" / "assistant"
 * - content: 消息内容
 * - timestamp: 发送时间
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
