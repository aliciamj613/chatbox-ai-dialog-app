package com.example.chatbox.data.repository

import com.example.chatbox.domain.model.User
import com.example.chatbox.domain.repository.AuthRepository
import com.example.chatbox.util.Result
import kotlinx.coroutines.delay

/**
 * 纯内存版登录实现：
 * - 只在内存里保存 lastUser
 * - 暂时不做密码校验
 */
class AuthRepositoryImpl : AuthRepository {

    private var lastUser: User? = null
    private var nextId: Long = 1L

    override suspend fun login(username: String): Result<User> {
        // 模拟网络延迟
        delay(300)

        if (username.isBlank()) {
            return Result.Error(IllegalArgumentException("Username cannot be empty"))
        }

        // 如果之前已经登录过同一个用户名，就复用；否则分配一个新的 id
        val user = lastUser?.takeIf { it.name == username } ?: User(
            id = nextId++,
            name = username
        ).also {
            lastUser = it
        }

        return Result.Success(user)
    }

    override suspend fun getLastUser(): User? {
        return lastUser
    }

    override suspend fun logout() {
        lastUser = null
    }
}
