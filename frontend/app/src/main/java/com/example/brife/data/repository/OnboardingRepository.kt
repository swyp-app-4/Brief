package com.example.brife.data.repository

import com.example.brife.data.model.CategoryResponse
import com.example.brife.data.model.InterestRequest
import com.example.brife.data.remote.api.OnboardingApiService
import android.util.Log

class OnboardingRepository(
    private val api: OnboardingApiService
) {

    suspend fun getCategories(): Result<List<CategoryResponse>> {
        return try {
            val response = api.getCategories()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("카테고리 응답이 비어 있습니다."))
                }
            } else {
                Result.failure(Exception("카테고리 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveInterests(categoryIds: List<Long>): Result<Unit> {
        return try {
            Log.d("OnboardingRepo", "saveInterests request: $categoryIds")
            val response = api.saveInterests(
                InterestRequest(categoryIds = categoryIds)
            )

            Log.d("OnboardingRepo", "saveInterests response code: ${response.code()}")

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("관심사 저장 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("OnboardingRepo", "saveInterests exception", e)
            Result.failure(e)
        }
    }
}