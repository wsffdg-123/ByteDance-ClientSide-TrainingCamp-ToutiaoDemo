package com.example.mydamo.data.api

data class NewsItem(
    val newsId: String,
    val title: String,
    val source: String,
    val time: Long,
    val type: Int, // 1: 纯文本, 2: 单图, 3: 三图, 4: 大图
    val imageUrls: List<String>
)