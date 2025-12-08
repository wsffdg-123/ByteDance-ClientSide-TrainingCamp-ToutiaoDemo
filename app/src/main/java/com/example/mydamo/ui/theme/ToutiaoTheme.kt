package com.example.mydamo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- 1. 定义颜色方案 ---
// 示例 Light Color Palette
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE43A3A), // 头条红
    onPrimary = Color.White,
    secondary = Color(0xFF495E96),
    tertiary = Color(0xFF277D7D),
    background = Color(0xFFF8F8F8), // 浅灰背景
    surface = Color.White,
    // ... 其他颜色定义
)

// 示例 Dark Color Palette
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFC5F5F),
    onPrimary = Color.Black,
    secondary = Color(0xFFAEC4FF),
    tertiary = Color(0xFF7DDADA),
    background = Color(0xFF1C1C1C), // 深色背景
    surface = Color(0xFF252525),
    // ... 其他颜色定义
)

// --- 2. 主题 Composable 函数 ---

@Composable
fun ToutiaoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 动态颜色 (Android 12+) 仅在 Light/Dark 模式不设置时默认开启
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // 根据主题选择颜色方案
    val colorScheme = when {
        // dynamicColor is recommended for Android 12+
        // dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //     val context = LocalContext.current
        //     if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        // }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 如果需要自定义排版，应在此处定义 Typography
    // val typography = Typography

    MaterialTheme(
        colorScheme = colorScheme,
        // typography = typography, // 使用默认或自定义排版
        content = content
    )
}