package com.example.brife.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.brife.R
import com.example.brife.ui.component.AppNavigationBar
import com.example.brife.ui.component.AppTopBar
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLoginClick: () -> Unit = {}
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val cardItems = remember {
        listOf("카드 뉴스 1", "카드 뉴스 2", "카드 뉴스 3", "카드 뉴스 4", "카드 뉴스 5")
    }

    val pagerState = rememberPagerState(pageCount = { cardItems.size })
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        delay(3000)
        showBottomSheet = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 배경 이미지를 가장 아래 레이어에 배치 (전체 화면 - 상태바 포함)
        Image(
            painter = painterResource(id = R.drawable.homescreen_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent, // 배경 이미지가 보이도록 투명 설정
            topBar = {
                AppTopBar(onSettingClick = { })
            },
            bottomBar = {
                AppNavigationBar(
                    selectedIndex = selectedIndex,
                    onItemSelected = { selectedIndex = it }
                )
            }
        ) { innerPadding ->
            // 상단바와 하단바를 제외한 영역
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 상단 유연한 여백
                    Spacer(modifier = Modifier.weight(1f))

                    // 카드 뉴스 캐로셀
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp),
                        contentPadding = PaddingValues(horizontal = 55.dp), // 옆 카드 노출
                        pageSpacing = 16.dp
                    ) { page ->
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

                                    // 현재 페이지 1.0, 주변 페이지 0.85 크기
                                    val scale = lerp(
                                        start = 0.85f,
                                        stop = 1f,
                                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                    )
                                    scaleX = scale
                                    scaleY = scale

                                    // 주변 카드는 약간 투명하게
                                    alpha = lerp(
                                        start = 0.6f,
                                        stop = 1f,
                                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                    )
                                },
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

                    // 페이지 인디케이터 영역
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(cardItems.size) { index ->
                            val color = if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.5f)
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }

                    // 하단 유연한 여백
                    Spacer(modifier = Modifier.weight(1.2f))
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}