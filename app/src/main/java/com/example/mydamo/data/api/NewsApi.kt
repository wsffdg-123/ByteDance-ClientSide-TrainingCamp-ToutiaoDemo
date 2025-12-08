package com.example.mydamo.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    /**
     * 获取新闻列表接口
     * @param category 新闻分类，例如 "recommend"
     * @param page 页码
     */
    @GET("v1/news/list")
    suspend fun getNewsList(
        @Query("category") category: String,
        @Query("page") page: Int
    ): NewsResponse
}