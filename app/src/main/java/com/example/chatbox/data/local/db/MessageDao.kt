package com.example.chatbox.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chatbox.data.model.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    /** 按会话 ID 订阅消息列表（按时间升序） */
    @Query(
        "SELECT * FROM messages " +
                "WHERE conversationId = :conversationId " +
                "ORDER BY timestamp ASC"
    )
    fun observeMessagesByConversation(conversationId: Long): Flow<List<MessageEntity>>

    /** 按会话 ID 一次性取出全部消息（用于构造历史上下文） */
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
