import SwiftUI

struct ConversationListView: View {
    @EnvironmentObject var appState: AppState
    @StateObject private var vm = ConversationListViewModel()

    @State private var showLogoutAlert = false
    @State private var editMode: EditMode = .inactive

    var body: some View {
        NavigationStack {
            ZStack {
                // ✅ 背景跟随黑白按钮变
                (appState.isDarkMode ? Color.black : Color.white)
                    .ignoresSafeArea()

                VStack {
                    if let user = appState.currentUser {
                        List {
                            Section {
                                if vm.conversations.isEmpty {
                                    Text("暂无会话，点击右下角 + 开始对话")
                                        .foregroundColor(appState.isDarkMode ? .gray : .secondary)
                                } else {
                                    ForEach(vm.conversations) { conv in
                                        NavigationLink {
                                            ChatView(
                                                conversationId: conv.id,
                                                store: appState.store,
                                                repository: appState.repository
                                            )
                                        } label: {
                                            ConversationRow(conversation: conv)
                                        }
                                    }
                                    .onDelete { indexSet in
                                        for index in indexSet {
                                            let conv = vm.conversations[index]
                                            vm.deleteConversation(
                                                conv,
                                                store: appState.store,
                                                user: user
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        .listStyle(.insetGrouped)
                        .environment(\.editMode, $editMode)
                        .scrollContentBackground(.hidden) // ✅ 让 List 背景透明
                        .navigationTitle("会话列表")
                        .toolbar {
                            ToolbarItem(placement: .navigationBarLeading) {
                                // ✅ 左上角 黑/白按钮
                                Button {
                                    appState.isDarkMode.toggle()
                                } label: {
                                    Image(systemName: appState.isDarkMode ? "sun.max.fill" : "moon.fill")
                                        .foregroundColor(appState.isDarkMode ? .yellow : .primary)
                                }
                            }
                            ToolbarItem(placement: .navigationBarTrailing) {
                                HStack {
                                    Button(editMode == .inactive ? "编辑" : "完成") {
                                        editMode = (editMode == .inactive) ? .active : .inactive
                                    }
                                    Button("退出") {
                                        showLogoutAlert = true
                                    }
                                }
                            }
                        }
                        .alert("确认退出登录？", isPresented: $showLogoutAlert) {
                            Button("取消", role: .cancel) {}
                            Button("退出", role: .destructive) {
                                appState.logout()
                            }
                        }

                        Button {
                            vm.createConversation(for: user, store: appState.store)
                        } label: {
                            Image(systemName: "plus")
                                .padding()
                                .background(Circle().fill(Color.accentColor))
                                .foregroundColor(.white)
                                .shadow(radius: 4)
                        }
                        .padding(.bottom, 16)
                    } else {
                        Text("当前用户信息丢失，请重新登录")
                            .foregroundColor(appState.isDarkMode ? .gray : .secondary)
                    }
                }
            }
            .onAppear {
                if let user = appState.currentUser {
                    vm.load(for: user, store: appState.store)
                }
            }
        }
    }
}

private struct ConversationRow: View {
    let conversation: Conversation

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(conversation.title.isEmpty ? "新会话" : conversation.title)
                .font(.headline)
            Text("最近更新：\(formattedDate(conversation.updatedAt))")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding(.vertical, 4)
    }

    private func formattedDate(_ date: Date) -> String {
        let fmt = DateFormatter()
        fmt.dateFormat = "MM-dd HH:mm"
        return fmt.string(from: date)
    }
}
