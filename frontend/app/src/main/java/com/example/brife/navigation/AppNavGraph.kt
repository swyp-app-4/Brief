package com.example.brife.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.brife.feature.auth.LoginRoute
import com.example.brife.feature.auth.LoginScreen
import com.example.brife.feature.home.HomeScreen
import com.example.brife.feature.onboarding.OnboardingGuideScreen
import com.example.brife.feature.onboarding.OnboardingInterestScreen
import com.example.brife.feature.onboarding.SplashScreen

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
                    navController.navigate(NavRoutes.ONBOARDING_GUIDE) {
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
            OnboardingInterestScreen(
                onNextClick = {
                    navController.navigate(NavRoutes.HOME)
                }
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(
                onLoginClick = {
                    navController.navigate(NavRoutes.LOGIN)
                }
            )
        }

        composable(NavRoutes.LOGIN) {
            LoginRoute(
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(NavRoutes.ONBOARDING_INTEREST) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
    }
}