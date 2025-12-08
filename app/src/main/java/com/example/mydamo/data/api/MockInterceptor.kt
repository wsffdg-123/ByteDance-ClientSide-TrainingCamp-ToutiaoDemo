package com.example.mydamo.data.api

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 模拟网络请求拦截器，用于在开发阶段返回硬编码的 JSON 数据。
 */
class MockInterceptor @Inject constructor() : Interceptor {

    // 假设 Base URL 为 "https://api.mocktoutiao.com/"
    private val MOCK_HOST = "api.mocktoutiao.com"
    private val JSON_MEDIA_TYPE = "application/json".toMediaTypeOrNull()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url

        // 仅拦截 mock host 的请求
        if (url.host != MOCK_HOST) {
            return chain.proceed(request)
        }

        // 模拟网络延迟
        Thread.sleep(1000)

        val path = url.encodedPath
        val page = url.queryParameter("page")?.toIntOrNull() ?: 1

        val mockResponseBody = when {
            path.endsWith("/v1/news/list") -> getMockResponseByPage(page)
            else -> getErrorResponse("Unknown path: $path")
        }

        return Response.Builder()
            .code(200) // 始终返回 200 成功状态码
            .message("OK")
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .body(mockResponseBody.toResponseBody(JSON_MEDIA_TYPE))
            .addHeader("content-type", "application/json")
            .build()
    }

    /**
     * 根据页码返回不同的模拟 JSON 数据
     */
    private fun getMockResponseByPage(page: Int): String {
        return when (page) {
            1 -> mockJsonPage1
            2 -> mockJsonPage2
            else -> getErrorResponse("No more data (Page $page)") // 模拟没有更多数据
        }
    }

    // ----------------------------------------------------
    // --- 模拟数据定义 ---
    // ----------------------------------------------------

    // 模拟 Page 1 数据 (包含单图和三图类型)
    private val mockJsonPage1 = """
        {
            "code": 200,
            "message": "success",
            "data": [
                {
                    "newsId": "001",
                    "title": "Compose 是未来的 UI 框架：声明式范式的优势与挑战分析。",
                    "source": "Compose Weekly",
                    "time": ${System.currentTimeMillis() - 12000000}, 
                    "type": 2, 
                    "imageUrls": ["https://picsum.photos/id/10/400/300"]
                },
                {
                    "newsId": "002",
                    "title": "Kotlin 协程 Flow 深入解析：构建强大的响应式数据流管道。",
                    "source": "Kotlin Corner",
                    "time": ${System.currentTimeMillis() - 6000000}, 
                    "type": 3, 
                    "imageUrls": [
                        "https://picsum.photos/id/11/150/100",
                        "https://picsum.photos/id/12/150/100",
                        "https://picsum.photos/id/13/150/100"
                    ]
                },
                {
                    "newsId": "005",
                    "title": "头条推荐算法原理揭秘：兴趣图谱与实时反馈机制。",
                    "source": "字节技术",
                    "time": ${System.currentTimeMillis() - 3000000}, 
                    "type": 2, 
                    "imageUrls": ["https://picsum.photos/id/16/400/300"]
                }
            ]
        }
    """.trimIndent()

    // 模拟 Page 2 数据 (模拟加载更多)
    private val mockJsonPage2 = """
        {
            "code": 200,
            "message": "success",
            "data": [
                {
                    "newsId": "003",
                    "title": "Room 数据库与 Hilt 集成：Android 最佳实践。",
                    "source": "Android Dev",
                    "time": ${System.currentTimeMillis() - 3000000}, 
                    "type": 4, 
                    "imageUrls": ["https://picsum.photos/id/14/800/400"]
                },
                {
                    "newsId": "004",
                    "title": "MVI 架构在 Compose 中的实践：State vs Effect 的巧妙分离。",
                    "source": "Architecture Review",
                    "time": ${System.currentTimeMillis() - 1000000}, 
                    "type": 2, 
                    "imageUrls": ["https://picsum.photos/id/15/400/300"]
                }
            ]
        }
    """.trimIndent()

    // 模拟错误响应
    private fun getErrorResponse(message: String) = """
        {
            "code": 500,
            "message": "$message",
            "data": []
        }
    """.trimIndent()
}