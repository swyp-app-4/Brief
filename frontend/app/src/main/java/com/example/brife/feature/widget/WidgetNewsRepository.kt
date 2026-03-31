package com.example.brife.feature.widget

import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.local.OnboardingLocalStorage
import com.example.brife.data.model.RecommendedNewsResponse
import com.example.brife.data.remote.api.HomeApiService
import com.example.brife.data.remote.api.NewsApiService

class WidgetNewsRepository(
    private val newsApi: NewsApiService,
    private val homeApi: HomeApiService,
    private val onboardingLocalStorage: OnboardingLocalStorage,
    private val authLocalStorage: AuthLocalStorage
) {
    /**
     * TOP5 뉴스를 가져온다.
     * - 로그인 상태: 회원 맞춤 API (/home/news/recommended) 우선
     * - 비로그인 또는 회원 API 실패: 관심사 기반 비회원 API (/news/top5) 사용
     * RemoteViewsFactory.onDataSetChanged()에서 호출되므로 blocking 호출 사용.
     */
    fun fetchTop5(): List<RecommendedNewsResponse> {
        return if (authLocalStorage.isLoggedIn()) {
            fetchMemberNews()
        } else {
            fetchGuestNews()
        }
    }

    // 회원: GET /home/news/recommended (JWT 필수)
    private fun fetchMemberNews(): List<RecommendedNewsResponse> {
        val token = authLocalStorage.getAccessToken()?.let { "Bearer $it" }
            ?: return fetchGuestNews()
        return try {
            val response = homeApi.getRecommendedNewsCall(token).execute()
            if (response.isSuccessful) response.body() ?: fetchGuestNews()
            else fetchGuestNews()
        } catch (e: Exception) {
            fetchGuestNews()
        }
    }

    // 비회원: GET /news/top5 (관심사 기반)
    private fun fetchGuestNews(): List<RecommendedNewsResponse> {
        val categoryIds = onboardingLocalStorage.getSelectedSubCategoryIds()
        val groupIds = onboardingLocalStorage.getSelectedCategoryIds()
        if (categoryIds.isEmpty() && groupIds.isEmpty()) return emptyList()
        return try {
            val response = newsApi.getTop5News(categoryIds, groupIds).execute()
            if (response.isSuccessful) response.body() ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
