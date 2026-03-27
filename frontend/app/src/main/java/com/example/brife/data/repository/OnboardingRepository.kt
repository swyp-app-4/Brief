package com.example.brife.data.repository

import com.example.brife.data.model.CategoryResponse
import com.example.brife.data.model.InterestRequest
import com.example.brife.data.remote.api.OnboardingApiService
import android.util.Log
import com.example.brife.data.local.OnboardingLocalStorage
import com.example.brife.data.model.SubCategoryResponse



class OnboardingRepository(
    private val api: OnboardingApiService,
    private val localStorage: OnboardingLocalStorage
) {

    suspend fun getCategories(): Result<List<CategoryResponse>> {
        return Result.success(
            listOf(
                CategoryResponse(1L, "시사 · 정치"),
                CategoryResponse(2L, "경제 · 재테크"),
                CategoryResponse(3L, "IT · 테크"),
                CategoryResponse(4L, "문화 · 예술"),
                CategoryResponse(5L, "엔터 · 스포츠"),
                CategoryResponse(6L, "라이프 · 성장")
            )
        )
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

    suspend fun getSubCategories(): Result<List<SubCategoryResponse>> {
        return Result.success(
            listOf(
                SubCategoryResponse(1L, "청와대", 1L, "시사 · 정치"),
                SubCategoryResponse(2L, "국회/정당", 1L, "시사 · 정치"),
                SubCategoryResponse(3L, "북한", 1L, "시사 · 정치"),
                SubCategoryResponse(4L, "행정", 1L, "시사 · 정치"),
                SubCategoryResponse(5L, "국방/외교", 1L, "시사 · 정치"),
                SubCategoryResponse(6L, "정치일반", 1L, "시사 · 정치"),

                SubCategoryResponse(11L, "금융", 2L, "경제 · 재테크"),
                SubCategoryResponse(12L, "증권", 2L, "경제 · 재테크"),
                SubCategoryResponse(13L, "산업/재계", 2L, "경제 · 재테크"),
                SubCategoryResponse(14L, "중기/벤처", 2L, "경제 · 재테크"),
                SubCategoryResponse(15L, "부동산", 2L, "경제 · 재테크"),

                SubCategoryResponse(21L, "모바일", 3L, "IT · 테크"),
                SubCategoryResponse(22L, "인터넷/SNS", 3L, "IT · 테크"),
                SubCategoryResponse(23L, "통신/뉴미디어", 3L, "IT · 테크"),
                SubCategoryResponse(24L, "보안/해킹", 3L, "IT · 테크"),

                SubCategoryResponse(31L, "공연/전시", 4L, "문화 · 예술"),
                SubCategoryResponse(32L, "책", 4L, "문화 · 예술"),
                SubCategoryResponse(33L, "종교", 4L, "문화 · 예술"),
                SubCategoryResponse(34L, "영화", 4L, "문화 · 예술"),

                SubCategoryResponse(41L, "드라마", 5L, "엔터 스포츠"),
                SubCategoryResponse(42L, "뮤직", 5L, "엔터 · 스포츠"),
                SubCategoryResponse(43L, "연예", 5L, "엔터 · 스포츠"),
                SubCategoryResponse(44L, "축구", 5L, "엔터 · 스포츠"),

                SubCategoryResponse(51L, "건강정보", 6L, "라이프 · 성장"),
                SubCategoryResponse(52L, "여행/레저", 6L, "라이프 · 성장"),
                SubCategoryResponse(53L, "음식/맛집", 6L, "라이프 · 성장"),
                SubCategoryResponse(54L, "패션/뷰티", 6L, "라이프 · 성장")
            )
        )
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

//
//class OnboardingRepository(
//    private val api: OnboardingApiService,
//    private val localStorage: OnboardingLocalStorage
//) {
//
//    suspend fun getCategories(): Result<List<CategoryResponse>> {
//        return try {
//            val response = api.getCategories()
//
//            if (response.isSuccessful) {
//                val body = response.body()
//                if (body != null) {
//                    Result.success(body)
//                } else {
//                    Result.failure(Exception("카테고리 응답이 비어 있습니다."))
//                }
//            } else {
//                Result.failure(Exception("카테고리 조회 실패: ${response.code()}"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun saveInterests(categoryIds: List<Long>): Result<Unit> {
//        return try {
//            Log.d("OnboardingRepo", "saveInterests request: $categoryIds")
//            val response = api.saveInterests(
//                InterestRequest(categoryIds = categoryIds)
//            )
//
//            Log.d("OnboardingRepo", "saveInterests response code: ${response.code()}")
//
//            if (response.isSuccessful) {
//                Result.success(Unit)
//            } else {
//                Result.failure(Exception("관심사 저장 실패: ${response.code()}"))
//            }
//        } catch (e: Exception) {
//            Log.e("OnboardingRepo", "saveInterests exception", e)
//            Result.failure(e)
//        }
//    }
//
//    suspend fun getSubCategories(): Result<List<SubCategoryResponse>> {
//        return runCatching {
//            api.getSubCategories()
//        }
//    }
//
//    suspend fun saveSubInterests(subCategoryIds: List<Long>): Result<Unit> {
//        return runCatching {
//            api.saveSubInterests(subCategoryIds)
//        }
//    }
//}
