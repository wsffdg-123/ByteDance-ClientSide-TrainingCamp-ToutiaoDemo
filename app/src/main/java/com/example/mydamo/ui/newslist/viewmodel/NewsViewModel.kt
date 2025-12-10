package com.example.mydamo.ui.newslist.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydamo.data.repository.NewsRepository
import com.example.mydamo.ui.newslist.datamodel.NewsListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
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

    private val handler = CoroutineExceptionHandler { _, exception ->
        // 当有未捕获的异常发生时，这里会被调用
        Log.e("NewsViewModel", "未捕获的协程异常: $exception")

        // 从异常中解析出用户友好的信息
        val message = when (exception) {
            // 捕获网络连接类问题
            is java.net.SocketTimeoutException, is java.net.UnknownHostException -> {
                "网络连接超时或不可达，请检查您的网络。"
            }
            // 捕获 HTTP 错误 (500, 404, 400 等)
            is retrofit2.HttpException -> {
                // 可以根据错误码进行更精细的判断
                when (exception.code()) {
                    500 -> "服务器内部错误，请稍后再试。"
                    404 -> "请求资源不存在。"
                    else -> "网络请求失败: HTTP ${exception.code()}"
                }
            }
            // 其他所有异常
            else -> {
                exception.message ?: "系统错误，请联系客服。"
            }
        }

        // 更新 UI 状态
        _state.update {
            it.copy(
                isRefreshing = false,
                isLoading = false,
                errorMessage = message // 显示错误信息
            )
        }
    }

    init {
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

        viewModelScope.launch(handler) {
            // 更新状态：进入刷新状态，通知 UI 显示加载指示器
            _state.update { it.copy(isRefreshing = true, errorMessage = null) }

            // 触发网络请求和数据库写入 (不会阻塞此协程)
            val newRefreshId = System.currentTimeMillis()
            repository.refreshCacheAndNetwork(refreshId = newRefreshId)
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

        viewModelScope.launch(handler) {
            // 仅请求网络数据，不走缓存，并追加到现有列表
            val newItems = repository.fetchNews(page = currentPage).items

            _state.update { currentState ->
                currentState.copy(
                    isPaginating = false,
                    newsItems = currentState.newsItems + newItems, // 追加新数据
                    canLoadMore = newItems.isNotEmpty()
                )
            }
        }
    }
}