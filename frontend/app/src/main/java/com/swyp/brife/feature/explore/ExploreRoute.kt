package com.swyp.brife.feature.explore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swyp.brife.data.local.SearchHistoryLocalStorage
import com.swyp.brife.data.remote.NetworkModule
import com.swyp.brife.data.repository.ExploreRepository
import com.swyp.brife.feature.archive.ArchiveNewsItem

@Composable
fun ExploreRoute(
    onNewsClick: ((ArchiveNewsItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: ExploreViewModel = viewModel(
        factory = ExploreViewModelFactory(
            searchHistoryStorage = SearchHistoryLocalStorage(context),
            exploreRepository = remember {
                ExploreRepository(
                    NetworkModule.exploreApiService,
                    NetworkModule.newsApiService
                )
            }
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    ExploreScreen(
        uiState = uiState,
        searchQuery = searchQuery,
        onSearchBarClick = viewModel::onSearchBarClick,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::onSearch,
        onBackFromSearch = viewModel::onBackFromSearch,
        onClearQuery = viewModel::onClearQuery,
        onDeleteRecentQuery = viewModel::onDeleteRecentQuery,
        onClearAllRecentQueries = viewModel::onClearAllRecentQueries,
        onRecentQueryClick = viewModel::onRecentQueryClick,
        onLoadMoreLatestNews = viewModel::loadMoreLatestNews,
        onLoadMoreSearchResults = viewModel::loadMoreSearchResults,
        onNewsClick = onNewsClick,
        modifier = modifier
    )
}
