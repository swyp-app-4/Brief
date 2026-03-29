package com.example.brife.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
//import com.example.brife.data.local.longsampleHomeNews
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.CategoryChip
import com.example.brife.ui.component.PrimaryButton
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.PrimaryNormal
import com.example.brife.ui.theme.TextCaption
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.style.TextAlign
import com.example.brife.data.local.BookmarkFolderUiModel
import com.example.brife.feature.archive.component.CreateFolderBottomSheet
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.platform.LocalDensity


@Composable
fun NewsLongScreen(
    item: HomeNewsCardItem,
    onBackClick: () -> Unit,
    isLoggedIn: Boolean = false,
    folders: List<BookmarkFolderUiModel> = emptyList(),
    onSaveToFolders: (selectedFolders: List<BookmarkFolderUiModel>) -> Unit = {},
    onCreateFolder: (folderName: String) -> Unit = {},
    onShareClick: () -> Unit = {},
    onNavigateToArchive: () -> Unit = {},
    onLoginRequired: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // 이미지 높이(260dp) - white 영역 오프셋(20dp) = 240dp가 white 시작 지점
    // TopBar 높이 약 56dp를 제외한 180dp 지점부터 white 영역이 TopBar에 닿음
    val density = LocalDensity.current
    val topBarColorThreshold = remember(density) { with(density) { 180.dp.toPx() } }
    val isTopBarWhite by remember { derivedStateOf { scrollState.value > topBarColorThreshold } }
    val topBarColor by animateColorAsState(
        targetValue = if (isTopBarWhite) Color.White else Color.Transparent,
        label = "newsLongTopBarColor"
    )

    // isExpanded 변화(if/else 분기 교체)로 NewsLongContent 인스턴스가 달라져도
    // 동일 item에 대해 이미지가 바뀌지 않도록 이 레벨에서 고정
    val imageRes = remember(item.category) {
        LongFormImageProvider.getRandomImageRes(item.category)
    }

    var showBookmarkSheet by remember { mutableStateOf(false) }
    var showCreateFolderSheet by remember { mutableStateOf(false) }

    // 확정된 북마크 상태 (저장 버튼 클릭 시에만 반영)
    // API에서 받아온 실제 폴더 목록으로 초기화
    var folderItems by remember { mutableStateOf(folders) }

    // 바텀시트 내 임시 선택 상태 — 취소 시 folderItems에 반영되지 않음
    var tempFolders by remember { mutableStateOf(folders) }

    // ViewModel이 새 폴더를 추가하면 (폴더 생성 API 성공) 로컬 목록에 반영
    LaunchedEffect(folders) {
        val existingIds = folderItems.map { it.id }.toSet()
        val newlyAdded = folders.filter { it.id !in existingIds }
        if (newlyAdded.isNotEmpty()) {
            folderItems = folderItems + newlyAdded
            tempFolders = tempFolders + newlyAdded
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .navigationBarsPadding()
            ) {
                NewsLongContent(
                    item = item,
                    isExpanded = true,
                    imageRes = imageRes
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                NewsLongContent(
                    item = item,
                    isExpanded = false,
                    imageRes = imageRes
                )
            }
        }

        NewsLongTopBar(
            containerColor = topBarColor,
            isBookmarked = folderItems.any { it.isSelected },
            onBackClick = onBackClick,
            onBookmarkClick = {
                if (isLoggedIn) {
                    tempFolders = folderItems  // 시트 열 때 확정 상태를 임시 상태로 동기화
                    showBookmarkSheet = true
                } else {
                    onLoginRequired()
                }
            },
            onShareClick = onShareClick
        )

        if (!isExpanded) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                PrimaryButton(
                    text = "관련 내용 보기",
                    enabled = true,
                    onClick = { isExpanded = true }
                )
            }
        }

        if (showBookmarkSheet) {
            NewsBookmarkBottomSheet(
                folders = tempFolders,
                onDismissRequest = { showBookmarkSheet = false },  // 취소 — tempFolders 버려짐
                onMyFolderClick = {
                    showBookmarkSheet = false
                    onNavigateToArchive()
                },
                onAddFolderClick = {
                    // BookmarkSheet를 닫지 않고 CreateFolderSheet를 위에 띄움
                    showCreateFolderSheet = true
                },
                onFolderBookmarkClick = { clickedFolder ->
                    // 임시 상태만 변경 — 저장 전까지 folderItems에 반영되지 않음
                    tempFolders = tempFolders.map { folder ->
                        if (folder.id == clickedFolder.id) folder.copy(isSelected = !folder.isSelected)
                        else folder
                    }
                },
                onSaveClick = {
                    // 선택된 폴더에 실제 저장 API 호출
                    val selectedFolders = tempFolders.filter { it.isSelected }
                    onSaveToFolders(selectedFolders)
                    // 로컬 상태도 확정 반영
                    folderItems = tempFolders
                    showBookmarkSheet = false
                }
            )
        }

        if (showCreateFolderSheet) {
            CreateFolderBottomSheet(
                onDismissRequest = { showCreateFolderSheet = false },
                onSave = { newFolderName ->
                    // API로 폴더 생성 → ViewModel이 성공 시 folders StateFlow 업데이트
                    // → LaunchedEffect(folders)가 감지하여 folderItems/tempFolders에 반영
                    onCreateFolder(newFolderName)
                    showCreateFolderSheet = false
                },
                currentFolderCount = folderItems.size,
                existingFolders = folderItems.map { it.name }
            )
        }
    }
}

