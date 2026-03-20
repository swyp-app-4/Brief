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
    val backgroundColor = if (currentRoute == NavRoutes.HOME) Color.Transparent else Color.White


    // 상위 Box에서 배경 이미지 제거
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = backgroundColor,
            topBar = {
                when (currentRoute) {
                    NavRoutes.HOME -> AppTopBar()
                    NavRoutes.EXPLORE -> AppTopBar(showLogo = false, showSearch = true,)
                    NavRoutes.ARCHIVE -> AppTopBar(
                        title = "아카이브",
                        showLogo = false,
                        showSettings = false,
                        centerTitle = true
                    )

                    NavRoutes.PROFILE -> AppTopBar(showSettings = false,)
                }
            },
            bottomBar = {
                AppNavigationBar(
                    selectedIndex = when (currentRoute) {
                        NavRoutes.HOME -> 0
                        NavRoutes.EXPLORE -> 1
                        NavRoutes.ARCHIVE -> 2
                        NavRoutes.PROFILE -> 3
                        else -> 0
                    },
                    onItemSelected = { index ->
                        val route = when (index) {
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
            // MainScreen.kt 내부 수정
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = NavRoutes.HOME,
                // NavHost 자체에는 바텀바 영역만 확보 (상단은 비워둠)
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                composable(NavRoutes.HOME) {
                    HomeScreen(
                        onLoginClick = onNavigateToLogin,
                        // HomeScreen은 이 padding을 배경 위에 콘텐츠 배치용으로만 사용 (이미지는 꽉 참)
                        topPadding = innerPadding.calculateTopPadding()
                    )
                }
                composable(NavRoutes.EXPLORE) {
                    // ExploreScreen, ArchiveScreen 등은 Modifier로 상단 패딩을 강제 적용
                    ExploreScreen(modifier = Modifier.padding(top = innerPadding.calculateTopPadding()))
                }
                composable(NavRoutes.ARCHIVE) {
                    ArchiveScreen(
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                        onNavigateToDetail = { folderName ->
                            navController.navigate("${NavRoutes.ARCHIVE_DETAIL}/$folderName")
                        }
                    )
                }
                composable(NavRoutes.PROFILE) {
                    ProfileScreen(modifier = Modifier.padding(top = innerPadding.calculateTopPadding()))
                }
            }
        }
    }
}