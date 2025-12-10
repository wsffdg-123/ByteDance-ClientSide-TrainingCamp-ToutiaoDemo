package com.example.mydamo.data.repository

import com.example.mydamo.data.api.NewsApi
import com.example.mydamo.data.db.NewsDao
import com.example.mydamo.data.db.toDomain
import com.example.mydamo.data.db.toEntity
import com.example.mydamo.data.api.NewsItem
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val newsApi: NewsApi,
    private val newsDao: NewsDao
) : NewsRepository {

    // 假设默认分类为推荐
    private val DEFAULT_CATEGORY = "recommend"

    /**
     * 实现 Cache-First 策略的核心方法。
     * 1. 监听数据库数据流。
     * 2. 同时触发网络请求并写入数据库。
     */
    override fun getNewsStream(page: Int): Flow<NewsResult> {
        // 确保在 IO 线程进行数据处理
        return newsDao.getAllNewsFlow()
            .map { entities ->
                // 数据库 Flow 监听：将 NewsEntity 列表映射为 NewsItem 列表
                val newsItems = entities.map { it.toDomain() }


                // 将数据包装为 NewsResult
                NewsResult(items = newsItems)
            }
    }

    /**
     * 仅从网络获取数据并返回（用于分页加载）
     */
    override suspend fun fetchNews(page: Int): NewsResult = withContext(Dispatchers.IO) {
        try {
            val response = newsApi.getNewsList(category = DEFAULT_CATEGORY, page = page)
            if (response.code == 200) {
                // 加载更多时，直接将数据写入 Room，Room Flow 会自动更新列表
                newsDao.insertAll(response.data.map { it.toEntity() })
                NewsResult(items = response.data)
            } else {
                NewsResult(emptyList(), error = response.message)
            }
        } catch (e: Exception) {
            NewsResult(emptyList(), error = "网络请求失败: ${e.message}")
        }
    }

    /**
     * 负责后台刷新缓存的 Suspend 函数
     */
    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun refreshCacheAndNetwork(refreshId: Long) {
        //强制执行最小延迟时间
        val minDelayTime = 300L
        val startTime = System.currentTimeMillis()

        // 在新的协程中执行网络请求和数据库写入，不阻塞 Flow 的映射
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // 假设网络请求始终请求第一页最新数据
                val response = newsApi.getNewsList(
                    category = DEFAULT_CATEGORY,
                    page = 1,
                    refreshId = refreshId)

                if (response.code == 200) {
                    // 仅当网络请求成功时，清除旧缓存并插入新数据
                    //newsDao.clearAll() //数据量太小时不clear的增量更新效果更好
                    newsDao.insertAll(response.data.map { it.toEntity() })
                } else {
                    // 处理网络错误
                    throw Exception("API 业务错误: ${response.message}")
                }
            } catch (e: Exception) {
                // 处理网络异常
                throw e
            } finally {
                // 确保最小延迟
                val elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime < minDelayTime) {
                    delay(minDelayTime - elapsedTime)
                }
            }

            //计算已用时间，并延迟到最小加载时间
            val elapsedTime = System.currentTimeMillis() - startTime
            val remainingDelay = minDelayTime - elapsedTime
            if (remainingDelay > 0) {
                delay(remainingDelay) // 协程延迟
            }
        }
    }
}