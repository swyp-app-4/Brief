package com.example.brife.feature.home

import android.R.attr.padding
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppNavigationBar
import com.example.brife.ui.component.AppTopBar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLoginClick: () -> Unit = {}
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val cardItems = listOf("카드 뉴스 1", "카드 뉴스 2", "카드 뉴스 3")
    val pagerState = rememberPagerState(pageCount = { cardItems.size })
    val sheetState = rememberModalBottomSheetState()

    // 3초 후 바텀시트 노출
    LaunchedEffect(Unit) {
        delay(3000)
        showBottomSheet = true
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.homescreen_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                AppTopBar(onSettingClick = { /* 설정 이동 로직 */ })
            },
            bottomBar = {
                AppNavigationBar(
                    selectedIndex = selectedIndex,
                    onItemSelected = { selectedIndex = it }
                )
            }
        ) { innerPadding ->

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(150.dp))

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentPadding = PaddingValues(horizontal = 40.dp),
                        pageSpacing = 16.dp
                    ) { page ->
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cardItems[page],
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                }

                if (showBottomSheet) {
                    HomeToLoginBottomSheet(
                        sheetState = sheetState,
                        onDismissRequest = { showBottomSheet = false },
                        onLoginClick = {
                            showBottomSheet = false
                            onLoginClick()
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}