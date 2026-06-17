package com.swyp.brife.navigation

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.swyp.brife.BuildConfig
import com.swyp.brife.data.local.AuthLocalStorage
import com.swyp.brife.data.local.OnboardingLocalStorage
import com.swyp.brife.data.local.SearchHistoryLocalStorage
import com.swyp.brife.data.remote.NetworkModule
import com.swyp.brife.data.repository.AuthRepository
import com.swyp.brife.data.repository.UserRepository
import com.swyp.brife.feature.auth.LoginRoute
import com.swyp.brife.feature.auth.kakaoUnlink
import com.swyp.brife.feature.auth.googleClearCredentialState
import com.swyp.brife.feature.auth.naverDisconnect
import com.swyp.brife.feature.auth.naverLogout
import com.swyp.brife.feature.main.MainScreen
import com.swyp.brife.feature.onboarding.OnboardingGuideScreen
import com.swyp.brife.feature.onboarding.OnboardingInterestRoute
import com.swyp.brife.feature.onboarding.OnboardingSubInterestRoute
import com.swyp.brife.feature.onboarding.SplashScreen
import com.swyp.brife.feature.archive.ArchiveDetailRoute
import com.swyp.brife.feature.auth.LoginTermsRoute
import com.swyp.brife.feature.setting.AlarmSettingRoute
import com.swyp.brife.feature.setting.SettingScreen
import com.swyp.brife.feature.setting.WidgetInstallGuideScreen
import com.swyp.brife.feature.widget.WidgetActionReceiver
import com.swyp.brife.feature.widget.WidgetRefreshHelper
import com.swyp.brife.feature.webview.WebViewScreen
import com.swyp.brife.feature.auth.LoginViewModel
import com.swyp.brife.feature.auth.LoginViewModelFactory
import com.swyp.brife.feature.setting.OneToOneInquiryRoute
import android.widget.Toast
import androidx.compose.runtime.saveable.rememberSaveable


