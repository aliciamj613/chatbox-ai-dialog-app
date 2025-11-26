package com.example.chatbox.di

import android.content.Context
import com.example.chatbox.data.local.db.AppDatabase
import com.example.chatbox.data.repository.ChatRepositoryImpl
import com.example.chatbox.domain.repository.ChatRepository

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
}
