package com.swyp.brife.feature.notification

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.swyp.brife.data.local.AuthLocalStorage
import com.swyp.brife.data.remote.NetworkModule
import com.swyp.brife.data.repository.NotificationSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object FcmTokenRegistrar {
    private const val TAG = "FcmTokenRegistrar"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun registerCurrentTokenIfLoggedIn(context: Context) {
        val appContext = context.applicationContext
        val authLocalStorage = AuthLocalStorage(appContext)
        if (!authLocalStorage.isLoggedIn()) return

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                registerTokenIfLoggedIn(appContext, token)
            }
            .addOnFailureListener { throwable ->
                Log.w(TAG, "Failed to get FCM token", throwable)
            }
    }

    fun registerTokenIfLoggedIn(context: Context, fcmToken: String) {
        if (fcmToken.isBlank()) return

        val appContext = context.applicationContext
        val authLocalStorage = AuthLocalStorage(appContext)
        if (!authLocalStorage.isLoggedIn()) return

        val repository = NotificationSettingsRepository(
            api = NetworkModule.notificationSettingsApiService,
            authLocalStorage = authLocalStorage
        )

        scope.launch {
            repository.registerFcmToken(fcmToken)
                .onFailure { throwable ->
                    Log.w(TAG, "Failed to register FCM token", throwable)
                }
        }
    }
}
