package com.example.brife.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.brife.feature.home.HomeScreen
import com.example.brife.feature.explore.ExploreScreen
import com.example.brife.feature.archive.ArchiveScreen
import com.example.brife.feature.profile.ProfileScreen
import com.example.brife.navigation.NavRoutes
import com.example.brife.ui.component.AppNavigationBar
import com.example.brife.ui.component.AppTopBar

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit // 1. 상위로 가는 콜백 추가
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.HOME

    // 상위 Box에서 배경 이미지 제거
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent, // 하위 화면의 배경색/이미지가 보이도록 투명 유지
            topBar = {
                when (currentRoute) {
                    NavRoutes.HOME -> AppTopBar(showLogo = true, showSettings = true)
                    NavRoutes.EXPLORE -> AppTopBar(showLogo = false, showSearch = true, showSettings = true)
                    NavRoutes.ARCHIVE -> AppTopBar(title = "아카이브", showLogo = false, showSettings = false)
                    NavRoutes.PROFILE -> AppTopBar(showLogo = true, showSettings = false)
                }
            },
            bottomBar = {
                AppNavigationBar(
                    selectedIndex = when(currentRoute) {
                        NavRoutes.HOME -> 0
                        NavRoutes.EXPLORE -> 1
                        NavRoutes.ARCHIVE -> 2
                        NavRoutes.PROFILE -> 3
                        else -> 0
                    },
                    onItemSelected = { index ->
                        val route = when(index) {
                            0 -> NavRoutes.HOME
                            1 -> NavRoutes.EXPLORE
                            2 -> NavRoutes.ARCHIVE
                            3 -> NavRoutes.PROFILE
                            else -> NavRoutes.HOME
                        }
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = NavRoutes.HOME,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                composable(NavRoutes.HOME) { HomeScreen(
                    onLoginClick = onNavigateToLogin, // 2. 여기다 전달!
                    topPadding = innerPadding.calculateTopPadding() // 상단바 높이를 전달
                )
                }
                composable(NavRoutes.EXPLORE) { ExploreScreen() }
                composable(NavRoutes.ARCHIVE) { ArchiveScreen() }
                composable(NavRoutes.PROFILE) { ProfileScreen() }
            }
        }
    }
}