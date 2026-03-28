package com.example.brife.data.repository

import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.model.InterestRequest
import com.example.brife.data.model.UpdateProfileRequest
import com.example.brife.data.model.UserProfileResponse
import com.example.brife.data.remote.api.UserApiService

class UserRepository(
    private val api: UserApiService,
    private val authLocalStorage: AuthLocalStorage
) {
    private fun bearerToken(): String? =
        authLocalStorage.getAccessToken()?.let { "Bearer $it" }

    // GET /users/me
    suspend fun getMyProfile(): Result<UserProfileResponse> {
        val token = bearerToken()
            ?: return Result.failure(Exception("로그인이 필요합니다."))
        return try {
            val response = api.getMyProfile(token)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("프로필 조회 실패: ${response.code()}"))
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
