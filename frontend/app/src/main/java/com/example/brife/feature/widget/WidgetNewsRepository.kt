package com.example.brife.feature.widget

import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.local.OnboardingLocalStorage
import com.example.brife.data.model.RecommendedNewsResponse
import com.example.brife.data.remote.api.ArchiveApiService
import com.example.brife.data.remote.api.HomeApiService
import com.example.brife.data.remote.api.NewsApiService
import kotlinx.coroutines.runBlocking

class WidgetNewsRepository(
    private val newsApi: NewsApiService,
    private val homeApi: HomeApiService,
    private val archiveApi: ArchiveApiService,
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

    /**
     * 서버에서 저장된 모든 뉴스 ID를 조회한다 (위젯 북마크 active 상태 동기화용).
     * - GET /archives → 각 폴더 ID 수집
     * - GET /archives/{id}/items → contentId 수집
     * onDataSetChanged()에서 호출되므로 runBlocking으로 blocking 실행.
     */
    fun fetchSavedNewsIds(): Set<Long> {
        if (!authLocalStorage.isLoggedIn()) return emptySet()
        val token = authLocalStorage.getAccessToken()?.let { "Bearer $it" } ?: return emptySet()
        return try {
            val foldersResponse = runBlocking { archiveApi.getArchives(token) }
            if (!foldersResponse.isSuccessful) return emptySet()
            val savedIds = mutableSetOf<Long>()
            for (folder in foldersResponse.body() ?: emptyList()) {
                val itemsResponse = runBlocking { archiveApi.getArchiveItems(token, folder.id) }
                if (itemsResponse.isSuccessful) {
                    itemsResponse.body()?.forEach { savedIds.add(it.contentId) }
                }
            }
            savedIds
        } catch (e: Exception) {
            emptySet()
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
