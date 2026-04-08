package com.swyp.brife.data.remote.api

import com.swyp.brife.data.model.NewsPageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ExploreApiService {

    // GET /news/latest — 전체 뉴스 최신순 조회 (기본 화면)
    @GET("news/latest")
    suspend fun getLatestNews(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<NewsPageResponse>

    // GET /news/search — 키워드 검색
    @GET("news/search")
    suspend fun searchNews(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<NewsPageResponse>
}
