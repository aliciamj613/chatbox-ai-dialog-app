package com.example.chatbox.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chatbox.di.AppModule
import com.example.chatbox.ui.chat.ChatScreen
import com.example.chatbox.ui.chat.ChatViewModel
import com.example.chatbox.ui.chat.ChatViewModelFactory
import com.example.chatbox.ui.login.LoginScreen
import com.example.chatbox.ui.login.LoginViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Chat : Screen("chat/{userId}") {
        fun route(userId: String) = "chat/$userId"
    }
}

@Composable
fun NavGraphRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val vm: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = vm,
                onLoginSuccess = { userId ->
                    navController.navigate(Screen.Chat.route(userId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: "default"

            val chatVm: ChatViewModel = viewModel(
                factory = ChatViewModelFactory(
                    userId = userId,
                    chatRepository = AppModule.chatRepository
                )
            )
            ChatScreen(viewModel = chatVm)
        }
    }
}
