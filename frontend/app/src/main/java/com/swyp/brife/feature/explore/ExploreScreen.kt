package com.swyp.brife.feature.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swyp.brife.R
import com.swyp.brife.feature.archive.ArchiveNewsCard
import com.swyp.brife.feature.archive.ArchiveNewsItem
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.theme.BgDefault
import com.swyp.brife.ui.theme.ComponentDefault
import com.swyp.brife.ui.theme.CtaActive
import com.swyp.brife.ui.theme.TextCaption
import com.swyp.brife.ui.theme.TextSubtitle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

// ── 메인 화면 ─────────────────────────────────────────────────────────────────

private const val SEARCH_QUERY_MAX_LENGTH = 20

@Composable
fun ExploreScreen(
    uiState: ExploreUiState,
    searchQuery: String,
    onSearchBarClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onBackFromSearch: () -> Unit,
    onClearQuery: () -> Unit,
    onDeleteRecentQuery: (String) -> Unit,
    onClearAllRecentQueries: () -> Unit,
    onRecentQueryClick: (String) -> Unit,
    onLoadMoreLatestNews: () -> Unit,
    onLoadMoreSearchResults: () -> Unit,
    onNewsClick: ((ArchiveNewsItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isSearchActive = uiState !is ExploreUiState.Default

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ExploreTopBar(
            isSearchActive = isSearchActive,
            searchQuery = searchQuery,
            onSearchBarClick = onSearchBarClick,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            onBackFromSearch = onBackFromSearch,
            onClearQuery = onClearQuery
        )

        when (uiState) {
            is ExploreUiState.Default -> ExploreDefaultBody(
                recentNewsList = uiState.recentNewsList,
                lastUpdatedTime = uiState.lastUpdatedTime,
                isLoadingMore = uiState.isLoadingMore,
                hasNextPage = uiState.hasNextPage,
                onLoadMore = onLoadMoreLatestNews,
                onNewsClick = onNewsClick
            )
            is ExploreUiState.Searching -> ExploreSearchingBody(
                recentQueries = uiState.recentQueries,
                onRecentQueryClick = onRecentQueryClick,
                onDeleteRecentQuery = onDeleteRecentQuery,
                onClearAllRecentQueries = onClearAllRecentQueries
            )
            is ExploreUiState.Results -> ExploreResultsBody(
                query = uiState.query,
                items = uiState.items,
                isLoadingMore = uiState.isLoadingMore,
                hasNextPage = uiState.hasNextPage,
                onLoadMore = onLoadMoreSearchResults,
                onNewsClick = onNewsClick
            )
            is ExploreUiState.Empty -> ExploreStateBody(
                text = "앗, 검색 결과가 없어요.\n다른 키워드로 검색해볼까요?",
                backgroundImageRes = R.drawable.img_explore_empty,
                characterImageRes = R.drawable.img_explore_empty_character,
                backgroundModifier = Modifier
                    .size(60.dp)
                    .offset(x = (-60).dp, y = (-10).dp) // 여기서 상하좌우 위치 조절
            )
            is ExploreUiState.NetworkError -> ExploreStateBody(
                text = "연결이 원활하지 않아요.\n잠시 후 다시 시도해주세요.",
                characterImageRes = R.drawable.img_explore_network_error_character,
                backgroundImageRes = R.drawable.img_explore_network_error,
                backgroundModifier = Modifier
                    .size(100.dp)
                    .offset(x = (60).dp, y = (-40).dp) // 여기서 상하좌우 위치 조절
            )
            is ExploreUiState.SpecialCharError -> ExploreStateBody(
                text = "특수문자를 제외한\n키워드로 검색해주세요.",
                characterImageRes = R.drawable.img_explore_error_character,
                backgroundImageRes = R.drawable.img_explore_error,
                backgroundModifier = Modifier
                    .size(75.dp)
                    .offset(x = (70).dp, y = (-50).dp) // 여기서 상하좌우 위치 조절
            )
        }
    }
}

// ── TopBar ────────────────────────────────────────────────────────────────────

@Composable
private fun ExploreTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchBarClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onBackFromSearch: () -> Unit,
    onClearQuery: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // 검색 활성화 시 키보드 자동 포커스
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSearchActive) {
            IconButton(
                onClick = onBackFromSearch,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = Color.Unspecified
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(ComponentDefault)
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .then(
                    if (!isSearchActive) Modifier.clickable { onSearchBarClick() }
                    else Modifier
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_explore_search),
                contentDescription = null,
                tint = TextCaption,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            if (isSearchActive) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { newQuery ->
                        if (newQuery.length <= SEARCH_QUERY_MAX_LENGTH) {
                            onQueryChange(newQuery)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextSubtitle),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onSearch(searchQuery)
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(CtaActive),
                    decorationBox = { innerTextField ->
                        Box {
                            if (searchQuery.isEmpty()) {
                                AppText(
                                    text = "검색어를 입력해주세요",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextCaption
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                if (searchQuery.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_explore_recent_clear),
                        contentDescription = "검색어 초기화",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onClearQuery() }
                    )
                }
            } else {
                AppText(
                    text = "검색어를 입력해주세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextCaption,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ── 기본 화면 본문 ─────────────────────────────────────────────────────────────

@Composable
private fun ExploreDefaultBody(
    recentNewsList: List<ArchiveNewsItem>,
    lastUpdatedTime: String,
    isLoadingMore: Boolean,
    hasNextPage: Boolean,
    onLoadMore: () -> Unit,
    onNewsClick: ((ArchiveNewsItem) -> Unit)? = null
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showTopButton = remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }

    LaunchedEffect(listState, recentNewsList.size, isLoadingMore, hasNextPage) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalCount = layoutInfo.totalItemsCount
            totalCount > 0 && lastVisibleIndex >= totalCount - 5
        }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore && hasNextPage && !isLoadingMore) {
                    onLoadMore()
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(top = 8.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                RecentNewsHeader(
                    lastUpdatedTime = lastUpdatedTime,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
            }
            items(recentNewsList) { item ->
                ArchiveNewsCard(
                    item = item,
                    onClick = onNewsClick?.let { { it(item) } }
                )
            }
            if (isLoadingMore) {
                item {
                    BottomLoadingIndicator()
                }
            }
        }

        if (showTopButton.value) {
            ExploreScrollTopButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
            )
        }
    }
}

@Composable
private fun RecentNewsHeader(lastUpdatedTime: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(start = 16.dp, bottom = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = CtaActive)) { append("최근 ") }
                append("뉴스")
            },
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextSubtitle
        )

        if (lastUpdatedTime.isNotBlank()) {
            AppText(
                text = "$lastUpdatedTime 기준",
                style = MaterialTheme.typography.labelSmall,
                color = TextCaption
            )
        }
    }
}

