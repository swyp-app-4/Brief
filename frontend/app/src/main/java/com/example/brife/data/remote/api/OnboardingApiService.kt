package com.example.brife.data.remote.api

import com.example.brife.data.model.CategoryResponse
import com.example.brife.data.model.InterestRequest
import com.example.brife.data.model.SubCategoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OnboardingApiService {

    // GET /categories — 대분류 카테고리 목록
    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryResponse>>

    // GET /categories/details?categoryGroupIds=1&categoryGroupIds=2 — 소분류 카테고리 목록
    @GET("categories/details")
    suspend fun getCategoryDetails(
        @Query("categoryGroupIds") categoryGroupIds: List<Long>
    ): Response<List<SubCategoryResponse>>

    @POST("users/me/interests")
    suspend fun saveInterests(
        @Body request: InterestRequest
    ): Response<Unit>
}