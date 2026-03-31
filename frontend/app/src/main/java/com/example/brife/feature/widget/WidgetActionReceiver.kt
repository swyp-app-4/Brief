package com.example.brife.feature.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.brife.MainActivity
import com.example.brife.R
import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.model.AddArchiveItemRequest
import com.example.brife.data.remote.NetworkModule
import kotlinx.coroutines.runBlocking

/**
 * 위젯 카드 클릭 이벤트를 처리하는 BroadcastReceiver.
 *
 * [사용되는 PendingIntent 흐름]
 * 1. BrifeWidget.kt 에서 StackView 에 setPendingIntentTemplate() 설정 (이 Receiver 대상)
 * 2. BrifeWidgetFactory.getViewAt() 에서 각 카드 view 에 setOnClickFillInIntent() 로
 *    EXTRA_ACTION / EXTRA_NEWS_ID extra 주입
 * 3. 사용자가 클릭하면 template + fillIn 이 합쳐진 Intent 로 이 Receiver 가 호출됨
 */
class WidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra(EXTRA_ACTION) ?: return
        val newsId = intent.getLongExtra(EXTRA_NEWS_ID, -1L)
        if (newsId == -1L) return

        when (action) {

            // ── 카드 본문 클릭 → 앱 뉴스 상세 화면 이동 ─────────────────────────
            ACTION_OPEN_NEWS -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    // MainActivity.extractNewsIdFromIntent() 가 파싱하는 형식 그대로 사용
                    data = Uri.parse("https://brife.app/news/$newsId")
                }
                context.startActivity(launchIntent)
            }

            // ── 북마크 버튼 클릭 → 즐겨찾기 API 호출 + 아이콘 즉시 토글 ─────────
            ACTION_BOOKMARK -> {
                // goAsync(): BroadcastReceiver 의 onReceive 종료 후에도 작업 유지
                val pendingResult = goAsync()
                Thread {
                    try {
                        val token = AuthLocalStorage(context)
                            .getAccessToken()?.let { "Bearer $it" }
                        if (token != null) {
                            runBlocking {
                                // POST archives/favorite/items
                                NetworkModule.archiveApiService.addToFavorites(
                                    token,
                                    AddArchiveItemRequest(newsId)
                                )
                            }
                        }
                        // API 성공 여부에 무관하게 로컬 상태 즉시 토글 (낙관적 업데이트)
                        toggleBookmark(context, newsId)

                        // 위젯 카드 목록 새로고침 (북마크 아이콘 상태 반영)
                        val manager = AppWidgetManager.getInstance(context)
                        val ids = manager.getAppWidgetIds(
                            ComponentName(context, BrifeWidgetReceiver::class.java)
                        )
                        manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_stack_view)
                    } finally {
                        pendingResult.finish()
                    }
                }.start()
            }
        }
    }

    companion object {
        const val ACTION_OPEN_NEWS = "ACTION_OPEN_NEWS"
        const val ACTION_BOOKMARK = "ACTION_BOOKMARK"
        const val EXTRA_ACTION = "extra_action"
        const val EXTRA_NEWS_ID = "extra_news_id"

        private const val PREFS_NAME = "widget_prefs"
        private const val KEY_BOOKMARKED = "bookmarked_ids"

        /** 로컬에 저장된 북마크된 newsId 목록 반환 */
        fun getBookmarkedIds(context: Context): Set<Long> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getStringSet(KEY_BOOKMARKED, emptySet())
                ?.mapNotNull { it.toLongOrNull() }
                ?.toSet()
                ?: emptySet()
        }

        private fun toggleBookmark(context: Context, newsId: Long) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val current = prefs.getStringSet(KEY_BOOKMARKED, mutableSetOf())
                ?.toMutableSet() ?: mutableSetOf()
            val idStr = newsId.toString()
            if (current.contains(idStr)) current.remove(idStr) else current.add(idStr)
            prefs.edit().putStringSet(KEY_BOOKMARKED, current).apply()
        }
    }
}
