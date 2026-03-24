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
                summaryPoints = listOf(
                    "한국은행이 기준금리를 동결하며 시장 안정 기대가 커지고 있다.",
                    "소비와 투자 심리 회복 여부가 향후 핵심 변수로 작용한다.",
                    "가계 부담 완화 여부가 경기 회복 속도를 좌우할 전망이다."
                ),
                insight = "금리 흐름은 대출, 소비, 투자 심리에 직접적인 영향을 미친다. 따라서 이번 이슈는 단순 금융 뉴스가 아니라 개인의 소비 전략과 자산관리에도 연결해서 해석할 필요가 있다."
            ),
            HomeNewsCardItem(
                category = "IT",
                title = "생성형 AI 서비스 경쟁 본격화",
                notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
                summaryPoints = listOf(
                    "글로벌 IT 기업들이 생성형 AI 기능을 서비스에 빠르게 도입 중이다.",
                    "AI 기능이 플랫폼 경쟁력의 핵심 요소로 자리잡고 있다.",
                    "사용자 경험 개선과 생산성 향상이 주요 경쟁 포인트다."
                ),
                insight = "생성형 AI는 단순 기능 추가를 넘어 플랫폼 락인 전략과 직결된다. 앞으로는 어떤 서비스가 더 자연스럽게 AI를 녹여내느냐가 경쟁력을 결정할 가능성이 높다."
            ),
            HomeNewsCardItem(
                category = "사회",
                title = "청년 주거 지원 정책 체감도 격차 심화",
                notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
                summaryPoints = listOf(
                    "청년 주거 지원 정책은 확대되고 있지만 체감도는 낮은 편이다.",
                    "지역과 소득 조건에 따라 정책 접근성이 크게 차이난다.",
                    "실제 혜택보다 신청 과정의 복잡성이 문제로 지적된다."
                ),
                insight = "정책의 효과는 단순 공급이 아니라 접근성과 체감도에서 결정된다. 따라서 정책 내용을 볼 때는 혜택뿐 아니라 신청 조건과 절차까지 함께 고려해야 한다."
            ),
            HomeNewsCardItem(
                category = "과학",
                title = "민간 중심 우주 산업 투자 확대 지속",
                notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
                summaryPoints = listOf(
                    "민간 기업의 우주 산업 투자 규모가 지속적으로 증가하고 있다.",
                    "위성, 발사체, 데이터 산업까지 영역이 확대되는 추세다.",
                    "국가 주도에서 민간 중심 구조로 변화가 진행 중이다."
                ),
                insight = "우주 산업은 단순 기술 경쟁을 넘어 통신, 국방, 물류 등 다양한 산업과 연결된다. 장기적으로는 새로운 산업 생태계를 형성할 가능성이 크다."
            ),
            HomeNewsCardItem(
                category = "문화",
                title = "숏폼 중심 뉴스 소비 패턴 강화",
                notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
                summaryPoints = listOf(
                    "짧은 영상과 카드형 콘텐츠를 통한 뉴스 소비가 증가하고 있다.",
                    "사용자들은 빠르고 간결한 정보 전달을 선호하는 경향을 보인다.",
                    "기존 긴 기사 중심의 소비 방식은 점차 줄어드는 추세다."
                ),
                insight = "콘텐츠 형식이 바뀌면 정보 해석 방식도 함께 변화한다. 따라서 뉴스 소비에서는 단순 전달뿐 아니라 신뢰도와 맥락 유지가 중요한 요소로 작용한다."
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
                .fillMaxSize()
                .padding(top = topPadding), // 이 부분을 추가해야 상단바 아이콘과 겹치지 않습니다.
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(540.dp),
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
                        item = cardItems[page],
                        modifier = Modifier.fillMaxWidth(),
                        onShareClick = {},
                        onDetailClick = {}
                    )
                }

//                Spacer(modifier = Modifier.height(24.dp))

                // 페이지 인디케이터
                Row(
                    modifier = Modifier.padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(cardItems.size) { index ->
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
//                Spacer(modifier = Modifier.weight(0.1f))
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
            onNavigateToLogin = {}
        )
    }
}

@Preview(showBackground = true, name = "2. 홈 화면 단독 (로그인 상태)")
@Composable
fun HomeScreenLoggedInPreview() {
    BrifeTheme {
        HomeScreen(
            isLoggedIn = true,
            onLoginRequired = {},
            onLoginClick = {},
            topPadding = 60.dp // 상단바 높이만큼 가상 여백
        )
    }
}

@Preview(showBackground = true, name = "3. 홈 화면 단독 (비로그인 상태)")
@Composable
fun HomeScreenLoggedOutPreview() {
    BrifeTheme {
        HomeScreen(
            isLoggedIn = false,
            onLoginRequired = {},
            onLoginClick = {},
            topPadding = 60.dp
        )
    }
}