package com.example.chatbox.data.remote

// ===================== 文本对话 =====================

/**
 * 聊天消息，用于请求体：role: user / assistant
 */
data class ChatMessage(
    val role: String,
    val content: String
)

/**
 * 聊天请求：使用 glm-4.5（你有大额免费额度）
 */
data class ChatRequest(
    val model: String = "glm-4.5",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.8,
    val top_p: Double = 0.7
)

/**
 * 返回的 message（assistant）
 */
data class ChatChoiceMessage(
    val role: String,
    val content: String
)

/**
 * choices 列表里的每一项
 */
data class Choice(
    val index: Int,
    val message: ChatChoiceMessage
)

/**
 * token 使用量（不一定每次都回）
 */
data class ChatUsage(
    val prompt_tokens: Int?,
    val completion_tokens: Int?,
    val total_tokens: Int?
)

/**
 * 聊天整体响应
 */
data class ChatResponse(
    val id: String?,
    val model: String?,
    val choices: List<Choice>,
    val created: Long?,
    val usage: ChatUsage?
)

// ===================== 文生图（CogView-4） =====================

/**
 * 文生图请求体
 * 文档：POST /api/paas/v4/images/generations
 * 模型：cogview-4-250304（你有 20 次免费包）
 */
data class ImageRequest(
    val model: String = "cogview-4-250304",
    val prompt: String,
    val size: String = "1024x1024",
    val quality: String? = null,    // "standard" / "hd"，可选
    val user_id: String? = null
)

/**
 * 文生图返回的单条数据
 */
data class ImageData(
    val url: String
)

/**
 * 文生图响应
 */
data class ImageResponse(
    val created: Long?,
    val data: List<ImageData>
    // content_filter 等字段这版用不到，就不写了
)

// ===================== 文生视频（CogVideoX-3 异步） =====================

/**
 * 文生视频请求体（创建任务）
 * 文档：POST /api/paas/v4/videos/generations
 * 模型：cogvideox-3（你有 20 次免费包）
 */
data class VideoRequest(
    val model: String = "cogvideox-3",
    val prompt: String,
    val size: String? = null,          // 可留空，走默认
    val duration: Int? = null,         // 秒数，可选
    val user_id: String? = null,
    val with_audio: Boolean? = null,   // 是否带声音
    val resolution: String? = null,    // 官方有些版本支持分辨率，这里预留
    val fps: Int? = null               // 帧率，可选
)

/**
 * 创建视频任务的响应（异步任务）
 *
 * 典型结构：
 * {
 *   "id": "task_xxx",
 *   "task_status": "PROCESSING",
 *   "model": "cogvideox-3"
 * }
 */
data class VideoTaskResponse(
    val id: String,
    val model: String?,
    val task_status: String?,
    val error: String? = null
)

/**
 * 单个视频结果（一般就一个）
 */
data class VideoResult(
    val url: String?,              // 视频下载地址 / 播放地址
    val cover_image_url: String?   // 封面图
)

/**
 * 查询异步结果的响应
 *
 * {
 *   "model": "cogvideox-3",
 *   "task_status": "SUCCESS",
 *   "video_result": [
 *     {
 *       "url": "...",
 *       "cover_image_url": "..."
 *     }
 *   ]
 * }
 */
data class VideoResultResponse(
    val model: String?,
    val video_result: List<VideoResult>?,
    val task_status: String?,
    val request_id: String? = null
)
