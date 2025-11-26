package com.example.chatbox.di

import com.example.chatbox.data.repository.AuthRepositoryImpl
import com.example.chatbox.data.repository.ChatRepositoryImpl
import com.example.chatbox.domain.repository.AuthRepository
import com.example.chatbox.domain.repository.ChatRepository

/**
 * 超轻量 DI 容器，只提供单例仓库。
 */
object AppModule {

    val chatRepository: ChatRepository by lazy {
        ChatRepositoryImpl()
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl()
    }
}
