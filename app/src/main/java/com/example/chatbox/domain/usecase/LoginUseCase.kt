package com.example.chatbox.domain.usecase

import com.example.chatbox.domain.model.User
import com.example.chatbox.domain.repository.AuthRepository
import com.example.chatbox.util.Result

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(userId: String): Result<User> =
        repository.login(userId)
}
