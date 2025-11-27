package com.example.chatbox.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApiService {

    /** 文本对话（你现在已经在用的那个接口） */
    @POST("paas/v4/chat/completions")
    suspend fun sendChat(
        @Body request: ChatRequest
    ): ChatResponse

    /** 文生图 */
    @POST("paas/v4/images/generations")
    suspend fun generateImage(
        @Body request: ImageRequest
    ): ImageResponse

    /** 文生视频 */
    @POST("paas/v4/videos/generations")
    suspend fun generateVideo(
        @Body request: VideoRequest
    ): VideoResponse
}
