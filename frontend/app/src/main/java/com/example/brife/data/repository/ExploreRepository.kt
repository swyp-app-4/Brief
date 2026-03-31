package com.example.brife.data.repository

import android.util.Log
import com.example.brife.data.model.NewsListItem
import com.example.brife.data.remote.api.ExploreApiService

class ExploreRepository(private val api: ExploreApiService) {

    // GET /news/latest — page=0, size=20 고정 (1차 연동)
    suspend fun getLatestNews(): Result<List<NewsListItem>> {
        return try {
            val response = api.getLatestNews()
            Log.d("ExploreRepository", "getLatestNews: code=${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.content
                Log.d("ExploreRepository", "getLatestNews: count=${items.size}")
                if (items.isNotEmpty()) {
                    val first = items.first()
                    Log.d("ExploreRepository", "getLatestNews: first={id=${first.id}, categoryName=${first.categoryName}, title=${first.title}, publishedDate=${first.publishedDate}}")
                }
                Result.success(items)
            } else {
                Log.e("ExploreRepository", "getLatestNews: 실패 code=${response.code()}")
                Result.failure(Exception("최근 뉴스 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ExploreRepository", "getLatestNews: exception=${e.message}")
            Result.failure(e)
        }
    }

    // GET /news/search — page=0, size=20 고정 (1차 연동)
    suspend fun searchNews(keyword: String): Result<List<NewsListItem>> {
        return try {
            val response = api.searchNews(keyword)
            Log.d("ExploreRepository", "searchNews: keyword=$keyword, code=${response.code()}")
            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.content
                Log.d("ExploreRepository", "searchNews: count=${items.size}")
                if (items.isNotEmpty()) {
                    val first = items.first()
                    Log.d("ExploreRepository", "searchNews: first={id=${first.id}, categoryName=${first.categoryName}, title=${first.title}, publishedDate=${first.publishedDate}}")
                }
                Result.success(items)
            } else {
                Log.e("ExploreRepository", "searchNews: 실패 code=${response.code()}")
                Result.failure(Exception("검색 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("ExploreRepository", "searchNews: exception=${e.message}")
            Result.failure(e)
        }
    }
}
