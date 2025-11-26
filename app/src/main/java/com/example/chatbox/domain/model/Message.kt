package com.example.chatbox.domain.model

data class Message(
    val id: Long = 0L,
    val userId: String,
    val role: String,      // "user" / "assistant"
    val content: String,
    val timestamp: Long
)
