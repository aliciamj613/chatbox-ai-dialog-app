package com.example.chatbox.domain.repository

import com.example.chatbox.domain.model.User
import com.example.chatbox.util.Result

interface AuthRepository {
    suspend fun login(userId: String): Result<User>
}
