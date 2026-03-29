package com.example.brife.data.repository

import android.util.Log
import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.local.OnboardingLocalStorage
import com.example.brife.data.model.RecommendedNewsResponse
import com.example.brife.data.remote.api.HomeApiService
import com.example.brife.feature.home.HomeNewsCardItem

class HomeRepository(
    private val api: HomeApiService,
    private val authLocalStorage: AuthLocalStorage,
    private val onboardingLocalStorage: OnboardingLocalStorage
) {
    private fun bearerToken(): String? =
        authLocalStorage.getAccessToken()?.let { "Bearer $it" }

    // 로그인 여부에 따라 회원/비회원 API를 자동 분기
    suspend fun getHomeNews(): Result<List<HomeNewsCardItem>> {
        return if (authLocalStorage.isLoggedIn()) {
            getMemberHomeNews()
        } else {
            getGuestHomeNews()
        }
    }

    // 회원: GET /home/news/recommended (JWT 필수)
    private suspend fun getMemberHomeNews(): Result<List<HomeNewsCardItem>> {
        val token = bearerToken()
            ?: return Result.failure(Exception("토큰이 없습니다."))
        return try {
            val response = api.getRecommendedNews(token)
            Log.d(TAG, "회원 응답 code=${response.code()}, body=${response.body()?.size}개")
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!
                items.firstOrNull()?.let {
                    Log.d(TAG, "첫 항목: id=${it.id}, groupName='${it.groupName}', categoryName='${it.categoryName}', bodyPreview='${it.bodyPreview.take(20)}'")
                }
                val mapped = items.map { it.toHomeNewsCardItem() }
                Log.d(TAG, "매핑 완료: ${mapped.size}개")
                Result.success(mapped)
            } else {
                Log.w(TAG, "추천 뉴스 조회 실패: ${response.code()} ${response.errorBody()?.string()}")
                Result.failure(Exception("추천 뉴스 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "회원 뉴스 로드 예외", e)
            Result.failure(e)
        }
    }

    // 비회원: GET /news/top5 (OnboardingLocalStorage에서 관심사 읽기)
    private suspend fun getGuestHomeNews(): Result<List<HomeNewsCardItem>> {
        val groupIds = onboardingLocalStorage.getSelectedCategoryIds()       // 부모(대분류)
        val categoryIds = onboardingLocalStorage.getSelectedSubCategoryIds() // 소분류

        Log.d(TAG, "비회원 요청: groupIds=$groupIds, categoryIds=$categoryIds")

        // 관심사 미설정 시 빈 리스트 반환 (API 필수 파라미터 없음)
        if (categoryIds.isEmpty() && groupIds.isEmpty()) {
            Log.d(TAG, "관심사 미설정 → 빈 리스트 반환")
            return Result.success(emptyList())
        }

        return try {
            val response = api.getTop5News(categoryIds, groupIds)
            Log.d(TAG, "비회원 응답 code=${response.code()}, body=${response.body()?.size}개")
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!
                items.firstOrNull()?.let {
                    Log.d(TAG, "첫 항목: id=${it.id}, groupName='${it.groupName}', categoryName='${it.categoryName}', bodyPreview='${it.bodyPreview.take(20)}'")
                }
                val mapped = items.map { it.toHomeNewsCardItem() }
                Log.d(TAG, "매핑 완료: ${mapped.size}개")
                Result.success(mapped)
            } else {
                Log.w(TAG, "비회원 뉴스 조회 실패: ${response.code()} ${response.errorBody()?.string()}")
                Result.failure(Exception("비회원 뉴스 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "비회원 뉴스 로드 예외", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "HomeRepo"
    }
}

// DTO → UI 모델 매핑 (회원/비회원 공통)
// groupName = 대분류(category 칩), categoryName = 소분류(subCategory 칩)
private fun RecommendedNewsResponse.toHomeNewsCardItem() = HomeNewsCardItem(
    newsId = id,
    category = groupName,
    subCategory = categoryName,
    title = title,
    notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
    summaryPoints = summaryList,
    insight = bodyPreview,
    articleCount = sourceCount,
    updatedAt = publishedDate
)
