package com.example.chatbox


import android.app.Application
import com.example.chatbox.di.AppModule

class ChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 很关键的一步：初始化 AppModule
        AppModule.init(this)
    }
}
