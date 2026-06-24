package com.swyp.brife.feature.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.swyp.brife.MainActivity
import com.swyp.brife.R

object BriefNotificationHelper {
    private const val TAG = "BriefNotificationHelper"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            BriefNotificationConstants.CHANNEL_ID,
            BriefNotificationConstants.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showPushNotification(
        context: Context,
        title: String,
        body: String,
        newsId: Long?
    ) {
        try {
            val pendingIntent = createContentPendingIntent(context, newsId)
            val notification = NotificationCompat.Builder(
                context,
                BriefNotificationConstants.CHANNEL_ID
            )
                .setSmallIcon(R.mipmap.ic_launcher_b)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            NotificationManagerCompat.from(context).notify(
                System.currentTimeMillis().toInt(),
                notification
            )
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission is not granted", e)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to show push notification", e)
        }
    }

    private fun createContentPendingIntent(
        context: Context,
        newsId: Long?
    ): PendingIntent {
        val intent = if (newsId != null) {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://brife.app/news/$newsId"), context, MainActivity::class.java)
        } else {
            Intent(context, MainActivity::class.java)
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        return PendingIntent.getActivity(
            context,
            newsId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
