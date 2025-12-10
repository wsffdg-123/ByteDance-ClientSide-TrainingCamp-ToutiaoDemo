package com.example.mydamo.ui.newslist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("头条推荐") })
        }
    ) { paddingValues ->
        // --- 使用 PullToRefreshBox 包裹内容 ---
        PullToRefreshBox(
            // 绑定 ViewModel 的 isRefreshing 状态
            isRefreshing = state.isRefreshing,
            // 绑定刷新触发事件
            onRefresh = { viewModel.loadData() },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            // --- 列表内容，直接放入 LazyColumn ---
            if (state.newsItems.isEmpty() && state.errorMessage != null && !state.isRefreshing) {

                // 显示错误信息和重试按钮
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = { viewModel.loadData() } // 点击重试
                    ) {
                        Text("点击重试")
                    }
                }
            }else if (state.newsItems.isEmpty() && state.isLoading) {
                // 首次加载（列表为空）时，显示居中加载指示器
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.errorMessage != null && state.newsItems.isEmpty()) {
                // 错误状态
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "加载失败: ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
                }
            } else {

                // 实际的列表内容 (包括分页逻辑)
                NewsListContent(
                    state = state,
                    onLoadMore = viewModel::loadMore
                )
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
                // SideEffect 确保在 Composable 渲染期间执行副作用操作
                SideEffect {
                    onLoadMore()
                }
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
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("正在加载...")
                }
            }
        }

        // 渲染没有更多数据提示
        //if (!state.canLoadMore && !state.isLoading && state.newsItems.isNotEmpty()) {
        if (!state.canLoadMore && !state.isPaginating && state.newsItems.isNotEmpty()) {
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