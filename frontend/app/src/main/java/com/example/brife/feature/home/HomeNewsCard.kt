package com.example.brife.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.example.brife.ui.theme.Gray600

data class HomeNewsCardItem(
    val category: String,
    val title: String,
    val notice: String,
    val summary: String,
    val insight: String
)

@Composable
fun HomeNewsCard(
    item: HomeNewsCardItem,
    modifier: Modifier = Modifier,
    onShareClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp)
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.notice,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(18.dp))

            SummaryInsightBox(
                summary = item.summary,
                insight = item.insight,
                modifier = Modifier.fillMaxWidth()
            )
        }
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
    summary: String,
    insight: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F9FF))
            .padding(vertical = 16.dp)
    ) {
        SectionBlock(
            title = "간단요약",
            content = summary,
            iconResId = R.drawable.brife_logo, // brife_logo 아이콘 추가
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
    }
}

@Composable
private fun SectionBlock(
    title: String,
    content: String,
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F3F3)
@Composable
private fun HomeNewsCardPreview() {
    Surface(
        color = Color(0xFFF3F3F3),
        modifier = Modifier.padding(16.dp)
    ) {
        HomeNewsCard(
            item = HomeNewsCardItem(
                category = "경제",
                title = "기준금리 동결 속 소비 회복 기대감 확대",
                notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
                summary = "한국은행의 기준금리 동결 이후 시장은 당분간 안정세를 유지할 것으로 전망되고 있다. 소비와 투자 심리 회복 여부가 핵심 변수로 꼽힌다.",
                insight = "금리 흐름은 대출, 소비, 투자 심리에 직접 영향을 미친다. 따라서 이번 뉴스는 단순 금융 이슈가 아니라 개인의 소비 계획과 자산관리 전략에도 연결해서 볼 필요가 있다."
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
        )
    }
}