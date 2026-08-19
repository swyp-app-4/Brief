package com.swyp.brife.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.brife.data.local.SearchHistoryLocalStorage
import com.swyp.brife.data.model.NewsDetailResponse
import com.swyp.brife.data.model.NewsListItem
import com.swyp.brife.data.repository.ExploreRepository
import com.swyp.brife.feature.archive.ArchiveNewsItem
import com.swyp.brife.feature.home.LongFormImageProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExploreViewModel(
    private val searchHistoryStorage: SearchHistoryLocalStorage,
    private val exploreRepository: ExploreRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(
        ExploreUiState.Default(recentNewsList = exploreMockNewsList)
    )
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // API 성공 시 캐시 — 검색 후 뒤로가기 시 재사용
    private var cachedLatestNews: List<ArchiveNewsItem> = exploreMockNewsList
    private var cachedLatestUpdatedTime: String = ""

    private val pageSize = 20

    private var latestPage = -1
    private var isLatestLoading = false
    private var hasNextLatestPage = true
    private val requestedLatestPages = mutableSetOf<Int>()

    private var searchPage = -1
    private var currentSearchKeyword = ""
    private var isSearchLoading = false
    private var hasNextSearchPage = true
    private val requestedSearchPages = mutableSetOf<Int>()

    init {
        loadLatestNews()
    }

    private fun loadLatestNews(reset: Boolean = true) {
        if (isLatestLoading) return
        if (!reset && !hasNextLatestPage) return

        val targetPage = if (reset) 0 else latestPage + 1
        if (requestedLatestPages.contains(targetPage)) return

        isLatestLoading = true
        requestedLatestPages.add(targetPage)

        if (!reset) {
            (_uiState.value as? ExploreUiState.Default)?.let {
                _uiState.value = it.copy(isLoadingMore = true)
            }
        }

        viewModelScope.launch {
            exploreRepository.getLatestNews(page = targetPage, size = pageSize)
                .onSuccess { pageResponse ->
                    val archiveItems = pageResponse.content.map { item ->
                        async {
                            item.toArchiveNewsItem(
                                detail = exploreRepository.getNewsDetail(item.id).getOrNull()
                            ).let { archiveItem ->
                                archiveItem.copy(
                                    time = formatExplorePublishedDate(archiveItem.time)
                                )
                            }
                        }
                    }.awaitAll()

                    val mergedItems = if (reset) archiveItems else cachedLatestNews + archiveItems
                    cachedLatestNews = mergedItems

                    if (reset) {
                        val rawDate = pageResponse.content.firstOrNull()?.publishedDate ?: ""
                        cachedLatestUpdatedTime = formatExplorePublishedDate(rawDate)
                    }

                    latestPage = targetPage
                    hasNextLatestPage = !pageResponse.last

                    _uiState.value = ExploreUiState.Default(
                        recentNewsList = mergedItems,
                        lastUpdatedTime = cachedLatestUpdatedTime,
                        isLoadingMore = false,
                        hasNextPage = hasNextLatestPage
                    )
                }
                .onFailure {
                    requestedLatestPages.remove(targetPage)
                    if (reset) {
                        _uiState.value = ExploreUiState.Default(
                            recentNewsList = exploreMockNewsList,
                            lastUpdatedTime = "",
                            isLoadingMore = false,
                            hasNextPage = false
                        )
                    } else {
                        (_uiState.value as? ExploreUiState.Default)?.let {
                            _uiState.value = it.copy(isLoadingMore = false)
                        }
                    }
                }

            isLatestLoading = false
        }
    }

    fun loadMoreLatestNews() {
        loadLatestNews(reset = false)
    }

    /** 기본 화면에서 검색창 탭 → 검색 입력 상태로 전환 */
    fun onSearchBarClick() {
        _searchQuery.value = ""
        _uiState.value = ExploreUiState.Searching(
            query = "",
            recentQueries = searchHistoryStorage.getRecentQueries()
        )
    }

    /** 검색창 텍스트 변경 */
    fun onQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.value = ExploreUiState.Searching(
            query = query,
            recentQueries = searchHistoryStorage.getRecentQueries()
        )
    }

    /** 검색 실행 (키보드 검색 버튼 또는 엔터) */
    fun onSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        if (containsSpecialChar(trimmed)) {
            _uiState.value = ExploreUiState.SpecialCharError(trimmed)
            return
        }

        searchHistoryStorage.addQuery(trimmed)

        currentSearchKeyword = trimmed
        searchPage = -1
        hasNextSearchPage = true
        requestedSearchPages.clear()
        loadSearchPage(reset = true)
    }

    private fun loadSearchPage(reset: Boolean) {
        if (isSearchLoading) return
        if (!reset && !hasNextSearchPage) return

        val targetPage = if (reset) 0 else searchPage + 1
        if (requestedSearchPages.contains(targetPage)) return

        isSearchLoading = true
        requestedSearchPages.add(targetPage)

        if (!reset) {
            (_uiState.value as? ExploreUiState.Results)?.let {
                _uiState.value = it.copy(isLoadingMore = true)
            }
        }

        viewModelScope.launch {
            exploreRepository.searchNews(
                keyword = currentSearchKeyword,
                page = targetPage,
                size = pageSize
            )
                .onSuccess { pageResponse ->
                    val archiveItems = pageResponse.content.map { item ->
                        async {
                            item.toArchiveNewsItem(
                                detail = exploreRepository.getNewsDetail(item.id).getOrNull()
                            ).let { archiveItem ->
                                archiveItem.copy(
                                    time = formatExplorePublishedDate(archiveItem.time)
                                )
                            }
                        }
                    }.awaitAll()

                    val previousItems = if (reset) {
                        emptyList()
                    } else {
                        (_uiState.value as? ExploreUiState.Results)?.items.orEmpty()
                    }
                    val mergedItems = previousItems + archiveItems

                    searchPage = targetPage
                    hasNextSearchPage = !pageResponse.last

                    _uiState.value = if (mergedItems.isEmpty()) {
                        ExploreUiState.Empty(currentSearchKeyword)
                    } else {
                        ExploreUiState.Results(
                            query = currentSearchKeyword,
                            items = mergedItems,
                            isLoadingMore = false,
                            hasNextPage = hasNextSearchPage
                        )
                    }
                }
                .onFailure {
                    requestedSearchPages.remove(targetPage)
                    if (reset) {
                        _uiState.value = ExploreUiState.NetworkError(currentSearchKeyword)
                    } else {
                        (_uiState.value as? ExploreUiState.Results)?.let {
                            _uiState.value = it.copy(isLoadingMore = false)
                        }
                    }
                }

            isSearchLoading = false
        }
    }

    fun loadMoreSearchResults() {
        loadSearchPage(reset = false)
    }

    /** 검색 상태에서 뒤로가기 → 기본 화면으로 복귀 */
    fun onBackFromSearch() {
        _searchQuery.value = ""
        _uiState.value = ExploreUiState.Default(
            recentNewsList = cachedLatestNews,
            lastUpdatedTime = cachedLatestUpdatedTime,
            isLoadingMore = false,
            hasNextPage = hasNextLatestPage
        )
    }

    /** 검색창 텍스트 초기화 */
    fun onClearQuery() {
        _searchQuery.value = ""
        _uiState.value = ExploreUiState.Searching(
            query = "",
            recentQueries = searchHistoryStorage.getRecentQueries()
        )
    }

    /** 최근 검색어 개별 삭제 */
    fun onDeleteRecentQuery(query: String) {
        searchHistoryStorage.removeQuery(query)
        _uiState.value = ExploreUiState.Searching(
            query = _searchQuery.value,
            recentQueries = searchHistoryStorage.getRecentQueries()
        )
    }

    /** 최근 검색어 전체 삭제 */
    fun onClearAllRecentQueries() {
        searchHistoryStorage.clearAll()
        _uiState.value = ExploreUiState.Searching(
            query = _searchQuery.value,
            recentQueries = emptyList()
        )
    }

    /** 최근 검색어 항목 탭 → 해당 검색어로 바로 검색 */
    fun onRecentQueryClick(query: String) {
        _searchQuery.value = query
        onSearch(query)
    }

    private fun containsSpecialChar(query: String): Boolean {
        return query.any { it in "!@#\$%^&*()+=[]{}|;':\",./<>?\\`~" }
    }
}

