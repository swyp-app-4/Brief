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
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isLoggedIn: Boolean,
    onLoginRequired: () -> Unit,
    onLoginClick: () -> Unit = {},
    topPadding: Dp = 0.dp
) {
//    var selectedIndex by remember { mutableIntStateOf(0) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val cardItems = remember {
        listOf(
            HomeNewsCardItem(
                category = "경제",
                title = "기준금리 동결 속 소비 회복 기대감 확대",
                notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
                summary = "한국은행의 기준금리 동결 이후 시장은 당분간 안정세를 유지할 것으로 전망되고 있다.",
                insight = "금리 흐름은 대출, 소비, 투자 심리에 직접 영향을 미치기 때문에 개인 자산관리와 소비 전략에도 연결해서 볼 필요가 있다."
            ),
            HomeNewsCardItem(
                category = "IT",
                title = "생성형 AI 서비스 경쟁 본격화",
                notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
                summary = "국내외 주요 기업들이 생성형 AI 기능을 자사 플랫폼에 빠르게 통합하고 있다.",
                insight = "단순 기능 추가를 넘어 사용자 체류 시간, 생산성, 플랫폼 락인 전략까지 연결되는 흐름으로 해석할 수 있다."
            ),
            HomeNewsCardItem(
                category = "사회",
                title = "청년 주거 지원 정책 체감도 점검 필요",
                notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
                summary = "청년 대상 주거 지원 정책은 확대되고 있지만 실제 체감도는 지역과 조건에 따라 차이가 크다.",
                insight = "정책 발표 자체보다 접근성, 신청 절차, 실질 혜택 범위를 함께 봐야 사용자가 체감할 수 있는 정보가 된다."
            ),
            HomeNewsCardItem(
                category = "과학",
                title = "우주 산업 민간 투자 확대 흐름 지속",
                notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
                summary = "민간 우주 산업에 대한 투자 확대와 기술 경쟁이 동시에 이어지고 있다.",
                insight = "장기적으로는 위성 통신, 국방, 물류, 데이터 산업까지 파급될 수 있어 단순 연구 이슈를 넘어 산업 구조 변화로 볼 수 있다."
            ),
            HomeNewsCardItem(
                category = "문화",
                title = "짧은 영상 중심의 뉴스 소비 패턴 강화",
                notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
                summary = "짧은 영상과 카드형 콘텐츠를 통해 뉴스를 소비하는 흐름이 더욱 강해지고 있다.",
                insight = "콘텐츠 형식이 바뀌면 전달 방식뿐 아니라 정보 신뢰도, 해석 방식, 사용자의 집중 시간도 함께 달라진다."
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { cardItems.size })

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
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp),
                contentPadding = PaddingValues(horizontal = 35.dp),
                pageSpacing = 0.dp
            ) { page ->
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val absOffset = pageOffset.absoluteValue

                Card(
                    modifier = Modifier
                        .fillMaxSize()
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        HomeNewsCardContent(
                            item = cardItems[page],
                            onShareClick = { /* 공유 로직 */ }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 페이지 인디케이터
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
            Spacer(modifier = Modifier.weight(0.1f))
        }

    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        isLoggedIn = false,
        onLoginRequired = {},
        onLoginClick = {}
    )
}