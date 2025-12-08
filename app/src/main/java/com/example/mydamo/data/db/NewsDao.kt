package com.example.mydamo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    /**
     * 查询所有新闻，并按时间降序排列。Room KTX 支持返回 Flow，当数据改变时自动发射新值。
     */
    @Query("SELECT * FROM news_items ORDER BY time DESC")
    fun getAllNewsFlow(): Flow<List<NewsEntity>>

    /**
     * 插入或更新新闻列表。如果主键冲突，则替换现有数据 (OnConflictStrategy.REPLACE)。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(news: List<NewsEntity>)

    /**
     * 清空所有新闻数据（用于彻底刷新前）。
     */
    @Query("DELETE FROM news_items")
    suspend fun clearAll()
}