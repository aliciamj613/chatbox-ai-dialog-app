// app/src/main/java/com/example/chatbox/ui/login/LoginScreen.kt
package com.example.chatbox.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.chatbox.data.local.db.AppDatabase
import com.example.chatbox.data.local.prefs.UserPreferences
import com.example.chatbox.data.model.UserEntity

@Composable
fun LoginScreen(
    onLoginSuccess: (Long) -> Unit    // 登录成功时，把 userId 传出去
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "欢迎登录 ChatBox",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        error = "用户名和密码不能为空"
                        return@Button
                    }

                    error = null
                    isLoggingIn = true

                    scope.launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            val userDao = db.userDao()
                            val prefs = UserPreferences(context)

                            val existing = userDao.getUserByName(username)

                            val userId: Long = if (existing == null) {
                                // 没有这个用户 → 直接注册
                                userDao.insertUser(
                                    UserEntity(
                                        name = username,
                                        password = password
                                    )
                                )
                            } else {
                                // 有这个用户 → 检查密码
                                if (existing.password != password) {
                                    error = "密码错误，请重试"
                                    isLoggingIn = false
                                    return@launch
                                }
                                existing.id
                            }

                            // 记住这次登录的用户
                            prefs.saveLastUserId(userId)

                            isLoggingIn = false
                            onLoginSuccess(userId)

                        } catch (e: Exception) {
                            e.printStackTrace()
                            error = "登录失败：${e.message ?: "未知错误"}"
                            isLoggingIn = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoggingIn
            ) {
                Text(if (isLoggingIn) "登录中…" else "登录")
            }
        }
    }
}
