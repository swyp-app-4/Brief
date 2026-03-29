package com.example.brife.feature.home

import android.content.Context
import android.content.Intent

/**
 * 뉴스 공유 링크 생성 및 시스템 공유 시트 실행 유틸
 *
 * [링크 구조]
 * 현재: brife://news/{newsId} (임시 — index 기반)
 * 추후: API에서 받은 실제 newsId / slug 로 교체
 *
 * TODO: 실제 도메인 확정 후 BRIFE_BASE_LINK 교체
 *   - 딥링크 전용: "brife://news"
 *   - App Links (권장): "https://brife.app/news"
 *     → App Links 적용 시 Manifest intent-filter + assetlinks.json 설정 필요
 */
private const val BRIFE_BASE_LINK = "brife://news" // TODO: 실제 도메인으로 교체

fun buildNewsLink(newsId: String): String = "$BRIFE_BASE_LINK/$newsId"

/**
 * 안드로이드 시스템 공유 시트를 띄운다.
 * Intent 실행은 Route(MainScreen) 에서만 호출할 것.
 *
 * @param newsId 현재는 index 기반 임시 ID. 추후 API newsId / slug 로 교체
 */
fun shareNews(context: Context, title: String, newsId: String) {
    val link = buildNewsLink(newsId)
    val shareText = "제목: $title\nBrife에서 확인하기: $link"

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
