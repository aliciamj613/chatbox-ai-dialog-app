// app/src/main/java/com/example/chatbox/ui/login/LoginScreen.kt
package com.example.chatbox.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.chatbox.ui.components.PasswordTextField
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

    // 忘记密码 / 重置密码
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
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (mode) {
                AuthMode.LOGIN -> {
                    // 用户名
                    OutlinedTextField(
                        value = loginUsername,
                        onValueChange = { loginUsername = it },
                        label = { Text("用户名") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 密码（带眼睛）
                    PasswordTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = "密码",
                        modifier = Modifier.fillMaxWidth()
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

                    Spacer(modifier = Modifier.height(8.dp))

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
                        Text(if (isLoggingIn) "登录中..." else "登录")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = {
                            mode = AuthMode.REGISTER
                            error = null
                            info = null
                        }) {
                            Text("没有账号？去注册")
                        }

                        TextButton(onClick = {
                            mode = AuthMode.RESET
                            error = null
                            info = null
                        }) {
                            Text("忘记密码？")
                        }
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

                    PasswordTextField(
                        value = registerPassword,
                        onValueChange = { registerPassword = it },
                        label = "密码",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PasswordTextField(
                        value = registerConfirm,
                        onValueChange = { registerConfirm = it },
                        label = "确认密码",
                        modifier = Modifier.fillMaxWidth()
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
                            if (registerUsername.isBlank() ||
                                registerPassword.isBlank() ||
                                registerConfirm.isBlank()
                            ) {
                                error = "用户名和密码不能为空"
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
                        Text(if (isRegistering) "注册中..." else "注册")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = {
                        mode = AuthMode.LOGIN
                        error = null
                        info = null
                    }) {
                        Text("已有账号？去登录")
                    }
                }

                AuthMode.RESET -> {
                    OutlinedTextField(
                        value = resetUsername,
                        onValueChange = { resetUsername = it },
                        label = { Text("用户名") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PasswordTextField(
                        value = resetNewPassword,
                        onValueChange = { resetNewPassword = it },
                        label = "新密码",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PasswordTextField(
                        value = resetConfirm,
                        onValueChange = { resetConfirm = it },
                        label = "确认新密码",
                        modifier = Modifier.fillMaxWidth()
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
                            if (resetUsername.isBlank() ||
                                resetNewPassword.isBlank() ||
                                resetConfirm.isBlank()
                            ) {
                                error = "用户名和密码不能为空"
                                info = null
                                return@Button
                            }

                            if (resetNewPassword != resetConfirm) {
                                error = "两次输入的密码不一致"
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
                                        info = "密码重置成功，请使用新密码登录"
                                        // 可选：自动切回登录
                                        mode = AuthMode.LOGIN
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
                        Text(if (isResetting) "重置中..." else "重置密码")
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
