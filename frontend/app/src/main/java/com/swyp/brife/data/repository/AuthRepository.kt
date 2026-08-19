package com.swyp.brife.data.repository

import com.google.gson.Gson
import com.swyp.brife.data.model.ApiErrorResponse
import com.swyp.brife.data.model.GoogleLoginRequest
import com.swyp.brife.data.model.LoginResponse
import com.swyp.brife.data.model.LogoutRequest
import com.swyp.brife.data.model.ReissueRequest
import com.swyp.brife.data.model.ReissueResponse
import com.swyp.brife.data.model.SocialAccessTokenRequest
import com.swyp.brife.data.model.TermsRequest
import com.swyp.brife.data.remote.api.AuthApiService
import retrofit2.Response

class AuthRepository(
    private val api: AuthApiService
) {
    suspend fun loginWithKakao(accessToken: String): Result<LoginResponse> {
        return try {
            val response = api.loginWithKakao(SocialAccessTokenRequest(accessToken))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                socialLoginFailure("카카오", response)
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
                socialLoginFailure("네이버", response)
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
                socialLoginFailure("구글", response)
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

    private fun socialLoginFailure(
        providerName: String,
        response: Response<LoginResponse>
    ): Result<LoginResponse> {
        val apiError = runCatching {
            response.errorBody()?.string()?.let {
                Gson().fromJson(it, ApiErrorResponse::class.java)
            }
        }.getOrNull()

        val message = if (apiError?.code == "ACCOUNT_WITHDRAWN") {
            apiError.message?.takeIf { it.isNotBlank() }
                ?: "탈퇴 후 30일 동안 로그인하거나 재가입할 수 없습니다."
        } else {
            "$providerName 로그인 실패: ${response.code()}"
        }

        return Result.failure(Exception(message))
    }
}
