package com.example.chatbox

import android.app.Application
import com.example.chatbox.di.AppModule

class ChatboxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
    }
}
