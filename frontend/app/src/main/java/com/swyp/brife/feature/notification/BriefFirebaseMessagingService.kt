package com.swyp.brife.feature.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BriefFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FcmTokenRegistrar.registerTokenIfLoggedIn(this, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Brief"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: ""
        val newsId = remoteMessage.data["newsId"]?.toLongOrNull()
        val primaryNewsId = remoteMessage.data["primaryNewsId"]?.toLongOrNull()

        BriefNotificationHelper.showPushNotification(
            context = this,
            title = title,
            body = body,
            newsId = newsId,
            primaryNewsId = primaryNewsId
        )
    }
}
