package com.example.chatbox.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.chatbox.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String
)

fun UserEntity.toDomain(): User = User(id = id, displayName = displayName)
fun User.toEntity(): UserEntity = UserEntity(id = id, displayName = displayName)
