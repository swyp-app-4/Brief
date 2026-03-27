package com.example.brife.feature.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.feature.archive.ArchiveNewsCard
import com.example.brife.feature.archive.ArchiveNewsItem
import com.example.brife.ui.component.AppText
import com.example.brife.ui.theme.BgDefault
import com.example.brife.ui.theme.BgSub
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.ComponentDefault
import com.example.brife.ui.theme.CtaActive
import com.example.brife.ui.theme.TextCaption
import com.example.brife.ui.theme.TextSubtitle

@Composable
fun ExploreScreen(
    newsList: List<ArchiveNewsItem> = emptyList(),
    lastUpdatedTime: String = "09:00",
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(White),
        contentPadding = PaddingValues(start = 16.dp,
            end = 16.dp,
            bottom = 16.dp,
            top = 0.dp // 상단 여백이 필요 없다면 0.dp (생략 가능)
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            ExploreSearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp)
            )
        }

        item {
            RecentNewsHeader(
                lastUpdatedTime = lastUpdatedTime,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
        }

        items(newsList) { item ->
            ArchiveNewsCard(item = item)
        }
    }
}

@Composable
private fun ExploreSearchBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color = ComponentDefault)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "검색",
            tint = TextCaption,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        AppText(
            text = "검색어를 입력해주세요",
            style = MaterialTheme.typography.bodyMedium,
            color = TextCaption
        )
    }
}

@Composable
private fun RecentNewsHeader(
    lastUpdatedTime: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = CtaActive)) {
                    append("최근 ")
                }
                append("뉴스")
            },
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextSubtitle
        )
        AppText(
            text = "$lastUpdatedTime 기준",
            style = MaterialTheme.typography.labelSmall,
            color = TextCaption
        )
    }
}


