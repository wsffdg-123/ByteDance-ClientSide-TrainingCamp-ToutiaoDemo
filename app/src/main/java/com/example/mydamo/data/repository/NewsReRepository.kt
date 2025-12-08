package com.example.mydamo.data.repository

import com.example.mydamo.data.api.NewsItem
import kotlinx.coroutines.flow.Flow

/**
 * 封装数据获取结果，用于统一处理数据和错误信息
 */
data class NewsResult(
    val items: List<NewsItem>,
    val error: String? = null
)

/**
 * 新闻数据仓库接口 (Data Layer)
 * 抽象了数据的来源，供 ViewModel 调用。
 */
interface NewsRepository {

    /**
     * 获取新闻数据流：实现 "Cache-First" 策略。
     * 1. 立即返回 Room 缓存数据 (Flow 第一次发射)。
     * 2. 同时请求网络数据，并写入 Room。
     * 3. Room 监测到变化后，Flow 发射最新的数据。
     * * @param page 页码
     * @return 持续发射数据的 Flow
     */
    fun getNewsStream(page: Int): Flow<NewsResult>

    /**
     * 仅从网络获取数据：用于加载更多 (分页)。
     * 此方法直接返回网络结果，不涉及复杂的缓存流程。
     * * @param page 页码
     * @return 仅包含网络数据的 NewsResult
     */
    suspend fun fetchNews(page: Int): NewsResult
}