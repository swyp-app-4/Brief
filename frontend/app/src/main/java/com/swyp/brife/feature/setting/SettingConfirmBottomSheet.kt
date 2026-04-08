package com.swyp.brife.feature.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.component.PrimaryButton
import com.swyp.brife.ui.theme.CtaDisabled

// HomeToLoginBottomSheet와 동일한 구조 — 일러스트·텍스트·버튼 텍스트만 파라미터로 분리
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingConfirmBottomSheet(
    illustRes: Int,
    message: String,
    primaryText: String,        // 윗버튼 ("조금 더 써볼게요")
    actionText: String,         // 아랫버튼 ("로그아웃" / "탈퇴하기")
    onPrimaryClick: () -> Unit, // 윗버튼: 시트 닫고 현재 화면 유지
    onActionClick: () -> Unit,  // 아랫버튼: 동작 확정
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .padding(horizontal = 24.dp)
                    .padding(top = 0.dp, bottom = 32.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(120.dp))

                AppText(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(25.dp))

                PrimaryButton(
                    text = primaryText,
                    onClick = onPrimaryClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onActionClick,
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
                        text = actionText,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }

            // 일러스트 — 바텀시트 경계선에 걸쳐서 올라오도록 배치
            Image(
                painter = painterResource(id = illustRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(200.dp)
            )
        }
    }
}
