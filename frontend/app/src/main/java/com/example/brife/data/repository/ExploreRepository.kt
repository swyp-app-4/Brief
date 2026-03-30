package com.example.brife.data.repository

import com.example.brife.data.model.NewsListItem
import com.example.brife.data.remote.api.ExploreApiService

class ExploreRepository(private val api: ExploreApiService) {

    // GET /news/latest — page=0, size=20 고정 (1차 연동)
    suspend fun getLatestNews(): Result<List<NewsListItem>> {
        return try {
            val response = api.getLatestNews()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.content)
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
                Result.success(response.body()!!.content)
            } else {
                Result.failure(Exception("검색 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
