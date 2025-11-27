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
import com.example.chatbox.data.local.db.AppDatabase
import com.example.chatbox.data.local.prefs.UserPreferences
import com.example.chatbox.data.model.UserEntity
import kotlinx.coroutines.launch

private enum class AuthMode {
    LOGIN, REGISTER, RESET
}

@Composable
fun LoginScreen(
    onLoginSuccess: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember { AppDatabase.getDatabase(context) }
    val userDao = remember { database.userDao() }
    val userPrefs = remember { UserPreferences(context) }

    var mode by remember { mutableStateOf(AuthMode.LOGIN) }

    // 登录
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var isLoggingIn by remember { mutableStateOf(false) }

    // 注册
    var registerUsername by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerConfirm by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    // 忘记密码
    var resetUsername by remember { mutableStateOf("") }
    var resetNewPassword by remember { mutableStateOf("") }
    var resetConfirm by remember { mutableStateOf("") }
    var isResetting by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = when (mode) {
                    AuthMode.LOGIN -> "登录"
                    AuthMode.REGISTER -> "注册新账号"
                    AuthMode.RESET -> "重置密码"
                },
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 顶部模式切换：登录 / 注册
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = { mode = AuthMode.LOGIN; error = null; info = null }) {
                    Text(
                        text = "登录",
                        color = if (mode == AuthMode.LOGIN)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(onClick = { mode = AuthMode.REGISTER; error = null; info = null }) {
                    Text(
                        text = "注册",
                        color = if (mode == AuthMode.REGISTER)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (mode) {
                AuthMode.LOGIN -> {
                    OutlinedTextField(
                        value = loginUsername,
                        onValueChange = { loginUsername = it },
                        label = { Text("用户名") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
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
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (info != null) {
                        Text(
                            text = info!!,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Button(
                        onClick = {
                            if (loginUsername.isBlank() || loginPassword.isBlank()) {
                                error = "用户名和密码不能为空"
                                info = null
                                return@Button
                            }

                            scope.launch {
                                isLoggingIn = true
                                error = null
                                info = null
                                try {
                                    val user = userDao.getUserByName(loginUsername.trim())
                                    if (user == null) {
                                        error = "用户不存在，请先注册"
                                    } else if (user.password != loginPassword) {
                                        error = "密码错误"
                                    } else {
                                        userPrefs.saveLastUserId(user.id)
                                        onLoginSuccess(user.id)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    error = "登录失败：${e.message ?: "未知错误"}"
                                } finally {
                                    isLoggingIn = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        enabled = !isLoggingIn
                    ) {
                        Text(if (isLoggingIn) "登录中…" else "登录")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(onClick = {
                        // 进入重置密码模式
                        mode = AuthMode.RESET
                        resetUsername = loginUsername
                        error = null
                        info = "请输入用户名和新密码来重置密码"
                    }) {
                        Text("忘记密码？")
                    }
                }

                AuthMode.REGISTER -> {
                    OutlinedTextField(
                        value = registerUsername,
                        onValueChange = { registerUsername = it },
                        label = { Text("用户名") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = registerPassword,
                        onValueChange = { registerPassword = it },
                        label = { Text("密码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = registerConfirm,
                        onValueChange = { registerConfirm = it },
                        label = { Text("确认密码") },
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
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Button(
                        onClick = {
                            if (registerUsername.isBlank()
                                || registerPassword.isBlank()
                                || registerConfirm.isBlank()
                            ) {
                                error = "请完整填写信息"
                                info = null
                                return@Button
                            }
                            if (registerPassword != registerConfirm) {
                                error = "两次输入的密码不一致"
                                info = null
                                return@Button
                            }

                            scope.launch {
                                isRegistering = true
                                error = null
                                info = null
                                try {
                                    val exist = userDao.getUserByName(registerUsername.trim())
                                    if (exist != null) {
                                        error = "该用户名已存在"
                                    } else {
                                        val id = userDao.insertUser(
                                            UserEntity(
                                                name = registerUsername.trim(),
                                                password = registerPassword
                                            )
                                        )
                                        userPrefs.saveLastUserId(id)
                                        onLoginSuccess(id)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    error = "注册失败：${e.message ?: "未知错误"}"
                                } finally {
                                    isRegistering = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        enabled = !isRegistering
                    ) {
                        Text(if (isRegistering) "注册中…" else "注册并登录")
                    }
                }

                AuthMode.RESET -> {
                    OutlinedTextField(
                        value = resetUsername,
                        onValueChange = { resetUsername = it },
                        label = { Text("需要重置的用户名") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = resetNewPassword,
                        onValueChange = { resetNewPassword = it },
                        label = { Text("新密码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = resetConfirm,
                        onValueChange = { resetConfirm = it },
                        label = { Text("确认新密码") },
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
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (info != null) {
                        Text(
                            text = info!!,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Button(
                        onClick = {
                            if (resetUsername.isBlank()
                                || resetNewPassword.isBlank()
                                || resetConfirm.isBlank()
                            ) {
                                error = "请完整填写信息"
                                info = null
                                return@Button
                            }
                            if (resetNewPassword != resetConfirm) {
                                error = "两次输入的新密码不一致"
                                info = null
                                return@Button
                            }

                            scope.launch {
                                isResetting = true
                                error = null
                                info = null
                                try {
                                    val user = userDao.getUserByName(resetUsername.trim())
                                    if (user == null) {
                                        error = "用户不存在"
                                    } else {
                                        userDao.updatePassword(
                                            name = resetUsername.trim(),
                                            newPassword = resetNewPassword
                                        )
                                        info = "密码已重置成功，请使用新密码登录"
                                        mode = AuthMode.LOGIN
                                        loginUsername = resetUsername.trim()
                                        loginPassword = ""
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    error = "重置失败：${e.message ?: "未知错误"}"
                                } finally {
                                    isResetting = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        enabled = !isResetting
                    ) {
                        Text(if (isResetting) "重置中…" else "确认重置")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(onClick = {
                        mode = AuthMode.LOGIN
                        error = null
                        info = null
                    }) {
                        Text("返回登录")
                    }
                }
            }
        }
    }
}
