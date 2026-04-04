package com.example.brife.feature.home

import android.graphics.Rect
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
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
    isLoading: Boolean = false,
    initialPage: Int = 0,
    forceResetToThirdPageKey: Int = 0,
    onLoginRequired: () -> Unit,
    onLoginClick: () -> Unit = {},
    onDetailClick: (HomeNewsCardItem) -> Unit,
    onShareClick: (HomeNewsCardItem) -> Unit = {},
    topPadding: Dp = 0.dp
) {
    val context = LocalContext.current
    val view = LocalView.current

//    var showBottomSheet by remember { mutableStateOf(false) }
//    // 한 세션 내에서 로그인 유도 바텀시트를 이미 표시했는지 여부
//    // "더 둘러보기" 클릭 후 다음 페이지로 넘어가도 다시 표시되지 않음
//    var loginPromptShown by remember { mutableStateOf(false) }

    val pageCount = if (isLoading) 1 else newsList.size


    // pagerState 초기화 시 initialPage 지원
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )

    // 현재 페이지 category 기반으로 가운데 일러스트 결정
    // 로딩 중에는 img_home_life 고정
    val currentCategory = if (!isLoading && newsList.isNotEmpty())
        newsList[pagerState.currentPage].category
    else ""
    val illustrationRes = if (isLoading) R.drawable.img_home_life
    else illustrationResForCategory(currentCategory)


    LaunchedEffect(newsList, isLoading) {
        android.util.Log.d(
            "HomeScreen",
            "받은 값 newsList=${newsList.size}, isLoading=$isLoading"
        )
    }


    // 비로그인 시 3번째 카드까지만 실제 열람 가능
    // targetPage 기반으로 4번째 이상 이동 시도를 감지 → 즉시 복귀 + 바텀시트
    // - distinctUntilChanged 미사용: 시도할 때마다 매번 트리거
    // - scrollToPage (애니메이션 없음): 역방향 애니메이션 중 재트리거 원천 차단
    val maxAccessiblePage = 2
    val isGuestLockedOnThirdCard =
        !isLoggedIn && !isLoading && pagerState.settledPage >= maxAccessiblePage
    val guestForwardBlocker = remember(isGuestLockedOnThirdCard, forceResetToThirdPageKey) {
        object : NestedScrollConnection {
            private var promptedThisGesture = false

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!isGuestLockedOnThirdCard) return Offset.Zero

                if (available.x < 0f) {
                    if (!promptedThisGesture) {
                        promptedThisGesture = true
                        onLoginRequired()
                    }
                    return Offset(x = available.x, y = 0f)
                }

                if (available.x > 0f) {
                    promptedThisGesture = false
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (!isGuestLockedOnThirdCard) return Velocity.Zero

                if (available.x < 0f) {
                    if (!promptedThisGesture) {
                        promptedThisGesture = true
                        onLoginRequired()
                    }
                    return Velocity(x = available.x, y = 0f)
                }

                promptedThisGesture = false
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                promptedThisGesture = false
                return Velocity.Zero
            }
        }
    }

    LaunchedEffect(isLoggedIn, pagerState) {
        snapshotFlow { pagerState.currentPage to pagerState.targetPage }
            .collect { (currentPage, targetPage) ->
                if (!isLoggedIn && (currentPage > maxAccessiblePage || targetPage > maxAccessiblePage)) {
                    onLoginRequired()
                    pagerState.scrollToPage(maxAccessiblePage)
                }
            }
    }

    LaunchedEffect(isLoggedIn, pageCount) {
        if (!isLoggedIn && pagerState.currentPage > maxAccessiblePage) {
            pagerState.scrollToPage(maxAccessiblePage)
        }
    }

    LaunchedEffect(forceResetToThirdPageKey, isLoggedIn) {
        if (!isLoggedIn && forceResetToThirdPageKey > 0) {
            pagerState.scrollToPage(maxAccessiblePage)
        }
    }




    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.homescreen_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
