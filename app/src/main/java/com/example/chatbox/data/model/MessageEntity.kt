// app/src/main/java/com/example/chatbox/data/model/MessageEntity.kt
package com.example.chatbox.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long
)