@Composable
fun AppNavGraph(
    initialNewsId: Long? = null,
    initialOpenBookmark: Boolean = false,
    deepLinkVersion: Int = 0
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authLocalStorage = remember { AuthLocalStorage(context) }
    val authRepository = remember { AuthRepository(NetworkModule.authApiService) }
    val onboardingLocalStorage = remember { OnboardingLocalStorage(context) }
    val searchHistoryLocalStorage = remember { SearchHistoryLocalStorage(context) }
    val userRepository = remember {
        UserRepository(
            api = NetworkModule.userApiService,
            authApi = NetworkModule.authApiService,
            authLocalStorage = authLocalStorage
        )
    }
    val scope = rememberCoroutineScope()

    // 회원탈퇴 상태 — SettingScreen에 전달
    var isWithdrawing by remember { mutableStateOf(false) }
    var withdrawErrorMessage by remember { mutableStateOf<String?>(null) }
    var isWithdrawn by remember { mutableStateOf(false) }

    // --- 추가: UI 실시간 반영을 위한 상태값 ---
    var isLoggedIn by remember { mutableStateOf(authLocalStorage.isLoggedIn()) }
    var loginMethod by remember { mutableStateOf(authLocalStorage.getLoginMethod() ?: "") }
    var mainSessionVersion by remember { mutableStateOf(0) }
    var previousLoggedInState by remember { mutableStateOf(isLoggedIn) }
    // --------------------------------------

    // 로그인 후 복귀할 위치 저장
    var pendingInternalRoute by rememberSaveable { mutableStateOf<String?>(null) }  // 로그인 성공 후 실제로 어디로 갈지 저장
    // 실제 홈 이동 시 사용할 index
    var pendingHomeIndex by rememberSaveable { mutableStateOf(0) }
    var pendingExternalRoute by rememberSaveable { mutableStateOf<String?>(null) }
    // 로그인 화면에서 뒤로왔을 때 어떤 preview/bottomsheet 상태를 복원할지 저장
    var pendingLoginDestination by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingLoginHomeIndex by rememberSaveable { mutableStateOf<Int?>(null) }


    fun clearArchiveLocalState() {
        WidgetActionReceiver.clearBookmarkedIds(context)
        WidgetRefreshHelper.refreshAll(context)

    }

    fun clearLocalSessionState(clearAllAuth: Boolean) {
        if (clearAllAuth) {
            authLocalStorage.clear()
        } else {
            authLocalStorage.clearAuthOnly()
        }
        searchHistoryLocalStorage.clearAll()
        clearArchiveLocalState()

        pendingInternalRoute = null
        pendingHomeIndex = 0
        pendingExternalRoute = null
    }

    LaunchedEffect(isLoggedIn) {
        if (previousLoggedInState != isLoggedIn) {
            mainSessionVersion++
            if (!isLoggedIn) {
                clearArchiveLocalState()
            }
            previousLoggedInState = isLoggedIn
        }
        if (!isLoggedIn) {
            pendingInternalRoute = null
            pendingHomeIndex = 0
            pendingExternalRoute = null
        }
    }

    fun navigateAfterLogin() {
        // pendingLoginDestination: 로그인 유도 시 임시 보관된 목적지 (MainScreen initialRoute 변경 없이 보관)
        // pendingInternalRoute: 딥링크/외부 진입 등 다른 경로로 세팅된 목적지
        val targetInternalRoute = pendingLoginDestination ?: pendingInternalRoute
//        val targetHomeIndex = pendingHomeIndex
        val targetHomeIndex = pendingLoginHomeIndex ?: pendingHomeIndex
        val targetExternalRoute = pendingExternalRoute

        navController.navigate(NavRoutes.MAIN) {
            popUpTo(NavRoutes.AUTH) { inclusive = true }
            launchSingleTop = true
        }

        pendingInternalRoute = null
        pendingLoginDestination = null
        pendingLoginHomeIndex = null
        pendingHomeIndex = 0
        pendingExternalRoute = null

        if (targetExternalRoute != null) {
            navController.navigate(targetExternalRoute) {
                launchSingleTop = true
            }
        } else {
            pendingInternalRoute = targetInternalRoute
            pendingHomeIndex = targetHomeIndex
        }
    }



    // NEWS_LONG 은 MainScreen 내부 NavHost 에 있으므로 AppNavGraph 의 navController 로는
    // 직접 navigate 불가. initialNewsId 를 MainScreen 에 파라미터로 전달하여 처리.

    // 위젯 딥링크 진입 시 SETTING 등 다른 화면에 있으면 MAIN으로 복귀
    // → 이후 MainScreen 의 LaunchedEffect 가 NEWS_LONG 으로 이동
    // deepLinkVersion을 키에 포함 → 동일 newsId 재진입 시에도 재실행 보장
    LaunchedEffect(initialNewsId, deepLinkVersion) {
        if (initialNewsId == null) return@LaunchedEffect
        val currentRoute = navController.currentDestination?.route ?: return@LaunchedEffect
        if (currentRoute != NavRoutes.MAIN && currentRoute != NavRoutes.SPLASH) {
            navController.navigate(NavRoutes.MAIN) {
                popUpTo(NavRoutes.MAIN) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onFinish = {
                    if (authLocalStorage.isLoggedIn()) {
                        navController.navigate(NavRoutes.MAIN) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    } else if (onboardingLocalStorage.hasSavedInterests()) {
                        navController.navigate(NavRoutes.MAIN) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavRoutes.ONBOARDING_GUIDE) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(NavRoutes.ONBOARDING_GUIDE) {
            OnboardingGuideScreen(
                onNextClick = {
                    navController.navigate(NavRoutes.ONBOARDING_INTEREST)
                }
            )
        }

        composable(NavRoutes.ONBOARDING_INTEREST) {
            OnboardingInterestRoute(
                onNextClick = { selectedCategoryIds ->
                    val selectedIds = selectedCategoryIds.joinToString(",")
                    navController.navigate("${NavRoutes.ONBOARDING_SUB_INTEREST}/$selectedIds")
                }
            )
        }


        composable(
            route = "${NavRoutes.ONBOARDING_SUB_INTEREST}/{selectedIds}"
        ) { backStackEntry ->
            val selectedIdsString =
                backStackEntry.arguments?.getString("selectedIds").orEmpty()

            val selectedParentCategoryIds =
                if (selectedIdsString.isBlank()) {
                    emptyList()
                } else {
                    selectedIdsString.split(",").mapNotNull { it.toLongOrNull() }
                }

            OnboardingSubInterestRoute(
                selectedParentCategoryIds = selectedParentCategoryIds,
                onNextClick = {
                    WidgetRefreshHelper.refreshAll(context)
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(NavRoutes.ONBOARDING_GUIDE) { inclusive = true }
                    }
                }
            )
        }

        navigation(
            route = NavRoutes.AUTH,
            startDestination = NavRoutes.LOGIN
        ) {
            composable(NavRoutes.LOGIN) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.AUTH)
                }

                val repository = AuthRepository(NetworkModule.authApiService)
                val loginViewModel: LoginViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = LoginViewModelFactory(
                        authRepository,
                        authLocalStorage,
                        onboardingLocalStorage,
                        userRepository
                    )
                )

                LoginRoute(
                    viewModel = loginViewModel,
                    onNavigateToHome = {
                        isLoggedIn = true
                        loginMethod = authLocalStorage.getLoginMethod() ?: ""
                        navigateAfterLogin()
                    },
                    onNavigateToTerms = {
                        navController.navigate(NavRoutes.LOGIN_TERMS) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(NavRoutes.LOGIN_TERMS) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavRoutes.AUTH)
                }

                val repository = AuthRepository(NetworkModule.authApiService)
                val loginViewModel: LoginViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = LoginViewModelFactory(
                        authRepository,
                        authLocalStorage,
                        onboardingLocalStorage,
                        userRepository
                    )
                )

                LoginTermsRoute(
                    viewModel = loginViewModel,
                    onNavigateToOnboarding = {
                        isLoggedIn = true // 신규 유저 온보딩 진입 시에도 로그인 상태로 판단
                        loginMethod = authLocalStorage.getLoginMethod() ?: ""
                        navigateAfterLogin()
                    },
                    onNavigateToHome = {
                        isLoggedIn = true // 상태 업데이트!
                        loginMethod = authLocalStorage.getLoginMethod() ?: ""
                        navigateAfterLogin()
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // 하단 바가 있는 전체 메인 화면
        // 하단 바가 있는 전체 메인 화면
        composable(NavRoutes.MAIN) {
            MainScreen(
                isLoggedIn = isLoggedIn,
                sessionVersion = mainSessionVersion,
                initialRoute = pendingInternalRoute ?: NavRoutes.HOME,
                initialHomeIndex = pendingHomeIndex,
                initialDeepLinkNewsId = initialNewsId,
                initialOpenBookmark = initialOpenBookmark,
                deepLinkVersion = deepLinkVersion,
                onLogout = {
                    // 수정: 화면 이동 대신 상태 업데이트 및 로컬 데이터만 삭제
                    scope.launch {
                        val refreshToken = authLocalStorage.getRefreshToken()
                        if (refreshToken != null) {
                            authRepository.logout(refreshToken)
                        }
                        // 네이버 로그인이었으면 SDK 로컬 세션 정리 → 재로그인 시 계정 선택 화면 재노출
                        if (loginMethod == "naver") naverLogout()
                        clearLocalSessionState(clearAllAuth = false)

                        // 전역 상태 변수 업데이트 -> MainScreen 및 하위 탭들이 즉시 Recomposition됨
                        isLoggedIn = false
                        loginMethod = ""

                        Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                },
                onNavigateToLogin = { route, index ->
                    pendingExternalRoute = null
                    pendingLoginDestination = route
//                    pendingInternalRoute = route
//                    pendingHomeIndex = index ?: 0
                    pendingLoginHomeIndex = index
                    navController.navigate(NavRoutes.AUTH) {
                        launchSingleTop = true
                    }
                },
                onNavigateToNewsLong = { index ->
                    navController.navigate("${NavRoutes.NEWS_LONG}/$index")
                },
                onNavigateToSetting = {
                    navController.navigate(NavRoutes.SETTING)
                }
            )
        }

        composable(NavRoutes.SETTING) {
            SettingScreen(
                loginMethod = loginMethod,
                appVersion = BuildConfig.VERSION_NAME,
                isLoggedIn = isLoggedIn,
                isWithdrawing = isWithdrawing,
                isWithdrawn = isWithdrawn,
                withdrawErrorMessage = withdrawErrorMessage,
                onWithdrawErrorDismiss = {
                    withdrawErrorMessage = null
                    isWithdrawn = false // ★ 토스트 출력 후 상태 리셋
                },
                onBackClick = { navController.popBackStack() },
                onLoginClick = {
                    pendingInternalRoute = null
                    pendingHomeIndex = 0
                    pendingExternalRoute = NavRoutes.SETTING
                    navController.navigate(NavRoutes.AUTH) {
                        launchSingleTop = true
                    }
                },
                onAlarmSettingClick = { navController.navigate(NavRoutes.ALARM_SETTING) },
                onWidgetSettingClick = { navController.navigate(NavRoutes.WIDGET_INSTALL_GUIDE) },
                onInquiryClick = { navController.navigate(NavRoutes.INQUIRY) },
                onTermsClick = {
                    CustomTabsIntent.Builder().setShowTitle(true).build()
                        .launchUrl(context, Uri.parse("https://buttered-palm-c4c.notion.site/32e4778e859280eca570ce215e9ee048"))
                },
                onPrivacyClick = {
                    CustomTabsIntent.Builder().setShowTitle(true).build()
                        .launchUrl(context, Uri.parse("https://buttered-palm-c4c.notion.site/32e4778e85928009966ac723b49f0f95"))
                },
                onLogoutClick = {
                    scope.launch {
                        val refreshToken = authLocalStorage.getRefreshToken()
                        if (refreshToken != null) {
                            authRepository.logout(refreshToken)
                        }
                        // 1. 네이버 로그인이었으면 SDK 로컬 세션 정리 → 재로그인 시 계정 선택 화면 재노출
                        if (loginMethod == "naver") naverLogout()
                        // 2. 로컬 데이터 삭제
                        clearLocalSessionState(clearAllAuth = false)

                        // 3. 상태값 변경 -> SettingScreen UI 즉시 갱신
                        isLoggedIn = false
                        loginMethod = ""

                        // 4. 안내 메시지
                        Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                },
                onWithdrawClick = {
                    scope.launch {
                        isWithdrawing = true
                        withdrawErrorMessage = null

                        val currentMethod = authLocalStorage.getLoginMethod()
//                        val deleteResult = userRepository.deleteUser()
//                        if (deleteResult.isSuccess) {
//                            clearLocalSessionState(clearAllAuth = true)
//
//                            // 순서 중요: 세션 버전을 먼저 올려 ViewModel key를 변경하고, 로그인을 false로 만듦
//                            mainSessionVersion++
//                            isLoggedIn = false
//                            loginMethod = ""
//                            isWithdrawing = false
//                            isWithdrawn = true
//                        }

                        // 1. 카카오 사용자면 SDK unlink 먼저
                        if (currentMethod == "kakao") {
                            val unlinkResult = kakaoUnlink()
                            if (unlinkResult.isFailure) {
                                isWithdrawing = false
                                withdrawErrorMessage = "카카오 연결 해제에 실패했습니다.\n잠시 후 다시 시도해주세요."
                                return@launch
                            }
                        }

                        // 2. 네이버 사용자면 NidOAuth.logout()으로 토큰 폐기
                        if (currentMethod == "naver") {
                            val disconnectResult = naverDisconnect()
                            if (disconnectResult.isFailure) {
                                isWithdrawing = false
                                withdrawErrorMessage = "네이버 연결 해제에 실패했습니다.\n잠시 후 다시 시도해주세요."
                                return@launch
                            }
                        }

                        // 3. 구글 사용자면 Credential Manager credential state 초기화
                        if (currentMethod == "google") {
                            val clearResult = googleClearCredentialState(context)
                            if (clearResult.isFailure) {
                                isWithdrawing = false
                                withdrawErrorMessage = "구글 인증 초기화에 실패했습니다.\n잠시 후 다시 시도해주세요."
                                return@launch
                            }
                        }

                        // 3. DELETE /users/me 호출 — 결과 반드시 확인
                        val deleteResult = userRepository.deleteUser()
                        if (deleteResult.isFailure) {
                            isWithdrawing = false
                            withdrawErrorMessage = "회원탈퇴에 실패했습니다.\n잠시 후 다시 시도해주세요."
                            return@launch
                        }

                        // 3. 로컬 데이터 완전 초기화 (약관 동의 상태 포함)
                        clearLocalSessionState(clearAllAuth = true)

                        // ★ 4. UI 즉시 반영을 위한 상태 업데이트 (이 부분이 핵심)
                        isLoggedIn = false
                        loginMethod = ""
                        mainSessionVersion++

                        isWithdrawing = false
                        isWithdrawn = true // Toast 메시지 출력을 위한 플래그
                    }
                }
            )
        }

        composable(NavRoutes.ALARM_SETTING) {
            AlarmSettingRoute(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.WIDGET_INSTALL_GUIDE) {
            WidgetInstallGuideScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.INQUIRY) {
            OneToOneInquiryRoute(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "${NavRoutes.WEB_VIEW}?title={title}&url={url}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("url") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = Uri.decode(backStackEntry.arguments?.getString("title") ?: "")
            val url = Uri.decode(backStackEntry.arguments?.getString("url") ?: "")
            WebViewScreen(
                title = title,
                url = url,
                onBackClick = { navController.popBackStack() }
            )
        }


        // NEWS_LONG은 MainScreen 내부 NavHost에서 처리
        // AppNavGraph.kt 내 NavHost 부분에 추가
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
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
