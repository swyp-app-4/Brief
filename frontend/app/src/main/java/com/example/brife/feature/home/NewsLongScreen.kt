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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.CategoryChip
import androidx.compose.ui.text.font.FontWeight
import com.example.brife.data.model.NewsDetailSection
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.PrimaryNormal
import com.example.brife.ui.theme.TextBody
import com.example.brife.ui.theme.TextCaption
import com.example.brife.ui.theme.TextSubtitle
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
    autoOpenBookmark: Boolean = false,
    folders: List<BookmarkFolderUiModel> = emptyList(),
    sections: List<NewsDetailSection> = emptyList(),
    onSaveToFolders: (selectedFolders: List<BookmarkFolderUiModel>) -> Unit = {},
    onCreateFolder: (folderName: String) -> Unit = {},
    onShareClick: () -> Unit = {},
    onNavigateToArchive: () -> Unit = {},
    onLoginRequired: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val scrollState = rememberScrollState()

    val density = LocalDensity.current
    val topBarColorThreshold = remember(density) { with(density) { 180.dp.toPx() } }
    val isTopBarWhite by remember { derivedStateOf { scrollState.value > topBarColorThreshold } }
    val topBarColor by animateColorAsState(
        targetValue = if (isTopBarWhite) Color.White else Color.Transparent,
        label = "newsLongTopBarColor"
    )

    val imageRes = remember(item.category) {
        LongFormImageProvider.getRandomImageRes(item.category)
    }

    var showBookmarkSheet by remember { mutableStateOf(false) }
    var showCreateFolderSheet by remember { mutableStateOf(false) }

    // 위젯 북마크 버튼 클릭 시 화면 진입과 동시에 북마크 시트 자동 오픈
    LaunchedEffect(autoOpenBookmark) {
        if (autoOpenBookmark) {
            showBookmarkSheet = true
        }
    }

    var folderItems by remember { mutableStateOf(folders) }
    var tempFolders by remember { mutableStateOf(folders) }

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
        ) {
            NewsLongContent(
                item = item,
                imageRes = imageRes,
                sections = sections
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        NewsLongTopBar(
            containerColor = topBarColor,
            isBookmarked = folderItems.any { it.isSelected },
            onBackClick = onBackClick,
            onBookmarkClick = {
                if (isLoggedIn) {
                    tempFolders = folderItems
                    showBookmarkSheet = true
                } else {
                    onLoginRequired()
                }
            },
            onShareClick = {
                captureComposableContent(
                    context = context,
                    onCaptured = { bitmap ->
                        shareImageBitmap(context, bitmap, item.title)
                    },
                    content = {
                        BrifeTheme {
                            Surface(color = Color.White) {
                                NewsLongShareContent(
                                    item = item,
                                    imageRes = imageRes,
                                    sections = sections
                                )
                            }
                        }
                    }
                )
            }
        )

        if (showBookmarkSheet) {
            NewsBookmarkBottomSheet(
                folders = tempFolders,
                onDismissRequest = { showBookmarkSheet = false },
                onMyFolderClick = {
                    showBookmarkSheet = false
                    onNavigateToArchive()
                },
                onAddFolderClick = {
                    showCreateFolderSheet = true
                },
                onFolderBookmarkClick = { clickedFolder ->
                    tempFolders = tempFolders.map { folder ->
                        if (folder.id == clickedFolder.id) folder.copy(isSelected = !folder.isSelected)
                        else folder
                    }
                },
                onSaveClick = {
                    val selectedFolders = tempFolders.filter { it.isSelected }
                    onSaveToFolders(selectedFolders)
                    folderItems = tempFolders
                    showBookmarkSheet = false
                }
            )
        }
        if (showCreateFolderSheet) {
            CreateFolderBottomSheet(
                onDismissRequest = { showCreateFolderSheet = false },
                onSave = { newFolderName ->
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
    imageRes: Int,
    sections: List<NewsDetailSection> = emptyList()
) {
    Column(
        modifier = Modifier.fillMaxWidth()
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

            if (sections.isNotEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))
                sections.take(3).forEachIndexed { index, section ->
                    if (index > 0) Spacer(modifier = Modifier.height(24.dp))
                    LongFormSectionBlock(index = index, section = section)
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else if (item.insight.isNotBlank()) {
                Spacer(modifier = Modifier.height(28.dp))
                AppText(
                    text = "살펴보기",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                AppText(
                    text = item.insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5F6368)
                )
                Spacer(modifier = Modifier.height(24.dp))
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
private fun LongFormSectionBlock(
    index: Int,
    section: NewsDetailSection
) {
    val iconRes = when (index) {
        0 -> R.drawable.ic_longform_number1
        1 -> R.drawable.ic_longform_number2
        else -> R.drawable.ic_longform_number3
    }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Unspecified
            )
            AppText(
                text = section.heading,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextSubtitle
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        section.contentList.forEach { line ->
            AppText(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = TextBody,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
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

@Composable
private fun NewsLongShareContent(
    item: HomeNewsCardItem,
    imageRes: Int,
    sections: List<NewsDetailSection>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        NewsLongContent(
            item = item,
            imageRes = imageRes,
            sections = sections
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

