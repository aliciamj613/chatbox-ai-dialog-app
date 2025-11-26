package com.example.chatbox.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chatbox.domain.model.Message

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userId: String,
    val role: String,
    val content: String,
    val timestamp: Long
)

fun MessageEntity.toDomain(): Message =
    Message(id = id, userId = userId, role = role, content = content, timestamp = timestamp)

fun Message.toEntity(): MessageEntity =
    MessageEntity(id = id, userId = userId, role = role, content = content, timestamp = timestamp)
