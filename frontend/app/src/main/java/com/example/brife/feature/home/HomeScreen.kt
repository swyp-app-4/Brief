package com.example.brife.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.ui.component.AppNavigationBar
import com.example.brife.ui.component.AppTopBar

@Composable
fun HomeScreen() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    // 캐로셀을 위한 데이터 (예시)
    val cardItems = listOf("카드 뉴스 1", "카드 뉴스 2", "카드 뉴스 3")
    val pagerState = rememberPagerState(pageCount = { cardItems.size })

    Scaffold(
        topBar = {
            AppTopBar(onSettingClick = { /* 설정 이동 로직 */ })
        },
        bottomBar = {
            AppNavigationBar(
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it }
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 카드 뉴스 캐로셀 (HorizontalPager)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp), // 카드 높이 조정
                contentPadding = PaddingValues(horizontal = 40.dp),
                pageSpacing = 16.dp
            ) { page ->
                Card(
                    modifier = Modifier.fillMaxSize(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = cardItems[page], style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }

            // 추가적인 내용이 필요하면 여기에 배치
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}