//                .padding(top = topPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 일러스트 영역 — 남은 공간을 채우며 카드 상단과 20dp 겹침
            // offset(y=20.dp): 시각적으로 20dp 아래로 내려가 카드 영역과 겹침
            // HorizontalPager가 나중에 그려지므로 카드가 일러스트 하단을 덮음
            HomeIllustrationArea(
                illustrationRes = illustrationRes,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .offset(y = 30.dp)
            )

            // 빈 상태: 로딩 완료 후 뉴스가 없으면 디버깅용 메시지 표시
            // (카드 자체를 숨기거나 fallback 데이터를 넣지 않음 — 백엔드 문제 확인용)
            if (!isLoading && newsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "추천 뉴스를 불러오지 못했습니다\n[DEBUG] isLoading=$isLoading, newsList=0",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(42.dp))
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .nestedScroll(guestForwardBlocker),
                    contentPadding = PaddingValues(horizontal = 36.dp),
                    pageSpacing = 12.dp,
                    beyondViewportPageCount = 1
                ) { page ->
                    val pageOffset =
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    val absOffset = pageOffset.absoluteValue

                    // 카드 이미지 공유를 위해 카드 영역의 window 내 좌표를 추적
                    var cardCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .zIndex(1f - absOffset.coerceIn(0f, 1f))
                            .graphicsLayer {
                                val scale = lerp(
                                    start = 0.89f,
                                    stop = 0.99f,
                                    fraction = 1f - absOffset.coerceIn(0f, 1f)
                                )
                                scaleX = scale
                                scaleY = scale
                                alpha = lerp(
                                    start = 0.6f,
                                    stop = 1f,
                                    fraction = 1f - absOffset.coerceIn(0f, 1f)
                                )
                            }
                            .onGloballyPositioned { cardCoords = it },
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        if (isLoading) {
                            HomeNewsCardSkeleton()
                        } else {
                            HomeNewsCardContent(
                                item = newsList[page],
                                modifier = Modifier.fillMaxWidth(),
                                onShareClick = {
                                    val coords = cardCoords ?: return@HomeNewsCardContent
                                    val bounds = coords.boundsInWindow()
                                    val srcRect = Rect(
                                        bounds.left.toInt(),
                                        bounds.top.toInt(),
                                        bounds.right.toInt(),
                                        bounds.bottom.toInt()
                                    )
                                    captureWindowBitmap(context, view, srcRect) { bitmap ->
                                        shareImageBitmap(context, bitmap, newsList[page].title)
                                    }
                                },
                                onDetailClick = { onDetailClick(newsList[page]) }
                            )
                        }
                    }
                    }

                Spacer(modifier = Modifier.height(18.dp))

                // 페이지 인디케이터
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pageCount) { index ->
                        val color =
                            if (pagerState.currentPage == index) Color.White
                            else Color.White.copy(alpha = 0.5f)
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 일러스트 영역
// 왼쪽: img_home_mailbox (고정)
// 가운데: category별 이미지 (illustrationRes로 전달)
// ─────────────────────────────────────────────────────────────

@Composable
private fun HomeIllustrationArea(
    illustrationRes: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // 왼쪽 고정 — mailbox
        Image(
            painter = painterResource(id = R.drawable.img_home_mailbox),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 70.dp)
                .height(85.dp)
        )
        // 가운데 — category 기반 이미지
        Image(
            painter = painterResource(id = illustrationRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(130.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// 스켈레톤 UI — 로딩 중 카드 내부를 대체
// ─────────────────────────────────────────────────────────────

@Composable
private fun HomeNewsCardSkeleton() {
    val shimmerColor = Color(0xFFE0E0E0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        // 카테고리 칩 영역
        Row {
            Box(
                Modifier
                    .width(60.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(shimmerColor)
            )
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .width(80.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(shimmerColor)
            )
        }
        Spacer(Modifier.height(8.dp))
        // 제목 (2줄)
        Box(
            Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerColor)
        )
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth(0.65f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerColor)
        )
        Spacer(Modifier.height(16.dp))
        // 알림 텍스트
        Box(
            Modifier
                .fillMaxWidth(0.5f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerColor)
        )
        Spacer(Modifier.height(18.dp))
        // 간단요약 + 살펴보기 박스
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF5F5F5))
        )
        Spacer(Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// category String → drawable res 매핑
// 매핑 없는 category는 img_home_life 기본값 사용
// ─────────────────────────────────────────────────────────────

// 구분자(·, /, 공백 등) 형식에 무관하게 contains로 매핑
private fun illustrationResForCategory(category: String): Int = when {
    category.contains("라이프") || category.contains("성장") -> R.drawable.img_home_life
    category.contains("IT") || category.contains("테크") -> R.drawable.img_home_tech
    category.contains("시사") || category.contains("정치") -> R.drawable.img_home_politics
    category.contains("경제") || category.contains("재테크") -> R.drawable.img_home_economy
    category.contains("엔터") || category.contains("스포츠") || category.contains("연예") -> R.drawable.img_home_entertainment
    category.contains("문화") || category.contains("예술") -> R.drawable.img_home_art
    else -> R.drawable.img_home_life
}


