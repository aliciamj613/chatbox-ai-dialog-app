import Foundation

final class ZhipuAPIClient {
    private let baseURL = URL(string: "https://open.bigmodel.cn/api/")!
    private let apiKey = ""

    private var jsonEncoder: JSONEncoder {
        JSONEncoder()
    }

    private var jsonDecoder: JSONDecoder {
        JSONDecoder()
    }

    private func makeRequest(path: String, method: String = "POST") -> URLRequest {
        var url = baseURL
        url.appendPathComponent(path)
        var req = URLRequest(url: url)
        req.httpMethod = method
        req.addValue("application/json", forHTTPHeaderField: "Content-Type")
        req.addValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        return req
    }

    // MARK: - 文本对话（glm-4.5）

    func chat(messages: [ChatMessagePayload]) async throws -> String {
        var req = makeRequest(path: "paas/v4/chat/completions")
        let body = ChatRequestPayload(model: "glm-4.5", messages: messages)
        req.httpBody = try jsonEncoder.encode(body)

        let (data, resp) = try await URLSession.shared.data(for: req)
        guard let http = resp as? HTTPURLResponse else {
            throw NSError(domain: "chat_http", code: -1, userInfo: [
                NSLocalizedDescriptionKey: "网络响应异常"
            ])
        }

        guard 200..<300 ~= http.statusCode else {
            throw NSError(domain: "chat_http", code: http.statusCode, userInfo: [
                NSLocalizedDescriptionKey: "对话请求失败（HTTP \(http.statusCode)）"
            ])
        }

        let decoded = try jsonDecoder.decode(ChatResponsePayload.self, from: data)
        return decoded.choices.first?.message.content ?? ""
    }

    // MARK: - 文生图（cogview-4-250304）

    func generateImage(prompt: String) async throws -> String {
        var req = makeRequest(path: "paas/v4/images/generations")

        // ✅ 模型名 & 字段完全对齐 Android 的 ImageRequest
        let body = ImageRequestPayload(
            model: "cogview-4-250304",
            prompt: prompt,
            size: "1024x1024",
            quality: nil,
            user_id: nil
        )
        req.httpBody = try jsonEncoder.encode(body)

        let (data, resp) = try await URLSession.shared.data(for: req)
        guard let http = resp as? HTTPURLResponse else {
            throw NSError(domain: "img_http", code: -1, userInfo: [
                NSLocalizedDescriptionKey: "图片生成网络响应异常"
            ])
        }

        // 429：限流 / 额度 / QPS 问题（不算扣次）
        if http.statusCode == 429 {
            // 打印一下服务端返回的 body，方便你在 Xcode 控制台里看具体提示
            if let bodyStr = String(data: data, encoding: .utf8) {
                print("Image 429 body:", bodyStr)
            }
            throw NSError(domain: "img_http", code: 429, userInfo: [
                NSLocalizedDescriptionKey: "图片生成请求过于频繁或额度受限（429），请稍后再试，或在控制台查看详细说明。"
            ])
        }

        guard 200..<300 ~= http.statusCode else {
            if let bodyStr = String(data: data, encoding: .utf8) {
                print("Image HTTP \(http.statusCode) body:", bodyStr)
            }
            throw NSError(domain: "img_http", code: http.statusCode, userInfo: [
                NSLocalizedDescriptionKey: "图片生成失败（HTTP \(http.statusCode)）"
            ])
        }

        let decoded = try jsonDecoder.decode(ImageResponsePayload.self, from: data)
        guard let url = decoded.data.first?.url else {
            throw NSError(domain: "img_http", code: -2, userInfo: [
                NSLocalizedDescriptionKey: "图片生成成功但未返回 URL"
            ])
        }
        return url
    }

    // MARK: - 文生视频：创建异步任务（cogvideox-3）

    func createVideoTask(prompt: String) async throws -> String {
        // ✅ 路径改成和 Android 一样：videos/generations
        var req = makeRequest(path: "paas/v4/videos/generations")

        let body = VideoRequestPayload(
            model: "cogvideox-3",
            prompt: prompt,
            size: nil,
            duration: nil,
            user_id: nil,
            with_audio: true
        )
        req.httpBody = try jsonEncoder.encode(body)

        let (data, resp) = try await URLSession.shared.data(for: req)
        guard let http = resp as? HTTPURLResponse else {
            throw NSError(domain: "video_http", code: -1, userInfo: [
                NSLocalizedDescriptionKey: "视频生成网络响应异常"
            ])
        }

        if http.statusCode == 429 {
            if let bodyStr = String(data: data, encoding: .utf8) {
                print("Video 429 body:", bodyStr)
            }
            throw NSError(domain: "video_http", code: 429, userInfo: [
                NSLocalizedDescriptionKey: "视频生成请求过于频繁或额度受限（429），请稍后再试。"
            ])
        }

        guard 200..<300 ~= http.statusCode else {
            if let bodyStr = String(data: data, encoding: .utf8) {
                print("Video HTTP \(http.statusCode) body:", bodyStr)
            }
            throw NSError(domain: "video_http", code: http.statusCode, userInfo: [
                NSLocalizedDescriptionKey: "视频生成失败（HTTP \(http.statusCode)）"
            ])
        }

        let decoded = try jsonDecoder.decode(VideoTaskResponse.self, from: data)
        return decoded.id
    }

    // MARK: - 文生视频：轮询异步结果（async-result）

    func pollVideoResult(taskId: String) async throws -> (url: String, cover: String?) {
        for _ in 0..<20 {
            try await Task.sleep(nanoseconds: 3_000_000_000) // 3s

            var req = makeRequest(path: "paas/v4/async-result/\(taskId)", method: "GET")
            let (data, resp) = try await URLSession.shared.data(for: req)
            guard let http = resp as? HTTPURLResponse else {
                throw NSError(domain: "video_poll_http", code: -1, userInfo: [
                    NSLocalizedDescriptionKey: "视频轮询网络响应异常"
                ])
            }

            if http.statusCode == 429 {
                if let bodyStr = String(data: data, encoding: .utf8) {
                    print("Video poll 429 body:", bodyStr)
                }
                throw NSError(domain: "video_poll_http", code: 429, userInfo: [
                    NSLocalizedDescriptionKey: "轮询视频结果过于频繁（429），请稍后再试。"
                ])
            }

            guard 200..<300 ~= http.statusCode else {
                if let bodyStr = String(data: data, encoding: .utf8) {
                    print("Video poll HTTP \(http.statusCode) body:", bodyStr)
                }
                throw NSError(domain: "video_poll_http", code: http.statusCode, userInfo: [
                    NSLocalizedDescriptionKey: "轮询视频结果失败（HTTP \(http.statusCode)）"
                ])
            }

            let decoded = try jsonDecoder.decode(VideoResultResponse.self, from: data)
            if decoded.task_status == "SUCCESS",
               let item = decoded.video_result?.first,
               let url = item.url, !url.isEmpty {
                return (url, item.cover_image_url)
            }
        }

        throw NSError(domain: "video_poll_http", code: -2, userInfo: [
            NSLocalizedDescriptionKey: "视频生成超时，请稍后从后台重新查看。"
        ])
    }
}
