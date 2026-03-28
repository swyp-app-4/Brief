package com.example.brife.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.remote.NetworkModule
import com.example.brife.data.repository.HomeRepository

@Composable
fun HomeRoute(
    isLoggedIn: Boolean,
    onLoginRequired: () -> Unit,
    onDetailClick: (HomeNewsCardItem) -> Unit,
    onShareClick: (HomeNewsCardItem) -> Unit,
    topPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            HomeRepository(
                api = NetworkModule.homeApiService,
                authLocalStorage = AuthLocalStorage(context)
            )
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    HomeScreen(
        newsList = uiState.newsList,
        isLoggedIn = isLoggedIn,
        isLoading = uiState.isLoading,
        onLoginRequired = onLoginRequired,
        onDetailClick = onDetailClick,
        onShareClick = onShareClick,
        topPadding = topPadding,
        modifier = modifier
    )
}
