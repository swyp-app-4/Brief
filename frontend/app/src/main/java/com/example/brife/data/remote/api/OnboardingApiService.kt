package com.example.brife.data.remote.api

import com.example.brife.data.model.CategoryResponse
import com.example.brife.data.model.InterestRequest
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
}