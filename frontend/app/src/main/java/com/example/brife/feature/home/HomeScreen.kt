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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.example.brife.R
import com.example.brife.feature.main.MainScreen
import com.example.brife.ui.theme.BrifeTheme
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    newsList: List<HomeNewsCardItem>,
    isLoggedIn: Boolean,
    onLoginRequired: () -> Unit,
    onLoginClick: () -> Unit = {},
    onDetailClick: (HomeNewsCardItem) -> Unit,
    onShareClick: (HomeNewsCardItem) -> Unit = {},
    topPadding: Dp = 0.dp
) {
//    var selectedIndex by remember { mutableIntStateOf(0) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(pageCount = { newsList.size })

    LaunchedEffect(pagerState.currentPage, isLoggedIn) {
        if (!isLoggedIn && pagerState.currentPage >= 3) {
            onLoginRequired()
        }
    }

    // Scaffold, TopBar, BottomBar, Image 배경을 모두 제거했습니다.
    // MainScreen에서 넘겨준 영역(Box) 안에서 콘텐츠만 배치합니다.
    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.homescreen_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding), // 이 부분을 추가해야 상단바 아이콘과 겹치지 않습니다.
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                contentPadding = PaddingValues(horizontal = 35.dp),
                pageSpacing = 0.dp
            ) { page ->
                val pageOffset =
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val absOffset = pageOffset.absoluteValue

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .zIndex(1f - absOffset.coerceIn(0f, 1f))
                        .graphicsLayer {
                            val scale = lerp(
                                start = 0.7f,
                                stop = 1f,
                                fraction = 1f - absOffset.coerceIn(0f, 1f)
                            )
                            scaleX = scale
                            scaleY = scale
                            alpha = lerp(
                                start = 0.6f,
                                stop = 1f,
                                fraction = 1f - absOffset.coerceIn(0f, 1f)
                            )
                            translationX = -pageOffset * 22.dp.toPx()
                        },
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    HomeNewsCardContent(
                        item = newsList[page],
                        modifier = Modifier.fillMaxWidth(),
                        onShareClick = { onShareClick(newsList[page]) },
                        onDetailClick = {
                            onDetailClick(newsList[page])
                        }
                    )
                }

                // 페이지 인디케이터
                Row(
                    modifier = Modifier.padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(newsList.size) { index ->
                        val color =
                            if (pagerState.currentPage == index) Color.White else Color.White.copy(
                                alpha = 0.5f
                            )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }

        }
    }
}

// --- 프리뷰 영역 ---

@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=2340px,dpi=440",
    name = "1. 홈 화면 (메인 레이아웃 적용)"
)
@Composable
fun HomeScreenInMainPreview() {
    BrifeTheme {
        // MainScreen을 호출하여 상단바가 투명하게 배경 위에 겹치는지 확인합니다.
        // (MainScreen에서 currentRoute가 HOME일 때 투명하게 설정했으므로 여기서 확인 가능)
        MainScreen(
            onLogout = {},
            onNavigateToLogin = {},
            onNavigateToNewsLong = {}
        )

    }
}