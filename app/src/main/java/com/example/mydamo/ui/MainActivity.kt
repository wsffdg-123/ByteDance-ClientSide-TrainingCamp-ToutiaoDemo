package com.example.mydamo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mydamo.ui.newslist.NewsListScreen
import com.example.mydamo.ui.theme.ToutiaoTheme
import dagger.hilt.android.AndroidEntryPoint

// 标记为 Hilt 入口点
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToutiaoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 应用程序主界面的入口
                    NewsListScreen()
                }
            }
        }
    }
}