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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.feature.home.LongFormImageProvider
import com.example.brife.ui.component.AppTopBar
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.CtaDisabled
import com.example.brife.ui.theme.Negative
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
    val archiveItemId: Long = 0L,   // archive item 고유 ID (삭제 시 사용)
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
    onDeleteItems: (Set<Long>) -> Unit ,
    modifier: Modifier = Modifier
) {
    var sortType by remember { mutableStateOf(SortType.LATEST) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf(setOf<Long>()) }

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
                showMore = !isEditMode,
                centerTitle = true,
                onBackClick = {
                    if (isEditMode) {
                        isEditMode = false
                        selectedItemIds = emptySet()
                    } else {
                        onBackClick()
                    }
                },
                onMoreClick = { showMoreSheet = true }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 기사 개수 + 정렬 필터 (편집 모드 아닐 때만)
                if (!isEditMode) {
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
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp,   // horizontal 대신 start 사용
                            end = 16.dp,     // horizontal 대신 end 사용
                            top = 8.dp,
                            bottom = if (isEditMode) 88.dp else 8.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(sortedItems) { item ->
                        if (isEditMode) {
                            EditableArchiveNewsCard(
                                item = item,
                                isSelected = item.archiveItemId in selectedItemIds,
                                onToggle = { id ->
                                    selectedItemIds = if (id in selectedItemIds)
                                        selectedItemIds - id
                                    else
                                        selectedItemIds + id
                                }
                            )
                        } else {
                            ArchiveNewsCard(item = item)
                        }
                    }
                }
            }

            // 편집 모드 하단 삭제 버튼
            if (isEditMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = {
                           onDeleteItems(selectedItemIds)
                            selectedItemIds = emptySet()
                            isEditMode = false
                        },
                        enabled = selectedItemIds.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Negative,
                            disabledContainerColor = CtaDisabled
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        AppText(
                            text = "삭제",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // 더보기 바텀시트 (목록 편집 / 취소)
    if (showMoreSheet) {
        ArchiveDetailMoreBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            onEditClick = {
                showMoreSheet = false
                isEditMode = true
                selectedItemIds = emptySet()
            }
        )
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

// 목록 편집 바텀시트
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveDetailMoreBottomSheet(
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit
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
            // 목록 편집
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditClick() }
                    .padding(vertical = 16.dp)
            ) {
                AppText(
                    text = "목록 편집",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTitle
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 취소 버튼
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

// 정렬 필터 바텀시트
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

// 편집 모드 카드 (체크 아이콘 + 오른쪽 치우침)
@Composable
private fun EditableArchiveNewsCard(
    item: ArchiveNewsItem,
    isSelected: Boolean,
    onToggle: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(item.archiveItemId) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                id = if (isSelected) R.drawable.ic_aftercheck else R.drawable.ic_beforecheck
            ),
            contentDescription = if (isSelected) "선택됨" else "선택 안됨",
            tint = Color.Unspecified,
            modifier = Modifier
                .padding(start = 4.dp, end = 8.dp)
                .size(24.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            ArchiveNewsCard(item = item)
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
