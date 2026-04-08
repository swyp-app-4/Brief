package com.swyp.brife.data.repository

import com.swyp.brife.data.model.NewsDetailResponse
import com.swyp.brife.data.model.NewsListItem
import com.swyp.brife.data.remote.api.ExploreApiService
import com.swyp.brife.data.remote.api.NewsApiService

class ExploreRepository(
    private val api: ExploreApiService,
    private val newsApi: NewsApiService
) {

    // GET /news/latest — page=0, size=20 고정 (1차 연동)
    suspend fun getLatestNews(): Result<List<NewsListItem>> {
        return try {
            val response = api.getLatestNews()
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.content
                if (items.isNotEmpty()) {
                    val first = items.first()
                }
                Result.success(items)
            } else {
                Result.failure(Exception("최근 뉴스 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // GET /news/search — page=0, size=20 고정 (1차 연동)
    suspend fun searchNews(keyword: String): Result<List<NewsListItem>> {
        return try {
            val response = api.searchNews(keyword)
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.content
                if (items.isNotEmpty()) {
                    val first = items.first()
                }
                Result.success(items)
            } else {
                Result.failure(Exception("검색 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNewsDetail(id: Long): Result<NewsDetailResponse> {
        return try {
            val response = newsApi.getNewsDetailAsync(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("뉴스 상세 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
