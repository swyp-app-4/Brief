package com.example.brife.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.brife.feature.archive.ArchiveScreen
import com.example.brife.feature.explore.ExploreScreen
import com.example.brife.feature.home.HomeScreen
import com.example.brife.feature.profile.ProfileScreen
import com.example.brife.navigation.NavRoutes
import com.example.brife.ui.component.AppNavigationBar
import com.example.brife.ui.component.AppTopBar
import com.example.brife.feature.home.HomeToLoginBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.HOME
    val backgroundColor =
        if (currentRoute == NavRoutes.HOME) Color.Transparent else Color.White

    val isLoggedIn = false
    var showLoginBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = backgroundColor,
            topBar = {
                when (currentRoute) {
                    NavRoutes.HOME -> AppTopBar()
                    NavRoutes.EXPLORE -> AppTopBar(showLogo = false, showSearch = true)
                    NavRoutes.ARCHIVE -> AppTopBar(
                        title = "아카이브",
                        showLogo = false,
                        showSettings = false,
                        centerTitle = true
                    )
                    NavRoutes.PROFILE -> AppTopBar(showSettings = false)
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
                        when (index) {
                            0 -> navigateTo(NavRoutes.HOME)
                            1 -> navigateTo(NavRoutes.EXPLORE)
                            2 -> {
                                if (isLoggedIn) {
                                    navigateTo(NavRoutes.ARCHIVE)
                                } else {
                                    showLoginBottomSheet = true
                                }
                            }
                            3 -> {
                                if (isLoggedIn) {
                                    navigateTo(NavRoutes.PROFILE)
                                } else {
                                    showLoginBottomSheet = true
                                }
                            }
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
                composable(NavRoutes.HOME) {
                    HomeScreen(
                        isLoggedIn = isLoggedIn,
                        onLoginRequired = {
                            showLoginBottomSheet = true
                        },
                        onLoginClick = onNavigateToLogin,
                        topPadding = innerPadding.calculateTopPadding()
                    )
                }

                composable(NavRoutes.EXPLORE) {
                    ExploreScreen(
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                    )
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
                    ProfileScreen(
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                    )
                }
            }
        }

        if (showLoginBottomSheet) {
            HomeToLoginBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { showLoginBottomSheet = false },
                onLoginClick = {
                    showLoginBottomSheet = false
                    onNavigateToLogin()
                }
            )
        }
    }
}