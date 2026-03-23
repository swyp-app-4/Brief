package com.example.brife.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.PrimaryButton
import com.example.brife.ui.theme.BgSub


data class OnboardingGuidePage(
    val title: String,
    val description: String,
    val imageRes: Int,
    val characterRes: Int
)

private val IndicatorInactive = Color(0xFFD9D9D9)

@Composable
fun OnboardingGuideScreen(
    modifier: Modifier = Modifier,
    onNextClick: () -> Unit = {}
) {
    val pages = listOf(
        OnboardingGuidePage(
            title = "바쁜 아침에도\n뉴스는 가볍게",
            description = "내가 고른 관심사를\n매일 아침 뉴스카드로 보여드려요.",
            imageRes = R.drawable.ill_onboarding_screen_01,
            characterRes = R.drawable.img_onboarding_character_01
        ),
        OnboardingGuidePage(
            title = "단락별 요약으로\n핵심만 빠르게",
            description = "긴 기사도 부담 없이\n핵심만 빠르게 볼 수 있어요.",
            imageRes = R.drawable.ill_onboarding_screen_02,
            characterRes = R.drawable.img_onboarding_character_02
        ),
        OnboardingGuidePage(
            title = "홈에서 뉴스를\n바로 확인",
            description = "궁금한 뉴스만 위젯으로\n빠르게 확인할 수 있어요.",
            imageRes = R.drawable.ill_onboarding_screen_03,
            characterRes = R.drawable.img_onboarding_character_03
        )
    )

    var currentPage by remember { mutableStateOf(0) }
    val item = pages[currentPage]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 Gray
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(0.70f)
                .background(BgSub)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                when (currentPage) {
                    0 -> {
                        // 01: 캐릭터가 메인 이미지 왼쪽 뒤, 약간 회전, 아래로 내려서 흰 영역에 살짝 가려짐
                        Image(
                            painter = painterResource(id = item.characterRes),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth(0.28f)
                                .offset(x = 24.dp, y = 72.dp)
                                .rotate(-45f)
                                .zIndex(0f),
                            contentScale = ContentScale.Fit
                        )

                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.9f)
                                .offset(y = 42.dp)
                                .zIndex(1f),
                            contentScale = ContentScale.Fit
                        )
                    }

                    1 -> {
                        // 02: 캐릭터가 메인 이미지 오른쪽 뒤, 아래로 내려서 흰 영역에 조금 가려짐
                        Image(
                            painter = painterResource(id = item.characterRes),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .fillMaxWidth(0.3f)
                                .offset(x = (-8).dp, y = 68.dp)
                                .zIndex(0f),
                            contentScale = ContentScale.Fit
                        )

                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.9f)
                                .offset(y = 42.dp)
                                .zIndex(1f),
                            contentScale = ContentScale.Fit
                        )
                    }

                    2 -> {
                        // 03: 캐릭터가 가운데 앞쪽, 메인 이미지보다 앞에 오도록
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.9f)
                                .offset(y = 42.dp)
                                .zIndex(0f),
                            contentScale = ContentScale.Fit
                        )

                        Image(
                            painter = painterResource(id = item.characterRes),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.26f)
                                .offset(y = 78.dp)
                                .zIndex(2f),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // 하단 White 카드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    AppText(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AppText(
                        text = item.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF767676),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 인디케이터
                    Row {
                        repeat(pages.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == currentPage)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            IndicatorInactive
                                    )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // 버튼
                    PrimaryButton(
                        text = if (currentPage == pages.lastIndex) "시작하기" else "다음",
                        onClick = {
                            if (currentPage == pages.lastIndex) {
                                onNextClick()
                            } else {
                                currentPage++
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
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