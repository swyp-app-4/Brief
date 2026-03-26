package com.example.brife.feature.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.brife.feature.archive.ArchiveScreen
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToNewsLong: (String) -> Unit
) {
    val context = LocalContext.current
    val onboardingStorage = remember { OnboardingLocalStorage(context) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.HOME

    val isNewsLongRoute = currentRoute?.startsWith("${NavRoutes.NEWS_LONG}/") == true
    val isInterestResetRoute = currentRoute == NavRoutes.ONBOARDING_INTEREST_RESET ||
            currentRoute?.startsWith("${NavRoutes.ONBOARDING_SUB_INTEREST_RESET}/") == true

    val backgroundColor =
        if (currentRoute == NavRoutes.HOME) Color.Transparent else Color.White

    val isLoggedIn = false
    var showLoginBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    AppTopBar()
                }
                isNewsLongRoute -> {
                    AppTopBar(
                        showLogo = false,
                        showSearch = false,
                        showSettings = false
//                        backgroundColor = Color.Transparent
                    )
                }
                currentRoute == NavRoutes.EXPLORE -> {
                    AppTopBar(showLogo = false, showSearch = true)
                }
                currentRoute == NavRoutes.ARCHIVE -> {
                    AppTopBar(
                        title = "보관함",
                        showLogo = false,
                        showSettings = false,
                        centerTitle = true
                    )
                }
                // PROFILE: topbar 없음 (요구사항)
            }
        },
        bottomBar = {
            if (!isNewsLongRoute && !isInterestResetRoute) {
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
                            3 -> {
                                if (isLoggedIn) navigateTo(NavRoutes.PROFILE)
                                else showLoginBottomSheet = true
                            }
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
                    newsList = shortsampleHomeNews,
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
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                    uiState = ProfileUiState(
                        isLoggedIn = false,
                        interests = profileInterests
                    ),
                    onResetInterestClick = {
                        navController.navigate(NavRoutes.ONBOARDING_INTEREST_RESET)
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
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        if (showLoginBottomSheet) {
            HomeToLoginBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { showLoginBottomSheet = false },
                onLoginClick = {
                    showLoginBottomSheet = false
                    onNavigateToLogin()
                },
                onBrowseClick = {
                    showLoginBottomSheet = false
                    navigateTo(NavRoutes.PROFILE)
                }
            )
        }
    }
}
