// app/src/main/java/com/example/chatbox/data/local/db/MessageDao.kt
package com.example.chatbox.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.chatbox.data.model.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    // 监听所有消息（按时间升序）
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun observeMessages(): Flow<List<MessageEntity>>

    // 插入一条消息
    @Insert
    suspend fun insertMessage(message: MessageEntity)

    // 清空所有消息
    @Query("DELETE FROM messages")
    suspend fun clearAll()

    // 如果以后要做单条删除，可以加这个（目前仓库里没用到，可以先不写）
    // @Query("DELETE FROM messages WHERE id = :id")
    // suspend fun deleteById(id: Long)
}
