package com.example.brife.data.remote.api

import com.example.brife.data.model.RecommendedNewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface HomeApiService {

    // GET /home/news/recommended
    // JWT로 인증된 유저의 관심 카테고리 기반 Top5 추천 뉴스
    @GET("home/news/recommended")
    suspend fun getRecommendedNews(
        @Header("Authorization") authorization: String
    ): Response<List<RecommendedNewsResponse>>
}
