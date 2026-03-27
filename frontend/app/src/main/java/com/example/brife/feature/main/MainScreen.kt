package com.example.brife.feature.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.brife.data.local.OnboardingLocalStorage
import com.example.brife.data.local.longsampleHomeNews
import com.example.brife.data.local.shortsampleHomeNews
import com.example.brife.feature.archive.ArchiveDetailScreen
import com.example.brife.feature.archive.ArchiveNewsItem
import com.example.brife.feature.archive.ArchiveScreen
import com.example.brife.feature.archive.component.ArchiveMoreBottomSheet
import com.example.brife.feature.explore.ExploreScreen
import com.example.brife.feature.home.HomeScreen
import com.example.brife.feature.onboarding.OnboardingInterestRoute
import com.example.brife.feature.onboarding.OnboardingSubInterestRoute
import com.example.brife.feature.profile.ProfileScreen
import com.example.brife.feature.profile.ProfileUiState
import com.example.brife.feature.profile.categoryItemFromId
import com.example.brife.navigation.NavRoutes
import com.example.brife.ui.component.AppNavigationBar
import com.example.brife.ui.component.AppTopBar
import com.example.brife.feature.home.HomeToLoginBottomSheet
import com.example.brife.feature.home.NewsLongScreen
import com.example.brife.feature.home.shareNews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToNewsLong: (String) -> Unit,
    onNavigateToSetting: () -> Unit = {}
) {
    val context = LocalContext.current
    val onboardingStorage = remember { OnboardingLocalStorage(context) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.HOME

    val isNewsLongRoute = currentRoute?.startsWith("${NavRoutes.NEWS_LONG}/") == true
    val isInterestResetRoute = currentRoute == NavRoutes.ONBOARDING_INTEREST_RESET ||
            currentRoute?.startsWith("${NavRoutes.ONBOARDING_SUB_INTEREST_RESET}/") == true
    val isArchiveDetailRoute = currentRoute?.startsWith("${NavRoutes.ARCHIVE_DETAIL}/") == true

    val backgroundColor =
        if (currentRoute == NavRoutes.HOME) Color.Transparent else Color.White

    val isLoggedIn = false
    var showLoginBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Archive 관련 상태
    var archiveFolders by remember { mutableStateOf(listOf<String>()) }
    var showArchiveMoreSheet by remember { mutableStateOf(false) }
    var isArchiveDeleteMode by remember { mutableStateOf(false) }
    var selectedFolderNames by remember { mutableStateOf(setOf<String>()) }
    var isArchiveRenameMode by remember { mutableStateOf(false) }
    val archiveMoreSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var profileInterests by remember {
        mutableStateOf(
            onboardingStorage.getSelectedCategoryIds().mapNotNull { categoryItemFromId(it) }
        )
    }

    val exploreNewsList = remember {
        listOf(
            ArchiveNewsItem(
                title = "미국 연준, 기준금리 동결 결정",
                summary = "연방준비제도가 이번 FOMC 회의에서 기준금리를 현 수준에서 동결하기로 결정했다.",
                time = "2시간 전",
                company = "한국경제",
                imageUrl = com.example.brife.R.drawable.homescreen_bg
            ),
            ArchiveNewsItem(
                title = "애플, 새로운 AI 기능 탑재한 아이폰 17 공개",
                summary = "애플이 차세대 아이폰에 온디바이스 AI 기능을 전면 탑재한다고 발표했다.",
                time = "4시간 전",
                company = "조선일보",
                imageUrl = com.example.brife.R.drawable.homescreen_bg
            ),
            ArchiveNewsItem(
                title = "국내 부동산 시장 안정세 지속",
                summary = "수도권 아파트 가격이 3개월 연속 보합세를 유지하며 안정세를 이어가고 있다.",
                time = "6시간 전",
                company = "매일경제",
                imageUrl = com.example.brife.R.drawable.homescreen_bg
            ),
            ArchiveNewsItem(
                title = "국내 전기차 판매량, 전년 대비 30% 증가",
                summary = "올해 상반기 국내 전기차 신규 등록 대수가 전년 동기 대비 30% 증가한 것으로 집계됐다.",
                time = "8시간 전",
                company = "동아일보",
                imageUrl = com.example.brife.R.drawable.homescreen_bg
            ),
            ArchiveNewsItem(
                title = "정부, 청년 주거 지원 정책 강화 발표",
                summary = "국토교통부가 청년층 주거 부담 완화를 위한 새로운 지원 정책 패키지를 발표했다.",
                time = "10시간 전",
                company = "연합뉴스",
                imageUrl = com.example.brife.R.drawable.homescreen_bg
            )
        )
    }

    // 저장된 대분류 관심사 기준으로 매칭 뉴스 우선, 나머지 후순위 정렬 후 Top5
    val homeNewsList = remember(profileInterests) {
        val interestNames = profileInterests.map { it.name }.toSet()
        if (interestNames.isEmpty()) {
            shortsampleHomeNews.take(5)
        } else {
            val matching = shortsampleHomeNews.filter { it.category in interestNames }
            val nonMatching = shortsampleHomeNews.filter { it.category !in interestNames }
            (matching + nonMatching).take(5)
        }
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            when {
                currentRoute == NavRoutes.HOME -> {
                    AppTopBar(onSettingClick = onNavigateToSetting)
                }
                isNewsLongRoute -> {
                    // NewsLongScreen이 자체 TopBar를 가지고 있으므로 렌더링하지 않음
                    // (MainScreen이 빈 AppTopBar를 렌더링하면 터치 이벤트를 가로챔)
                }
                currentRoute == NavRoutes.ARCHIVE -> {
                    AppTopBar(
                        title = "보관함",
                        showLogo = false,
                        showSettings = false,
                        centerTitle = true,
                        showMore = true,
                        onMoreClick = { showArchiveMoreSheet = true }
                    )
                }
                // PROFILE: topbar 없음 (요구사항)
            }
        },
        bottomBar = {
            if (!isNewsLongRoute && !isInterestResetRoute && !isArchiveDeleteMode && !isArchiveDetailRoute) {
                AppNavigationBar(
                    selectedIndex = when (currentRoute) {
                        NavRoutes.HOME -> 0
                        NavRoutes.EXPLORE -> 1
                        NavRoutes.ARCHIVE -> 2
                        NavRoutes.PROFILE -> 3
                        else -> 0
                    },
                    onItemSelected = { index ->
                        when (index) {
                            0 -> navigateTo(NavRoutes.HOME)
                            1 -> navigateTo(NavRoutes.EXPLORE)
                            2 -> {
                                if (isLoggedIn) navigateTo(NavRoutes.ARCHIVE)
                                else showLoginBottomSheet = true
                            }
                            3 -> navigateTo(NavRoutes.PROFILE)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(NavRoutes.HOME) {
                HomeScreen(
                    newsList = homeNewsList,
                    isLoggedIn = isLoggedIn,
                    onLoginRequired = {
                        showLoginBottomSheet = true
                    },
                    onLoginClick = onNavigateToLogin,
                    onDetailClick = { item ->
                        val index = shortsampleHomeNews.indexOf(item)
                        if (index != -1) {
                            navController.navigate("${NavRoutes.NEWS_LONG}/$index")
                        }
                    },
                    onShareClick = { item ->
                        // TODO: API 연동 후 item.id(실제 newsId)로 교체
                        val index = shortsampleHomeNews.indexOf(item)
                        shareNews(context, item.title, index.toString())
                    },
                    topPadding = innerPadding.calculateTopPadding()
                )
            }

            composable(NavRoutes.EXPLORE) {
                ExploreScreen(
                    newsList = exploreNewsList,
                    modifier = Modifier.statusBarsPadding()
                )
            }

            composable(NavRoutes.ARCHIVE) {
                ArchiveScreen(
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                    folders = archiveFolders,
                    onFolderAdd = { name -> archiveFolders = archiveFolders + name },
                    onNavigateToDetail = { folderName ->
                        navController.navigate("${NavRoutes.ARCHIVE_DETAIL}/$folderName")
                    },
                    isDeleteMode = isArchiveDeleteMode,
                    selectedFolderNames = selectedFolderNames,
                    onToggleFolderSelect = { name ->
                        selectedFolderNames = if (name in selectedFolderNames)
                            selectedFolderNames - name
                        else
                            selectedFolderNames + name
                    },
                    onCancelDelete = {
                        isArchiveDeleteMode = false
                        selectedFolderNames = emptySet()
                    },
                    onConfirmDelete = {
                        archiveFolders = archiveFolders.filter { it !in selectedFolderNames }
                        selectedFolderNames = emptySet()
                        isArchiveDeleteMode = false
                    },
                    isRenameMode = isArchiveRenameMode,
                    onFolderRename = { oldName, newName ->
                        archiveFolders = archiveFolders.map { if (it == oldName) newName else it }
                    },
                    onCancelRename = { isArchiveRenameMode = false }
                )
            }

            composable(NavRoutes.PROFILE) {
                ProfileScreen(
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                    uiState = ProfileUiState(
                        isLoggedIn = false,
                        interests = profileInterests
                    ),
                    onResetInterestClick = {
                        navController.navigate(NavRoutes.ONBOARDING_INTEREST_RESET)
                    },
                    onLoginClick = onNavigateToLogin
                )
            }

            composable(
                route = "${NavRoutes.ARCHIVE_DETAIL}/{folderName}",
                arguments = listOf(navArgument("folderName") { type = NavType.StringType })
            ) { backStackEntry ->
                val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
                ArchiveDetailScreen(
                    folderName = folderName,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.ONBOARDING_INTEREST_RESET) {
                OnboardingInterestRoute(
                    onNextClick = { selectedIds ->
                        navController.navigate(
                            "${NavRoutes.ONBOARDING_SUB_INTEREST_RESET}/${selectedIds.joinToString(",")}"
                        )
                    }
                )
            }

            composable(
                route = "${NavRoutes.ONBOARDING_SUB_INTEREST_RESET}/{idsArg}",
                arguments = listOf(navArgument("idsArg") { type = NavType.StringType })
            ) { backStackEntry ->
                val idsArg = backStackEntry.arguments?.getString("idsArg") ?: ""
                val selectedIds = idsArg.split(",").mapNotNull { it.toLongOrNull() }
                OnboardingSubInterestRoute(
                    selectedParentCategoryIds = selectedIds,
                    onNextClick = {
                        profileInterests = onboardingStorage.getSelectedCategoryIds()
                            .mapNotNull { categoryItemFromId(it) }
                        navController.popBackStack(NavRoutes.PROFILE, false)
                    }
                )
            }

            composable(
                route = "${NavRoutes.NEWS_LONG}/{newsIndex}",
                arguments = listOf(navArgument("newsIndex") { type = NavType.IntType })
            ) { backStackEntry ->
                val newsIndex = backStackEntry.arguments?.getInt("newsIndex") ?: 0
                val item = longsampleHomeNews.getOrNull(newsIndex) ?: longsampleHomeNews.first()

                NewsLongScreen(
                    item = item,
                    isLoggedIn = isLoggedIn,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToArchive = {
                        navController.popBackStack()
                        navigateTo(NavRoutes.ARCHIVE)
                    },
                    onShareClick = {
                        // TODO: API 연동 후 item.id(실제 newsId)로 교체
                        shareNews(context, item.title, newsIndex.toString())
                    },
                    onLoginRequired = { showLoginBottomSheet = true }
                )
            }
        }

        if (showArchiveMoreSheet) {
            ArchiveMoreBottomSheet(
                sheetState = archiveMoreSheetState,
                onDismissRequest = { showArchiveMoreSheet = false },
                onRenameClick = {
                    showArchiveMoreSheet = false
                    isArchiveRenameMode = true
                },
                onDeleteClick = {
                    showArchiveMoreSheet = false
                    isArchiveDeleteMode = true
                }
            )
        }

        if (showLoginBottomSheet) {
            HomeToLoginBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { showLoginBottomSheet = false },
                onLoginClick = {
                    showLoginBottomSheet = false
                    onNavigateToLogin()
                },
                onBrowseClick = { showLoginBottomSheet = false }
            )
        }
    }
}
