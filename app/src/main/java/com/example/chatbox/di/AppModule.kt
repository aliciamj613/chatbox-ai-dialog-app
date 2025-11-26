package com.example.chatbox.di

import android.content.Context
import androidx.room.Room
import com.example.chatbox.data.local.db.AppDatabase
import com.example.chatbox.data.local.prefs.UserPreferences
import com.example.chatbox.data.repository.AuthRepositoryImpl
import com.example.chatbox.data.repository.ChatRepositoryImpl
import com.example.chatbox.domain.repository.AuthRepository
import com.example.chatbox.domain.repository.ChatRepository

object AppModule {

    private lateinit var database: AppDatabase
    private lateinit var prefs: UserPreferences

    lateinit var chatRepository: ChatRepository
        private set

    lateinit var authRepository: AuthRepository
        private set

    fun init(context: Context) {
        if (::database.isInitialized) return

        database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "chatbox.db"
        ).build()

        prefs = UserPreferences(context.applicationContext)

        chatRepository = ChatRepositoryImpl(database.messageDao())
        authRepository = AuthRepositoryImpl(prefs)
    }
}
