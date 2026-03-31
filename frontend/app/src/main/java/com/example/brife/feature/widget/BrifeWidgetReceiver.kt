package com.example.brife.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.brife.R

class BrifeWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val rv = RemoteViews(context.packageName, R.layout.widget_layout)

        rv.setRemoteAdapter(
            R.id.widget_stack_view,
            Intent(context, BrifeWidgetService::class.java)
        )

        val templateIntent = Intent(context, WidgetActionReceiver::class.java)
        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingTemplate = PendingIntent.getBroadcast(context, 0, templateIntent, pendingFlags)
        rv.setPendingIntentTemplate(R.id.widget_stack_view, pendingTemplate)

        appWidgetManager.updateAppWidget(appWidgetId, rv)
    }
}
