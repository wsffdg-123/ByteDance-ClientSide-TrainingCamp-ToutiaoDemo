package com.example.mydamo.ui.newslist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydamo.data.repository.NewsRepository
import com.example.mydamo.ui.newslist.datamodel.NewsListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository // Hilt 注入 Repository 接口
) : ViewModel() {

    // 内部可变状态
    private val _state = MutableStateFlow(NewsListState())
    // 外部暴露给 UI 的只读状态
    val state: StateFlow<NewsListState> = _state.asStateFlow()

    private var currentPage = 1

    init {
        // 核心修改 1: 在 ViewModel 初始化时，启动一个协程永久观察数据库变化。
        viewModelScope.launch {
            // 我们只观察第一页的数据流（代表整个列表）
            repository.getNewsStream(page = 1)
                .collect { result ->
                    // 数据库一旦有变化（新增、刷新），这里就会自动更新 UI。
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isRefreshing = false, // 无论如何，数据库更新成功后，结束刷新或加载状态
                            newsItems = result.items,
                            errorMessage = result.error,
                            canLoadMore = result.items.isNotEmpty() // 假设逻辑
                        )
                    }
                }
        }

        // 首次启动时，触发一次网络请求，获取最新数据并填充缓存
        loadData()
    }

    /**
     * 加载/刷新第一页数据 (包含缓存策略)
     */
    fun loadData() {
        // 确保不会重复触发刷新（现在只需要检查 isRefreshing 即可）
        if (_state.value.isRefreshing) return

        viewModelScope.launch {
            // 1. 更新状态：进入刷新状态，通知 UI 显示加载指示器
            _state.update { it.copy(isRefreshing = true, errorMessage = null) }

            try {
                // 2. 触发网络请求和数据库写入 (不会阻塞此协程)
                val newRefreshId = System.currentTimeMillis()
                repository.refreshCacheAndNetwork(refreshId = newRefreshId)

                // 成功后：数据会被 init 块中的 Flow 自动接收和更新。

            } catch (e: Exception) {
                // 3. 失败时：更新错误状态，并取消刷新指示器
                _state.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = "刷新失败: ${e.message}"
                    )
                }
            }
            // 注意：这里不需要手动设置 isRefreshing = false，它会由 Flow 接收到新数据时自动处理。
            // 但如果需要确保网络请求完成后立即关闭刷新状态，可以放在 finally 块中（视具体业务逻辑而定）。
            /* finally {
                _state.update { it.copy(isRefreshing = false) }
            } */
        }
    }

    /**
     * 加载更多数据 (分页)
     */
    fun loadMore() {
        // 只有当没有加载中、且还可以加载更多时，才执行
        if (_state.value.isLoading || _state.value.isPaginating || !_state.value.canLoadMore) return

        _state.update { it.copy(isPaginating = true, errorMessage = null) }
        currentPage++

        viewModelScope.launch {
            try {
                // 仅请求网络数据，不走缓存，并追加到现有列表
                val newItems = repository.fetchNews(page = currentPage).items

                _state.update { currentState ->
                    currentState.copy(
                        isPaginating = false,
                        newsItems = currentState.newsItems + newItems, // 追加新数据
                        canLoadMore = newItems.isNotEmpty()
                    )
                }
            } catch (e: Exception) {
                // 处理加载更多时的错误
                _state.update { it.copy(isPaginating = false, errorMessage = "加载更多失败: ${e.message}") }
            }
        }
    }
}