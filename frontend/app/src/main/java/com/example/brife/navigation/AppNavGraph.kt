package com.example.brife.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.brife.data.local.OnboardingLocalStorage
import com.example.brife.data.local.longsampleHomeNews
import com.example.brife.data.remote.NetworkModule
import com.example.brife.data.repository.AuthRepository
import com.example.brife.feature.auth.LoginRoute
import com.example.brife.feature.main.MainScreen
import com.example.brife.feature.onboarding.OnboardingGuideScreen
import com.example.brife.feature.onboarding.OnboardingInterestRoute
import com.example.brife.feature.onboarding.OnboardingSubInterestRoute
import com.example.brife.feature.onboarding.SplashScreen
import com.example.brife.feature.archive.ArchiveDetailScreen
import com.example.brife.feature.auth.LoginTermsRoute
import com.example.brife.feature.setting.SettingScreen
import com.example.brife.feature.setting.SettingUiState
import com.example.brife.feature.setting.WidgetInstallGuideScreen
import com.example.brife.feature.webview.WebViewScreen
import com.example.brife.feature.auth.LoginViewModel
import com.example.brife.feature.auth.LoginViewModelFactory
import com.example.brife.feature.onboarding.OnboardingInterestRoute
import com.example.brife.navigation.NavRoutes


@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

//    val context = LocalContext.current
//    val hasCompletedOnboarding = OnboardingLocalStorage(context).hasCompletedOnboarding()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onFinish = {
//                    if (hasCompletedOnboarding) {
//                        navController.navigate(NavRoutes.MAIN) {
//                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
//                        }
//                    } else {
                        navController.navigate(NavRoutes.ONBOARDING_GUIDE) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
//                    }
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
                    factory = LoginViewModelFactory(repository)
                )

                LoginRoute(
                    viewModel = loginViewModel,
                    onNavigateToHome = {
                        navController.navigate(NavRoutes.MAIN) {
                            popUpTo(NavRoutes.AUTH) { inclusive = true }
                        }
                    },
                    onNavigateToTerms = {
                        navController.navigate(NavRoutes.LOGIN_TERMS)
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
                    factory = LoginViewModelFactory(repository)
                )

                LoginTermsRoute(
                    viewModel = loginViewModel,
                    onNavigateToOnboarding = {
                        navController.navigate(NavRoutes.ONBOARDING_GUIDE) {
                            popUpTo(NavRoutes.AUTH) { inclusive = true }
                        }
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
                onLogout = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.MAIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN)
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
                uiState = SettingUiState(loginMethod = "Google", appVersion = "1.0.0"),
                isLoggedIn = false, // TODO: 실제 로그인 상태로 교체
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.navigate(NavRoutes.LOGIN) },
                onWidgetSettingClick = { navController.navigate(NavRoutes.WIDGET_INSTALL_GUIDE) },
                onTermsClick = {
                    val encodedUrl = Uri.encode("https://buttered-palm-c4c.notion.site/32e4778e859280eca570ce215e9ee048")
                    val encodedTitle = Uri.encode("서비스 이용약관")
                    navController.navigate("${NavRoutes.WEB_VIEW}?title=$encodedTitle&url=$encodedUrl")
                },
                onPrivacyClick = {
                    val encodedUrl = Uri.encode("https://buttered-palm-c4c.notion.site/32e4778e85928009966ac723b49f0f95")
                    val encodedTitle = Uri.encode("개인정보 처리방침")
                    navController.navigate("${NavRoutes.WEB_VIEW}?title=$encodedTitle&url=$encodedUrl")
                }
            )
        }

        composable(NavRoutes.WIDGET_INSTALL_GUIDE) {
            WidgetInstallGuideScreen(
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
            route = "${NavRoutes.ARCHIVE_DETAIL}/{folderName}"
        ) { backStackEntry ->
            val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
            ArchiveDetailScreen(
                folderName = folderName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}