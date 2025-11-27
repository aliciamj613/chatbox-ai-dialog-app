package com.example.chatbox.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chatbox.data.model.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    /**
     * 按会话 ID 订阅消息列表（Flow），按时间升序
     * 用于聊天界面实时展示历史 + 新消息
     */
    @Query(
        "SELECT * FROM messages " +
                "WHERE conversationId = :conversationId " +
                "ORDER BY timestamp ASC"
    )
    fun observeMessages(conversationId: Long): Flow<List<MessageEntity>>

    /**
     * 按会话 ID 一次性取出全部消息（用于构造大模型上下文）
     */
    @Query(
        "SELECT * FROM messages " +
                "WHERE conversationId = :conversationId " +
                "ORDER BY timestamp ASC"
    )
    suspend fun getMessagesByConversation(conversationId: Long): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun clearConversation(conversationId: Long)
}
