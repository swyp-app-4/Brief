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

    companion object {
        const val ACTION_PREV = "com.example.brife.WIDGET_PREV"
        const val ACTION_NEXT = "com.example.brife.WIDGET_NEXT"
        const val EXTRA_WIDGET_ID = "extra_widget_id"

        private const val PREF_NAME = "brife_widget_prefs"
        private const val ITEM_COUNT = 5  // top5 고정

        private fun getPosition(context: Context, widgetId: Int): Int =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt("pos_$widgetId", 0)

        private fun savePosition(context: Context, widgetId: Int, pos: Int) {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putInt("pos_$widgetId", pos).apply()
        }
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
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        when (intent.action) {
            ACTION_PREV -> {
                val current = getPosition(context, appWidgetId)
                if (current > 0) {
                    savePosition(context, appWidgetId, current - 1)
                    updateWidget(context, appWidgetManager, appWidgetId)
                }
            }
            ACTION_NEXT -> {
                val current = getPosition(context, appWidgetId)
                if (current < ITEM_COUNT - 1) {
                    savePosition(context, appWidgetId, current + 1)
                    updateWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val rv = RemoteViews(context.packageName, R.layout.widget_layout)

        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // 카드 목록 어댑터 연결
        rv.setRemoteAdapter(
            R.id.widget_flipper,
            Intent(context, BrifeWidgetService::class.java)
        )

        // 현재 위치의 카드를 표시 (setDisplayedChild via reflection)
        val currentPos = getPosition(context, appWidgetId)
        rv.setInt(R.id.widget_flipper, "setDisplayedChild", currentPos)

        // 카드 클릭(상세이동 / 북마크) 템플릿
        val templateIntent = Intent(context, WidgetActionReceiver::class.java)
        val pendingTemplate = PendingIntent.getBroadcast(context, 0, templateIntent, pendingFlags)
        rv.setPendingIntentTemplate(R.id.widget_flipper, pendingTemplate)

        // ◀ 이전 버튼 클릭 인텐트
        val prevIntent = Intent(context, BrifeWidgetReceiver::class.java).apply {
            action = ACTION_PREV
            putExtra(EXTRA_WIDGET_ID, appWidgetId)
        }
        val prevPending = PendingIntent.getBroadcast(
            context, appWidgetId * 10 + 1, prevIntent, pendingFlags
        )
        rv.setOnClickPendingIntent(R.id.widget_btn_prev, prevPending)

        // ▶ 다음 버튼 클릭 인텐트
        val nextIntent = Intent(context, BrifeWidgetReceiver::class.java).apply {
            action = ACTION_NEXT
            putExtra(EXTRA_WIDGET_ID, appWidgetId)
        }
        val nextPending = PendingIntent.getBroadcast(
            context, appWidgetId * 10 + 2, nextIntent, pendingFlags
        )
        rv.setOnClickPendingIntent(R.id.widget_btn_next, nextPending)

        // 첫 페이지: 이전 버튼 비활성, 마지막 페이지: 다음 버튼 비활성 (알파 30%)
        rv.setInt(R.id.widget_btn_prev, "setAlpha", if (currentPos == 0) 77 else 255)
        rv.setInt(R.id.widget_btn_next, "setAlpha", if (currentPos == ITEM_COUNT - 1) 77 else 255)

        // 페이지 인디케이터 도트 업데이트
        val dotIds = intArrayOf(
            R.id.widget_dot_0, R.id.widget_dot_1, R.id.widget_dot_2,
            R.id.widget_dot_3, R.id.widget_dot_4
        )
        for (i in dotIds.indices) {
            rv.setImageViewResource(
                dotIds[i],
                if (i == currentPos) R.drawable.widget_dot_active else R.drawable.widget_dot_inactive
            )
        }

        // 헤더 북마크: 현재 카드의 북마크 상태 반영
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val currentNewsId = prefs.getLong("newsId_$currentPos", 0L)
        val bookmarkedIds = WidgetActionReceiver.getBookmarkedIds(context)
        rv.setImageViewResource(
            R.id.iv_widget_bookmark,
            if (currentNewsId != 0L && currentNewsId in bookmarkedIds)
                R.drawable.img_widget_bookmark_active
            else
                R.drawable.img_widget_bookmark_inactive
        )

        // 헤더 북마크 클릭 → 현재 카드 북마크 액션
        val bookmarkHeaderIntent = Intent(context, WidgetActionReceiver::class.java).apply {
            putExtra(WidgetActionReceiver.EXTRA_ACTION, WidgetActionReceiver.ACTION_BOOKMARK)
            putExtra(WidgetActionReceiver.EXTRA_NEWS_ID, currentNewsId)
        }
        val bookmarkHeaderPending = PendingIntent.getBroadcast(
            context, appWidgetId * 10 + 3, bookmarkHeaderIntent, pendingFlags
        )
        rv.setOnClickPendingIntent(R.id.iv_widget_bookmark, bookmarkHeaderPending)

        appWidgetManager.updateAppWidget(appWidgetId, rv)
    }
}
