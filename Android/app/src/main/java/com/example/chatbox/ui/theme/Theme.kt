package com.example.chatbox.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 简单的一套亮色主题配置
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006875),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF4A6268),
    onSecondary = Color.White,
    background = Color(0xFFFDFDFD),
    onBackground = Color(0xFF191C1D),
    surface = Color(0xFFFDFDFD),
    onSurface = Color(0xFF191C1D)
)

// 简单的一套暗色主题配置
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4FD8EB),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = Color(0xFFB1CBD2),
    onSecondary = Color(0xFF1C343A),
    background = Color(0xFF111417),
    onBackground = Color(0xFFE0E2E5),
    surface = Color(0xFF111417),
    onSurface = Color(0xFFE0E2E5)
)

/**
 * App 主题：默认跟随系统深浅色
 */
@Composable
fun ChatboxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
