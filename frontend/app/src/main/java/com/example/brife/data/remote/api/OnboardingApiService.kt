package com.example.brife.data.remote.api

import com.example.brife.data.model.CategoryResponse
import com.example.brife.data.model.InterestRequest
import com.example.brife.data.model.SubCategoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OnboardingApiService {

    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryResponse>>

    @POST("users/me/interests")
    suspend fun saveInterests(
        @Body request: InterestRequest
    ): Response<Unit>
    @GET("api/sub-categories")
    suspend fun getSubCategories(): List<SubCategoryResponse>

    @POST("api/sub-interests")
    suspend fun saveSubInterests(
        @Body subCategoryIds: List<Long>
    )
}