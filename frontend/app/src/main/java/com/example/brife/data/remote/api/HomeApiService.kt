package com.example.brife.data.remote.api

import com.example.brife.data.model.RecommendedNewsResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface HomeApiService {

    // GET /home/news/recommended — 회원용 (JWT 필수), suspend 버전 (앱 내부용)
    @GET("home/news/recommended")
    suspend fun getRecommendedNews(
        @Header("Authorization") authorization: String
    ): Response<List<RecommendedNewsResponse>>

    // GET /home/news/recommended — 회원용, blocking Call<> 버전 (위젯 RemoteViewsFactory 전용)
    @GET("home/news/recommended")
    fun getRecommendedNewsCall(
        @Header("Authorization") authorization: String
    ): Call<List<RecommendedNewsResponse>>

    // GET /news/top5 — 비회원용 (인증 불필요, 로컬 관심사 기반)
    // 위젯은 NewsApiService의 Call<> 버전 사용 (blocking 필요)
    // 홈 화면은 suspend 버전 사용
    @GET("news/top5")
    suspend fun getTop5News(
        @Query("categoryIds") categoryIds: List<Long>,
        @Query("groupIds") groupIds: List<Long>
    ): Response<List<RecommendedNewsResponse>>
}
