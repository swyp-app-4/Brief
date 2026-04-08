package com.swyp.brife.data.repository

import com.swyp.brife.data.local.AuthLocalStorage
import com.swyp.brife.data.model.InterestRequest
import com.swyp.brife.data.model.UpdateProfileRequest
import com.swyp.brife.data.model.UserProfileResponse
import com.swyp.brife.data.remote.api.UserApiService

class UserRepository(
    private val api: UserApiService,
    private val authLocalStorage: AuthLocalStorage
) {
    private fun bearerToken(): String? =
        authLocalStorage.getAccessToken()?.let { "Bearer $it" }

    private fun bearerToken(accessToken: String): String = "Bearer $accessToken"

    // 토큰 존재 여부로 로그인 상태 판단
    fun isLoggedIn(): Boolean = authLocalStorage.isLoggedIn()

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
