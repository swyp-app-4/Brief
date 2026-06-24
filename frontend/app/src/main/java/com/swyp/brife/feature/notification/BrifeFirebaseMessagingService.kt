package com.swyp.brife.feature.notification

import com.google.firebase.messaging.FirebaseMessagingService

class BrifeFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FcmTokenRegistrar.registerTokenIfLoggedIn(this, token)
    }
}
