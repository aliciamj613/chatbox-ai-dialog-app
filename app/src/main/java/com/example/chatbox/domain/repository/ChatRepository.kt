package com.example.chatbox.domain.repository

import com.example.chatbox.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * 聊天数据仓库接口
 * 上层（UseCase / ViewModel）只依赖这个接口
 */
interface ChatRepository {

    /** 监听聊天历史（单用户版本） */
    fun observeMessages(): Flow<List<Message>>

    /** 保存一条本地消息 */
    suspend fun addLocalMessage(text: String, isUser: Boolean)
}
