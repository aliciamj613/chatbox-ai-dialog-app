// app/src/main/java/com/example/chatbox/ui/navigation/NavGraph.kt
package com.example.chatbox.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatbox.ui.chat.ChatScreen
import com.example.chatbox.ui.login.LoginScreen

object Routes {
    const val LOGIN = "login"
    const val CHAT = "chat"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { _userId ->
                    // 目前先不把 userId 传到 ChatScreen，
                    // 后面做“按用户/会话区分历史”时再用。
                    navController.navigate(Routes.CHAT) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CHAT) {
            ChatScreen()
        }
    }
}
