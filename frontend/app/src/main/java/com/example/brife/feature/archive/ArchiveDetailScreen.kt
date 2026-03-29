package com.example.brife.feature.archive

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.feature.home.LongFormImageProvider
import com.example.brife.ui.component.AppTopBar
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.CtaDisabled
import com.example.brife.ui.theme.PrimaryNormal
import com.example.brife.ui.theme.TextCaption
import com.example.brife.ui.theme.TextTitle

// 정렬 타입
enum class SortType(val label: String) {
    LATEST("최신순"),
    OLDEST("오래된순"),
    NAME("이름순")
}

// 뉴스 아이템 데이터 모델
data class ArchiveNewsItem(
    val title: String,
    val summary: String,
    val time: String,
    val company: String,
    val imageUrl: Int,
    val newsId: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveDetailScreen(
    folderName: String,
    newsItems: List<ArchiveNewsItem>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sortType by remember { mutableStateOf(SortType.LATEST) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val sortedItems = when (sortType) {
        SortType.LATEST -> newsItems
        SortType.OLDEST -> newsItems.reversed()
        SortType.NAME -> newsItems.sortedBy { it.title }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = folderName,
                showLogo = false,
                showBack = true,
                showSettings = false,
                showMore = true,
                centerTitle = true,
                onBackClick = onBackClick,
                onMoreClick = { /* 상세 더보기 동작 (추후 구현) */ }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 기사 개수 + 정렬 필터 Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText(
                    text = "총 ${sortedItems.size}개",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextCaption
                )
                Row(
                    modifier = Modifier.clickable { showFilterSheet = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_archive_filter_detail),
                        contentDescription = "정렬",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    AppText(
                        text = sortType.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextCaption
                    )
                }
            }


            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(sortedItems) { item ->
                    ArchiveNewsCard(item = item)
                }
            }
        }
    }

    if (showFilterSheet) {
        SortFilterBottomSheet(
            currentSort = sortType,
            onSortSelected = { selected ->
                sortType = selected
                showFilterSheet = false
            },
            onDismissRequest = { showFilterSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortFilterBottomSheet(
    currentSort: SortType,
    onSortSelected: (SortType) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 20.dp)
        ) {
            SortType.entries.forEachIndexed { index, type ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSortSelected(type) }
                        .padding(vertical = 16.dp)
                ) {
                    AppText(
                        text = type.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (type == currentSort) PrimaryNormal else TextTitle
                    )
                }
                if (index < SortType.entries.lastIndex) {
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CtaDisabled),
                contentPadding = PaddingValues(0.dp)
            ) {
                AppText(
                    text = "취소",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                AppText(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppText(
                        text = item.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextCaption
                    )
                    // 가운데 구분 점 추가
                    AppText(
                        text = "·",
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

            Image(
                painter = painterResource(id = item.imageUrl),
                contentDescription = "뉴스 이미지",
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}


// ────────────────────────────────────────────
// Preview
// ────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "1. 기본 상태 (최신순)")
@Composable
fun ArchiveDetailScreenPreview() {
    BrifeTheme {
        ArchiveDetailScreen(
            folderName = "경제 공부",
            newsItems = listOf(
                ArchiveNewsItem(
                    title = "경제",
                    summary = "기준금리 동결 속 소비 회복 기대감 확대에 따른 시장 변화 분석",
                    time = "1시간 전",
                    company = "경제신문",
                    imageUrl = LongFormImageProvider.getStableImageRes("경제 · 재테크", 0)
                )
            ),
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "2. 빈 폴더")
@Composable
fun ArchiveDetailEmptyPreview() {
    BrifeTheme {
        ArchiveDetailScreen(
            folderName = "비어있는 폴더",
            newsItems = emptyList(),
            onBackClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "3. 정렬 필터 바텀시트 (최신순 선택)")
@Composable
fun SortFilterSheetLatestPreview() {
    BrifeTheme {
        SortFilterBottomSheet(
            currentSort = SortType.LATEST,
            onSortSelected = {},
            onDismissRequest = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "4. 정렬 필터 바텀시트 (이름순 선택)")
@Composable
fun SortFilterSheetNamePreview() {
    BrifeTheme {
        SortFilterBottomSheet(
            currentSort = SortType.NAME,
            onSortSelected = {},
            onDismissRequest = {}
        )
    }
}
