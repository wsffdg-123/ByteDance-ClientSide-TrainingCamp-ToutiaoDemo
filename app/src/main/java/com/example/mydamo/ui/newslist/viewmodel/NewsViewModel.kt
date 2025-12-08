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
        // ViewModel 初始化时自动加载数据
        loadData()
    }

    /**
     * 加载/刷新第一页数据 (包含缓存策略)
     */
    fun loadData() {
        if (_state.value.isLoading || _state.value.isPaginating) return

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        currentPage = 1

        viewModelScope.launch {
            repository.getNewsStream(page = currentPage)
                .collect { result ->
                    // Flow 收集到的数据可能是缓存或网络最新数据
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            newsItems = result.items, // 接收到的数据
                            errorMessage = result.error,
                            // 假设如果返回的列表小于 pageSize，则没有更多数据了
                            canLoadMore = result.items.isNotEmpty()
                        )
                    }
                }
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