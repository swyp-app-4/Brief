package com.example.brife.data.repository

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
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toHomeNewsCardItem() })
            } else {
                Result.failure(Exception("추천 뉴스 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 비회원: GET /news/top5 (OnboardingLocalStorage에서 관심사 읽기)
    private suspend fun getGuestHomeNews(): Result<List<HomeNewsCardItem>> {
        val categoryIds = onboardingLocalStorage.getSelectedCategoryIds()
        val groupIds = onboardingLocalStorage.getSelectedSubCategoryIds()

        // 관심사 미설정 시 빈 리스트 반환 (API 필수 파라미터 없음)
        if (categoryIds.isEmpty() && groupIds.isEmpty()) {
            return Result.success(emptyList())
        }

        return try {
            val response = api.getTop5News(categoryIds, groupIds)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toHomeNewsCardItem() })
            } else {
                Result.failure(Exception("비회원 뉴스 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// DTO → UI 모델 매핑 (회원/비회원 공통)
private fun RecommendedNewsResponse.toHomeNewsCardItem() = HomeNewsCardItem(
    newsId = id,
    category = categoryName,
    subCategory = "",
    title = title,
    notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
    summaryPoints = summaryList,
    insight = bodyPreview,
    articleCount = sourceCount,
    updatedAt = publishedDate
)
