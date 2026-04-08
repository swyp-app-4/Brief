package com.swyp.brife.feature.explore

import com.swyp.brife.feature.archive.ArchiveNewsItem

sealed class ExploreUiState {

    /** 기본 화면 — 검색창 비활성화, 최근 뉴스 리스트 노출 */
    data class Default(
        val recentNewsList: List<ArchiveNewsItem>,
        val lastUpdatedTime: String = ""
    ) : ExploreUiState()

    /** 검색어 입력 중 — 검색창 활성화, 최근 검색어 리스트 노출 */
    data class Searching(
        val query: String,
        val recentQueries: List<String>
    ) : ExploreUiState()

    /** 검색 결과 — 관련 뉴스 리스트 노출 */
    data class Results(
        val query: String,
        val items: List<ArchiveNewsItem>
    ) : ExploreUiState()

    /** 검색 결과 없음 */
    data class Empty(val query: String) : ExploreUiState()

    /** 네트워크 연결 오류 */
    data class NetworkError(val query: String) : ExploreUiState()

    /** 특수문자 입력 오류 */
    data class SpecialCharError(val query: String) : ExploreUiState()
}
