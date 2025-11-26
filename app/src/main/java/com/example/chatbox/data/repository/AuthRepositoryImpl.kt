package com.example.chatbox.data.repository

import com.example.chatbox.data.local.prefs.UserPreferences
import com.example.chatbox.domain.model.User
import com.example.chatbox.domain.repository.AuthRepository
import com.example.chatbox.util.Result

class AuthRepositoryImpl(
    private val prefs: UserPreferences
) : AuthRepository {

    override suspend fun login(userId: String): Result<User> = try {
        val user = User(id = userId, displayName = userId)
        // 这里调用上面 UserPreferences 里的方法
        prefs.saveLastUserId(userId)
        Result.Success(user)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
