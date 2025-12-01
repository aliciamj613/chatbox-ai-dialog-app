# Chatbox AI Dialog App

一个支持 **多轮对话记忆 + 多模态生成（文生图 / 文生视频）** 的双端原生 Demo 项目。  
仓库下包含：

- `Android/`：Android 原生客户端（Kotlin + Jetpack Compose + Room）
- `Chatbox-iOS/`：iOS 客户端工程（SwiftUI）


## ✨ 功能概览（Android 端）

- **账号体系**
  - 支持注册 / 登录
  - 基于本地存储区分不同用户，登录不同账号看到各自的对话历史

- **会话管理**
  - 会话列表页：展示所有对话，按更新时间排序
  - 自动生成会话标题（截取首轮用户输入前若干字）
  - 支持进入单独会话、查看历史记录，后续可扩展删除等管理能力

- **有记忆的多轮对话**
  - 使用 Room 按 `conversationId` 存储消息
  - 每次请求时，将当前会话历史打包为 `messages` 列表发送给大模型，实现「上下文记忆」

- **多模态生成**
  - 文生图：调用智谱图像模型（CogView 系列），生成图片 URL，并在聊天气泡中直接展示图片
  - 文生视频：调用视频模型（如 CogVideoX 系列），生成视频 URL，以卡片形式展示，可点击跳转外部播放器播放

- **聊天界面（Jetpack Compose）**
  - 消息列表：`LazyColumn`，支持上下滚动
  - 左右对齐气泡：用户消息右对齐、AI 消息左对齐
  - 输入区域：输入框 + 发送按钮 +「文生图」「文生视频」快捷按钮
  - 错误提示：接口出错时在底部显示错误信息

- **主题切换**
  - 顶栏提供 ☀️/🌙 按钮，一键在浅色 / 深色模式之间切换（Compose + 自定义主题）

- **本地数据持久化**
  - 使用 Room 存储：
    - `User`（用户信息）
    - `Conversation`（会话元信息）
    - `Message`（聊天消息）
  - App 重启后仍可看到历史对话


## 🧠 技术栈

### Android

* 语言：Kotlin
* UI：Jetpack Compose, Material 3
* 状态管理：ViewModel + StateFlow
* 网络：Retrofit + OkHttp
* 数据库：Room
* 图片加载：Coil（用于加载智谱返回的图片 URL）
* 其它：Kotlin 协程、AndroidX

### iOS

* 语言：Swift
* UI：SwiftUI
* 状态管理：`ObservableObject` + `@Published`
* 网络层：使用 `URLSession` 对接与 Android 对齐的 API


## 🔑 智谱 AI 接入说明（重要）

本项目 **不自建后端服务器**，Android / iOS 客户端 **直接调用智谱 AI 公共 API**。
因此，要运行本项目，需要先在智谱控制台获得一个可用的 **API Key**。

1. 注册并登录智谱 AI 平台
2. 在控制台创建应用 / 获取 API Key（形如 `sk-xxxx`）
3. 确保该 Key 具备：

   * 文本模型（例如 `glm-4.5` / `glm-4.5-air`）
   * 图片模型（例如 `CogView-4-250304`）
   * 视频模型（例如 `CogVideoX-3`）
     对应的免费额度或资源包

## 🚀 运行方式

### 1. Android 端运行方式

**前提条件：**

* JDK 17+
* Android Studio（Ladybug / Koala 及以上）
* Android SDK & 模拟器已安装（API 级别 29+）
* 已在 `Android/local.properties` 中配置 `ZHIPU_API_KEY`

**步骤：**

1. 打开 Android Studio → `Open` → 选择仓库下的 `Android/` 目录
2. 等待 Gradle Sync 完成
3. 确认 `Build Variants` 使用 `debug` / `release` 任一可用配置
4. 选择一个虚拟或真机设备（API 29+）
5. 点击运行（绿色 ▶️）启动 `app` 模块
6. App 启动后：

   * 注册一个账号并登录
   * 在会话列表中新建 / 进入对话
   * 体验文本对话、文生图、文生视频等功能

若调用 API 出现 401 / 429 等错误，可在 Logcat 查看具体错误信息，检查：

* API Key 是否正确
* 模型名称与控制台权限是否匹配
* 调用频率是否超过限制


### 2. iOS 端运行方式（当前为骨架版本）

**前提条件：**

* macOS
* Xcode 15+
* 已安装 iOS 模拟器（iOS 17+）
* 后续会加入与 Android 对齐的 API 接入逻辑

**步骤：**

1. 打开 Xcode → `Open` → 选择仓库中的 `Chatbox-iOS/你的工程名.xcodeproj`
2. 选择目标模拟器（例如 iPhone 15）
3. 点击运行（`Cmd + R`）启动工程
4. 当前主要用于界面与项目结构搭建，后续将逐步接入智谱 API，实现与 Android 端对齐的多轮对话与多模态功能


## 🧱 后端说明

* 本项目**没有自建后端服务器**，所有智能能力均由 **智谱 AI 平台** 提供；
* 移动端直接通过 HTTPS 调用智谱的 RESTful API，包括：

  * 文本对话：`/chat/completions`（GLM 系列模型）
  * 文生图：CogView 系列接口
  * 文生视频：CogVideoX / Vidu 系列接口
* 因此 **无需单独启动任何后端服务**，只要配置好 API Key 即可运行 Demo。
