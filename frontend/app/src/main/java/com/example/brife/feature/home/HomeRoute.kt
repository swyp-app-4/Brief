package com.example.brife.feature.home

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.local.OnboardingLocalStorage
import com.example.brife.data.remote.NetworkModule
import com.example.brife.data.repository.HomeRepository

@Composable
fun HomeRoute(
    isLoggedIn: Boolean,
    // 관심사 재설정 완료 시 MainScreen에서 이 값을 증가시켜 홈 뉴스 재로드를 트리거
    reloadVersion: Int = 0,
    onLoginRequired: () -> Unit,
    onDetailClick: (HomeNewsCardItem) -> Unit,
    onShareClick: (HomeNewsCardItem) -> Unit,
    topPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            remember {
                HomeRepository(
                    api = NetworkModule.homeApiService,
                    authLocalStorage = AuthLocalStorage(context),
                    onboardingLocalStorage = OnboardingLocalStorage(context)
                )
            }
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    // reloadVersion이 0보다 커지면 관심사 재설정 완료 신호 → 홈 뉴스 재로드
    LaunchedEffect(reloadVersion) {
        if (reloadVersion > 0) {
            Log.d("HomeRoute", "관심사 재설정 후 홈 뉴스 재로드 (reloadVersion=$reloadVersion)")
            viewModel.loadHomeNews()
        }
    }

    Log.d(
        "HomeRoute",
        "전달 직전 newsList=${uiState.newsList.size}, isLoading=${uiState.isLoading}, error=${uiState.errorMessage}"
    )
    uiState.newsList.forEach {
        Log.d(
            "HomeRoute",
            "item category='${it.category}', subCategory='${it.subCategory}', title='${it.title}'"
        )
    }

    HomeScreen(
        newsList = uiState.newsList,
        isLoggedIn = isLoggedIn,
        isLoading = uiState.isLoading,
        onLoginRequired = onLoginRequired,
        onDetailClick = onDetailClick,
        onShareClick = onShareClick,
        topPadding = topPadding
    )
}
