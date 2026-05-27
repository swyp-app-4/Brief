package com.swyp.brife.data.repository

import com.swyp.brife.data.local.AuthLocalStorage
import com.swyp.brife.data.local.OnboardingLocalStorage
import com.swyp.brife.data.model.RecommendedNewsResponse
import com.swyp.brife.data.remote.api.HomeApiService
import com.swyp.brife.feature.home.HomeNewsCardItem
import android.util.Log
import com.swyp.brife.data.model.ReissueRequest
import com.swyp.brife.data.remote.api.AuthApiService

class HomeRepository(
    private val api: HomeApiService,
    private val authApi: AuthApiService,
    private val authLocalStorage: AuthLocalStorage,
    private val onboardingLocalStorage: OnboardingLocalStorage
) {
    private fun bearerToken(): String? =
        authLocalStorage.getAccessToken()?.let { "Bearer $it" }

    // 로그인 여부에 따라 회원/비회원 API를 자동 분기
    suspend fun getHomeNews(): Result<List<HomeNewsCardItem>> {
        Log.d(
            "HomeNewsDebug",
            "getHomeNews isLoggedIn=${authLocalStorage.isLoggedIn()}, hasAccessToken=${authLocalStorage.getAccessToken() != null}"
        )
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
                val mapped = response.body()!!.map { it.toHomeNewsCardItem() }
                return Result.success(mapped)
            }

            if (response.code() == 401) {
                Log.d("AuthReissue", "home initial response 401, reissue will start")
                val newAccessToken = reissueAccessToken()
                    ?: return Result.failure(Exception("토큰 재발급 실패"))

                val retryResponse = api.getRecommendedNews("Bearer $newAccessToken")
                Log.d(
                    "AuthReissue",
                    "home retry response code=${retryResponse.code()}, success=${retryResponse.isSuccessful}"
                )
                if (retryResponse.isSuccessful && retryResponse.body() != null) {
                    val mapped = retryResponse.body()!!.map { it.toHomeNewsCardItem() }
                    Result.success(mapped)
                } else {
                    Result.failure(Exception("추천 뉴스 재조회 실패: ${retryResponse.code()}"))
                }
            } else {
                Result.failure(Exception("추천 뉴스 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun reissueAccessToken(): String? {
        val refreshToken = authLocalStorage.getRefreshToken()
            ?: run {
                authLocalStorage.clearAuthOnly()
                return null
            }

        return try {
            Log.d("AuthReissue", "home reissue start hasRefreshToken=${refreshToken.isNotBlank()}")

            val response = authApi.reissueToken(ReissueRequest(refreshToken))

            Log.d("AuthReissue", "home reissue response code=${response.code()}, success=${response.isSuccessful}")
            if (response.isSuccessful && response.body() != null) {
                val newAccessToken = response.body()!!.accessToken
                authLocalStorage.saveAccessToken(newAccessToken)
                newAccessToken
            } else {
                authLocalStorage.clearAuthOnly()
                null
            }
        } catch (e: Exception) {
            authLocalStorage.clearAuthOnly()
            null
        }
    }


    // 비회원: GET /news/top5 (OnboardingLocalStorage에서 관심사 읽기)
    private suspend fun getGuestHomeNews(): Result<List<HomeNewsCardItem>> {
        val groupIds = onboardingLocalStorage.getSelectedCategoryIds()
        val categoryIds = onboardingLocalStorage.getSelectedSubCategoryIds()

        Log.d(
            "HomeNewsDebug",
            "getGuestHomeNews groupIds=$groupIds, categoryIds=$categoryIds"
        )

        if (categoryIds.isEmpty() && groupIds.isEmpty()) {
            Log.d("HomeNewsDebug", "getGuestHomeNews skipped: empty interests")
            return Result.success(emptyList())
        }

        return try {
            val response = api.getTop5News(categoryIds, groupIds)

            Log.d(
                "HomeNewsDebug",
                "getGuestHomeNews response code=${response.code()}, bodySize=${response.body()?.size}"
            )

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
