package com.example.chatbox.data.remote

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatApiService {

    @Headers("Content-Type: application/json")
    @POST("v4/chat/completions")
    suspend fun sendChat(
        @Body request: ChatRequest
    ): ChatResponse
}
