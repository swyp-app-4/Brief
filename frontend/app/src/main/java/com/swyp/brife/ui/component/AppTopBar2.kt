package com.swyp.brife.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.TextTitle
import com.swyp.brife.ui.theme.brifeColors
import com.swyp.brife.R

/**
 * 뒤로가기 + 중앙 타이틀 + 선택적 우측 슬롯으로 구성된 공통 상단바
 *
 * 사용 화면: SettingScreen, WidgetInstallGuideScreen, LoginTerms TermsDetailPage, OneToOneInquiryScreen
 *
 * @param title 가운데에 표시할 제목 텍스트
 * @param onBackClick 뒤로가기 아이콘 클릭 콜백
 * @param rightContent 오른쪽 영역에 표시할 컴포저블 (null이면 빈 공간으로 제목 중앙 유지)
 */
@Composable
fun AppTopBar2(
    title: String,
    onBackClick: () -> Unit,
    rightContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .background(MaterialTheme.brifeColors.backgroundDefault)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽: 뒤로가기 아이콘 (48dp 고정)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "뒤로가기",
                tint = Color.Unspecified
            )
        }

        // 가운데: 제목 (남은 공간 채움)
        AppText(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.brifeColors.textTitle,
            modifier = Modifier.weight(1f)
        )

        // 오른쪽: optional 슬롯 (없으면 48dp 빈 공간으로 제목 중앙 균형 유지)
        Box(modifier = Modifier.size(48.dp)) {
            rightContent?.invoke()
        }
    }
}

// ────────────────────────────────────────────
// Preview
// ────────────────────────────────────────────

@Preview(showBackground = true, name = "1. 기본 (우측 빈 공간)")
@Composable
private fun AppTopBar2DefaultPreview() {
    BrifeTheme {
        AppTopBar2(
            title = "설정",
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "2. 우측 아이콘 있음")
@Composable
private fun AppTopBar2WithRightIconPreview() {
    BrifeTheme {
        AppTopBar2(
            title = "1:1 문의",
            onBackClick = {},
            rightContent = {
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_setting),
                        contentDescription = null
                    )
                }
            }
        )
    }
}
