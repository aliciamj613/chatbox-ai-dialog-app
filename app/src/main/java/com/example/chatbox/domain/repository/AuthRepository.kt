package com.example.chatbox.domain.repository

import com.example.chatbox.domain.model.User
import com.example.chatbox.util.Result

/**
 * 登录相关仓库接口。
 * 暂时只用 username 登录，简化链路先跑通。
 */
interface AuthRepository {

    /**
     * 登录：传入用户名，返回 Result<User>
     */
    suspend fun login(username: String): Result<User>

    /**
     * 获取最近一次登录的用户（如果有）
     */
    suspend fun getLastUser(): User?

    /**
     * 退出登录
     */
    suspend fun logout()
}
