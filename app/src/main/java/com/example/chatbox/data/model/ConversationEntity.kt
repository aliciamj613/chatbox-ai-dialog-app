// app/src/main/java/com/example/chatbox/data/model/ConversationEntity.kt
package com.example.chatbox.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 会话表：后面用来做“会话列表 + 点击进入某个对话”
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // 会话标题（可以是用户自定义，也可以先用“对话 #1”这种）
    val title: String,

    // 创建时间
    val createdAt: Long,

    // 最近一次更新（有新消息时更新）
    val updatedAt: Long
)
