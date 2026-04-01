package com.example.brife.feature.main

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.local.OnboardingLocalStorage
import com.example.brife.data.remote.NetworkModule
import com.example.brife.data.repository.UserRepository
import com.example.brife.feature.profile.ProfileRoute
//import com.example.brife.data.local.longsampleHomeNews
import com.example.brife.feature.archive.ArchiveDetailRoute
import com.example.brife.feature.archive.ArchiveRoute
import com.example.brife.feature.archive.component.ArchiveMoreBottomSheet
import com.example.brife.feature.explore.ExploreRoute
import com.example.brife.data.repository.ArchiveRepository
import com.example.brife.feature.home.HomeNewsCardItem
import com.example.brife.feature.home.HomeRoute
import com.example.brife.feature.home.NewsLongViewModel
import com.example.brife.feature.home.NewsLongViewModelFactory
import com.example.brife.feature.onboarding.OnboardingInterestRoute
import com.example.brife.feature.onboarding.OnboardingSubInterestRoute
import com.example.brife.feature.profile.categoryItemFromId
import com.example.brife.navigation.NavRoutes
import com.example.brife.ui.component.AppNavigationBar
import com.example.brife.ui.component.AppTopBar
import com.example.brife.feature.home.HomeToLoginBottomSheet
import com.example.brife.feature.home.NewsLongScreen
import com.example.brife.feature.widget.BrifeWidgetReceiver
import com.example.brife.feature.widget.WidgetActionReceiver

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToNewsLong: (String) -> Unit,
    onNavigateToSetting: () -> Unit = {},
    initialDeepLinkNewsId: Long? = null,
    initialOpenBookmark: Boolean = false
) {
    val context = LocalContext.current
    val onboardingStorage = remember { OnboardingLocalStorage(context) }
    val authStorage = remember { AuthLocalStorage(context) }
    val userRepository = remember { UserRepository(NetworkModule.userApiService, authStorage) }
    val scope = rememberCoroutineScope()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.HOME

    val isNewsLongRoute = currentRoute?.startsWith("${NavRoutes.NEWS_LONG}/") == true
    val isInterestResetRoute = currentRoute == NavRoutes.ONBOARDING_INTEREST_RESET ||
            currentRoute?.startsWith("${NavRoutes.ONBOARDING_SUB_INTEREST_RESET}/") == true
    val isArchiveDetailRoute = currentRoute?.startsWith("${NavRoutes.ARCHIVE_DETAIL}/") == true

    val backgroundColor =
        if (currentRoute == NavRoutes.HOME) Color.Transparent else Color.White

    // remember {} 없이 직접 읽어 항상 최신 로그인 상태 반영
    // SharedPreferences는 메모리 캐시 기반이므로 재구성 시 호출해도 부담 없음
    val isLoggedIn = authStorage.isLoggedIn()
    var showLoginBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Archive 관련 상태
    var showArchiveMoreSheet by remember { mutableStateOf(false) }
    var isArchiveDeleteMode by remember { mutableStateOf(false) }
    var isArchiveRenameMode by remember { mutableStateOf(false) }
    val archiveMoreSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    //롱폼 관련
    var selectedNewsItem by remember { mutableStateOf<HomeNewsCardItem?>(null) }
    // ArchiveDetail에서 진입 시 해당 폴더 ID를 전달 → 북마크 아이콘 사전 활성화
    var preselectedArchiveId by remember { mutableStateOf<Long?>(null) }

    // 위젯 또는 딥링크로 진입 시 뉴스 상세 화면으로 자동 이동
    // initialDeepLinkNewsId 가 변경될 때마다 재실행 (앱 실행 중 위젯 클릭 포함)
    LaunchedEffect(initialDeepLinkNewsId) {
        if (initialDeepLinkNewsId != null) {
            selectedNewsItem = HomeNewsCardItem(
                newsId = initialDeepLinkNewsId,
                category = "",
                title = "",
                notice = "",
                summaryPoints = emptyList(),
                insight = "",
                articleCount = 0
            )
            val newsLongRoute = if (initialOpenBookmark) {
                "${NavRoutes.NEWS_LONG}/$initialDeepLinkNewsId?openBookmark=true"
            } else {
                "${NavRoutes.NEWS_LONG}/$initialDeepLinkNewsId"
            }
            navController.navigate(newsLongRoute)
        }
    }

    // 관심사 재설정 완료 시 증가 → HomeRoute에서 감지하여 홈 뉴스 재로드
    var homeReloadVersion by remember { mutableStateOf(0) }

    // 보관함 탭 재진입 또는 뉴스 저장 후 ArchiveRoute가 폴더 목록을 재로드하도록 하는 버전 카운터
    var archiveReloadVersion by remember { mutableStateOf(0) }


    var profileInterests by remember {
        mutableStateOf(
            onboardingStorage.getSelectedCategoryIds().mapNotNull { categoryItemFromId(it) }
        )
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
                                if (isLoggedIn) {
                                    archiveReloadVersion++
                                    navigateTo(NavRoutes.ARCHIVE)
                                } else showLoginBottomSheet = true
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
                HomeRoute(
                    isLoggedIn = isLoggedIn,
                    reloadVersion = homeReloadVersion,
                    onLoginRequired = { showLoginBottomSheet = true },
                    onDetailClick = { item ->
                        selectedNewsItem = item
                        preselectedArchiveId = null
                        navController.navigate("${NavRoutes.NEWS_LONG}/${item.newsId}")
                    },
                    onShareClick = { /* 이미지 공유는 HomeScreen 내부에서 처리 */ },
                    topPadding = innerPadding.calculateTopPadding()
                )
            }

            composable(NavRoutes.EXPLORE) {
                ExploreRoute(
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                    onNewsClick = { archiveItem ->
                        selectedNewsItem = com.example.brife.feature.home.HomeNewsCardItem(
                            newsId = archiveItem.newsId,
                            category = "",
                            title = archiveItem.title,
                            notice = "",
                            summaryPoints = emptyList(),
                            insight = "",
                            updatedAt = archiveItem.time,
                            articleCount = 0
                        )
                        preselectedArchiveId = null
                        navController.navigate("${NavRoutes.NEWS_LONG}/${archiveItem.newsId}")
                    }
                )
            }

            composable(NavRoutes.ARCHIVE) {
                ArchiveRoute(
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                    reloadVersion = archiveReloadVersion,
                    isDeleteMode = isArchiveDeleteMode,
                    isRenameMode = isArchiveRenameMode,
                    onDeleteModeExit = { isArchiveDeleteMode = false },
                    onRenameModeExit = { isArchiveRenameMode = false },
                    onNavigateToDetail = { archiveId, folderName ->
                        navController.navigate("${NavRoutes.ARCHIVE_DETAIL}/$archiveId/$folderName")
                    }
                )
            }

            composable(NavRoutes.PROFILE) {
                ProfileRoute(
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                    onResetInterestClick = {
                        navController.navigate(NavRoutes.ONBOARDING_INTEREST_RESET)
                    },
                    onLoginClick = onNavigateToLogin
                )
            }

            composable(
                route = "${NavRoutes.ARCHIVE_DETAIL}/{archiveId}/{folderName}",
                arguments = listOf(
                    navArgument("archiveId") { type = NavType.LongType },
                    navArgument("folderName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val archiveId = backStackEntry.arguments?.getLong("archiveId") ?: 0L
                val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
                ArchiveDetailRoute(
                    archiveId = archiveId,
                    folderName = folderName,
                    onBackClick = { navController.popBackStack() },
                    onNewsClick = { archiveItem ->
                        selectedNewsItem = com.example.brife.feature.home.HomeNewsCardItem(
                            newsId = archiveItem.newsId,
                            category = "",
                            title = archiveItem.title,
                            notice = "",
                            summaryPoints = emptyList(),
                            insight = "",
                            updatedAt = archiveItem.time,
                            articleCount = 0
                        )
                        preselectedArchiveId = archiveId  // 저장된 폴더 ID → 북마크 아이콘 활성화
                        navController.navigate("${NavRoutes.NEWS_LONG}/${archiveItem.newsId}")
                    }
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
                        // 로컬 관심사 갱신
                        profileInterests = onboardingStorage.getSelectedCategoryIds()
                            .mapNotNull { categoryItemFromId(it) }
                        // PUT /users/me/interests — 서버에 관심사 재설정
                        // 성공 시에만 홈 뉴스 재로드 트리거
                        scope.launch {
                            val result = userRepository.updateInterests(
                                categoryIds = onboardingStorage.getSelectedSubCategoryIds(),
                                groupIds = onboardingStorage.getSelectedCategoryIds()
                            )
                            if (result.isSuccess) {
                                Log.d("MainScreen", "관심사 재설정 PUT 성공 → 홈 뉴스 재로드")
                                homeReloadVersion++
                            } else {
                                Log.w("MainScreen", "관심사 재설정 PUT 실패: ${result.exceptionOrNull()?.message}")
                            }
                        }
                        navController.popBackStack(NavRoutes.PROFILE, false)
                    }
                )
            }
            composable(
                route = "${NavRoutes.NEWS_LONG}/{newsId}?openBookmark={openBookmark}",
                arguments = listOf(
                    navArgument("newsId") { type = NavType.LongType },
                    navArgument("openBookmark") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val newsId = backStackEntry.arguments?.getLong("newsId") ?: 0L
                val openBookmark = backStackEntry.arguments?.getBoolean("openBookmark") ?: false
                val item = selectedNewsItem

                if (item != null) {
                    val archiveRepository = remember {
                        ArchiveRepository(
                            api = NetworkModule.archiveApiService,
                            newsApi = NetworkModule.newsApiService, // 이 인자를 추가하세요
                            authLocalStorage = authStorage
                        )
                    }
                    val newsLongViewModel: NewsLongViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        key = "newslong_$newsId",
                        factory = NewsLongViewModelFactory(archiveRepository)
                    )
                    val newsLongFolders by newsLongViewModel.folders.collectAsState()
                    val newsLongSections by newsLongViewModel.sections.collectAsState()
                    val newsLongGroupName by newsLongViewModel.groupName.collectAsState()
                    val newsLongCategoryName by newsLongViewModel.categoryName.collectAsState()
                    val newsLongSummaryPoints by newsLongViewModel.summaryPoints.collectAsState()
                    val newsLongArticleCount by newsLongViewModel.articleCount.collectAsState()

                    // 로그인 상태일 때만 폴더 목록 로드, 섹션은 항상 로드
                    // ArchiveDetail 진입: preselectedArchiveId로 해당 폴더 isSelected=true
                    // Home/Explore 진입: newsId로 이미 저장된 폴더 자동 감지
                    LaunchedEffect(newsId) {
                        if (isLoggedIn) newsLongViewModel.loadFolders(preselectedArchiveId, newsId)
                        newsLongViewModel.loadSections(newsId)
                    }

                    // API 응답으로 누락 필드 보정
                    // Home 진입: item에 이미 완전한 데이터 → API 응답이 있으면 덮어씀(동일값)
                    // Explore/Archive 진입: item의 summaryPoints/articleCount가 빈값 → API 로드 후 반영
                    val resolvedItem = item.copy(
                        category = if (newsLongGroupName.isNotBlank()) newsLongGroupName else item.category,
                        subCategory = if (newsLongCategoryName.isNotBlank()) newsLongCategoryName else item.subCategory,
                        summaryPoints = if (newsLongSummaryPoints.isNotEmpty()) newsLongSummaryPoints else item.summaryPoints,
                        articleCount = if (newsLongArticleCount > 0) newsLongArticleCount else item.articleCount
                    )

                    NewsLongScreen(
                        item = resolvedItem,
                        isLoggedIn = isLoggedIn,
                        autoOpenBookmark = openBookmark && isLoggedIn,
                        folders = newsLongFolders,
                        sections = newsLongSections,
                        onSaveToFolders = { selectedFolders ->
                            newsLongViewModel.saveToFolders(item.newsId, selectedFolders)
                            // 저장 성공 시 위젯 북마크 상태 active로 동기화
                            if (selectedFolders.isNotEmpty()) {
                                WidgetActionReceiver.saveBookmarkedId(context, item.newsId)
                                val manager = AppWidgetManager.getInstance(context)
                                val ids = manager.getAppWidgetIds(
                                    ComponentName(context, BrifeWidgetReceiver::class.java)
                                )
                                if (ids.isNotEmpty()) {
                                    context.sendBroadcast(
                                        Intent(context, BrifeWidgetReceiver::class.java).apply {
                                            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                                        }
                                    )
                                }
                            }
                        },
                        onCreateFolder = { folderName ->
                            newsLongViewModel.createFolder(folderName)
                        },
                        onBackClick = { navController.popBackStack() },
                        onNavigateToArchive = {
                            archiveReloadVersion++
                            navController.popBackStack()
                            navigateTo(NavRoutes.ARCHIVE)
                        },
                        onShareClick = { /* 이미지 공유는 NewsLongScreen 내부에서 처리 */ },
                        onLoginRequired = { showLoginBottomSheet = true }
                    )
                }
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
