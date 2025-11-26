package com.example.chatbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.chatbox.ui.navigation.AppNavGraph
import com.example.chatbox.ui.theme.ChatboxTheme   // 如果你主题叫别的名字，就用自己的

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatboxTheme {
                AppNavGraph()   // ✅ 现在有这个函数了
            }
        }
    }
}