// ── 검색어 입력 중 본문 ────────────────────────────────────────────────────────

@Composable
private fun ExploreSearchingBody(
    recentQueries: List<String>,
    onRecentQueryClick: (String) -> Unit,
    onDeleteRecentQuery: (String) -> Unit,
    onClearAllRecentQueries: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = "최근 검색어",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextSubtitle
            )
            AppText(
                text = "전체 삭제",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextCaption,
                modifier = Modifier.clickable { onClearAllRecentQueries() }
            )
        }

        recentQueries.forEach { query ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRecentQueryClick(query) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_explore_recent),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                AppText(
                    text = query,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextCaption,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_explore_clear),
                    contentDescription = "삭제",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDeleteRecentQuery(query) }
                )
            }
        }
    }
}

// ── 검색 결과 본문 ─────────────────────────────────────────────────────────────

@Composable
private fun ExploreResultsBody(
    query: String,
    items: List<ArchiveNewsItem>,
    isLoadingMore: Boolean,
    hasNextPage: Boolean,
    onLoadMore: () -> Unit,
    onNewsClick: ((ArchiveNewsItem) -> Unit)? = null
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showTopButton = remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }

    LaunchedEffect(listState, items.size, isLoadingMore, hasNextPage) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalCount = layoutInfo.totalItemsCount
            totalCount > 0 && lastVisibleIndex >= totalCount - 5
        }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore && hasNextPage && !isLoadingMore) {
                    onLoadMore()
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(top = 8.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
        item {

            AppText(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = CtaActive)) { append(query) }
                    append(" 관련 뉴스")
                },
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextSubtitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 4.dp)
            )
        }
        items(items) { item ->
            ArchiveNewsCard(
                item = item,
                onClick = onNewsClick?.let { { it(item) } }
            )
        }
        if (isLoadingMore) {
            item {
                BottomLoadingIndicator()
            }
        }
        }

        if (showTopButton.value) {
            ExploreScrollTopButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
            )
        }
    }
}

@Composable
private fun ExploreScrollTopButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSystemInDarkTheme()) {
        Color(0xFF212328).copy(alpha = 0.9f)
    } else {
        BgDefault.copy(alpha = 0.9f)
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_direction_right),
            contentDescription = "최상단으로 이동",
            tint = Color.Unspecified,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun BottomLoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = CtaActive,
            strokeWidth = 2.dp
        )
    }
}

// ── 에러 / 빈 결과 공통 UI ──────────────────────────────────────────────────────

@Composable
private fun ExploreStateBody(
    text: String,
    backgroundImageRes: Int,
    characterImageRes: Int,
    // 배경 이미지의 위치나 크기를 조절하기 위한 파라미터
    backgroundModifier: Modifier = Modifier.size(100.dp)
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center // 모든 요소를 기본적으로 중앙 정렬
        ) {
            // 1. 배경 이미지: 파라미터로 받은 modifier를 통해 상하좌우 offset이나 size 조절 가능
            Image(
                painter = painterResource(id = backgroundImageRes),
                contentDescription = null,
                modifier = backgroundModifier
            )

            // 2. 캐릭터 이미지: 무조건 Horizontal Center 고정
            Image(
                painter = painterResource(id = characterImageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp) // 캐릭터 사이즈 고정 혹은 필요시 파라미터화
                // 가로 중앙은 Box의 Alignment.Center에 의해 고정됨
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AppText(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            color = TextCaption,
            textAlign = TextAlign.Center
        )
    }
}
