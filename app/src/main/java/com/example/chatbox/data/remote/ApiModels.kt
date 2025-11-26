package com.example.chatbox.data.remote

data class ChatRequest(
    val userId: String,
    val message: String
)

data class ChatResponse(
    val reply: String
)
