package com.swyp.brife.data.repository

import com.swyp.brife.data.model.CategoryResponse
import com.swyp.brife.data.model.SubCategoryResponse
import com.swyp.brife.data.remote.api.OnboardingApiService
import com.swyp.brife.data.local.AuthLocalStorage
import com.swyp.brife.data.local.OnboardingLocalStorage


class OnboardingRepository(
    private val api: OnboardingApiService,
    private val localStorage: OnboardingLocalStorage,
    private val authLocalStorage: AuthLocalStorage
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

    // 온보딩 소분류 선택 결과를 로컬에만 저장
    // 서버 전송 책임은 호출 시점에 따라 분리됨:
    //   - 초기 온보딩(비로그인): 로그인 완료 후 LoginViewModel.agreeTerms()에서 PUT
    //   - 관심사 재설정(로그인): MainScreen의 onNextClick 람다에서 PUT
    suspend fun saveSubInterests(
        subCategoryIds: List<Long>,
        parentCategoryIds: List<Long>
    ): Result<Unit> {
        return try {
            localStorage.saveSelectedSubCategoryIds(subCategoryIds)
            localStorage.saveOnboardingCompleted(true)
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
