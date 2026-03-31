package com.example.brife.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.brife.R

class BrifeWidgetReceiver : AppWidgetProvider() {

    companion object {
        const val ACTION_SHOW_NEXT = "com.example.brife.widget.ACTION_SHOW_NEXT"
        const val ACTION_SHOW_PREV = "com.example.brife.widget.ACTION_SHOW_PREV"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_SHOW_NEXT -> flipCard(context, next = true)
            ACTION_SHOW_PREV -> flipCard(context, next = false)
        }
    }

    private fun flipCard(context: Context, next: Boolean) {
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(ComponentName(context, BrifeWidgetReceiver::class.java))
        val rv = RemoteViews(context.packageName, R.layout.widget_layout)
        if (next) rv.showNext(R.id.widget_flipper) else rv.showPrevious(R.id.widget_flipper)
        mgr.partiallyUpdateAppWidget(ids, rv)
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val rv = RemoteViews(context.packageName, R.layout.widget_layout)

        // AdapterViewFlipper에 RemoteAdapter 연결 — 루트 RemoteViews에서 직접 호출
        rv.setRemoteAdapter(
            R.id.widget_flipper,
            Intent(context, BrifeWidgetService::class.java)
        )

        // 카드 클릭 PendingIntentTemplate
        val templateIntent = Intent(context, WidgetActionReceiver::class.java)
        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingTemplate = PendingIntent.getBroadcast(context, 0, templateIntent, pendingFlags)
        rv.setPendingIntentTemplate(R.id.widget_flipper, pendingTemplate)

        // ‹ 이전 버튼
        val prevIntent = Intent(context, BrifeWidgetReceiver::class.java).apply {
            action = ACTION_SHOW_PREV
        }
        rv.setOnClickPendingIntent(
            R.id.widget_btn_prev,
            PendingIntent.getBroadcast(context, 1, prevIntent, pendingFlags)
        )

        // › 다음 버튼
        val nextIntent = Intent(context, BrifeWidgetReceiver::class.java).apply {
            action = ACTION_SHOW_NEXT
        }
        rv.setOnClickPendingIntent(
            R.id.widget_btn_next,
            PendingIntent.getBroadcast(context, 2, nextIntent, pendingFlags)
        )

        appWidgetManager.updateAppWidget(appWidgetId, rv)
    }
}
