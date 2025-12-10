package com.example.mydamo.data.api

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

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

        val refreshId = url.queryParameter("refresh_id")

        val mockResponseBody = when {
            path.endsWith("/v1/news/list") -> getMockResponseByPage(page, refreshId)
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
    private fun getMockResponseByPage(page: Int, refreshId: String?): String {
        if (refreshId != null) {
            // 我们只在 Page 1 (刷新) 时随机生成，分页时仍然使用固定数据
            if (page == 1) {
                return generateMockNewsJson(count = 10, startId = 100)
            }
        }
        return when (page) {
            1 -> generateMockNewsJson(10, startId = 100)
            2 -> generateMockNewsJson(10, startId = 100)
            3 -> generateMockNewsJson(10, startId = 100)
            else -> getErrorResponse("No more data (Page $page)") // 模拟没有更多数据
        }
    }

    // --- 模拟数据生成 ---
    fun generateMockNewsJson(
        count: Int,
        startId: Int = 100,          // 起始 newsId
        timeRangeMs: Long = 10_000_000L // 随机时间范围
    ): String {

        val titles = listOf(
            "Compose 性能全解析：Recomposition 到 Layout 的底层机制。",
            "Kotlin 协程在 2025 的最佳实践。",
            "Android 架构演进：从 MVC 到 MVI 的十年。",
            "Hilt 依赖注入的底层实现原理。",
            "Retrofit vs Ktor：新一代网络层如何选择？",
            "Room 持久化最佳实践：Flow + Paging 的玩法。",
            "AI 本地推理在移动端的突破。",
            "Compose Navigation 多栈管理深度解析。",
            "GNN 在网络流分析中的应用。",
            "Transformer 在日志分析中的落地方案。"
        )

        val sources = listOf(
            "Android Dev",
            "Compose Weekly",
            "ByteDance Tech",
            "Kotlin Corner",
            "AI Frontier",
            "Architecture Review"
        )

        fun randomImageList(): List<String> {
            val count = Random.nextInt(1, 4) // 1~3 张图
            return List(count) {
                val id = Random.nextInt(10, 70)
                "https://picsum.photos/id/$id/400/300"
            }
        }

        val items = (0 until count).joinToString(",") { index ->
            val id = startId + index
            val title = titles.random()
            val source = sources.random()
            val type = Random.nextInt(1, 5)
            val time = System.currentTimeMillis() - Random.nextLong(timeRangeMs)
            val images = randomImageList().joinToString(",") { "\"$it\"" }

            """
        {
            "newsId": "$id",
            "title": "$title",
            "source": "$source",
            "time": $time,
            "type": $type,
            "imageUrls": [$images]
        }
        """.trimIndent()
        }

        return """
        {
            "code": 200,
            "message": "success",
            "data": [$items]
        }
    """.trimIndent()
    }


    // 模拟错误响应
    private fun getErrorResponse(message: String) = """
        {
            "code": 500,
            "message": "$message",
            "data": []
        }
    """.trimIndent()
}