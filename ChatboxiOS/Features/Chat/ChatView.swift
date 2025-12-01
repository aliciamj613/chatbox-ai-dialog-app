import SwiftUI
import AVKit

struct ChatView: View {
    @EnvironmentObject var appState: AppState

    let conversationId: Int64
    let store: ChatStore
    let repository: ChatRepository

    @StateObject private var vm: ChatViewModel

    init(conversationId: Int64, store: ChatStore, repository: ChatRepository) {
        self.conversationId = conversationId
        self.store = store
        self.repository = repository
        _vm = StateObject(
            wrappedValue: ChatViewModel(
                conversationId: conversationId,
                store: store,
                repository: repository
            )
        )
    }

    var body: some View {
        ZStack {
            // ✅ 背景跟随黑白模式
            (appState.isDarkMode ? Color.black : Color.white)
                .ignoresSafeArea()

            VStack {
                Text("已开启上下文记忆：AI 会参考本会话所有历史消息。")
                    .font(.footnote)
                    .foregroundColor(appState.isDarkMode ? .gray : .secondary)
                    .padding(.horizontal)
                    .padding(.top, 8)

                ScrollViewReader { proxy in
                    ScrollView {
                        VStack(spacing: 8) {
                            if vm.messages.isEmpty {
                                Text("开始和 AI 聊天吧～")
                                    .foregroundColor(appState.isDarkMode ? .gray : .secondary)
                                    .padding(.top, 32)
                            } else {
                                ForEach(vm.messages) { msg in
                                    MessageBubble(message: msg)
                                }
                            }
                        }
                        .padding(.horizontal, 12)
                        .padding(.top, 8)
                    }
                    .onChange(of: vm.messages.count) { _ in
                        if let last = vm.messages.last {
                            withAnimation {
                                proxy.scrollTo(last.id, anchor: .bottom)
                            }
                        }
                    }
                }

                if let err = vm.error {
                    Text(err)
                        .foregroundColor(.red)
                        .font(.footnote)
                        .padding(.horizontal)
                }

                Divider()
                    .background(appState.isDarkMode ? Color.gray : Color.secondary)
                    .padding(.vertical, 8)

                ChatInputBar(
                    text: vm.inputText,
                    isSending: vm.isSending,
                    onTextChange: vm.onInputChange,
                    onSendClick: vm.sendMessage,
                    onGenerateImageClick: vm.generateImage,
                    onGenerateVideoClick: vm.generateVideo
                )
                .padding(.horizontal)
                .padding(.bottom, 8)
            }
        }
        .navigationTitle("对话")
        .toolbar {
            // ✅ 对话页右上角同样也有黑白按钮
            ToolbarItem(placement: .navigationBarTrailing) {
                Button {
                    appState.isDarkMode.toggle()
                } label: {
                    Image(systemName: appState.isDarkMode ? "sun.max.fill" : "moon.fill")
                        .foregroundColor(appState.isDarkMode ? .yellow : .primary)
                }
            }
        }
    }
}

// MARK: - 气泡 & 内容

private struct MessageBubble: View {
    let message: Message

    private var bgColor: Color {
        message.isUser ? Color.accentColor : Color(.systemGray5)
    }

    private var fgColor: Color {
        message.isUser ? .white : .primary
    }

    var body: some View {
        let text = message.text
        let imagePrefix = "图片已生成："
        let videoPrefix = "视频已生成："

        HStack {
            if message.isUser { Spacer() }

            VStack(alignment: .leading, spacing: 4) {
                if text.hasPrefix(imagePrefix) {
                    let url = text
                        .replacingOccurrences(of: imagePrefix, with: "")
                        .trimmingCharacters(in: .whitespacesAndNewlines)
                    ImageMessageContent(url: url)
                } else if text.hasPrefix(videoPrefix) {
                    let raw = text
                        .replacingOccurrences(of: videoPrefix, with: "")
                        .trimmingCharacters(in: .whitespacesAndNewlines)
                    let url = raw.components(separatedBy: .newlines).first ?? ""
                    VideoMessageContent(url: url)
                } else {
                    // ✅ 普通文本支持 Markdown 渲染
                    if let attributed = try? AttributedString(markdown: text) {
                        Text(attributed)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                    } else {
                        Text(text)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                    }
                }
            }
            .background(bgColor)
            .foregroundColor(fgColor)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))

            if !message.isUser { Spacer() }
        }
        .padding(.vertical, 4)
        .id(message.id)
    }
}

private struct ImageMessageContent: View {
    let url: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("图片已生成：")
                .font(.caption)

            AsyncImage(url: URL(string: url)) { phase in
                switch phase {
                case .empty:
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: 200)
                case .success(let image):
                    image
                        .resizable()
                        .scaledToFill()
                        .frame(maxWidth: .infinity, maxHeight: 260)
                        .clipShape(
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                        )
                case .failure:
                    Text("图片加载失败")
                        .foregroundColor(.secondary)
                @unknown default:
                    EmptyView()
                }
            }
        }
        .padding(8)
    }
}

private struct VideoMessageContent: View {
    let url: String
    @State private var showPlayer = false

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("视频已生成：")
                .font(.caption)

            Button {
                showPlayer = true
            } label: {
                ZStack {
                    Rectangle()
                        .fill(Color.black.opacity(0.1))
                        .frame(height: 200)
                        .clipShape(
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                        )

                    Image(systemName: "play.circle.fill")
                        .resizable()
                        .frame(width: 48, height: 48)
                }
            }
            .buttonStyle(.plain)
        }
        .padding(8)
        .sheet(isPresented: $showPlayer) {
            if let u = URL(string: url) {
                VideoPlayer(player: AVPlayer(url: u))
                    .edgesIgnoringSafeArea(.all)
            } else {
                Text("视频链接无效")
            }
        }
    }
}

// MARK: - 输入栏

private struct ChatInputBar: View {
    let text: String
    let isSending: Bool
    let onTextChange: (String) -> Void
    let onSendClick: () -> Void
    let onGenerateImageClick: () -> Void
    let onGenerateVideoClick: () -> Void

    var body: some View {
        VStack(spacing: 8) {
            TextField(
                "和 AI 聊点什么…",
                text: Binding(
                    get: { text },
                    set: { onTextChange($0) }
                ),
                axis: .vertical
            )
            .lineLimit(1...4)
            .textFieldStyle(.roundedBorder)

            HStack(spacing: 8) {
                Button {
                    onSendClick()
                } label: {
                    if isSending {
                        ProgressView()
                    } else {
                        Text("发送")
                    }
                }
                .buttonStyle(.borderedProminent)
                .disabled(isSending || text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)

                Button("✨ 文生图") {
                    onGenerateImageClick()
                }
                .disabled(isSending || text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)

                Button("🎬 文生视频") {
                    onGenerateVideoClick()
                }
                .disabled(isSending || text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
            }
        }
    }
}
