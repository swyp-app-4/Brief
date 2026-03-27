package com.example.brife.feature.explore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brife.data.local.SearchHistoryLocalStorage

@Composable
fun ExploreRoute(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: ExploreViewModel = viewModel(
        factory = ExploreViewModelFactory(SearchHistoryLocalStorage(context))
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
