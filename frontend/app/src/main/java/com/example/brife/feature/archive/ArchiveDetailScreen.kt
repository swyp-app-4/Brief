// C:/Users/SAMSUNG/Desktop/brife/frontend/app/src/main/java/com/example/brife/feature/archive/ArchiveDetailScreen.kt

package com.example.brife.feature.archive

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.feature.home.HomeNewsCardContent
import com.example.brife.feature.home.HomeNewsCardItem
import com.example.brife.ui.component.AppText
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.TextCaption



// 가상의 데이터 모델 (기존 HomeNewsCardItem 활용 또는 확장)
data class ArchiveNewsItem(
    val title: String,
    val summary: String,
    val time: String,
    val company: String,
    val imageUrl: Int // 실제로는 String(URL)이겠지만 여기선 Resource ID 사용
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveDetailScreen(
    folderName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
// 1. 예시 데이터 (2~3개)
    val newsItems = listOf(
        ArchiveNewsItem(
            title = "경제",
            summary = "기준금리 동결 속 소비 회복 기대감 확대에 따른 시장 변화 분석",
            time = "1시간 전",
            company = "경제신문",
            imageUrl = R.drawable.homescreen_bg // 예시 이미지
        ),
        ArchiveNewsItem(
            title = "IT",
            summary = "생성형 AI의 진화, 이제는 개인 맞춤형 비서 시대로 접어든다",
            time = "3시간 전",
            company = "테크리뷰",
            imageUrl = R.drawable.homescreen_bg
        ),
        ArchiveNewsItem(
            title = "사회",
            summary = "도심 속 녹지 공간 확대 사업, 시민들의 삶의 질 만족도 높여",
            time = "5시간 전",
            company = "브리프뉴스",
            imageUrl = R.drawable.homescreen_bg
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { AppText(text = folderName, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "뒤로가기",
                            tint = Color.Unspecified
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            // 가로 전체를 채우되, 카드와 화면 끝 사이에 적절한 여백(20dp)을 둠
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(newsItems) { item ->
                ArchiveNewsCard(item = item)
            }
        }
    }
}

@Composable
fun ArchiveNewsCard(item: ArchiveNewsItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // [왼쪽 영역] 요약 텍스트 + 메타 정보 (시간, 회사)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween // 위아래 끝으로 배치
            ) {
                // 1. 뉴스 요약 (최대 2줄, 넘치면 ...)
                // AppText가 maxLines와 overflow를 직접 지원하지 않을 수 있으므로
                // 아래와 같이 기본 Text 속성을 사용하거나 AppText가 이를 지원하도록 확인해야 합니다.
                AppText(
                    text = item.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                    // 만약 AppText에서 maxLines를 지원하지 않는다면 파라미터에서 제외해야 합니다.
                )

                // 2. 하단 정보 영역 (왼쪽 정렬)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppText(
                        text = item.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCaption
                    )
                    AppText(
                        text = item.company,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCaption
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // [오른쪽 영역] 정사각형 이미지
            Image(
                painter = painterResource(id = item.imageUrl),
                contentDescription = "뉴스 이미지",
                modifier = Modifier
                    .size(78.dp) // 정사각형 크기
                    .clip(RoundedCornerShape(8.dp)), // 요청하신 약간의 radius
                contentScale = ContentScale.Crop
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ArchiveDetailPreview() {
    BrifeTheme {
        ArchiveDetailScreen(
            folderName = "경제/금융",
            onBackClick = {}
        )
    }
}