// NewsListItem → ArchiveNewsItem 매핑
// - summary: API list 미제공 → ""
// - company: categoryName으로 대체
// - imageUrl: newsId (id) 기반 결정론적 이미지로 수정
private val exploreDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

private fun formatExplorePublishedDate(value: String): String = runCatching {
    LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE).format(exploreDateFormatter)
}.getOrDefault(value)

private fun NewsListItem.toArchiveNewsItem(detail: NewsDetailResponse?) = ArchiveNewsItem(
    title = title.ifBlank { detail?.title?.ifBlank { "뉴스 #$id" } ?: "뉴스 #$id" },
    summary = "",
    time = publishedDate.ifBlank { detail?.publishedDate?.ifBlank { "-" } ?: "-" },
    company = detail?.groupName?.ifBlank { categoryName } ?: categoryName,
    imageUrl = LongFormImageProvider.getStableImageRes(
        detail?.groupName ?: categoryName,
        detail?.categoryName ?: categoryName,
        id
    ),
    newsId = id
)

// ── fallback Mock 데이터 (API 실패 시 표시) ──────────────────────────────────
private val exploreMockNewsList = listOf(
    ArchiveNewsItem(
        title = "미국 연준, 기준금리 동결 결정",
        summary = "연방준비제도가 이번 FOMC 회의에서 기준금리를 현 수준에서 동결하기로 결정했다.",
        time = "2시간 전",
        company = "한국경제",
        imageUrl = LongFormImageProvider.getStableImageRes("경제 · 재테크", "", 1001L),
        newsId = 1001L
    ),
    ArchiveNewsItem(
        title = "애플, AI 기능 탑재한 아이폰 17 공개",
        summary = "애플이 차세대 아이폰에 온디바이스 AI 기능을 전면 탑재한다고 발표했다.",
        time = "4시간 전",
        company = "조선일보",
        imageUrl = LongFormImageProvider.getStableImageRes("IT · 테크", "", 1002L),
        newsId = 1002L
    ),
    ArchiveNewsItem(
        title = "국내 부동산 시장 안정세 지속",
        summary = "수도권 아파트 가격이 3개월 연속 보합세를 유지하며 안정세를 이어가고 있다.",
        time = "6시간 전",
        company = "매일경제",
        imageUrl = LongFormImageProvider.getStableImageRes("경제 · 재테크", "", 1003L),
        newsId = 1003L
    ),
    ArchiveNewsItem(
        title = "국내 전기차 판매량, 전년 대비 30% 증가",
        summary = "올해 상반기 국내 전기차 신규 등록 대수가 전년 동기 대비 30% 증가한 것으로 집계됐다.",
        time = "8시간 전",
        company = "동아일보",
        imageUrl = LongFormImageProvider.getStableImageRes("IT · 테크", "", 1004L),
        newsId = 1004L
    ),
    ArchiveNewsItem(
        title = "정부, 청년 주거 지원 정책 강화 발표",
        summary = "국토교통부가 청년층 주거 부담 완화를 위한 새로운 지원 정책 패키지를 발표했다.",
        time = "10시간 전",
        company = "연합뉴스",
        imageUrl = LongFormImageProvider.getStableImageRes("시사 · 정치", "", 1005L),
        newsId = 1005L
    )
)
