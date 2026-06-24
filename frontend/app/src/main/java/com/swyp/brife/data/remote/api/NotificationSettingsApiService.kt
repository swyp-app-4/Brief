package com.swyp.brife.data.remote.api

import com.swyp.brife.data.model.FcmTokenRequest
import com.swyp.brife.data.model.NotificationSettingsRequest
import com.swyp.brife.data.model.NotificationSettingsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface NotificationSettingsApiService {

    @GET("notifications/settings")
    suspend fun getNotificationSettings(
        @Header("Authorization") authorization: String
    ): Response<NotificationSettingsResponse>

    @PATCH("notifications/settings")
    suspend fun updateNotificationSettings(
        @Header("Authorization") authorization: String,
        @Body request: NotificationSettingsRequest
    ): Response<NotificationSettingsResponse>

    @POST("notifications/fcm-token")
    suspend fun registerFcmToken(
        @Header("Authorization") authorization: String,
        @Body request: FcmTokenRequest
    ): Response<Unit>
}
