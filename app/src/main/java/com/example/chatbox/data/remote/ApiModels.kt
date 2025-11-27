package com.example.chatbox.data.remote

// ====== 文本对话 ======

data class ChatMessage(
    val role: String,   // "user" / "assistant" / "system"
    val content: String
)

data class ChatRequest(
    val model: String = "glm-4",
    val messages: List<ChatMessage>
)

data class Choice(
    val index: Int,
    val message: ChatMessage
)

data class ChatResponse(
    val choices: List<Choice>
)

// ====== 文生图（示例结构，路径 & model 你后面可以根据智谱文档微调） ======

data class ImageRequest(
    val model: String = "cogview-4",  // 智谱文生图模型名，可以按文档改
    val prompt: String
)

data class ImageData(
    val url: String
)

data class ImageResponse(
    val data: List<ImageData>
)

// ====== 文生视频（示例结构） ======

data class VideoRequest(
    val model: String = "video-model", // 占位模型名，按实际文档替换
    val prompt: String
)

data class VideoData(
    val url: String
)

data class VideoResponse(
    val data: List<VideoData>
)
