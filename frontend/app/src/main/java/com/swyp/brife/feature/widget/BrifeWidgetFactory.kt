package com.swyp.brife.feature.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.swyp.brife.R
import com.swyp.brife.data.local.AuthLocalStorage
import com.swyp.brife.data.local.OnboardingLocalStorage
import com.swyp.brife.data.remote.NetworkModule

class BrifeWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private data class WidgetNews(
        val newsId: Long,
        val title: String,
        val summary1: String,
        val summary2: String,
        val summary3: String,
        val summary4: String
    )

    // API 실패 또는 관심사 미설정 시 표시할 fallback mock 데이터
    private val mockData = listOf(
        WidgetNews(
            newsId = 0L,
            title = "미국 연준, 기준금리 동결 결정",
            summary1 = "• 연방준비제도 FOMC 기준금리 동결",
            summary2 = "• 소비자물가지수 2.3% 유지",
            summary3 = "• 연내 1~2회 인하 가능성 시사",
            summary4 = "• 시장은 향후 물가 지표에 주목"
        ),
        WidgetNews(
            newsId = 0L,
            title = "애플, AI 탑재 아이폰 17 공개",
            summary1 = "• 온디바이스 AI 기능 전면 탑재",
            summary2 = "• 새로운 카메라 시스템 업그레이드",
            summary3 = "• 국내 출시 9월 예정",
            summary4 = "• 배터리 효율과 성능도 개선"
        ),
        WidgetNews(
            newsId = 0L,
            title = "국내 부동산 시장 안정세 지속",
            summary1 = "• 수도권 아파트 3개월 연속 보합",
            summary2 = "• 전세가율 완만한 하락 추세",
            summary3 = "• 금리 인하 기대감에 매수 심리 개선",
            summary4 = "• 지역별 가격 흐름은 차별화"
        ),
        WidgetNews(
            newsId = 0L,
            title = "국내 전기차 판매량 30% 증가",
            summary1 = "• 상반기 신규 등록 전년비 30% 상승",
            summary2 = "• 보조금 정책 효과 본격화",
            summary3 = "• 충전 인프라 확대가 성장 견인",
            summary4 = "• 신차 출시로 선택지도 확대"
        ),
        WidgetNews(
            newsId = 0L,
            title = "정부, 청년 주거 지원 정책 발표",
            summary1 = "• 청년 전세 대출 한도 상향",
            summary2 = "• 공공임대 물량 2만 호 추가 공급",
            summary3 = "• 주거급여 수급 기준 완화",
            summary4 = "• 온라인 통합 신청 창구 운영"
        )
    )

    private val repository = WidgetNewsRepository(
        newsApi = NetworkModule.newsApiService,
        homeApi = NetworkModule.homeApiService,
        archiveApi = NetworkModule.archiveApiService,
        onboardingLocalStorage = OnboardingLocalStorage(context),
        authLocalStorage = AuthLocalStorage(context)
    )

    private var currentData: List<WidgetNews> = mockData

    override fun onCreate() {
        Log.d("BrifeWidgetFactory", "onCreate")
    }

    // background thread에서 호출됨 — 블로킹 API 호출 가능
    override fun onDataSetChanged() {
        Log.d("BrifeWidgetFactory", "onDataSetChanged 시작")
        val apiResult = repository.fetchTop5()
        currentData = if (apiResult.isNotEmpty()) {
            apiResult.map { news ->
                WidgetNews(
                    newsId = news.id,
                    title = news.title,
                    summary1 = news.summaryList.getOrNull(0).toSummaryBullet(),
                    summary2 = news.summaryList.getOrNull(1).toSummaryBullet(),
                    summary3 = news.summaryList.getOrNull(2).toSummaryBullet(),
                    summary4 = news.summaryList.getOrNull(3).toSummaryBullet()
                )
            }
        } else {
            mockData
        }
        Log.d("BrifeWidgetFactory", "onDataSetChanged 끝: itemCount=${currentData.size}")

        // BrifeWidgetReceiver에서 현재 카드의 newsId를 조회할 수 있도록 저장
        val prefs = context.getSharedPreferences("brife_widget_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        currentData.forEachIndexed { index, news -> editor.putLong("newsId_$index", news.newsId) }
        editor.apply()

        // newsId_$pos 저장 완료 → 위젯 헤더 북마크 PendingIntent 재갱신
        // (onUpdate에서 updateWidget이 먼저 실행되는 타이밍 문제 해결)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, BrifeWidgetReceiver::class.java)
        )
        if (widgetIds.isNotEmpty()) {
            context.sendBroadcast(
                Intent(context, BrifeWidgetReceiver::class.java).apply {
                    action = BrifeWidgetReceiver.ACTION_REFRESH_HEADER
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                }
            )
        }

        // 서버에서 저장된 뉴스 ID를 조회하여 widget_prefs 북마크 상태 동기화
        // → 이미 앱에서 저장된 뉴스도 위젯에서 active 상태로 표시
        val savedIds = repository.fetchSavedNewsIds()
        if (savedIds.isNotEmpty()) {
            val widgetPrefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            val existing = widgetPrefs.getStringSet("bookmarked_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
            existing.addAll(savedIds.map { it.toString() })
            widgetPrefs.edit().putStringSet("bookmarked_ids", existing).apply()
            Log.d("BrifeWidgetFactory", "북마크 동기화 완료: ${savedIds.size}개")
        }
    }

    override fun onDestroy() {}
    override fun getCount() = currentData.size
    override fun getLoadingView() = null
    override fun getViewTypeCount() = 1
    override fun getItemId(position: Int) = position.toLong()
    override fun hasStableIds() = true

    override fun getViewAt(position: Int): RemoteViews {
        val news = currentData[position]
        val rv = RemoteViews(context.packageName, R.layout.widget_stack_item)

        // ── 텍스트 바인딩 ─────────────────────────────────────────────────────
        rv.setTextViewText(R.id.widget_tv_title, news.title)
        rv.bindSummary(R.id.widget_tv_summary_1, news.summary1)
        rv.bindSummary(R.id.widget_tv_summary_2, news.summary2)
        rv.bindSummary(R.id.widget_tv_summary_3, news.summary3)
        rv.bindSummary(R.id.widget_tv_summary_4, news.summary4)

        // ── 카드 본문 클릭 → 뉴스 상세 이동 ──────────────────────────────────
        val cardFillIn = Intent().apply {
            putExtra(WidgetActionReceiver.EXTRA_ACTION, WidgetActionReceiver.ACTION_OPEN_NEWS)
            putExtra(WidgetActionReceiver.EXTRA_NEWS_ID, news.newsId)
        }
        rv.setOnClickFillInIntent(R.id.widget_card_root, cardFillIn)
        rv.setOnClickFillInIntent(R.id.widget_tv_title, cardFillIn)
        rv.setOnClickFillInIntent(R.id.widget_card_body, cardFillIn)
        rv.setOnClickFillInIntent(R.id.widget_tv_summary_1, cardFillIn)
        rv.setOnClickFillInIntent(R.id.widget_tv_summary_2, cardFillIn)
        rv.setOnClickFillInIntent(R.id.widget_tv_summary_3, cardFillIn)
        rv.setOnClickFillInIntent(R.id.widget_tv_summary_4, cardFillIn)

        return rv
    }

    private fun String?.toSummaryBullet(): String =
        this?.takeIf { it.isNotBlank() }?.let { "• $it" }.orEmpty()

    private fun RemoteViews.bindSummary(viewId: Int, summary: String) {
        setTextViewText(viewId, summary)
        setViewVisibility(viewId, if (summary.isBlank()) View.GONE else View.VISIBLE)
    }
}
