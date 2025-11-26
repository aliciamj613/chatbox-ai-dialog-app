package com.example.chatbox.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.chatbox.data.model.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    // 监听全部消息（按时间升序）
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun observeMessages(): Flow<List<MessageEntity>>

    // 插入一条消息
    @Insert
    suspend fun insertMessage(message: MessageEntity)

    // 清空历史（后面如果想做“清除会话”可以用）
    @Query("DELETE FROM messages")
    suspend fun clearAll()
}
