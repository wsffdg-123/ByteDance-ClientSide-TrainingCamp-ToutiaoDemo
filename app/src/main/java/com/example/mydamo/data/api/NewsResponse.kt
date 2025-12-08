package com.example.mydamo.data.api

import com.example.mydamo.data.api.NewsItem

/**
 * 模拟网络 API 返回的完整结构
 */
data class NewsResponse(
    val code: Int,
    val message: String,
    val data: List<NewsItem>
)