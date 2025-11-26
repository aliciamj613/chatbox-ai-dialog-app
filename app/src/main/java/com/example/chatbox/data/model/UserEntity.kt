package com.example.chatbox.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room 用的用户实体。
 * 和现在的领域模型 User 保持一致：id + name。
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String
)
