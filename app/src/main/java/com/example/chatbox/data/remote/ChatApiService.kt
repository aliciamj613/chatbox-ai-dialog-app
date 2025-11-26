package com.example.chatbox.data.remote

interface ChatApiService {
    suspend fun sendChat(request: ChatRequest): ChatResponse
}
