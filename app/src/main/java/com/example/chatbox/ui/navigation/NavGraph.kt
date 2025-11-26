package com.example.chatbox.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chatbox.ui.chat.ChatScreen
import com.example.chatbox.ui.conversation.ConversationListScreen
import com.example.chatbox.ui.login.LoginScreen

object Routes {
    const val LOGIN = "login"
    const val CONVERSATIONS = "conversations"
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
        // 登录页
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { _userId ->
                    navController.navigate(Routes.CONVERSATIONS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 会话列表页
        composable(Routes.CONVERSATIONS) {
            ConversationListScreen(
                onOpenConversation = { conversationId ->
                    // 👉 把会话 ID 拼进路由
                    navController.navigate("${Routes.CHAT}/$conversationId")
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 聊天页：带会话 ID 参数
        composable(
            route = "${Routes.CHAT}/{conversationId}",
            arguments = listOf(
                navArgument("conversationId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val conversationId =
                backStackEntry.arguments?.getLong("conversationId") ?: 1L
            ChatScreen(conversationId = conversationId)
        }
    }
}
