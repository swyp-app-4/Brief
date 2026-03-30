package com.example.brife.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.PrimaryButton
import com.example.brife.ui.theme.BgBlueDark
import com.example.brife.ui.theme.BgBlueLight
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.example.brife.R
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.alpha

data class OnboardingGuidePage(
    val title: String,
    val description: String,
    val imageRes: Int
)

@Composable
fun OnboardingGuideScreen(
    modifier: Modifier = Modifier,
    onNextClick: () -> Unit = {}
) {
    val pages = listOf(
        OnboardingGuidePage(
            title = "바쁜 아침,\n1분이면 충분해요",
            description = "오늘 꼭 알아야 할 뉴스만\n3줄로 정리해드려요",
            imageRes = R.drawable.illust1
        ),
        OnboardingGuidePage(
            title = "원하는 소식만\n딱 모아드려요",
            description = "내가 궁금한 분야 소식만\n알아서 모아드릴게요.",
            imageRes = R.drawable.illust2
        ),
        OnboardingGuidePage(
            title = "바쁜 날도\n뉴스는 빠짐없이",
            description = "앱을 들어가지 않아도\n홈 화면에서 바로 확인해요.",
            imageRes = R.drawable.illust3
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BgBlueLight, BgBlueDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val item = pages[page]

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppText(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    AppText(
                        text = item.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // 일러스트 자리
                    Image(
                        painter = painterResource(id = item.imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(280.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 페이지 인디케이터
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(pages.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            val size by animateDpAsState(
                                targetValue = if (isSelected) 10.dp else 8.dp,
                                label = ""
                            )

                            val alpha by animateFloatAsState(
                                targetValue = if (isSelected) 1f else 0.3f,
                                label = ""
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(size)
                                    .alpha(alpha)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                }
            }



            PrimaryButton(
                text = if (pagerState.currentPage == pages.lastIndex) "다음" else "다음",
                onClick = {
                    if (pagerState.currentPage == pages.lastIndex) {
                        onNextClick()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingGuideScreenPreview() {
    MaterialTheme {
        OnboardingGuideScreen()
    }
}