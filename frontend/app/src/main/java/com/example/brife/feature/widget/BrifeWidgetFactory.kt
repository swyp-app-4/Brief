package com.example.brife.feature.widget

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.brife.R

class BrifeWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private data class WidgetNews(
        val title: String,
        val summary1: String,
        val summary2: String,
        val summary3: String
    )

    private val mockData = listOf(
        WidgetNews(
            title = "미국 연준, 기준금리 동결 결정",
            summary1 = "• 연방준비제도 FOMC 기준금리 동결",
            summary2 = "• 소비자물가지수 2.3% 유지",
            summary3 = "• 연내 1~2회 인하 가능성 시사"
        ),
        WidgetNews(
            title = "애플, AI 탑재 아이폰 17 공개",
            summary1 = "• 온디바이스 AI 기능 전면 탑재",
            summary2 = "• 새로운 카메라 시스템 업그레이드",
            summary3 = "• 국내 출시 9월 예정"
        ),
        WidgetNews(
            title = "국내 부동산 시장 안정세 지속",
            summary1 = "• 수도권 아파트 3개월 연속 보합",
            summary2 = "• 전세가율 완만한 하락 추세",
            summary3 = "• 금리 인하 기대감에 매수 심리 개선"
        ),
        WidgetNews(
            title = "국내 전기차 판매량 30% 증가",
            summary1 = "• 상반기 신규 등록 전년비 30% 상승",
            summary2 = "• 보조금 정책 효과 본격화",
            summary3 = "• 충전 인프라 확대가 성장 견인"
        ),
        WidgetNews(
            title = "정부, 청년 주거 지원 정책 발표",
            summary1 = "• 청년 전세 대출 한도 상향",
            summary2 = "• 공공임대 물량 2만 호 추가 공급",
            summary3 = "• 주거급여 수급 기준 완화"
        )
    )

    // 인디케이터 dot View ID 목록
    private val dotIds = listOf(
        R.id.widget_dot_0,
        R.id.widget_dot_1,
        R.id.widget_dot_2,
        R.id.widget_dot_3,
        R.id.widget_dot_4
    )

    override fun onCreate() {}
    override fun onDataSetChanged() {}
    override fun onDestroy() {}
    override fun getCount() = mockData.size
    override fun getLoadingView() = null
    override fun getViewTypeCount() = 1
    override fun getItemId(position: Int) = position.toLong()
    override fun hasStableIds() = true

    override fun getViewAt(position: Int): RemoteViews {
        val news = mockData[position]
        val rv = RemoteViews(context.packageName, R.layout.widget_stack_item)

        // 텍스트 세팅
        rv.setTextViewText(R.id.widget_tv_title, news.title)
        rv.setTextViewText(R.id.widget_tv_summary_1, news.summary1)
        rv.setTextViewText(R.id.widget_tv_summary_2, news.summary2)
        rv.setTextViewText(R.id.widget_tv_summary_3, news.summary3)

        // 인디케이터: 현재 위치만 active (White), 나머지 inactive (White 50%)
        // HomeScreen과 동일: active=Color.White, inactive=Color.White.copy(alpha=0.5f)
        dotIds.forEachIndexed { index, dotId ->
            rv.setImageViewResource(
                dotId,
                if (index == position) R.drawable.widget_dot_active
                else R.drawable.widget_dot_inactive
            )
        }

        return rv
    }
}
