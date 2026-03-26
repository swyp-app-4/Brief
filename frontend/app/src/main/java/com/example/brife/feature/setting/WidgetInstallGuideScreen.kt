package com.example.brife.feature.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.PrimaryButton
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.TextBody
import com.example.brife.ui.theme.TextCaption
import com.example.brife.ui.theme.TextTitle

@Composable
fun WidgetInstallGuideScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var showInstallSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 커스텀 TopBar (SettingScreen과 동일한 구조)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .background(Color.White)
                .padding(horizontal = 4.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = Color.Unspecified
                )
            }
            AppText(
                text = "위젯 설정",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextTitle
            )
        }

        // 본문: 일러스트 + 설명 텍스트
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 110.dp)          // TopBar 높이만큼 내려오기
                .padding(bottom = 80.dp),      // 하단 버튼 영역 확보
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 일러스트 + 하단 흰색 페이드 오버레이
            // 일러스트 영역: weight(1f)를 제거하고 고정 높이를 주거나 비율을 조정합니다.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(424.dp) // 1. 높이를 원하는 크기로 지정 (예: 280dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ill_setting_screen),
                    contentDescription = null,
                    // 2. ContentScale을 Fit으로 변경하면 이미지가 잘리지 않고 박스 안에 들어옵니다.
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 40.dp) // 3. 좌우 패딩을 주어 이미지 크기를 더 줄임
                )

                // 하단 흰색 그라데이션 페이드 (이미지 크기에 맞춰 높이 조절 가능)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp) // 1. 높이를 조금 더 키워 범위를 넓힘
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                // 2. 컬러 스탑을 활용해 투명 구간은 짧게, 흰색 구간은 길게 설정
                                0.0f to Color.Transparent,
                                0.3f to Color.White.copy(alpha = 0.5f), // 중간 지점부터 이미 흰색이 섞임
                                0.6f to Color.White.copy(alpha = 0.95f), // 더 일찍 진해짐
                                1.0f to Color.White                     // 바닥은 완전히 흰색
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.height(22.dp))

            AppText(
                text = "위젯을 설치하여\n홈 화면에서 뉴스를 확인 하세요",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextTitle,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(33.dp))

            AppText(
                text = "위젯을 설치하고 홈 화면에서 빠르게 뉴스를 확인하세요",
                style = MaterialTheme.typography.bodySmall,
                color = TextCaption,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // 하단 고정 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp, top = 12.dp)
        ) {
            PrimaryButton(
                text = "위젯 설치하러 가기",
                onClick = { showInstallSheet = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showInstallSheet) {
        WidgetInstallBottomSheet(
            onDismissRequest = { showInstallSheet = false },
            onCancelClick = { showInstallSheet = false },
            onAddClick = {
                showInstallSheet = false
                requestPinWidget(context)
            }
        )
    }
}

// ────────────────────────────────────────────
// Preview
// ────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "1. 위젯 설치 가이드 화면")
@Composable
private fun WidgetInstallGuideScreenPreview() {
    BrifeTheme {
        WidgetInstallGuideScreen(
            onBackClick = {}
        )
    }
}
