package com.example.brife.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.PrimaryButton
import com.example.brife.ui.theme.CtaDisabled
import androidx.compose.foundation.background
import com.example.brife.ui.theme.BrifeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeToLoginBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onLoginClick: () -> Unit,
    onBrowseClick: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
//                .heightIn(min = 520.dp)
        ) {


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp) // 일러스트 크기(160dp)의 절반만큼 아래로 내려서 배치
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .padding(horizontal = 24.dp)
                    .padding(top = 0.dp, bottom = 32.dp)
                    .navigationBarsPadding(), // 하단 시스템바 안전 여백
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ✅ 핵심: 위 공간만 추가 (기존 레이아웃 유지)
                Spacer(modifier = Modifier.height(120.dp))

                // 텍스트 (위치 그대로 유지됨)
                AppText(
                    text = "지금 로그인하고 \n나만의 맞춤 뉴스 받아보기!",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(25.dp))

                PrimaryButton(
                    text = "로그인",
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onBrowseClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CtaDisabled,
                        contentColor = Color.White
                    )
                ) {
                    AppText(
                        text = "더 둘러보기",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
            // 상단 일러스트 (바텀시트 경계선에 걸쳐서 튀어나오게 배치)
            Image(
                painter = painterResource(id = R.drawable.illust4_login),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(200.dp) // 전체 모습이 잘 보이도록 크기 설정
            )
        }
    }
}

