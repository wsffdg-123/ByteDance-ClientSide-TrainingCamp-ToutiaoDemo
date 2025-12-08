package com.example.mydamo.ui.newslist.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mydamo.data.api.NewsItem

/**
 * 基础新闻卡片 - 单图模式
 */
@Composable
fun NewsCardSingleImage(item: NewsItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        onClick = { /* TODO: 导航到详情页 */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧：标题和元信息
            Column(
                modifier = Modifier
                    .weight(1f) // 占据剩余空间
                    .padding(end = 8.dp)
            ) {
                // 标题
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 来源和时间
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item.source,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // 假设时间已格式化或在此处处理格式化
                    Text(
                        text = formatTime(item.time),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 右侧：图片
            item.imageUrls.firstOrNull()?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(96.dp)
                        .padding(start = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

// 简单的时间格式化辅助函数
private fun formatTime(timestamp: Long): String {
    // 实际项目中应使用 SimpleDateFormat 或 Instant/LocalDateTime 进行格式化
    val diff = (System.currentTimeMillis() - timestamp) / 1000 / 60 // 分钟差
    return when {
        diff < 60 -> "${diff}分钟前"
        diff < 60 * 24 -> "${diff / 60}小时前"
        else -> "N天前" // 简化处理
    }
}