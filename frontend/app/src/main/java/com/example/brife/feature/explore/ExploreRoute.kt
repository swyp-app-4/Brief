package com.example.brife.feature.explore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brife.data.local.SearchHistoryLocalStorage
import com.example.brife.data.remote.NetworkModule
import com.example.brife.data.repository.ExploreRepository

@Composable
fun ExploreRoute(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: ExploreViewModel = viewModel(
        factory = ExploreViewModelFactory(
            searchHistoryStorage = SearchHistoryLocalStorage(context),
            exploreRepository = remember { ExploreRepository(NetworkModule.exploreApiService) }
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
        modifier = modifier
    )
}
