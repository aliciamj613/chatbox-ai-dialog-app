// app/src/main/java/com/example/chatbox/data/model/UserEntity.kt
package com.example.chatbox.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room 用的用户表：
 * - name 作为用户名
 * - password 简单明文保存（只在本地练手项目里用）
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val password: String
)
