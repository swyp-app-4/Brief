package com.swyp.brife.data.repository

import com.swyp.brife.data.local.AuthLocalStorage
import com.swyp.brife.data.local.OnboardingLocalStorage
import com.swyp.brife.data.model.RecommendedNewsResponse
import com.swyp.brife.data.remote.api.HomeApiService
import com.swyp.brife.feature.home.HomeNewsCardItem

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
                val items = response.body()!!
                items.firstOrNull()?.let {
                }
                val mapped = items.map { it.toHomeNewsCardItem() }
                Result.success(mapped)
            } else {
                Result.failure(Exception("추천 뉴스 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 비회원: GET /news/top5 (OnboardingLocalStorage에서 관심사 읽기)
    private suspend fun getGuestHomeNews(): Result<List<HomeNewsCardItem>> {
        val groupIds = onboardingLocalStorage.getSelectedCategoryIds()       // 부모(대분류)
        val categoryIds = onboardingLocalStorage.getSelectedSubCategoryIds() // 소분류


        if (categoryIds.isEmpty() && groupIds.isEmpty()) {
            return Result.success(emptyList())
        }

        return try {
            val response = api.getTop5News(categoryIds, groupIds)
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!
                items.firstOrNull()?.let {

                }
                val mapped = items.map { it.toHomeNewsCardItem() }
                Result.success(mapped)
            } else {
                Result.failure(Exception("비회원 뉴스 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
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
