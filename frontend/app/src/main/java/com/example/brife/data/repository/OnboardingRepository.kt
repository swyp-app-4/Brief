package com.example.brife.data.repository

import com.example.brife.data.model.CategoryResponse
import com.example.brife.data.model.InterestRequest
import com.example.brife.data.model.SubCategoryResponse
import com.example.brife.data.remote.api.OnboardingApiService
import android.util.Log
import com.example.brife.data.local.OnboardingLocalStorage


class OnboardingRepository(
    private val api: OnboardingApiService,
    private val localStorage: OnboardingLocalStorage
) {

    suspend fun getCategories(): Result<List<CategoryResponse>> {
        return try {
            val response = api.getCategories()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("카테고리 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveInterests(categoryIds: List<Long>): Result<Unit> {
        return try {
            localStorage.saveSelectedCategoryIds(categoryIds)
            Log.d("OnboardingRepo", "saved categoryIds locally: $categoryIds")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // categoryGroupIds: 사용자가 선택한 대분류 ID 목록
    // GET /categories/details 호출 후, GET /categories로 대분류 이름 매핑
    suspend fun getSubCategories(categoryGroupIds: List<Long>): Result<List<SubCategoryResponse>> {
        return try {
            val detailsResponse = api.getCategoryDetails(categoryGroupIds)
            if (!detailsResponse.isSuccessful || detailsResponse.body() == null) {
                return Result.failure(Exception("소분류 조회 실패: ${detailsResponse.code()}"))
            }

            // 대분류 이름을 채우기 위해 categories API 호출
            val categoriesResponse = api.getCategories()
            val categoryNameMap: Map<Long, String> = if (categoriesResponse.isSuccessful) {
                categoriesResponse.body()?.associate { it.id to it.groupName } ?: emptyMap()
            } else {
                emptyMap()
            }

            val result = detailsResponse.body()!!.map { item ->
                item.copy(parentCategoryName = categoryNameMap[item.parentCategoryId] ?: "")
            }

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveSubInterests(subCategoryIds: List<Long>): Result<Unit> {
        return try {
            localStorage.saveSelectedSubCategoryIds(subCategoryIds)
            localStorage.saveOnboardingCompleted(true)
            Log.d("OnboardingRepo", "saved subCategoryIds locally: $subCategoryIds")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun hasCompletedOnboarding(): Boolean {
        return localStorage.hasCompletedOnboarding()
    }

    fun getSavedCategoryIds(): List<Long> {
        return localStorage.getSelectedCategoryIds()
    }

    fun getSavedSubCategoryIds(): List<Long> {
        return localStorage.getSelectedSubCategoryIds()
    }
}
