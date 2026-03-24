package com.example.brife.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.brife.feature.auth.LoginRoute
import com.example.brife.feature.main.MainScreen
import com.example.brife.feature.onboarding.OnboardingGuideScreen
import com.example.brife.feature.onboarding.OnboardingInterestScreen
import com.example.brife.feature.onboarding.SplashScreen
import com.example.brife.feature.archive.ArchiveDetailScreen
import com.example.brife.feature.onboarding.OnboardingInterestRoute
import com.example.brife.navigation.NavRoutes


@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onFinish = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
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
                onNextClick = {
                    navController.navigate(NavRoutes.MAIN)
                }
            )
        }

//        composable(NavRoutes.HOME) {
//            HomeScreen(
//                onLoginClick = {
//                    navController.navigate(NavRoutes.LOGIN)
//                }
//            )
//        }

        composable(NavRoutes.LOGIN) {
            LoginRoute(
                onNavigateToHome = {
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(NavRoutes.ONBOARDING_GUIDE) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 하단 바가 있는 전체 메인 화면
        composable(NavRoutes.MAIN) {
            MainScreen(
                onLogout = { // 필요 시 로그아웃 로직
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.MAIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    // 상위 navController(AppNavGraph꺼)를 사용하여 이동
                    navController.navigate(NavRoutes.LOGIN)
                }
            )
        }

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