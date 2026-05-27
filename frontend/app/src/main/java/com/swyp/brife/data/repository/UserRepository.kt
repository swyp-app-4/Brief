package com.swyp.brife.data.repository

import com.swyp.brife.data.local.AuthLocalStorage
import com.swyp.brife.data.model.InterestRequest
import com.swyp.brife.data.model.UpdateProfileRequest
import com.swyp.brife.data.model.UserProfileResponse
import com.swyp.brife.data.remote.api.UserApiService
import android.util.Log
import com.swyp.brife.data.remote.api.AuthApiService
import com.swyp.brife.data.model.ReissueRequest

class UserRepository(
    private val api: UserApiService,
    private val authApi: AuthApiService,
    private val authLocalStorage: AuthLocalStorage
) {
    private fun bearerToken(): String? =
        authLocalStorage.getAccessToken()?.let { "Bearer $it" }

    private fun bearerToken(accessToken: String): String = "Bearer $accessToken"

    // 토큰 존재 여부로 로그인 상태 판단
    fun isLoggedIn(): Boolean = authLocalStorage.isLoggedIn()

    // GET /users/me
    // GET /users/me


    private suspend fun reissueAccessToken(): String? {
        val refreshToken = authLocalStorage.getRefreshToken()
            ?: run {
                authLocalStorage.clearAuthOnly()
                return null
            }

        return try {
            Log.d("AuthReissue", "user reissue start hasRefreshToken=${refreshToken.isNotBlank()}")
            val response = authApi.reissueToken(ReissueRequest(refreshToken))
            Log.d("AuthReissue", "user reissue response code=${response.code()}, success=${response.isSuccessful}")
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




    suspend fun getMyProfile(): Result<UserProfileResponse> {
        val token = bearerToken()
            ?: return Result.failure(Exception("로그인이 필요합니다."))

        return try {
            val response = api.getMyProfile(token)

            if (response.isSuccessful && response.body() != null) {
                return Result.success(response.body()!!)
            }

            if (response.code() == 401) {
                Log.d("AuthReissue", "user initial response 401, reissue will start")
                val newAccessToken = reissueAccessToken()
                    ?: return Result.failure(Exception("토큰 재발급 실패"))

                val retryResponse = api.getMyProfile("Bearer $newAccessToken")
                Log.d(
                    "AuthReissue",
                    "user retry response code=${retryResponse.code()}, success=${retryResponse.isSuccessful}"
                )
                if (retryResponse.isSuccessful && retryResponse.body() != null) {
                    Result.success(retryResponse.body()!!)
                } else {
                    Result.failure(Exception("프로필 재조회 실패: ${retryResponse.code()}"))
                }
            } else {
                Result.failure(Exception("프로필 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // POST /users/me/interests — 신규회원 최초 관심사 저장 (accessToken 직접 지정)
    suspend fun saveInterests(
        accessToken: String,
        categoryIds: List<Long>,
        groupIds: List<Long>
    ): Result<Unit> {
        return try {
            val response = api.saveInterests(
                authorization = bearerToken(accessToken),
                request = InterestRequest(categoryIds = categoryIds, groupIds = groupIds)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("관심사 저장 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // PUT /users/me/interests — 관심사 재설정
    suspend fun updateInterests(categoryIds: List<Long>, groupIds: List<Long>): Result<Unit> {
        val token = bearerToken()
            ?: return Result.failure(Exception("로그인이 필요합니다."))
        return try {
            val response = api.updateInterests(
                authorization = token,
                request = InterestRequest(categoryIds = categoryIds, groupIds = groupIds)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("관심사 업데이트 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // PUT /users/me/interests — 기존회원 로그인 직후 (accessToken 직접 전달)
    suspend fun updateInterests(
        accessToken: String,
        categoryIds: List<Long>,
        groupIds: List<Long>
    ): Result<Unit> {
        return try {
            val response = api.updateInterests(
                authorization = bearerToken(accessToken),
                request = InterestRequest(categoryIds = categoryIds, groupIds = groupIds)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("관심사 업데이트 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(): Result<Unit> {
        val token = bearerToken()
            ?: return Result.failure(Exception("로그인이 필요합니다."))
        return try {
            val response = api.deleteUser(token)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("회원 탈퇴 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // PATCH /users/me — 프로필 수정 (닉네임, 이미지) — UI 연결은 추후 진행
    suspend fun updateProfile(nickname: String? = null, profileImageUrl: String? = null): Result<Unit> {
        val token = bearerToken()
            ?: return Result.failure(Exception("로그인이 필요합니다."))
        return try {
            val response = api.updateProfile(
                authorization = token,
                request = UpdateProfileRequest(nickname = nickname, profileImageUrl = profileImageUrl)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("프로필 수정 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
