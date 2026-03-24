package com.example.brife.data.repository

import com.example.brife.data.model.GoogleLoginRequest
import com.example.brife.data.model.ReissueRequest
import com.example.brife.data.model.ReissueResponse
import com.example.brife.data.model.SocialAccessTokenRequest
import com.example.brife.data.model.TermsRequest
import com.example.brife.data.remote.api.AuthApiService

class AuthRepository(
    private val api: AuthApiService
) {
    suspend fun loginWithKakao(accessToken: String): Result<LoginResponse> {
        return try {
            val response = api.loginWithKakao(SocialAccessTokenRequest(accessToken))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("카카오 로그인 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithNaver(accessToken: String): Result<LoginResponse> {
        return try {
            val response = api.loginWithNaver(SocialAccessTokenRequest(accessToken))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("네이버 로그인 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<LoginResponse> {
        return try {
            val response = api.loginWithGoogle(GoogleLoginRequest(idToken))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("구글 로그인 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun agreeTerms(accessToken: String): Result<Unit> {
        return try {
            val response = api.agreeTerms(
                authorization = "Bearer $accessToken",
                request = TermsRequest(
                    serviceTermsAgreed = true,
                    privacyTermsAgreed = true
                )
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("약관 동의 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reissueToken(refreshToken: String): Result<ReissueResponse> {
        return try {
            val response = api.reissueToken(ReissueRequest(refreshToken))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("토큰 재발급 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(refreshToken: String): Result<Unit> {
        return try {
            val response = api.logout(LogoutRequest(refreshToken))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("로그아웃 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}