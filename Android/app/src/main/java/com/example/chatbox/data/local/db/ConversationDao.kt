// app/src/main/java/com/example/chatbox/data/local/db/ConversationDao.kt
package com.example.chatbox.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chatbox.data.model.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    /** ✅ 只监听某个用户的会话，按更新时间倒序 */
    @Query(
        "SELECT * FROM conversations " +
                "WHERE userId = :userId " +
                "ORDER BY updatedAt DESC"
    )
    fun observeConversations(userId: Long): Flow<List<ConversationEntity>>

    /** 插入新会话，返回自增 ID */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    /** 根据 ID 查询单个会话 */
    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: Long): ConversationEntity?

    /** 更新会话（比如更新 updatedAt 或 title） */
    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    /** ✅ 删除某个会话记录 */
    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: Long)
}
