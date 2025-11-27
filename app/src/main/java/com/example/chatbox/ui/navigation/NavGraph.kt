package com.example.chatbox.ui.navigation

import androidx.compose.runtime.Composable
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
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.CONVERSATIONS) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CONVERSATIONS) {
            ConversationListScreen(
                onOpenConversation = { conversationId ->
                    navController.navigate("${Routes.CHAT}/$conversationId")
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Routes.CHAT}/{conversationId}",
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val conversationId =
                backStackEntry.arguments?.getLong("conversationId") ?: 0L

            ChatScreen(
                conversationId = conversationId,
                onBackToConversations = { navController.navigateUp() }
            )
        }
    }
}
