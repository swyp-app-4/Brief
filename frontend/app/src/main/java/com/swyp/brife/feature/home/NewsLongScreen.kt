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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.swyp.brife.R
import com.swyp.brife.data.local.BookmarkFolderUiModel
import com.swyp.brife.data.model.NewsDetailSection
import com.swyp.brife.data.model.NewsListItem
import com.swyp.brife.data.model.NewsSourceItemResponse
import com.swyp.brife.feature.archive.component.CreateFolderBottomSheet
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.component.PrimaryButton
import com.swyp.brife.ui.component.CategoryChip
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.DarkBlue200
import com.swyp.brife.ui.theme.LongFormCtaTextStyle
import com.swyp.brife.ui.theme.PrimaryNormal
import com.swyp.brife.ui.theme.Gray600
import com.swyp.brife.ui.theme.TextCaption
import com.swyp.brife.ui.theme.DarkBackground
import com.swyp.brife.ui.theme.DarkBorderStrong
import com.swyp.brife.ui.theme.DarkComponentDefault
import com.swyp.brife.ui.theme.DarkGray400
import com.swyp.brife.ui.theme.DarkGray300
import com.swyp.brife.ui.theme.DarkGray600
import com.swyp.brife.ui.theme.DarkTextTitle
import com.swyp.brife.ui.theme.brifeColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val NewsLongTopBarContentHeight = 64.dp
private val NewsLongScrollProgressHeight = 3.dp

