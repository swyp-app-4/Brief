package com.example.brife.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.Gray600
import com.example.brife.ui.theme.Positive

data class HomeNewsCardItem(
    val category: String,
    val title: String,
    val notice: String,
    val summaryPoints: List<String>,
    val insight: String
)

@Composable
fun HomeNewsCardContent(
    item: HomeNewsCardItem,
    modifier: Modifier = Modifier,
    onShareClick: () -> Unit = {},
    onDetailClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryChip(text = item.category)

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

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        SummaryInsightBox(
            summaryPoints = item.summaryPoints,
            insight = item.insight,
            onDetailClick = onDetailClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
private fun CategoryChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Gray600)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun SummaryInsightBox(
    summaryPoints: List<String>,
    insight: String,
    modifier: Modifier = Modifier,
    onDetailClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F9FF))
            .wrapContentHeight()
            .padding(vertical = 18.dp)
    ) {
        SummarySection(
            title = "간단요약",
            points = summaryPoints,
            iconResId = R.drawable.brife_logo,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            color = Color(0xFFE3E3E3),
            thickness = 1.dp
        )

        SectionBlock(
            title = "살펴보기",
            content = insight,
            iconResId = R.drawable.ic_look,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDetailClick() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "자세히 보기",
                    style = MaterialTheme.typography.labelLarge,
                    color = Positive
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = Positive,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
@Composable
private fun SectionBlock(
    title: String,
    content: String,
    iconResId: Int,
    modifier: Modifier = Modifier
        .fillMaxHeight()
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
                style = MaterialTheme.typography.labelLarge,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SummarySection(
    title: String,
    points: List<String>,
    iconResId: Int,
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
                style = MaterialTheme.typography.labelLarge,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        points.forEachIndexed { index, point ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = point,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (index != points.lastIndex) {
                Spacer(modifier = Modifier.height(6.dp))
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        HomeNewsCardContent(
            item = HomeNewsCardItem(
                category = "경제",
                title = "기준금리 동결 속 소비 회복 기대감 확대",
                notice = "AI가 분석하여 핵심만 재구성한 요약본입니다.",
                summaryPoints = listOf(
                    "기준금리 동결로 당분간 시장 안정세가 이어질 가능성이 크다.",
                    "소비와 투자 심리 회복 여부가 향후 핵심 변수로 꼽힌다.",
                    "가계와 기업 모두 금리 변화보다 경기 흐름을 더 주목하고 있다."
                ),
                insight = "금리 흐름은 대출, 소비, 투자 심리에 직접 영향을 미친다. 따라서 이번 뉴스는 단순 금융 이슈가 아니라 개인의 소비 계획과 자산관리 전략에도 연결해서 볼 필요가 있다."
            ),
            modifier = Modifier.fillMaxWidth(),
            onShareClick = {},
            onDetailClick = {}
        )
    }
}