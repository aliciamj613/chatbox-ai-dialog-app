package com.example.chatbox.data.remote

/**
 * 智谱 API 请求 / 响应数据模型
 */

data class ChatRequest(
    val model: String = "glm-4-air",        // 或者你有权限的模型
    val messages: List<ChatMessage>
)

data class ChatMessage(
    val role: String,                      // "user" / "assistant" / "system"
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ChatMessage
)
