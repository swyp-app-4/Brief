package com.swyp.brife.data.remote.api

import com.swyp.brife.data.model.NewsDetailResponse
import com.swyp.brife.data.model.NewsSourceItemResponse
import com.swyp.brife.data.model.RecommendedNewsResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsApiService {

    // GET /news/top5 — 관심사 기반 Top5 뉴스
    // RemoteViewsFactory.onDataSetChanged()는 background thread에서 동작하므로
    // suspend 대신 블로킹 Call<> 사용 → .execute() 로 호출
    @GET("news/top5")
    fun getTop5News(
        @Query("categoryIds") categoryIds: List<Long>,
        @Query("groupIds") groupIds: List<Long>
    ): Call<List<RecommendedNewsResponse>>

    // GET /news/{id} — 뉴스 상세 조회 (위젯용 blocking 버전)
    @GET("news/{id}")
    fun getNewsDetail(
        @Path("id") id: Long
    ): Call<NewsDetailResponse>

    // GET /news/{id} — 뉴스 상세 조회 (coroutine suspend 버전)
    @GET("news/{id}")
    suspend fun getNewsDetailAsync(
        @Path("id") id: Long
    ): Response<NewsDetailResponse>




    @GET("news/{id}/sources")
    suspend fun getNewsSources(
        @Path("id") id: Long
    ): Response<List<NewsSourceItemResponse>>





}


