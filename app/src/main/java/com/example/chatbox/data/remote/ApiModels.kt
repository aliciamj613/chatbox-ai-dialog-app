package com.example.chatbox.data.remote

import com.squareup.moshi.Json

data class ChatRequest(
    val model: String = "glm-4-air",
    val messages: List<ChatMessage>
)

data class ChatMessage(
    val role: String,   // "user" or "assistant"
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ChatMessage
)
