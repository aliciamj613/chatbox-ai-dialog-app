// app/src/main/java/com/example/chatbox/di/AppModule.kt
package com.example.chatbox.di

import android.content.Context
import com.example.chatbox.data.local.db.AppDatabase
import com.example.chatbox.data.repository.ChatRepositoryImpl
import com.example.chatbox.domain.repository.ChatRepository
import com.example.chatbox.domain.usecase.GetHistoryUseCase
import com.example.chatbox.domain.usecase.SendMessageUseCase

/**
 * 一个简单的“手写依赖注入”，方便在 ViewModel 里拿到 UseCase 和 Repository
 */
object AppModule {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(appContext)
    }

    private val chatRepositoryImpl: ChatRepositoryImpl by lazy {
        ChatRepositoryImpl(
            messageDao = database.messageDao()
        )
    }

    val chatRepository: ChatRepository
        get() = chatRepositoryImpl

    fun provideGetHistoryUseCase(): GetHistoryUseCase =
        GetHistoryUseCase(chatRepository)

    fun provideSendMessageUseCase(): SendMessageUseCase =
        SendMessageUseCase(chatRepository)
}
