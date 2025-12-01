// app/src/main/java/com/example/chatbox/data/local/db/UserDao.kt
package com.example.chatbox.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chatbox.data.model.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE name = :name LIMIT 1")
    suspend fun getUserByName(name: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    /** 忘记密码：根据用户名更新密码 */
    @Query("UPDATE users SET password = :newPassword WHERE name = :name")
    suspend fun updatePassword(name: String, newPassword: String)
}
