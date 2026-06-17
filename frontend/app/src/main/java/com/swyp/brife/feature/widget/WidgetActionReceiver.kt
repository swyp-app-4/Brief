package com.swyp.brife.feature.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.swyp.brife.MainActivity
import com.swyp.brife.data.local.AuthLocalStorage

/**
 * 위젯 카드 클릭 이벤트를 처리하는 BroadcastReceiver.
 *
 * [사용되는 PendingIntent 흐름]
 * 1. BrifeWidgetReceiver 에서 AdapterViewFlipper 에 setPendingIntentTemplate() 설정 (이 Receiver 대상)
 * 2. BrifeWidgetFactory.getViewAt() 에서 각 카드 view 에 setOnClickFillInIntent() 로
 *    EXTRA_ACTION / EXTRA_NEWS_ID extra 주입
 * 3. 사용자가 클릭하면 template + fillIn 이 합쳐진 Intent 로 이 Receiver 가 호출됨
 */
class WidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra(EXTRA_ACTION) ?: run {
            Log.d(TAG, "ignored widget click: missing action")
            return
        }
        val newsId = intent.getLongExtra(EXTRA_NEWS_ID, -1L)
        // newsId=0은 mock 데이터(API 미로드 상태) → 유효하지 않은 ID이므로 차단
        if (newsId <= 0L) {
            Log.d(TAG, "ignored widget click: invalid newsId=$newsId action=$action")
            return
        }

        Log.d(TAG, "received widget click: action=$action, newsId=$newsId")

        when (action) {

            // ── 카드 본문 클릭 → 앱 뉴스 상세 화면 이동 ─────────────────────────
            ACTION_OPEN_NEWS -> {
                Log.d(TAG, "open news from widget: newsId=$newsId")
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    data = Uri.parse("https://brife.app/news/$newsId")
                }
                context.startActivity(launchIntent)
            }

            // ── 북마크 버튼 클릭 → 로그인 확인 후 앱 뉴스 상세 + 북마크 시트 자동 오픈 ─
            ACTION_BOOKMARK -> {
                if (!AuthLocalStorage(context).isLoggedIn()) return

                Log.d(TAG, "open bookmark from widget: newsId=$newsId")
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    data = Uri.parse("https://brife.app/news/$newsId")
                    putExtra(EXTRA_OPEN_BOOKMARK, true)
                }
                context.startActivity(launchIntent)
            }
        }
    }

    companion object {
        const val ACTION_OPEN_NEWS = "ACTION_OPEN_NEWS"
        const val ACTION_BOOKMARK = "ACTION_BOOKMARK"
        const val EXTRA_ACTION = "extra_action"
        const val EXTRA_NEWS_ID = "extra_news_id"
        const val EXTRA_OPEN_BOOKMARK = "open_bookmark"

        private const val PREFS_NAME = "widget_prefs"
        private const val KEY_BOOKMARKED = "bookmarked_ids"
        private const val TAG = "WidgetActionReceiver"

        /** 로컬에 저장된 북마크된 newsId 목록 반환 (위젯 이미지 전환용) */
        fun getBookmarkedIds(context: Context): Set<Long> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getStringSet(KEY_BOOKMARKED, emptySet())
                ?.mapNotNull { it.toLongOrNull() }
                ?.toSet()
                ?: emptySet()
        }

        /** 앱에서 보관함 저장 완료 후 위젯 북마크 상태를 active로 동기화 */
        fun saveBookmarkedId(context: Context, newsId: Long) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val current = prefs.getStringSet(KEY_BOOKMARKED, emptySet())?.toMutableSet() ?: mutableSetOf()
            current.add(newsId.toString())
            prefs.edit().putStringSet(KEY_BOOKMARKED, current).apply()
        }

        fun removeBookmarkedId(context: Context, newsId: Long) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val current = prefs.getStringSet(KEY_BOOKMARKED, emptySet())?.toMutableSet() ?: mutableSetOf()
            current.remove(newsId.toString())
            prefs.edit().putStringSet(KEY_BOOKMARKED, current).apply()
        }

        fun clearBookmarkedIds(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_BOOKMARKED)
                .apply()
        }
    }
}
