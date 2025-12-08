package com.example.mydamo.ui.newslist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mydamo.ui.newslist.components.NewsCardSingleImage
import com.example.mydamo.ui.newslist.viewmodel.NewsViewModel

/**
 * 新闻列表主屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsListScreen(
    // 使用 hiltViewModel() 获取由 Hilt 注入的 ViewModel 实例
    viewModel: NewsViewModel = hiltViewModel()
) {
    // 观察 ViewModel 暴露的 StateFlow
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("头条推荐") })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // --- 1. 渲染新闻列表 ---
            if (state.newsItems.isNotEmpty()) {
                NewsListContent(
                    state = state,
                    onLoadMore = viewModel::loadMore
                )
            }

            // --- 2. 渲染初始加载状态或空数据状态 ---
            else if (state.isLoading) {
                // 初次加载时显示全屏加载指示器
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.errorMessage != null) {
                // 显示错误信息
                Text(
                    text = "加载失败: ${state.errorMessage}",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                // 显示空数据提示
                Button(
                    onClick = { viewModel.loadData() },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text("暂无数据，点击重试")
                }
            }
        }
    }
}

/**
 * 列表内容 Composable (包含分页逻辑)
 */
@Composable
private fun NewsListContent(
    state: com.example.mydamo.ui.newslist.datamodel.NewsListState,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(state.newsItems) { index, item ->

            // 触发加载更多的逻辑：当滚动到倒数第5个元素时，且可以加载更多，则触发。
            if (index >= state.newsItems.size - 5 && state.canLoadMore && !state.isPaginating) {
                onLoadMore()
            }

            // 渲染新闻卡片，这里仅以单图卡片为例
            NewsCardSingleImage(item = item)
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        // --- 3. 渲染分页加载状态 ---
        if (state.isPaginating) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("正在加载...")
                }
            }
        }

        // 渲染没有更多数据提示
        if (!state.canLoadMore && !state.isLoading && state.newsItems.isNotEmpty()) {
            item {
                Text(
                    text = "— 已加载全部内容 —",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}