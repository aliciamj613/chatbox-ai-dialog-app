// app/src/main/java/com/example/chatbox/domain/model/Message.kt
package com.example.chatbox.domain.model

data class Message(
    val id: Long = 0L,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
