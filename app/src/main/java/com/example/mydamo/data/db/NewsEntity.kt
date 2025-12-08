package com.example.mydamo.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mydamo.data.api.NewsItem

/**
 * 新闻数据的 Room 实体
 */
@Entity(tableName = "news_items")
data class NewsEntity(
    @PrimaryKey
    val newsId: String,
    val title: String,
    val source: String,
    val time: Long,
    val type: Int,
    // Room 不直接支持 List<String>，需要定义 TypeConverter
    val imageUrlsJson: String
)

// 简单的扩展函数，用于业务模型和实体之间的转换
fun NewsEntity.toDomain(): NewsItem {
    // 假设我们有一个 Json 转换工具 (如 Gson) 来处理 imageUrlsJson
    val imageUrlsList = imageUrlsJson.split(",") // 示例简单分隔符转换
    return NewsItem(newsId, title, source, time, type, imageUrlsList)
}

fun NewsItem.toEntity(): NewsEntity {
    val imageUrlsJson = this.imageUrls.joinToString(",") // 示例简单分隔符转换
    return NewsEntity(newsId, title, source, time, type, imageUrlsJson)
}