private val SimilarPoliticsImages = listOf(
    R.drawable.similar_longform_politics_1,
    R.drawable.similar_longform_politics_2,
    R.drawable.similar_longform_politics_3,
    R.drawable.similar_longform_politics_4,
    R.drawable.similar_longform_politics_5_1,
    R.drawable.similar_longform_politics_6_1,
    R.drawable.similar_longform_politics_7_1,
    R.drawable.similar_longform_politics_8_1,
    R.drawable.similar_longform_politics_9_1
)
private val SimilarEconomyImages = listOf(
    R.drawable.similar_longform_economy_1,
    R.drawable.similar_longform_economy_2,
    R.drawable.similar_longform_economy_3,
    R.drawable.similar_longform_economy_4,
    R.drawable.similar_longform_economy_5_1,
    R.drawable.similar_longform_economy_6_1,
    R.drawable.similar_longform_economy_7_1,
    R.drawable.similar_longform_economy_8_1,
    R.drawable.similar_longform_economy_9_1
)
private val SimilarTechImages = listOf(
    R.drawable.similar_longform_tech_1,
    R.drawable.similar_longform_tech_2,
    R.drawable.similar_longform_tech_3,
    R.drawable.similar_longform_tech_4,
    R.drawable.similar_longform_tech_5_1,
    R.drawable.similar_longform_tech_6_1,
    R.drawable.similar_longform_tech_7_1,
    R.drawable.similar_longform_tech_8_1,
    R.drawable.similar_longform_tech_9_1
)
private val SimilarCultureImages = listOf(
    R.drawable.similar_longform_culture_1,
    R.drawable.similar_longform_culture_2,
    R.drawable.similar_longform_culture_3,
    R.drawable.similar_longform_culture_4
)
private val SimilarEntertainmentImages = listOf(
    R.drawable.similar_longform_entertainment_1,
    R.drawable.similar_longform_entertainment_2,
    R.drawable.similar_longform_entertainment_3,
    R.drawable.similar_longform_entertainment_4
)
private val SimilarLifeImages = listOf(
    R.drawable.similar_longform_life_1,
    R.drawable.similar_longform_life_2,
    R.drawable.similar_longform_life_3,
    R.drawable.similar_longform_life_4
)

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
    similarNews: List<NewsListItem> = emptyList(),
    isSimilarNewsLoading: Boolean = false,
    similarNewsError: String? = null,
    onSimilarNewsClick: (Long) -> Unit = {},
    showSourcesBottomSheet: Boolean = false,
    onSourcesBottomSheetRequest: () -> Unit = {},
    onSourcesBottomSheetDismiss: () -> Unit = {},
    onRetryLoadSources: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onNavigateToArchive: () -> Unit = {},
    onLoginRequired: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val scrollState = rememberScrollState()
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topFixedAreaHeight =
        statusBarTopPadding + NewsLongTopBarContentHeight + NewsLongScrollProgressHeight
    val scrollProgress by remember {
        derivedStateOf {
            val maxScroll = scrollState.maxValue
            if (maxScroll == 0) {
                0f
            } else {
                (scrollState.value / maxScroll.toFloat()).coerceIn(0f, 1f)
            }
        }
    }

    val window = (view.context as? Activity)?.window
    val screenBackground = MaterialTheme.brifeColors.backgroundDefault
    val isDarkTheme = screenBackground == DarkBackground
    val useDarkSystemBarIcons = !isDarkTheme

    DisposableEffect(view) {
        val effectWindow = window
        if (effectWindow == null) {
            onDispose { }
        } else {
            val controller = WindowCompat.getInsetsController(effectWindow, view)
            val previousStatusBarColor = effectWindow.statusBarColor
            val previousLightStatusBars = controller.isAppearanceLightStatusBars

            onDispose {
                effectWindow.statusBarColor = previousStatusBarColor
                controller.isAppearanceLightStatusBars = previousLightStatusBars
            }
        }
    }

    SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            window.statusBarColor = screenBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                useDarkSystemBarIcons
        }
    }

    val imageRes = remember(item.imageRes, item.category, item.subCategory, item.newsId) {
        val similarCategoryName = item.subCategory.ifBlank { item.category }
        getSimilarNewsImageRes(
            categoryName = similarCategoryName,
            groupName = item.category,
            newsId = item.newsId
        )
            ?: item.imageRes
            ?: LongFormImageProvider.getStableImageRes(
                item.category,
                item.subCategory,
                item.newsId
            )
    }

    var shareFlowStep by remember { mutableStateOf(ShareFlowStep.Closed) }
    var selectedShareData by remember { mutableStateOf<NewsShareData?>(null) }
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
            .background(MaterialTheme.brifeColors.backgroundDefault)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = topFixedAreaHeight)
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

            SimilarNewsSection(
                similarNews = similarNews,
                isLoading = isSimilarNewsLoading,
                error = similarNewsError,
                onNewsClick = onSimilarNewsClick
            )

            // 하단 고정 CTA 영역(위 패딩 12 + 버튼 50 + 아래 패딩 16 + nav bar)에 가려지지 않도록 여백 확보
            Spacer(modifier = Modifier.height(12.dp + 50.dp + 16.dp + navBarBottomPadding))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            NewsLongTopBar(
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
                    selectedShareData = item.toNewsShareData()
                    shareFlowStep = ShareFlowStep.Template
                }
            )
            NewsLongScrollProgressBar(progress = scrollProgress)
        }

        // 하단 고정 CTA 영역
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(if (isDarkTheme) DarkGray400 else MaterialTheme.brifeColors.backgroundDefault)
                .then(
                    if (isDarkTheme) {
                        Modifier.border(width = 1.dp, color = DarkBorderStrong)
                    } else {
                        Modifier
                    }
                )
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            PrimaryButton(
                text = "관련 뉴스기사 보기",
                onClick = { onSourcesBottomSheetRequest() },
                textStyle = LongFormCtaTextStyle
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

        NewsShareFlowHost(
            step = shareFlowStep,
            shareData = selectedShareData,
            onStepChange = { shareFlowStep = it },
            onDismiss = {
                shareFlowStep = ShareFlowStep.Closed
                selectedShareData = null
            }
        )
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
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground

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
                .background(MaterialTheme.brifeColors.backgroundDefault)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChip(
                    text = item.category,
                    containerColor = if (isDarkTheme) DarkGray600 else Gray600
                )
                if (item.subCategory.isNotBlank()) {
                    CategoryChip(
                        text = item.subCategory,
                        containerColor = if (isDarkTheme) DarkGray600 else Gray600
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            AppText(
                text = item.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.brifeColors.textTitle,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            AppText(
                text = formatLongFormPublishedDate(item.updatedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.brifeColors.textCaption
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (isDarkTheme) {
                            Modifier
                        } else {
                            Modifier.border(
                                width = 1.dp,
                                color = Color(0xFFE7EBF0),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    )
                    .background(
                        if (isDarkTheme) {
                            DarkComponentDefault
                        } else {
                            MaterialTheme.brifeColors.backgroundDefault
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    text = "본 요약은 ${item.articleCount}개 언론사의 보도를\n교차 검증해 AI가 재구성한 내용입니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.brifeColors.textCaption,
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
                            color = MaterialTheme.brifeColors.textCaption
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
                        color = MaterialTheme.brifeColors.textTitle
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
private fun SimilarNewsSection(
    similarNews: List<NewsListItem>,
    isLoading: Boolean,
    error: String?,
    onNewsClick: (Long) -> Unit
) {
    if (error != null || (!isLoading && similarNews.isEmpty())) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 24.dp)
    ) {
        AppText(
            text = "브리프가 만든 추천 요약 기사예요.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.brifeColors.textTitle,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(2) {
                    SimilarNewsPlaceholderCard()
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = similarNews.take(5),
                    key = { it.id }
                ) { news ->
                    SimilarNewsCard(
                        news = news,
                        onClick = { onNewsClick(news.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SimilarNewsCard(
    news: NewsListItem,
    onClick: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground
    val imageRes = remember(news.groupName, news.categoryName, news.id) {
        getSimilarNewsImageRes(
            categoryName = news.categoryName,
            groupName = news.groupName.orEmpty(),
            newsId = news.id
        )
    }

    Column(
        modifier = Modifier
            .width(184.dp)
            .clickable(onClick = onClick)
    ) {
        if (imageRes != null) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEFF2F5))
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            news.groupName?.takeIf { it.isNotBlank() }?.let {
                CategoryChip(
                    text = it,
                    containerColor = if (isDarkTheme) Gray600 else Gray600
                )
            }
            if (news.categoryName.isNotBlank()) {
                CategoryChip(
                    text = news.categoryName,
                    containerColor = if (isDarkTheme) Gray600 else Gray600
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        AppText(
            text = news.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.brifeColors.textTitle,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 44.dp)
        )
    }
}

private fun getSimilarNewsImageRes(
    categoryName: String,
    groupName: String,
    newsId: Long
): Int? = getSimilarNewsImageResForName(categoryName, newsId)
    ?: getSimilarNewsImageResForName(groupName, newsId)

private fun getSimilarNewsImageResForName(categoryName: String, newsId: Long): Int? {
    val images = when {
        categoryName.contains("책") -> listOf(
            R.drawable.similar_longform_culture_book,
            R.drawable.similar_longform_culture_book_1_1
        )
        categoryName.contains("종교") -> listOf(
            R.drawable.similar_longform_culture_religion_1,
            R.drawable.similar_longform_culture_religion_2,
            R.drawable.similar_longform_culture_religion_3_1
        )
        categoryName.contains("야구") -> listOf(
            R.drawable.similar_longform_entertainment_baseball,
            R.drawable.similar_longform_entertainment_baseball_1_1
        )
        categoryName.contains("농구") -> listOf(
            R.drawable.similar_longform_entertainment_basketball,
            R.drawable.similar_longform_entertainment_basketball_1_1
        )
        categoryName.contains("e스포츠", ignoreCase = true) -> listOf(
            R.drawable.similar_longform_entertainment_esports,
            R.drawable.similar_longform_entertainment_esports_1_1
        )
        categoryName.contains("골프") -> listOf(
            R.drawable.similar_longform_entertainment_golf,
            R.drawable.similar_longform_entertainment_golf_1_1
        )
        categoryName.contains("배구") -> listOf(
            R.drawable.similar_longform_entertainment_volleyball,
            R.drawable.similar_longform_entertainment_volleyball_1_1
        )
        categoryName.contains("아웃도어") -> listOf(
            R.drawable.similar_longform_entertainment_outdoor,
            R.drawable.similar_longform_entertainment_outdoor_1_1
        )
        categoryName.contains("스포츠일반") -> listOf(
            R.drawable.similar_longform_entertainment_general,
            R.drawable.similar_longform_entertainment_general_1_1
        )
        categoryName.contains("자동차") || categoryName.contains("시승기") -> listOf(
            R.drawable.similar_longform_life_car,
            R.drawable.similar_longform_life_car_1_1
        )
        categoryName.contains("도로") || categoryName.contains("교통") -> listOf(
            R.drawable.similar_longform_life_traffic,
            R.drawable.similar_longform_life_traffic_1_1
        )
        categoryName.contains("패션") || categoryName.contains("뷰티") -> listOf(
            R.drawable.similar_longform_life_fashion,
            R.drawable.similar_longform_life_fashion_1_1
        )
        categoryName.contains("음식") || categoryName.contains("맛집") -> listOf(
            R.drawable.similar_longform_life_food,
            R.drawable.similar_longform_life_food_1_1
        )
        categoryName.contains("여행") || categoryName.contains("레저") -> listOf(
            R.drawable.similar_longform_life_travel,
            R.drawable.similar_longform_life_travel_1_1
        )
        categoryName.contains("날씨") -> listOf(
            R.drawable.similar_longform_life_weather,
            R.drawable.similar_longform_life_weather_1_1
        )
        categoryName.contains("산업/재계") -> SimilarEconomyImages
        categoryName.contains("시사") || categoryName.contains("정치") -> SimilarPoliticsImages
        categoryName.contains("경제") || categoryName.contains("재테크") -> SimilarEconomyImages
        categoryName.contains("IT", ignoreCase = true) || categoryName.contains("테크") -> SimilarTechImages
        categoryName.contains("문화") || categoryName.contains("예술") -> SimilarCultureImages
        categoryName.contains("엔터") || categoryName.contains("스포츠") || categoryName.contains("연예") ->
            SimilarEntertainmentImages
        categoryName.contains("라이프") || categoryName.contains("성장") -> SimilarLifeImages
        else -> return null
    }

    val remainder = newsId % images.size.toLong()
    val index = if (remainder >= 0) remainder else remainder + images.size
    return images[index.toInt()]
}

@Composable
private fun SimilarNewsPlaceholderCard() {
    Column(modifier = Modifier.width(184.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFEFF2F5))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .width(72.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEFF2F5))
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFEFF2F5))
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .width(132.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFEFF2F5))
        )
    }
}

@Composable
private fun SummaryCard(
    summaryPoints: List<String>
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDarkTheme) DarkBlue200 else Color(0xFFF5F9FF))
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
                    text = "네줄 간편요약",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isDarkTheme) DarkTextTitle else MaterialTheme.brifeColors.textTitle
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            summaryPoints.take(4).forEach { point ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    AppText(
                        text = "•",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isDarkTheme) DarkTextTitle else PrimaryNormal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AppText(
                        text = point,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isDarkTheme) DarkTextTitle else Color.DarkGray
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
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground
    val iconRes = if (isDarkTheme) {
        when (index) {
            0 -> R.drawable.longform_number1_darkmode
            1 -> R.drawable.longform_number2_darkmode
            else -> R.drawable.longform_number3_darkmode
        }
    } else {
        when (index) {
            0 -> R.drawable.ic_longform_number1
            1 -> R.drawable.ic_longform_number2
            else -> R.drawable.ic_longform_number3
        }
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
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isDarkTheme) DarkTextTitle else MaterialTheme.brifeColors.textSubtitle
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        section.contentList.forEach { line ->
            AppText(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkTheme) DarkTextTitle else MaterialTheme.brifeColors.textBody,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
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
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground

    TopAppBar(
        modifier = Modifier
            .background(MaterialTheme.brifeColors.backgroundDefault)
            .statusBarsPadding()
            .height(NewsLongTopBarContentHeight),
        title = {},
        navigationIcon = {
            Box(modifier = Modifier.padding(start = 12.dp)) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "뒤로가기",
                        tint = MaterialTheme.brifeColors.topBarIcon
                    )
                }
            }
        },
        actions = {
            Row(modifier = Modifier.padding(end = 12.dp)) {
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
                        tint = if (isDarkTheme && !isBookmarked) {
                            DarkTextTitle
                        } else {
                            Color.Unspecified
                        }
                    )
                }

                IconButton(onClick = onShareClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_upload),
                        contentDescription = "공유",
                        tint = MaterialTheme.brifeColors.topBarIcon
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.brifeColors.backgroundDefault,
            scrolledContainerColor = MaterialTheme.brifeColors.backgroundDefault
        ),
        windowInsets = WindowInsets(0, 0, 0, 0)
    )
}

@Composable
private fun NewsLongScrollProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(NewsLongScrollProgressHeight)
            .background(Color(0xFFE7E8EC))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(NewsLongScrollProgressHeight)
                .background(PrimaryNormal)
        )
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
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = if (isDarkTheme) DarkGray300 else MaterialTheme.brifeColors.backgroundDefault,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            AppText(
                text = "관련 뉴스기사",
                style = MaterialTheme.typography.titleMedium,
                color = if (isDarkTheme) DarkTextTitle else MaterialTheme.brifeColors.textTitle,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

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
                            color = if (isDarkTheme) DarkTextTitle else MaterialTheme.brifeColors.textCaption
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
                            color = if (isDarkTheme) DarkTextTitle else MaterialTheme.brifeColors.textCaption
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
                                publishedDate = source.publishedDate,
                                pressName = source.pressName,
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
    publishedDate: String,
    pressName: String,
    onClick: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground
    val formattedDate = formatSourcePublishedDate(publishedDate)
    val metaText = listOf(formattedDate, pressName)
        .filter { it.isNotBlank() }
        .joinToString(" · ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDarkTheme) DarkGray600 else Color(0xFFF7F9FD))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AppText(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkTheme) DarkTextTitle else MaterialTheme.brifeColors.textTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (metaText.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                AppText(
                    text = metaText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkTheme) DarkTextTitle else MaterialTheme.brifeColors.textCaption
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = Color(0xFF9AA3AF)
        )
    }
}

private fun formatSourcePublishedDate(value: String): String {
    return formatPublishedDate(value, SOURCE_PUBLISHED_DATE_FORMATTER)
}

private fun formatLongFormPublishedDate(value: String): String {
    return formatPublishedDate(value, LONG_FORM_PUBLISHED_DATE_FORMATTER)
}

private fun formatPublishedDate(value: String, formatter: DateTimeFormatter): String {
    val trimmedValue = value.trim()
    val dateValue = SOURCE_PUBLISHED_DATE_PATTERN
        .matchEntire(trimmedValue)
        ?.groupValues
        ?.get(1)
        ?: return value

    return runCatching {
        LocalDate.parse(dateValue).format(formatter)
    }.getOrElse { value }
}

private val SOURCE_PUBLISHED_DATE_PATTERN = Regex("""^(\d{4}-\d{2}-\d{2})(?:[T ].*)?$""")
private val SOURCE_PUBLISHED_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private val LONG_FORM_PUBLISHED_DATE_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy.MM.dd EEEE", Locale.KOREAN)
