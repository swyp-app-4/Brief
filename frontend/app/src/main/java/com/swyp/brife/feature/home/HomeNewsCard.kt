package com.swyp.brife.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.swyp.brife.ui.theme.brifeColors
import androidx.compose.ui.unit.sp
import com.swyp.brife.R
import com.swyp.brife.ui.component.CategoryChip
import com.swyp.brife.ui.component.PrimaryButton
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.CompactPrimaryButtonTextStyle



@Composable
fun HomeNewsCardContent(
    item: HomeNewsCardItem,
    modifier: Modifier = Modifier,
    onShareClick: () -> Unit = {},
    onDetailClick: () -> Unit = {},
    contentScaleFactor: Float = 1f
) {
    val scale = contentScaleFactor.coerceIn(0f, 1f)
    val titleStyle = MaterialTheme.typography.titleMedium.copy(
        fontSize = (19.5f + 2.5f * scale).sp,
        lineHeight = (27.5f + 2.5f * scale).sp
    )
    val bodyStyle = MaterialTheme.typography.bodySmall.copy(
        fontSize = (10.5f + 1.5f * scale).sp,
        lineHeight = (15.5f + 2.5f * scale).sp
    )
    val sectionTitleStyle = MaterialTheme.typography.labelLarge.copy(
        fontSize = (13.5f + 2.5f * scale).sp,
        lineHeight = (19f + 5f * scale).sp
    )
    val summaryBulletStyle = MaterialTheme.typography.bodyMedium.copy(
        fontSize = (12.5f + 1.5f * scale).sp,
        lineHeight = (19f + 3f * scale).sp
    )
    val outerVerticalPadding = (6f + 4f * scale).dp
    val headerTitleSpacing = (6f + 2f * scale).dp
    val titleNoticeSpacing = (8f + 6f * scale).dp
    val noticeSummarySpacing = (10f + 6f * scale).dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onDetailClick() }
            .padding(horizontal = 20.dp, vertical = outerVerticalPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryChip(text = item.category)
                if (item.subCategory.isNotBlank()) {
                    CategoryChip(text = item.subCategory)
                }
            }

            IconButton(
                onClick = onShareClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_upload),
                    contentDescription = "공유",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(headerTitleSpacing))

        Text(
            text = item.title,
            style = titleStyle,
            color = MaterialTheme.brifeColors.textTitle,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(titleNoticeSpacing))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home_alert),
                contentDescription = "알림",
                tint = Color.Unspecified,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = item.notice,
                modifier = Modifier.weight(1f),
                style = bodyStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(noticeSummarySpacing))

        SummaryInsightBox(
            summaryPoints = item.summaryPoints,
            insight = item.insight,
            onDetailClick = onDetailClick,
            contentScaleFactor = scale,
            sectionTitleStyle = sectionTitleStyle,
            bodyStyle = bodyStyle,
            summaryBulletStyle = summaryBulletStyle,
            modifier = Modifier.fillMaxWidth()
        )
    }
}




@Composable
private fun SummaryInsightBox(
    summaryPoints: List<String>,
    insight: String,
    modifier: Modifier = Modifier,
    onDetailClick: () -> Unit = {},
    contentScaleFactor: Float,
    sectionTitleStyle: TextStyle,
    bodyStyle: TextStyle,
    summaryBulletStyle: TextStyle
) {
    val verticalPadding = (12.5f + 4f * contentScaleFactor).dp
    val summaryHeaderSpacing = (7.5f + 3f * contentScaleFactor).dp
    val summaryItemSpacing = (4.5f + 2f * contentScaleFactor).dp
    val dividerVerticalPadding = (10.25f + 4f * contentScaleFactor).dp
    val buttonSpacing = (8.5f + 4f * contentScaleFactor).dp
    val buttonHeight = (40f + 10f * contentScaleFactor).dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.brifeColors.backgroundSub)
            .wrapContentHeight()
            .padding(vertical = verticalPadding)
    ) {
        SummarySection(
            title = "간단요약",
            points = summaryPoints,
            iconResId = R.drawable.brife_logo,
            titleStyle = sectionTitleStyle,
            pointStyle = bodyStyle,
            bulletStyle = summaryBulletStyle,
            headerSpacing = summaryHeaderSpacing,
            itemSpacing = summaryItemSpacing,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(
                vertical = dividerVerticalPadding,
                horizontal = 16.dp
            ),
            color = Color(0xFFE3E3E3),
            thickness = 1.dp
        )

        Text(
            text = insight,
            style = bodyStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(buttonSpacing))

        PrimaryButton(
            text = "자세히 보기",
            onClick = onDetailClick,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .heightIn(min = buttonHeight),
            textStyle = CompactPrimaryButtonTextStyle
        )
    }
}




@Composable
private fun SummarySection(
    title: String,
    points: List<String>,
    iconResId: Int,
    titleStyle: TextStyle,
    pointStyle: TextStyle,
    bulletStyle: TextStyle,
    headerSpacing: Dp,
    itemSpacing: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = titleStyle,
                color = MaterialTheme.brifeColors.textTitle
            )
        }

        Spacer(modifier = Modifier.height(headerSpacing))

        points.forEachIndexed { index, point ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    style = bulletStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = point,
                    modifier = Modifier.weight(1f),
                    style = pointStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    // --- 추가된 속성 ---
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                    // ------------------
                )
            }

            if (index != points.lastIndex) {
                Spacer(modifier = Modifier.height(itemSpacing))
            }
        }
    }
}




@Preview(
    name = "Home News Card",
    showBackground = true,
    backgroundColor = 0xFFF3F3F3,
    widthDp = 360,
    heightDp = 520
)
@Composable
private fun HomeNewsCardPreview() {
    BrifeTheme {
        Surface(
            color = Color(0xFFF3F3F3),
            modifier = Modifier.padding(16.dp)
        ) {
            HomeNewsCardPreviewContent()
        }
    }
}

@Composable
private fun HomeNewsCardPreviewContent() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.brifeColors.componentDefault),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        HomeNewsCardContent(
            item = HomeNewsCardItem(
                category = "경제",
                subCategory = "금리/통화정책",
                title = "기준금리 동결 속 소비 회복 기대감 확대",
                notice = "AI가 분석하여 핵심만 재구성한 요약본입니다.",
                summaryPoints = listOf(
                    "기준금리 동결로 당분간 시장 안정세가 이어질 가능성이 크다.",
                    "소비와 투자 심리 회복 여부가 향후 핵심 변수로 꼽힌다.",
                    "가계와 기업 모두 금리 변화보다 경기 흐름을 더 주목하고 있다."
                ),
                articleCount= 3,
                insight = "금리 흐름은 대출, 소비, 투자 심리에 직접 영향을 미친다. 따라서 이번 뉴스는 단순 금융 이슈가 아니라 개인의 소비 계획과 자산관리 전략에도 연결해서 볼 필요가 있다."
            ),


            modifier = Modifier.fillMaxWidth(),
            onShareClick = {},
            onDetailClick = {}
        )
    }
}
