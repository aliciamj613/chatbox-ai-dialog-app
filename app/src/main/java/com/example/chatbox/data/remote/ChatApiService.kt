package com.example.chatbox.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 对接智谱 AI 的 Retrofit 接口
 * baseUrl 在 AppModule 里应该是：
 *   https://open.bigmodel.cn/api/
 */
interface ChatApiService {

    // ============ 文本对话 ============

    @POST("paas/v4/chat/completions")
    suspend fun sendChat(
        @Body request: ChatRequest
    ): ChatResponse

    // ============ 文生图（同步） ============

    @POST("paas/v4/images/generations")
    suspend fun generateImage(
        @Body request: ImageRequest
    ): ImageResponse

    // ============ 文生视频：创建异步任务 ============

    @POST("paas/v4/videos/generations")
    suspend fun generateVideoTask(
        @Body request: VideoRequest
    ): VideoTaskResponse

    // ============ 文生视频：轮询异步结果 ============
    //
    // 智谱开放平台上，视频属于异步任务，需要通过 async-result 接口轮询。
    // open.bigmodel.cn 上视频异步结果对应：
    //   GET /api/paas/v4/videos/{id}
    // 或 /api/paas/v4/async-result/{id}（不同文档版本写法略有出入）
    // 这里用 async-result 这一套，你的 baseUrl 里已经带了 /api/ 前缀。
    //
    @GET("paas/v4/async-result/{id}")
    suspend fun getAsyncResult(
        @Path("id") taskId: String
    ): VideoResultResponse
}
