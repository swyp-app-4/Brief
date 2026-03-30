package com.example.brife.data.remote.api

import com.example.brife.data.model.NewsDetailResponse
import com.example.brife.data.model.RecommendedNewsResponse
import retrofit2.Call
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

    // GET /news/{id} — 뉴스 상세 조회 (2차 연동 예정)
    @GET("news/{id}")
    fun getNewsDetail(
        @Path("id") id: Long
    ): Call<NewsDetailResponse>
}