@Composable
private fun NewsLongContent(
    item: HomeNewsCardItem,
    isExpanded: Boolean,
    imageRes: Int
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-20).dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp
                    )
                )
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(text = item.category)
                if (item.subCategory.isNotBlank()) {
                    CategoryChip(text = item.subCategory)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            AppText(
                text = item.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            AppText(
                text = "${item.updatedAt} ",
                style = MaterialTheme.typography.bodySmall,
                color = TextCaption
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFE7EBF0),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center

            ) {
                AppText(
                    text = "본 요약은 ${item.articleCount}개 언론사의 보도를\n교차 검증하여 AI가 재구성한 내용입니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextCaption,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SummaryCard(
                summaryPoints = item.summaryPoints
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(28.dp))

                AppText(
                    text = "관련 기사 요약",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                item.relatedArticles.forEachIndexed { index, article ->
                    RelatedArticleItem(
                        title = article.title,
                        content = article.content
                    )

                    if (index != item.relatedArticles.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 20.dp),
                            color = Color(0xFFE9EDF2)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

@Composable
private fun SummaryCard(
    summaryPoints: List<String>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F9FF))
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_longform_pencil),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                AppText(
                    text = "세줄 간편요약",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            summaryPoints.take(3).forEach { point ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    AppText(
                        text = "•",
                        style = MaterialTheme.typography.bodyLarge,
                        color = PrimaryNormal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AppText(
                        text = point,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
private fun RelatedArticleItem(
    title: String,
    content: String
) {
    Column {
        AppText(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppText(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF5F6368)
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsLongTopBar(
    containerColor: Color,
    isBookmarked: Boolean,
    onBackClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = {},
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = Color.Unspecified
                )
            }
        },
        actions = {
            IconButton(onClick = onBookmarkClick) {
                Icon(
                    painter = painterResource(
                        id = if (isBookmarked) {
                            R.drawable.ic_longform_bookmark_active
                        } else {
                            R.drawable.ic_longform_bookmark_inactive
                        }
                    ),
                    contentDescription = "즐겨찾기",
                    tint = Color.Unspecified
                )
            }

            IconButton(onClick = onShareClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_upload),
                    contentDescription = "공유",
                    tint = Color.Unspecified
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = containerColor
        ),
        windowInsets = WindowInsets(0, 0, 0, 0)
    )
}

//@Preview(
//    showBackground = true,
//    showSystemUi = true,
//    name = "News Long Screen"
//)
//@Composable
//fun NewsLongScreenPreview() {
//    BrifeTheme {
//        NewsLongScreen(
//            item = longsampleHomeNews.first(),
//            onBackClick = {},
//            onShareClick = {}
//        )
//    }
//}