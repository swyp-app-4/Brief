package com.swyp.brife.feature.home

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import kotlin.math.absoluteValue
import com.swyp.brife.R

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
    onPageChanged: (Int) -> Unit = {},
    topPadding: Dp = 0.dp
) {
    var shareFlowStep by remember { mutableStateOf(ShareFlowStep.Closed) }
    var selectedShareData by remember { mutableStateOf<NewsShareData?>(null) }

//    var showBottomSheet by remember { mutableStateOf(false) }
//    // 한 세션 내에서 로그인 유도 바텀시트를 이미 표시했는지 여부
//    // "더 둘러보기" 클릭 후 다음 페이지로 넘어가도 다시 표시되지 않음
//    var loginPromptShown by remember { mutableStateOf(false) }

    val pageCount = if (isLoading) 1 else newsList.size
    var restoredPage by rememberSaveable { mutableIntStateOf(initialPage) }

    // pagerState 초기화 시 initialPage 지원
    val pagerState = rememberPagerState(
        initialPage = restoredPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0)),
        pageCount = { pageCount }
    )

    LaunchedEffect(pagerState, pageCount) {
        snapshotFlow { pagerState.settledPage }
            .collect { page ->
                val clamped = page.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
                restoredPage = clamped
                // B: 로딩 중 일시적 클램핑(page=0)은 저장하지 않음
                if (!isLoading) onPageChanged(clamped)
            }
    }

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

    // 마지막으로 실제 처리한 reset key 추적
    // remember (not rememberSaveable): HomeScreen 재진입 시 현재 key로 재초기화되어
    // "이미 처리된 이벤트"로 인식 → 뒤로가기 복귀 시 불필요한 scrollToPage 방지
    var lastProcessedResetKey by remember { mutableIntStateOf(forceResetToThirdPageKey) }

    LaunchedEffect(forceResetToThirdPageKey, isLoggedIn) {
        // 값이 실제로 새로 증가한 경우에만 page 제한 적용
        // 재진입(뒤로가기 복귀) 시에는 lastProcessedResetKey == forceResetToThirdPageKey이므로 스킵
        if (!isLoggedIn && forceResetToThirdPageKey > lastProcessedResetKey) {
            lastProcessedResetKey = forceResetToThirdPageKey
            pagerState.scrollToPage(maxAccessiblePage)
        }
    }




    val illustrationHeight = 130.dp
    val illustrationCardOverlap = 40.dp
    val emptyStateHeight = 220.dp
    val minCardHeight = 410.dp
    val maxCardHeight = 540.dp
    val pagerCardHeightDifference = 15.dp
    val pagerIndicatorAreaHeight = 42.dp
    val homeBottomBreathingSpace = 15.dp

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableContentHeight = (maxHeight - topPadding).coerceAtLeast(0.dp)
        val reservedHomeHeight =
            illustrationHeight - illustrationCardOverlap +
                pagerCardHeightDifference + pagerIndicatorAreaHeight +
                homeBottomBreathingSpace
        val cardHeight = (availableContentHeight - reservedHomeHeight)
            .coerceIn(minCardHeight, maxCardHeight)
        val cardContentScale = ((cardHeight - minCardHeight).value /
            (maxCardHeight - minCardHeight).value).coerceIn(0f, 1f)
        val pagerHeight = cardHeight + pagerCardHeightDifference

        Image(
            painter = painterResource(id = R.drawable.homescreen_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val contentHeight =
                if (!isLoading && newsList.isEmpty()) emptyStateHeight else pagerHeight

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(illustrationHeight + contentHeight - illustrationCardOverlap)
            ) {
                HomeIllustrationArea(
                    illustrationRes = illustrationRes,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(illustrationHeight)
                        .zIndex(0f)
                )

                // 빈 상태: 로딩 완료 후 뉴스가 없으면 디버깅용 메시지 표시
                // (카드 자체를 숨기거나 fallback 데이터를 넣지 않음 — 백엔드 문제 확인용)
                if (!isLoading && newsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(emptyStateHeight)
                            .zIndex(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "추천 뉴스를 불러오지 못했습니다",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(pagerHeight)
                            .zIndex(1f)
                            .nestedScroll(guestForwardBlocker),
                        contentPadding = PaddingValues(horizontal = 36.dp),
                        pageSpacing = 12.dp,
                        beyondViewportPageCount = 1
                    ) { page ->
                        val pageOffset =
                            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        val absOffset = pageOffset.absoluteValue

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = minCardHeight, max = cardHeight)
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
                                },
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
                                    contentScaleFactor = cardContentScale,
                                    onShareClick = {
                                        selectedShareData = newsList[page].toNewsShareData()
                                        shareFlowStep = ShareFlowStep.Template
                                    },
                                    onDetailClick = { onDetailClick(newsList[page]) }
                                )
                            }
                        }
                    }
                }
            }

            if (!isLoading && newsList.isEmpty()) {
                Spacer(modifier = Modifier.height(42.dp))
            } else {
                Spacer(modifier = Modifier.height(10.dp))

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

        NewsShareFlowHost(
            step = shareFlowStep,
            shareData = selectedShareData,
            onStepChange = { shareFlowStep = it },
            onDismiss = {
                shareFlowStep = ShareFlowStep.Closed
                selectedShareData = null
            }
        )
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


