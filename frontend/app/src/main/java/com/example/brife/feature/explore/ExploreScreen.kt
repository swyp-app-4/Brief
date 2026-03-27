package com.example.brife.feature.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.feature.archive.ArchiveNewsCard
import com.example.brife.feature.archive.ArchiveNewsItem
import com.example.brife.ui.component.AppText
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.ComponentDefault
import com.example.brife.ui.theme.CtaActive
import com.example.brife.ui.theme.TextCaption
import com.example.brife.ui.theme.TextSubtitle

// ── 메인 화면 ─────────────────────────────────────────────────────────────────

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
                lastUpdatedTime = uiState.lastUpdatedTime
            )
            is ExploreUiState.Searching -> ExploreSearchingBody(
                recentQueries = uiState.recentQueries,
                onRecentQueryClick = onRecentQueryClick,
                onDeleteRecentQuery = onDeleteRecentQuery,
                onClearAllRecentQueries = onClearAllRecentQueries
            )
            is ExploreUiState.Results -> ExploreResultsBody(
                query = uiState.query,
                items = uiState.items
            )
            is ExploreUiState.Empty -> ExploreStateBody(
                text = "앗, 검색 결과가 없어요.\n다른 키워드로 검색해볼까요?",
                leftImageRes = R.drawable.img_explore_empty,
                rightImageRes = R.drawable.img_explore_empty_character
            )
            is ExploreUiState.NetworkError -> ExploreStateBody(
                text = "연결이 원활하지 않아요.\n잠시 후 다시 시도해주세요.",
                leftImageRes = R.drawable.img_explore_network_error_character,
                rightImageRes = R.drawable.img_explore_network_error
            )
            is ExploreUiState.SpecialCharError -> ExploreStateBody(
                text = "특수문자를 제외한\n키워드로 검색해주세요.",
                leftImageRes = R.drawable.img_explore_error_character,
                rightImageRes = R.drawable.img_explore_error
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
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextSubtitle),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch(searchQuery) }),
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
    lastUpdatedTime: String
) {
    LazyColumn(
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
            ArchiveNewsCard(item = item)
        }
    }
}

@Composable
private fun RecentNewsHeader(lastUpdatedTime: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
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
        AppText(
            text = "$lastUpdatedTime 기준",
            style = MaterialTheme.typography.labelSmall,
            color = TextCaption
        )
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
    items: List<ArchiveNewsItem>
) {
    LazyColumn(
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
                    .padding(bottom = 4.dp)
            )
        }
        items(items) { item ->
            ArchiveNewsCard(item = item)
        }
    }
}

// ── 에러 / 빈 결과 공통 UI ──────────────────────────────────────────────────────

@Composable
private fun ExploreStateBody(
    text: String,
    leftImageRes: Int,
    rightImageRes: Int
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = leftImageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .height(100.dp)
                        .offset(x = (-20).dp) // 왼쪽 이미지를 중앙에서 왼쪽으로 15dp 이동
                )
                Image(
                    painter = painterResource(id = rightImageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .height(100.dp)
                        .offset(x = 20.dp)  // 오른쪽 이미지를 중앙에서 오른쪽으로 15dp 이동
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            AppText(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextCaption,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "탐색 - 기본 화면")
@Composable
private fun ExploreDefaultPreview() {
    BrifeTheme {
        ExploreScreen(
            uiState = ExploreUiState.Default(
                recentNewsList = listOf(
                    ArchiveNewsItem("미국 연준, 기준금리 동결 결정", "연방준비제도가 이번 FOMC 회의에서 기준금리를 현 수준에서 동결하기로 결정했다.", "2시간 전", "한국경제", R.drawable.homescreen_bg),
                    ArchiveNewsItem("애플, AI 기능 탑재한 아이폰 17 공개", "애플이 차세대 아이폰에 온디바이스 AI 기능을 전면 탑재한다고 발표했다.", "4시간 전", "조선일보", R.drawable.homescreen_bg),
                    ArchiveNewsItem("국내 부동산 시장 안정세 지속", "수도권 아파트 가격이 3개월 연속 보합세를 유지하며 안정세를 이어가고 있다.", "6시간 전", "매일경제", R.drawable.homescreen_bg)
                )
            ),
            searchQuery = "",
            onSearchBarClick = {}, onQueryChange = {}, onSearch = {},
            onBackFromSearch = {}, onClearQuery = {}, onDeleteRecentQuery = {},
            onClearAllRecentQueries = {}, onRecentQueryClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "탐색 - 검색어 입력 중")
@Composable
private fun ExploreSearchingPreview() {
    BrifeTheme {
        ExploreScreen(
            uiState = ExploreUiState.Searching(
                query = "경제",
                recentQueries = listOf("연준", "아이폰", "부동산")
            ),
            searchQuery = "경제",
            onSearchBarClick = {}, onQueryChange = {}, onSearch = {},
            onBackFromSearch = {}, onClearQuery = {}, onDeleteRecentQuery = {},
            onClearAllRecentQueries = {}, onRecentQueryClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "탐색 - 검색 결과")
@Composable
private fun ExploreResultsPreview() {
    BrifeTheme {
        ExploreScreen(
            uiState = ExploreUiState.Results(
                query = "경제",
                items = listOf(
                    ArchiveNewsItem("미국 연준, 기준금리 동결 결정", "연방준비제도가 기준금리를 동결하기로 결정했다.", "2시간 전", "한국경제", R.drawable.homescreen_bg)
                )
            ),
            searchQuery = "경제",
            onSearchBarClick = {}, onQueryChange = {}, onSearch = {},
            onBackFromSearch = {}, onClearQuery = {}, onDeleteRecentQuery = {},
            onClearAllRecentQueries = {}, onRecentQueryClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "탐색 - 결과 없음")
@Composable
private fun ExploreEmptyPreview() {
    BrifeTheme {
        ExploreScreen(
            uiState = ExploreUiState.Empty(query = "알수없는키워드"),
            searchQuery = "알수없는키워드",
            onSearchBarClick = {}, onQueryChange = {}, onSearch = {},
            onBackFromSearch = {}, onClearQuery = {}, onDeleteRecentQuery = {},
            onClearAllRecentQueries = {}, onRecentQueryClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "탐색 - 네트워크 오류")
@Composable
private fun ExploreNetworkErrorPreview() {
    BrifeTheme {
        ExploreScreen(
            uiState = ExploreUiState.NetworkError(query = "경제"),
            searchQuery = "경제",
            onSearchBarClick = {}, onQueryChange = {}, onSearch = {},
            onBackFromSearch = {}, onClearQuery = {}, onDeleteRecentQuery = {},
            onClearAllRecentQueries = {}, onRecentQueryClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "탐색 - 특수문자 오류")
@Composable
private fun ExploreSpecialCharErrorPreview() {
    BrifeTheme {
        ExploreScreen(
            uiState = ExploreUiState.SpecialCharError(query = "경제!@#"),
            searchQuery = "경제!@#",
            onSearchBarClick = {}, onQueryChange = {}, onSearch = {},
            onBackFromSearch = {}, onClearQuery = {}, onDeleteRecentQuery = {},
            onClearAllRecentQueries = {}, onRecentQueryClick = {}
        )
    }
}
