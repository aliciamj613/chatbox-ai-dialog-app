import Foundation

// ======================================================
//  Zhipu API 模型定义（对齐 Android 版 ApiModels.kt）
// ======================================================

// MARK: - 聊天模型 (Chat Completions)

struct ChatMessagePayload: Codable {
    let role: String
    let content: String
}

struct ChatRequestPayload: Codable {
    let model: String
    let messages: [ChatMessagePayload]
}

struct ChatChoice: Codable {
    struct ChoiceMessage: Codable {
        let role: String
        let content: String
    }
    let message: ChoiceMessage
}

struct ChatResponsePayload: Codable {
    let choices: [ChatChoice]
}

// MARK: - 文生图（对应 ImageRequest / ImageResponse）

/// 文生图请求体，对齐 Android：
/// model: cogview-4-250304
/// size: "1024x1024"
struct ImageRequestPayload: Codable {
    let model: String              // e.g. "cogview-4-250304"
    let prompt: String
    let size: String               // e.g. "1024x1024"
    let quality: String?           // "standard" / "hd"
    let user_id: String?           // 可选
}

struct ImageDataItem: Codable {
    let url: String
}

struct ImageResponsePayload: Codable {
    let data: [ImageDataItem]
}

// MARK: - 文生视频（对应 VideoRequest / VideoTaskResponse / VideoResultResponse）

/// 文生视频请求体（创建异步任务），对齐 Android：
/// POST /api/paas/v4/videos/generations
/// model: "cogvideox-3"
struct VideoRequestPayload: Codable {
    let model: String              // "cogvideox-3"
    let prompt: String
    let size: String?              // 分辨率，可选
    let duration: Int?             // 秒数，可选
    let user_id: String?           // 可选
    let with_audio: Bool?          // 是否带音频
}

struct VideoTaskResponse: Codable {
    let id: String
    let task_status: String        // "QUEUED" / "PROCESSING" / "SUCCESS" / "FAILED"
}

struct VideoResultItem: Codable {
    let url: String?
    let cover_image_url: String?
}

struct VideoResultResponse: Codable {
    let model: String?
    let video_result: [VideoResultItem]?
    let task_status: String?
    let request_id: String?        // 可选
}
