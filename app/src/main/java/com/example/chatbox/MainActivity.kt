package com.example.chatbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.chatbox.di.AppModule
import com.example.chatbox.ui.navigation.NavGraphRoot
import com.example.chatbox.ui.theme.ChatboxTheme  // 如果你的 Theme 叫别的名字就改成对应的

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppModule.init(applicationContext)

        setContent {
            ChatboxTheme {
                NavGraphRoot()
            }
        }
    }
}
