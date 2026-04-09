package com.swyp.brife.feature.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.swyp.brife.R
import com.swyp.brife.data.local.BookmarkFolderUiModel
import com.swyp.brife.data.model.NewsDetailSection
import com.swyp.brife.data.model.NewsSourceItemResponse
import com.swyp.brife.feature.archive.component.CreateFolderBottomSheet
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.component.PrimaryButton
import com.swyp.brife.ui.component.CategoryChip
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.PrimaryNormal
import com.swyp.brife.ui.theme.TextBody
import com.swyp.brife.ui.theme.TextCaption
import com.swyp.brife.ui.theme.TextSubtitle

@Composable
fun NewsLongScreen(
    item: HomeNewsCardItem,
    onBackClick: () -> Unit,
    isLoggedIn: Boolean = false,
    autoOpenBookmark: Boolean = false,
    folders: List<BookmarkFolderUiModel> = emptyList(),
    isSavingFolders: Boolean = false,
    sections: List<NewsDetailSection> = emptyList(),
    isSectionsLoading: Boolean = false,
    sectionsError: Boolean = false,
    onRetryLoadSections: () -> Unit = {},
    onSaveToFolders: (
        targetFolders: List<BookmarkFolderUiModel>,
        onCompleted: (Boolean) -> Unit
    ) -> Unit = { _, onCompleted -> onCompleted(false) },
    onCreateFolder: (
        folderName: String,
        onCreated: (BookmarkFolderUiModel) -> Unit
    ) -> Unit = { _, _ -> },
    sources: List<NewsSourceItemResponse> = emptyList(),
    isSourcesLoading: Boolean = false,
    sourcesError: Boolean = false,
    showSourcesBottomSheet: Boolean = false,
    onSourcesBottomSheetRequest: () -> Unit = {},
    onSourcesBottomSheetDismiss: () -> Unit = {},
    onRetryLoadSources: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onNavigateToArchive: () -> Unit = {},
    onLoginRequired: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scrollState = rememberScrollState()

    val density = LocalDensity.current
    val topBarColorThreshold = remember(density) { with(density) { 180.dp.toPx() } }
    val isOverImageSection by remember {
        derivedStateOf { scrollState.value > topBarColorThreshold }
    }
    val statusBarHeight = remember(view, density) {
        with(density) {
            (
                ViewCompat.getRootWindowInsets(view)
                    ?.getInsets(WindowInsetsCompat.Type.statusBars())
                    ?.top ?: 0
            ).toDp()
        }
    }

    DisposableEffect(view, isOverImageSection) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                isOverImageSection
        }

        onDispose {
            val disposeWindow = (view.context as? Activity)?.window ?: return@onDispose
            disposeWindow.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(disposeWindow, view).isAppearanceLightStatusBars = false
        }
    }

    val imageRes = remember(item.imageRes, item.category, item.subCategory, item.newsId) {
        item.imageRes ?: LongFormImageProvider.getStableImageRes(
            item.category,
            item.subCategory,
            item.newsId
        )
    }

    var showBookmarkSheet by remember { mutableStateOf(false) }
    var showCreateFolderSheet by remember { mutableStateOf(false) }
    var folderItems by remember { mutableStateOf(folders) }
    var tempFolders by remember { mutableStateOf(folders) }

    LaunchedEffect(autoOpenBookmark) {
        if (autoOpenBookmark) {
            showBookmarkSheet = true
        }
    }

    LaunchedEffect(folders) {
        folderItems = folders
        tempFolders = if (showBookmarkSheet && !isSavingFolders) {
            val tempSelectionsById = tempFolders.associateBy { it.id }
            folders.map { folder ->
                tempSelectionsById[folder.id]
                    ?.let { existing -> folder.copy(isSelected = existing.isSelected) }
                    ?: folder
            }
        } else {
            folders
        }
    }

    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            NewsLongContent(
                item = item,
                imageRes = imageRes,
                sections = sections,
                isSectionsLoading = isSectionsLoading,
                sectionsError = sectionsError,
                onRetryLoadSections = onRetryLoadSections
            )

            // 하단 고정 CTA 영역(위 패딩 12 + 버튼 50 + 아래 패딩 16 + nav bar)에 가려지지 않도록 여백 확보
            Spacer(modifier = Modifier.height(12.dp + 50.dp + 16.dp + navBarBottomPadding))
        }

        if (isOverImageSection) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarHeight)
                    .background(Color.White)
                    .align(Alignment.TopCenter)
            )
        }

        NewsLongTopBar(
            showIconBackground = isOverImageSection,
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
                onShareClick()
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

        // 하단 고정 CTA 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            PrimaryButton(
                text = "관련 뉴스기사 보기",
                onClick = { onSourcesBottomSheetRequest() }
            )
        }

        if (showBookmarkSheet) {
            NewsBookmarkBottomSheet(
                folders = tempFolders,
                isSaving = isSavingFolders,
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
                        if (folder.id == clickedFolder.id) {
                            folder.copy(isSelected = !folder.isSelected)
                        } else {
                            folder
                        }
                    }
                },
                onSaveClick = {
                    onSaveToFolders(tempFolders) { isSuccess ->
                        if (isSuccess) {
                            showBookmarkSheet = false
                        } else {
                            tempFolders = folderItems
                        }
                    }
                }
            )
        }

        if (showCreateFolderSheet) {
            CreateFolderBottomSheet(
                onDismissRequest = { showCreateFolderSheet = false },
                onSave = { newFolderName ->
                    onCreateFolder(newFolderName) { createdFolder ->
                        tempFolders = tempFolders + createdFolder.copy(isSelected = true)
                    }
                    showCreateFolderSheet = false
                },
                currentFolderCount = folderItems.size,
                existingFolders = folderItems.map { it.name }
            )
        }

        if (showSourcesBottomSheet) {
            NewsSourcesBottomSheet(
                sources = sources,
                isLoading = isSourcesLoading,
                isError = sourcesError,
                onDismissRequest = onSourcesBottomSheetDismiss,
                onRetry = onRetryLoadSources
            )
        }
    }
}

