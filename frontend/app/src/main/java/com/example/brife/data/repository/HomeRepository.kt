package com.example.brife.data.repository

import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.model.RecommendedNewsResponse
import com.example.brife.data.remote.api.HomeApiService
import com.example.brife.feature.home.HomeNewsCardItem

class HomeRepository(
    private val api: HomeApiService,
    private val authLocalStorage: AuthLocalStorage
) {
    private fun bearerToken(): String? =
        authLocalStorage.getAccessToken()?.let { "Bearer $it" }

    // GET /home/news/recommended
    suspend fun getRecommendedNews(): Result<List<HomeNewsCardItem>> {
        val token = bearerToken()
            ?: return Result.failure(Exception("로그인이 필요합니다."))
        return try {
            val response = api.getRecommendedNews(token)
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.map { it.toHomeNewsCardItem() }
                Result.success(items)
            } else {
                Result.failure(Exception("추천 뉴스 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// DTO → UI 모델 매핑
// - subCategory, companyName, relatedArticles: API 미제공 → 기본값 사용
// - notice: API 미제공 → 고정 문자열 사용
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
