package com.example.brife.feature.widget

import com.example.brife.data.local.OnboardingLocalStorage
import com.example.brife.data.model.RecommendedNewsResponse
import com.example.brife.data.remote.api.NewsApiService

class WidgetNewsRepository(
    private val api: NewsApiService,
    private val onboardingLocalStorage: OnboardingLocalStorage
) {
    // RemoteViewsFactory.onDataSetChanged()에서 호출 (background thread)
    // .execute()를 사용하는 블로킹 호출 — suspend 불가 환경
    fun fetchTop5(): List<RecommendedNewsResponse> {
        val categoryIds = onboardingLocalStorage.getSelectedSubCategoryIds()
        val groupIds = onboardingLocalStorage.getSelectedCategoryIds()

        // API 필수 조건: categoryIds/groupIds 중 최소 하나 필요
        if (categoryIds.isEmpty() && groupIds.isEmpty()) return emptyList()

        return try {
            val response = api.getTop5News(categoryIds, groupIds).execute()
            if (response.isSuccessful) response.body() ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
