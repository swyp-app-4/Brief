package com.example.brife.feature.main

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.brife.feature.archive.ArchiveScreen
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
import com.example.brife.feature.profile.ProfileEditScreen
import com.example.brife.feature.profile.categoryItemFromId
import com.example.brife.feature.profile.profileImageUrlFromDrawableRes
import com.example.brife.navigation.NavRoutes
import com.example.brife.ui.component.AppNavigationBar
import com.example.brife.ui.component.AppTopBar
import com.example.brife.feature.home.HomeToLoginBottomSheet
import com.example.brife.feature.home.NewsLongScreen
import com.example.brife.feature.widget.WidgetRefreshHelper
import com.example.brife.feature.widget.WidgetActionReceiver

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isLoggedIn: Boolean,
    sessionVersion: Int = 0,
    onLogout: () -> Unit,
    onNavigateToLogin: (String?, Int?) -> Unit, // 시그니처 변경 (경로, 인덱스)
    // ...
    onNavigateToNewsLong: (String) -> Unit,
    onNavigateToSetting: () -> Unit = {},
    initialDeepLinkNewsId: Long? = null,
    initialOpenBookmark: Boolean = false,
    initialRoute: String = NavRoutes.HOME, // 추가
    initialHomeIndex: Int = 0,             // 추가
    deepLinkVersion: Int = 0
) {
    val context = LocalContext.current
    val onboardingStorage = remember { OnboardingLocalStorage(context) }
    val authStorage = remember { AuthLocalStorage(context) }
    val userRepository = remember { UserRepository(NetworkModule.userApiService, authStorage) }
    val scope = rememberCoroutineScope()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.HOME
    val mainStartDestination = when (initialRoute) {
        NavRoutes.HOME,
        NavRoutes.EXPLORE,
        NavRoutes.ARCHIVE,
        NavRoutes.PROFILE,
        NavRoutes.ONBOARDING_INTEREST_RESET -> initialRoute
        NavRoutes.PROFILE_EDIT -> NavRoutes.PROFILE
        else -> NavRoutes.HOME
    }

    val isNewsLongRoute = currentRoute?.startsWith("${NavRoutes.NEWS_LONG}/") == true
    val isInterestResetRoute = currentRoute == NavRoutes.ONBOARDING_INTEREST_RESET ||
            currentRoute?.startsWith("${NavRoutes.ONBOARDING_SUB_INTEREST_RESET}/") == true
    val isArchiveDetailRoute = currentRoute?.startsWith("${NavRoutes.ARCHIVE_DETAIL}/") == true
    val isProfileEditRoute = currentRoute == NavRoutes.PROFILE_EDIT

    val backgroundColor =
        if (currentRoute == NavRoutes.HOME) Color.Transparent else Color.White

    // remember {} 없이 직접 읽어 항상 최신 로그인 상태 반영
    // SharedPreferences는 메모리 캐시 기반이므로 재구성 시 호출해도 부담 없음
    var showLoginBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Archive 관련 상태
    var showArchiveMoreSheet by remember { mutableStateOf(false) }
    var isArchiveDeleteMode by remember { mutableStateOf(false) }
    var isArchiveRenameMode by remember { mutableStateOf(false) }
    val archiveMoreSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)



    // --- 추가: 로그인 성공 후 복귀를 위한 임시 상태 저장 ---
    var pendingRouteForLogin by remember { mutableStateOf<String?>(null) }
    var pendingIndexForLogin by remember { mutableStateOf<Int?>(null) }
    var returnRouteAfterGuestArchiveSheet by remember { mutableStateOf<String?>(null) }
    var returnRouteAfterGuestProfileEditSheet by remember { mutableStateOf<String?>(null) }
    var forceResetHomePagerKey by remember { mutableStateOf(0) }
    val guestPreviewRoute = if (showLoginBottomSheet && !isLoggedIn) {
        when (pendingRouteForLogin) {
            NavRoutes.ARCHIVE -> NavRoutes.ARCHIVE
            NavRoutes.PROFILE_EDIT -> NavRoutes.PROFILE_EDIT
            else -> null
        }
    } else {
        null
    }
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            forceResetHomePagerKey++
            showLoginBottomSheet = false
            pendingRouteForLogin = null
            pendingIndexForLogin = null
            returnRouteAfterGuestArchiveSheet = null
            returnRouteAfterGuestProfileEditSheet = null
        }
    }
    // ------------------------------------------------


    //롱폼 관련
    var selectedNewsItem by remember { mutableStateOf<HomeNewsCardItem?>(null) }
    // ArchiveDetail에서 진입 시 해당 폴더 ID를 전달 → 북마크 아이콘 사전 활성화
    var preselectedArchiveId by remember { mutableStateOf<Long?>(null) }

    // 위젯 또는 딥링크로 진입 시 뉴스 상세 화면으로 자동 이동
    // deepLinkVersion을 키에 포함 → 동일 newsId 재진입 시에도 재실행 보장
    LaunchedEffect(initialDeepLinkNewsId, deepLinkVersion) {
        if (initialDeepLinkNewsId != null) {
            preselectedArchiveId = null  // 위젯 진입 시 이전 ArchiveDetail 폴더 선택 상태 초기화
            selectedNewsItem = HomeNewsCardItem(
                newsId = initialDeepLinkNewsId,
                category = "",
                imageRes = null,
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
    var profileEditUserName by remember { mutableStateOf("브리프") }
    var profileEditImageRes by remember { mutableStateOf(com.example.brife.R.drawable.img_profile_avatar) }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun dismissLoginSheet() {
        showLoginBottomSheet = false
        if (pendingRouteForLogin == NavRoutes.HOME && pendingIndexForLogin == 2) {
            forceResetHomePagerKey++
        }
        if (!isLoggedIn &&
            pendingRouteForLogin == NavRoutes.ARCHIVE &&
            currentRoute == NavRoutes.ARCHIVE
        ) {
            navigateTo(returnRouteAfterGuestArchiveSheet ?: NavRoutes.HOME)
        }
        if (!isLoggedIn &&
            pendingRouteForLogin == NavRoutes.PROFILE_EDIT &&
            currentRoute == NavRoutes.PROFILE_EDIT
        ) {
            navController.popBackStack()
        }
        pendingRouteForLogin = null
        pendingIndexForLogin = null
        returnRouteAfterGuestArchiveSheet = null
        returnRouteAfterGuestProfileEditSheet = null
    }

    LaunchedEffect(initialRoute) {
        if (initialRoute != mainStartDestination && currentRoute == mainStartDestination) {
            navController.navigate(initialRoute) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(sessionVersion, mainStartDestination, initialRoute) {
        showLoginBottomSheet = false
        showArchiveMoreSheet = false
        isArchiveDeleteMode = false
        isArchiveRenameMode = false
        pendingRouteForLogin = null
        pendingIndexForLogin = null
        returnRouteAfterGuestArchiveSheet = null
        returnRouteAfterGuestProfileEditSheet = null
        selectedNewsItem = null
        preselectedArchiveId = null

        navController.navigate(mainStartDestination) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }

        if (initialRoute != mainStartDestination) {
            navController.navigate(initialRoute) {
                launchSingleTop = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        showMore = isLoggedIn,
                        onMoreClick = { showArchiveMoreSheet = true }
                    )
                }
                isProfileEditRoute -> {
                    // ProfileEditScreen 내부 전용 TopBar 사용
                }
                // PROFILE: topbar 없음 (요구사항)
            }
        },
        bottomBar = {
            if (!isNewsLongRoute && !isInterestResetRoute && !isArchiveDeleteMode && !isArchiveDetailRoute && !isProfileEditRoute) {
                AppNavigationBar(
                    selectedIndex = if (showLoginBottomSheet) {
                        // 바텀시트가 떠 있을 때는 현재 실제 경로에 따른 인덱스 유지
                        when (currentRoute) {
                            NavRoutes.HOME -> 0
                            NavRoutes.EXPLORE -> 1
                            NavRoutes.ARCHIVE -> 2
                            NavRoutes.PROFILE -> 3
                            else -> 0
                        }
                    } else {
                        // 기존 로직 유지
                        when (currentRoute) {
                            NavRoutes.HOME -> 0
                            NavRoutes.EXPLORE -> 1
                            NavRoutes.ARCHIVE -> 2
                            NavRoutes.PROFILE -> 3
                            else -> 0
                        }
                    },
                    onItemSelected = { index ->
                        when (index) {
                            0 -> navigateTo(NavRoutes.HOME)
                            1 -> navigateTo(NavRoutes.EXPLORE)
                            2 -> {
                                if (isLoggedIn) {
                                    archiveReloadVersion++
                                    navigateTo(NavRoutes.ARCHIVE)
                                } else {
                                    // 요구사항: 이동 없이 바텀시트만 등장
                                    returnRouteAfterGuestArchiveSheet = currentRoute
                                    pendingRouteForLogin = NavRoutes.ARCHIVE
                                    pendingIndexForLogin = null
                                    showLoginBottomSheet = true
                                }
                            }
                            3 -> { // 프로필
                                // 요구사항: ProfileScreen으로 먼저 이동 후 바텀시트 등장
                                navigateTo(NavRoutes.PROFILE)
                                if (!isLoggedIn) {
                                    pendingRouteForLogin = NavRoutes.PROFILE
                                    pendingIndexForLogin = null
                                    showLoginBottomSheet = true
                                }
                            }
                        }
                    }
                )
            }
        }
        ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = mainStartDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(NavRoutes.HOME) {
                HomeRoute(
                    isLoggedIn = isLoggedIn,
                    reloadVersion = homeReloadVersion,
                    initialPage = initialHomeIndex, // ★ 로그인 전 보던 인덱스로 복귀
                    forceResetToThirdPageKey = forceResetHomePagerKey,
                    onLoginRequired = {
                        // 홈 3->4 스와이프 차단 시 호출됨
                        pendingRouteForLogin = NavRoutes.HOME
                        pendingIndexForLogin = 2 // 3번째 카드(index 2)로 복귀하도록 설정
                        showLoginBottomSheet = true
                    },
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
                            category = archiveItem.category,
                            subCategory = archiveItem.subCategory,
                            imageRes = null,
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
                    isLoggedIn = isLoggedIn,
                    reloadVersion = archiveReloadVersion,
                    isDeleteMode = isArchiveDeleteMode,
                    isRenameMode = isArchiveRenameMode,
                    onDeleteModeExit = { isArchiveDeleteMode = false },
                    onRenameModeExit = { isArchiveRenameMode = false },
                    onNavigateToDetail = { archiveId, folderName ->
                        navController.navigate(
                            "${NavRoutes.ARCHIVE_DETAIL}/$archiveId/${Uri.encode(folderName)}"
                        )
                    }
                )
            }

            composable(NavRoutes.PROFILE) {
                ProfileRoute(
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                    onResetInterestClick = {
                        navController.navigate(NavRoutes.ONBOARDING_INTEREST_RESET)
                    },
                    onEditProfileImageClick = { username, imageRes ->
                        profileEditUserName = username
                        profileEditImageRes = imageRes
                        if (isLoggedIn) {
                            navController.navigate(NavRoutes.PROFILE_EDIT)
                        } else {
                            returnRouteAfterGuestProfileEditSheet = NavRoutes.PROFILE
                            pendingRouteForLogin = NavRoutes.PROFILE_EDIT
                            pendingIndexForLogin = null
                            showLoginBottomSheet = true
                        }
                    },
                    onLoginClick = {
                        // ★ 타입 불일치 해결: 파라미터 없이 호출되는 콜백을 인자 2개짜리 함수로 연결
                        onNavigateToLogin(NavRoutes.PROFILE, null)
                    }
                )
            }

            composable(NavRoutes.PROFILE_EDIT) {
                ProfileEditScreen(
                    username = profileEditUserName,
                    selectedImageRes = profileEditImageRes,
                    isGuestPreview = !isLoggedIn,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { imageRes ->
                        if (!isLoggedIn) return@ProfileEditScreen
                        scope.launch {
                            val profileImageUrl = profileImageUrlFromDrawableRes(imageRes)
                            val result = userRepository.updateProfile(profileImageUrl = profileImageUrl)
                            if (result.isSuccess) {
                                profileEditImageRes = imageRes
                                navController.popBackStack()
                            } else {
                                Log.w("MainScreen", "프로필 이미지 저장 실패: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    }
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
                val folderName = Uri.decode(backStackEntry.arguments?.getString("folderName") ?: "")
                ArchiveDetailRoute(
                    archiveId = archiveId,
                    folderName = folderName,
                    onBackClick = { navController.popBackStack() },
                    onNewsClick = { archiveItem ->
                        selectedNewsItem = com.example.brife.feature.home.HomeNewsCardItem(
                            newsId = archiveItem.newsId,
                            category = archiveItem.category,
                            subCategory = archiveItem.subCategory,
                            imageRes = null,
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

            // MainScreen.kt 의 NavRoutes.ONBOARDING_SUB_INTEREST_RESET 부분 수정

            composable(
                route = "${NavRoutes.ONBOARDING_SUB_INTEREST_RESET}/{idsArg}",
                arguments = listOf(navArgument("idsArg") { type = NavType.StringType })
            ) { backStackEntry ->
                val idsArg = backStackEntry.arguments?.getString("idsArg") ?: ""
                val selectedIds = idsArg.split(",").mapNotNull { it.toLongOrNull() }
                OnboardingSubInterestRoute(
                    selectedParentCategoryIds = selectedIds,
                    onNextClick = {
                        // 1. 로컬 관심사 갱신 (프로필 화면 UI 즉시 반영용)
                        profileInterests = onboardingStorage.getSelectedCategoryIds()
                            .mapNotNull { categoryItemFromId(it) }

                        if (isLoggedIn) {
                            // 2-A. 로그인 상태: 서버와 관심사 동기화 시도
                            scope.launch {
                                val result = userRepository.updateInterests(
                                    categoryIds = onboardingStorage.getSelectedSubCategoryIds(),
                                    groupIds = onboardingStorage.getSelectedCategoryIds()
                                )
                                if (result.isSuccess) {
                                    Log.d("MainScreen", "관심사 재설정 PUT 성공 → 홈 뉴스 재로드")
                                    homeReloadVersion++
                                    WidgetRefreshHelper.refreshAll(context)
                                } else {
                                    Log.w("MainScreen", "관심사 재설정 PUT 실패: ${result.exceptionOrNull()?.message}")
                                }
                            }
                        } else {
                            // 2-B. 비로그인 상태: 로컬 저장소 데이터만 사용하므로 즉시 홈 리로드 트리거
                            Log.d("MainScreen", "비로그인 관심사 재설정 완료 → 홈 뉴스 재로드")
                            homeReloadVersion++
                            WidgetRefreshHelper.refreshAll(context)
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

                // selectedNewsItem이 null이어도 newsId로 fallback 항목 생성
                // → 위젯 deeplink 진입 시 race condition 방어 + 항상 API 재조회 보장
                val item = selectedNewsItem?.takeIf { it.newsId == newsId } ?: HomeNewsCardItem(
                    newsId = newsId,
                    category = "",
                    imageRes = null,
                    title = "",
                    notice = "",
                    summaryPoints = emptyList(),
                    insight = "",
                    articleCount = 0
                )

                val archiveRepository = remember {
                    ArchiveRepository(
                        api = NetworkModule.archiveApiService,
                        newsApi = NetworkModule.newsApiService,
                        authLocalStorage = authStorage
                    )
                }
                val newsLongViewModel: NewsLongViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    key = "newslong_$newsId",
                    factory = NewsLongViewModelFactory(archiveRepository)
                )
                val newsLongFolders by newsLongViewModel.folders.collectAsState()
                val isSavingFolders by newsLongViewModel.isSavingFolders.collectAsState()
                val newsLongSections by newsLongViewModel.sections.collectAsState()
                val newsLongGroupName by newsLongViewModel.groupName.collectAsState()
                val newsLongCategoryName by newsLongViewModel.categoryName.collectAsState()
                val newsLongSummaryPoints by newsLongViewModel.summaryPoints.collectAsState()
                val newsLongArticleCount by newsLongViewModel.articleCount.collectAsState()
                val newsLongTitle by newsLongViewModel.title.collectAsState()
                val isSectionsLoading by newsLongViewModel.isSectionsLoading.collectAsState()
                val sectionsError by newsLongViewModel.sectionsError.collectAsState()

                // 로그인 상태일 때만 폴더 목록 로드, 섹션은 항상 로드
                // ArchiveDetail 진입: preselectedArchiveId로 해당 폴더 isSelected=true
                // Home/Explore 진입: newsId로 이미 저장된 폴더 자동 감지
                LaunchedEffect(newsId) {
                    if (isLoggedIn) newsLongViewModel.loadFolders(preselectedArchiveId, newsId)
                    newsLongViewModel.loadSections(newsId)
                }

                // API 응답으로 누락 필드 보정
                // Home 진입: item에 이미 완전한 데이터 → API 응답이 있으면 덮어씀(동일값)
                // Explore/Archive/위젯 진입: item의 title/summaryPoints 등 빈값 → API 로드 후 반영
                val resolvedItem = item.copy(
                    title = if (newsLongTitle.isNotBlank()) newsLongTitle else item.title,
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
                    isSavingFolders = isSavingFolders,
                    sections = newsLongSections,
                    isSectionsLoading = isSectionsLoading,
                    sectionsError = sectionsError,
                    onRetryLoadSections = { newsLongViewModel.loadSections(newsId) },
                    onSaveToFolders = { targetFolders, onCompleted ->
                        newsLongViewModel.saveToFolders(item.newsId, targetFolders) { isSuccess, hasAnySavedFolder ->
                            if (isSuccess) {
                                if (hasAnySavedFolder) {
                                    WidgetActionReceiver.saveBookmarkedId(context, item.newsId)
                                } else {
                                    WidgetActionReceiver.removeBookmarkedId(context, item.newsId)
                                }
                                WidgetRefreshHelper.refreshAll(context)
                            }
                            onCompleted(isSuccess)
                        }
                    },
                    onCreateFolder = { folderName, onCreated ->
                        newsLongViewModel.createFolder(folderName, onCreated)
                    },
                    onBackClick = { navController.popBackStack() },
                    onNavigateToArchive = {
                        archiveReloadVersion++
                        navController.popBackStack()
                        navigateTo(NavRoutes.ARCHIVE)
                    },
                    onShareClick = { /* 이미지 공유는 NewsLongScreen 내부에서 처리 */ },
                    onLoginRequired = {
                        // 롱폼에서 로그인 유도 시 현재 경로 저장
                        pendingRouteForLogin = "${NavRoutes.NEWS_LONG}/$newsId"
                        pendingIndexForLogin = null
                        showLoginBottomSheet = true
                    }
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

        // 로그인 바텀시트 호출부 수정
        if (guestPreviewRoute != null) {
            GuestLoginPreviewOverlay(
                previewRoute = guestPreviewRoute,
                username = profileEditUserName,
                selectedImageRes = profileEditImageRes
            )
        }

        if (showLoginBottomSheet) {
            HomeToLoginBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { dismissLoginSheet() },
                onLoginClick = {
                    showLoginBottomSheet = false
                    returnRouteAfterGuestArchiveSheet = null
                    returnRouteAfterGuestProfileEditSheet = null
                    // ★ AppNavGraph에 복귀 정보를 넘기며 로그인 화면으로 이동
                    onNavigateToLogin(pendingRouteForLogin, pendingIndexForLogin)
                },
                onBrowseClick = { dismissLoginSheet() }
            )
        }
    }
}
}

@Composable
private fun GuestLoginPreviewOverlay(
    previewRoute: String,
    username: String,
    selectedImageRes: Int
) {
    val consumeClicks = Modifier.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = {}
    )

    Box(modifier = Modifier.fillMaxSize()) {
        when (previewRoute) {
            NavRoutes.ARCHIVE -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    AppTopBar(
                        title = "보관함",
                        showLogo = false,
                        showSettings = false,
                        centerTitle = true,
                        showMore = false
                    )
                    ArchiveScreen(
                        modifier = Modifier.fillMaxSize(),
                        folders = emptyList(),
                        favoriteArchiveId = 0L,
                        favoriteItemCount = 0,
                        onFolderAdd = {},
                        onNavigateToDetail = { _, _ -> }
                    )
                }
            }

            NavRoutes.PROFILE_EDIT -> {
                ProfileEditScreen(
                    username = username,
                    selectedImageRes = selectedImageRes,
                    isGuestPreview = true,
                    onBackClick = {},
                    onSaveClick = {}
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(consumeClicks)
        )
    }
}
