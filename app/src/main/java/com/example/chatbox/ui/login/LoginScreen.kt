// app/src/main/java/com/example/chatbox/ui/login/LoginScreen.kt
package com.example.chatbox.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.chatbox.data.local.db.AppDatabase
import com.example.chatbox.data.local.prefs.UserPreferences
import com.example.chatbox.data.model.UserEntity

@Composable
fun LoginScreen(
    onLoginSuccess: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Room + SharedPreferences
    val database = remember { AppDatabase.getDatabase(context) }
    val userDao = remember { database.userDao() }
    val userPrefs = remember { UserPreferences(context) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isRegisterMode) "注册新账号" else "登录",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        error = "用户名和密码不能为空"
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        error = null
                        try {
                            if (isRegisterMode) {
                                // 注册逻辑
                                val exist = userDao.getUserByName(username)
                                if (exist != null) {
                                    error = "该用户名已存在"
                                } else {
                                    val userId = userDao.insertUser(
                                        UserEntity(
                                            name = username,
                                            password = password
                                        )
                                    )
                                    userPrefs.saveLastUserId(userId)
                                    onLoginSuccess(userId)
                                }
                            } else {
                                // 登录逻辑
                                val user = userDao.getUserByName(username)
                                if (user == null) {
                                    error = "用户不存在，请先注册"
                                } else if (user.password != password) {
                                    error = "密码错误"
                                } else {
                                    userPrefs.saveLastUserId(user.id)
                                    onLoginSuccess(user.id)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            error = "操作失败：${e.message ?: "未知错误"}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                enabled = !isLoading
            ) {
                Text(
                    if (isLoading) "处理中…" else if (isRegisterMode) "注册并登录"
                    else "登录"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                Text(
                    if (isRegisterMode) "已有账号？返回登录"
                    else "没有账号？去注册"
                )
            }

            // 简单版“忘记密码”：提示信息（真正改密码可以后面再做）
            TextButton(onClick = {
                error = "忘记密码功能可以后续接入重置逻辑（目前仅本地练习项目）。"
            }) {
                Text("忘记密码？")
            }
        }
    }
}