@Composable
private fun NewsLongContent(
    item: HomeNewsCardItem,
    imageRes: Int,
    sections: List<NewsDetailSection> = emptyList(),
    isSectionsLoading: Boolean = false,
    sectionsError: Boolean = false,
    onRetryLoadSections: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                text = item.updatedAt,
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
                    text = "본 요약은 ${item.articleCount}개 언론사의 보도를\n교차 검증해 AI가 재구성한 내용입니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextCaption,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SummaryCard(summaryPoints = item.summaryPoints)

            Spacer(modifier = Modifier.height(28.dp))

            when {
                isSectionsLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryNormal)
                    }
                }

                sectionsError -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppText(
                            text = "내용을 불러오지 못했어요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextCaption
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onRetryLoadSections) {
                            AppText(
                                text = "다시 시도",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryNormal
                            )
                        }
                    }
                }

                sections.isNotEmpty() -> {
                    sections.take(3).forEachIndexed { index, section ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        LongFormSectionBlock(index = index, section = section)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item.insight.isNotBlank() -> {
                    AppText(
                        text = "인사이트",
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_longform_pencil),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(8.dp))
                AppText(
                    text = "핵심 간편요약",
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
    showIconBackground: Boolean,
    isBookmarked: Boolean,
    onBackClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        title = {},
        navigationIcon = {
            val navigationModifier = if (showIconBackground) {
                Modifier
                    .padding(start = 12.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    )
                    .background(Color.White, CircleShape)
            } else {
                Modifier.padding(start = 12.dp)
            }

            Box(modifier = navigationModifier) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "뒤로가기",
                        tint = Color.Unspecified
                    )
                }
            }
        },
        actions = {
            val actionsModifier = if (showIconBackground) {
                Modifier
                    .padding(end = 12.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(999.dp),
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    )
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White)
            } else {
                Modifier.padding(end = 12.dp)
            }

            Row(modifier = actionsModifier) {
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
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsSourcesBottomSheet(
    sources: List<NewsSourceItemResponse>,
    isLoading: Boolean,
    isError: Boolean,
    onDismissRequest: () -> Unit,
    onRetry: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            AppText(
                text = "관련 뉴스기사",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryNormal)
                    }
                }

                isError -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AppText(
                            text = "기사를 불러오지 못했어요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextCaption
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onRetry) {
                            AppText(
                                text = "다시 시도",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryNormal
                            )
                        }
                    }
                }

                sources.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AppText(
                            text = "관련 뉴스기사가 없습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextCaption
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        sources.forEach { source ->
                            NewsSourceCard(
                                title = source.title,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.sourceUrl))
                                    context.startActivity(intent)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsSourceCard(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F9FF))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = Color(0xFF9AA3AF)
        )
    }
}
