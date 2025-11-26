package com.example.chatbox.data.remote

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * 智谱大模型聊天接口
 * 最终 URL = https://open.bigmodel.cn/api/paas/v4/chat/completions
 * （baseUrl 在 AppModule 里配置成 https://open.bigmodel.cn/api/）
 */
interface ChatApiService {

    @Headers("Content-Type: application/json")
    @POST("paas/v4/chat/completions")
    suspend fun sendChat(
        @Body request: ChatRequest   // 这里用的是 ApiModels.kt 里的 ChatRequest
    ): ChatResponse                 // 返回 ApiModels.kt 里的 ChatResponse
}
