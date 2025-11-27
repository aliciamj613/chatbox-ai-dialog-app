// app/src/main/java/com/example/chatbox/data/model/ConversationEntity.kt
package com.example.chatbox.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 会话表：做“会话列表 + 点击进入某个对话”
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // ✅ 绑定所属用户
    val userId: Long,

    // 会话标题（可以自动生成，也可以后面支持改名）
    val title: String,

    // 创建时间
    val createdAt: Long,

    // 最近一次更新（有新消息时更新）
    val updatedAt: Long
)
