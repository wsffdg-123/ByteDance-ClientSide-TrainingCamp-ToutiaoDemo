package com.example.mydamo.ui.newslist.datamodel

import com.example.mydamo.data.api.NewsItem

/**
 * 新闻列表的 UI 状态模型
 */
data class NewsListState(
    // 当前是否正在加载第一页或刷新
    val isLoading: Boolean = false,
    // 用于下拉刷新
    val isRefreshing: Boolean = false,
    // 当前是否正在加载更多 (分页)
    val isPaginating: Boolean = false,
    // 新闻列表数据
    val newsItems: List<NewsItem> = emptyList(),
    // 错误信息 (如果出现错误)
    val errorMessage: String? = null,
    // 是否还有下一页数据可供加载
    val canLoadMore: Boolean = true
)