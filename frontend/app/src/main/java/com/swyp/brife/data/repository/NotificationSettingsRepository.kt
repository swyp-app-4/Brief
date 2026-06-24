package com.swyp.brife.data.repository

import com.swyp.brife.data.local.AuthLocalStorage
import com.swyp.brife.data.model.FcmTokenRequest
import com.swyp.brife.data.model.NotificationSettingsRequest
import com.swyp.brife.data.model.NotificationSettingsResponse
import com.swyp.brife.data.remote.api.NotificationSettingsApiService

class NotificationSettingsRepository(
    private val api: NotificationSettingsApiService,
    private val authLocalStorage: AuthLocalStorage
) {
    private fun bearerToken(): String? =
        authLocalStorage.getAccessToken()?.let { "Bearer $it" }

    suspend fun getNotificationSettings(): Result<NotificationSettingsResponse> {
        val token = bearerToken()
            ?: return Result.failure(Exception("로그인이 필요합니다."))

        return try {
            val response = api.getNotificationSettings(token)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("알림 설정 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNotificationSettings(
        request: NotificationSettingsRequest
    ): Result<NotificationSettingsResponse> {
        val token = bearerToken()
            ?: return Result.failure(Exception("로그인이 필요합니다."))

        return try {
            val response = api.updateNotificationSettings(token, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("알림 설정 변경 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerFcmToken(fcmToken: String): Result<Unit> {
        val token = bearerToken()
            ?: return Result.failure(Exception("로그인이 필요합니다."))

        return try {
            val response = api.registerFcmToken(
                authorization = token,
                request = FcmTokenRequest(fcmToken)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("FCM 토큰 등록 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
