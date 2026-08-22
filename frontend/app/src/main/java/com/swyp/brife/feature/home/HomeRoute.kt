package com.swyp.brife.feature.home

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swyp.brife.data.local.AuthLocalStorage
import com.swyp.brife.data.local.OnboardingLocalStorage
import com.swyp.brife.data.remote.NetworkModule
import com.swyp.brife.data.repository.HomeRepository

@Composable
fun HomeRoute(
    isLoggedIn: Boolean,
    sessionVersion: Int = 0,
    // 관심사 재설정 완료 시 MainScreen에서 이 값을 증가시켜 홈 뉴스 재로드를 트리거
    reloadVersion: Int = 0,
    notificationPrimaryNewsId: Long? = null,
    notificationAnchorVersion: Int = 0,
    onNotificationAnchorConsumed: () -> Unit = {},
    initialPage: Int = 0, // ★ 추가
    forceResetToThirdPageKey: Int = 0,
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
                    authApi = NetworkModule.authApiService,
                    authLocalStorage = AuthLocalStorage(context),
                    onboardingLocalStorage = OnboardingLocalStorage(context)
                )
            }
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    // B: 뒤로가기 후 홈 카드 위치 복원용 ViewModel 상태
    val savedPageIndex by viewModel.savedPageIndex.collectAsState()
    var consumedNotificationAnchorVersion by rememberSaveable { mutableIntStateOf(-1) }

    // 세션이 변경될 때마다 홈 뉴스 재로드 (로그인/로그아웃 시 최신 관심사 반영)
    // 초기 로드 역할도 겸함 (HomeViewModel에서 init 블록 제거)
    LaunchedEffect(sessionVersion, reloadVersion, notificationAnchorVersion) {
        val anchorNewsId = if (
            notificationPrimaryNewsId != null &&
            notificationAnchorVersion > consumedNotificationAnchorVersion
        ) {
            consumedNotificationAnchorVersion = notificationAnchorVersion
            onNotificationAnchorConsumed()
            notificationPrimaryNewsId.takeIf { isLoggedIn }
        } else {
            null
        }
        Log.d(
            "HomeRoute",
            "홈 뉴스 로드 (isLoggedIn=$isLoggedIn, sessionVersion=$sessionVersion, " +
                "reloadVersion=$reloadVersion, notificationAnchorVersion=$notificationAnchorVersion)"
        )
        viewModel.loadHomeNews(anchorNewsId)
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
        // B: 로그인 복귀용 initialPage가 명시된 경우 우선, 아니면 ViewModel 저장값 사용
        initialPage = if (initialPage > 0) initialPage else savedPageIndex,
        forceResetToThirdPageKey = forceResetToThirdPageKey,
        onLoginRequired = onLoginRequired,
        onDetailClick = onDetailClick,
        onShareClick = onShareClick,
        onPageChanged = { viewModel.savePageIndex(it) },
        topPadding = topPadding
    )
}
