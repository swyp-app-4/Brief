package com.example.brife.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.PrimaryButton
import com.example.brife.ui.theme.CtaActive
import com.example.brife.ui.theme.CtaDisabled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeToLoginBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onLoginClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null //손잡이 ui 숨김
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp, top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 텍스트 (가운데 정렬)
            AppText(
                text = "지금 로그인하고 \n나만의 맞춤 뉴스 받아보기!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 로그인 버튼 (CtaActive 색상)
            PrimaryButton(
                text = "로그인",
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            )

            // 더 둘러보기 버튼 (CtaDisabled 색상, 클릭 가능)
            // PrimaryButton의 기본 colors를 오버라이드하기 위해 별도 처리하거나 수정한 컴포넌트 사용
            Button(
                onClick = onDismissRequest, // 계속 홈 화면에 머무름 (시트 닫기)
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CtaDisabled, // 색상 지정
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun HomeToLoginBottomSheetPreview() {
    HomeToLoginBottomSheet(
        sheetState = rememberModalBottomSheetState(),
        onDismissRequest = {},
        onLoginClick = {}
    )
}