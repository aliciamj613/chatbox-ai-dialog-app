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

    /** 监听所有会话，按更新时间倒序（最近的排前面） */
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun observeConversations(): Flow<List<ConversationEntity>>

    /** 插入新会话 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    /** 根据 ID 查询会话 */
    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: Long): ConversationEntity?

    /** 更新会话（标题 / 更新时间等） */
    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    /** 删除会话记录 */
    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: Long)
}
