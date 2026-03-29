package com.example.brife.feature.explore

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brife.data.local.SearchHistoryLocalStorage
import com.example.brife.data.model.NewsListItem
import com.example.brife.data.repository.ExploreRepository
import com.example.brife.feature.archive.ArchiveNewsItem
import com.example.brife.feature.home.LongFormImageProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val searchHistoryStorage: SearchHistoryLocalStorage,
    private val exploreRepository: ExploreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(
        ExploreUiState.Default(recentNewsList = exploreMockNewsList)
    )
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // API 성공 시 캐시 — 검색 후 뒤로가기 시 재사용
    private var cachedLatestNews: List<ArchiveNewsItem> = exploreMockNewsList

    init {
        loadLatestNews()
    }

    private fun loadLatestNews() {
        viewModelScope.launch {
            exploreRepository.getLatestNews()
                .onSuccess { items ->
                    Log.d("ExploreViewModel", "loadLatestNews: ${items.size}개 수신")
                    val archiveItems = items.mapIndexed { index, item -> item.toArchiveNewsItem(index) }
                    cachedLatestNews = archiveItems
                    _uiState.value = ExploreUiState.Default(recentNewsList = archiveItems)
                }
                .onFailure { e ->
                    Log.e("ExploreViewModel", "loadLatestNews 실패: ${e.message} → mock 데이터 사용")
                    _uiState.value = ExploreUiState.Default(recentNewsList = exploreMockNewsList)
                }
        }
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

        viewModelScope.launch {
            exploreRepository.searchNews(trimmed)
                .onSuccess { items ->
                    Log.d("ExploreViewModel", "searchNews '${trimmed}': ${items.size}개 수신")
                    val archiveItems = items.mapIndexed { index, item -> item.toArchiveNewsItem(index) }
                    _uiState.value = if (archiveItems.isEmpty()) {
                        ExploreUiState.Empty(trimmed)
                    } else {
                        ExploreUiState.Results(trimmed, archiveItems)
                    }
                }
                .onFailure { e ->
                    Log.e("ExploreViewModel", "searchNews '${trimmed}' 실패: ${e.message}")
                    _uiState.value = ExploreUiState.NetworkError(trimmed)
                }
        }
    }

    /** 검색 상태에서 뒤로가기 → 기본 화면으로 복귀 */
    fun onBackFromSearch() {
        _searchQuery.value = ""
        _uiState.value = ExploreUiState.Default(recentNewsList = cachedLatestNews)
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
// - imageUrl: categoryName + index 기반 결정론적 이미지
private fun NewsListItem.toArchiveNewsItem(index: Int) = ArchiveNewsItem(
    title = title,
    summary = "",
    time = publishedDate,
    company = categoryName,
    imageUrl = LongFormImageProvider.getStableImageRes(categoryName, index),
    newsId = id
)

// ── fallback Mock 데이터 (API 실패 시 표시) ──────────────────────────────────
private val exploreMockNewsList = listOf(
    ArchiveNewsItem(
        title = "미국 연준, 기준금리 동결 결정",
        summary = "연방준비제도가 이번 FOMC 회의에서 기준금리를 현 수준에서 동결하기로 결정했다.",
        time = "2시간 전",
        company = "한국경제",
        imageUrl = LongFormImageProvider.getStableImageRes("경제 · 재테크", 0)
    ),
    ArchiveNewsItem(
        title = "애플, AI 기능 탑재한 아이폰 17 공개",
        summary = "애플이 차세대 아이폰에 온디바이스 AI 기능을 전면 탑재한다고 발표했다.",
        time = "4시간 전",
        company = "조선일보",
        imageUrl = LongFormImageProvider.getStableImageRes("IT · 테크", 0)
    ),
    ArchiveNewsItem(
        title = "국내 부동산 시장 안정세 지속",
        summary = "수도권 아파트 가격이 3개월 연속 보합세를 유지하며 안정세를 이어가고 있다.",
        time = "6시간 전",
        company = "매일경제",
        imageUrl = LongFormImageProvider.getStableImageRes("경제 · 재테크", 1)
    ),
    ArchiveNewsItem(
        title = "국내 전기차 판매량, 전년 대비 30% 증가",
        summary = "올해 상반기 국내 전기차 신규 등록 대수가 전년 동기 대비 30% 증가한 것으로 집계됐다.",
        time = "8시간 전",
        company = "동아일보",
        imageUrl = LongFormImageProvider.getStableImageRes("IT · 테크", 1)
    ),
    ArchiveNewsItem(
        title = "정부, 청년 주거 지원 정책 강화 발표",
        summary = "국토교통부가 청년층 주거 부담 완화를 위한 새로운 지원 정책 패키지를 발표했다.",
        time = "10시간 전",
        company = "연합뉴스",
        imageUrl = LongFormImageProvider.getStableImageRes("시사 · 정치", 0)
    )
)
