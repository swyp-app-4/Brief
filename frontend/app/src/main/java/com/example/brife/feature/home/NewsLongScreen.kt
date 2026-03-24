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
import com.example.brife.data.local.longsampleHomeNews
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


@Composable
fun NewsLongScreen(
    item: HomeNewsCardItem,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit = {},
    onBookmarkClick: (Boolean) -> Unit = {},
    isBookmarkedInitial: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isBookmarked by remember { mutableStateOf(isBookmarkedInitial) }
    var isExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    var showBookmarkSheet by remember { mutableStateOf(false) }
    var showCreateFolderSheet by remember { mutableStateOf(false) }


    var folderItems by remember {
        mutableStateOf(
            listOf(
                BookmarkFolderUiModel(
                    id = 1L,
                    name = "즐겨찾기",
                    newsCount = 12,
                    isSelected = true
                ),
                BookmarkFolderUiModel(
                    id = 2L,
                    name = "전쟁",
                    newsCount = 3,
                    isSelected = false
                )
            )
        )
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
                    isExpanded = true
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
                    isExpanded = false
                )
            }
        }

        NewsLongTopBar(
            isBookmarked = folderItems.any { it.isSelected },
            onBackClick = onBackClick,
            onBookmarkClick = {
                isBookmarked = !isBookmarked
                onBookmarkClick(isBookmarked)
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
                folders = folderItems,
                onDismissRequest = { showBookmarkSheet = false },
                onMyFolderClick = {
                    showBookmarkSheet = false
                    // TODO: 내 폴더 화면 이동
                },
                onAddFolderClick = {
                    showBookmarkSheet = false
                    showCreateFolderSheet = true
                },
                //다중선택
                onFolderBookmarkClick = { clickedFolder ->
                    folderItems = folderItems.map { folder ->
                        if (folder.id == clickedFolder.id) {
                            folder.copy(isSelected = !folder.isSelected)
                        } else {
                            folder
                        }
                    }
                }
//단일선택
//                onFolderBookmarkClick = { clickedFolder ->
//                    folderItems = folderItems.map { folder ->
//                        folder.copy(isSelected = folder.id == clickedFolder.id)
//                    }
//                }

            )
        }

        if (showCreateFolderSheet) {
            CreateFolderBottomSheet(
                onDismissRequest = { showCreateFolderSheet = false },

                //여러 폴더 동시 저장
                onSave = { newFolderName ->
                    folderItems = folderItems + BookmarkFolderUiModel(
                        id = (folderItems.maxOfOrNull { it.id } ?: 0L) + 1L,
                        name = newFolderName,
                        newsCount = 1,
                        isSelected = true
                    )
                    showCreateFolderSheet = false
                },

                //하나의 폴더에만 저장
//                onSave = { newFolderName ->
//                    folderItems = folderItems.map { it.copy(isSelected = false) } +
//                            BookmarkFolderUiModel(
//                                id = (folderItems.maxOfOrNull { it.id } ?: 0L) + 1L,
//                                name = newFolderName,
//                                newsCount = 1,
//                                isSelected = true
//                            )
//                    showCreateFolderSheet = false
//                },


                currentFolderCount = folderItems.size,
                existingFolders = folderItems.map { it.name }
            )
        }
    }
}

@Composable
private fun NewsLongContent(
    item: HomeNewsCardItem,
    isExpanded: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.homescreen_bg),
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
            CategoryChip(
                text = item.category,
                modifier = Modifier.align(Alignment.Start)
            )

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
                text = "${item.updatedAt} · ${item.companyName}",
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
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center

            ) {
                AppText(
                    text = "본 요약은 ${item.companyName}의 보도 자료를\n바탕으로 AI가 재구성했습니다.",
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
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        windowInsets = WindowInsets(0, 0, 0, 0)
    )
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "News Long Screen"
)
@Composable
fun NewsLongScreenPreview() {
    BrifeTheme {
        NewsLongScreen(
            item = longsampleHomeNews.first(),
            onBackClick = {},
            onShareClick = {},
            onBookmarkClick = {}
        )
    }
}