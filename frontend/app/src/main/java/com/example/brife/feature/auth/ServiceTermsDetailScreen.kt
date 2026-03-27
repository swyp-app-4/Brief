package com.example.brife.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.AppTopBar2
import com.example.brife.ui.component.PrimaryButton
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.Negative
import com.example.brife.ui.theme.TextSubtitle

@Composable
fun ServiceTermsDetailScreen(
    isChecked: Boolean,
    onCheckChange: (Boolean) -> Unit,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            AppTopBar2(
                title = "서비스 이용약관 필수 동의",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 스크롤 가능한 약관 본문 영역
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                AppText(
                    text = "서비스 이용약관\n\n" +
                            "서비스 제공자 및 이용자의 권리, 의무, 책임사항, " +
                            "서비스 이용조건 및 절차 등에 관한 기본적인 사항을 규정합니다.\n\n" +
                            "제1조 (목적)\n" +
                            "이 약관은 서비스 이용에 관한 조건과 절차에 관한 사항을 규정함을 목적으로 합니다.\n\n" +
                            "제2조 (정의)\n" +
                            "이 약관에서 사용하는 용어의 정의는 다음과 같습니다.\n\n" +
                            "제3조 (약관의 효력 및 변경)\n" +
                            "본 약관은 이용자가 동의한 날로부터 효력이 발생합니다.\n\n" +
                            "제4조 (서비스의 제공)\n" +
                            "회사는 이용자에게 아래와 같은 서비스를 제공합니다.\n\n" +
                            "제5조 (서비스 이용)\n" +
                            "이용자는 본 약관에 동의함으로써 서비스를 이용할 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 하단 고정 영역 (체크 row + 다음 버튼)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(color = Color(0xFFE9EDF2))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCheckChange(!isChecked) }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isChecked) R.drawable.ic_aftercheck else R.drawable.ic_beforecheck
                        ),
                        contentDescription = if (isChecked) "동의됨" else "동의 안됨",
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    AppText(
                        text = "서비스 이용약관에 동의합니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSubtitle,
                        modifier = Modifier.weight(1f)
                    )

                    AppText(
                        text = "(필수)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Negative
                    )
                }

                PrimaryButton(
                    text = "다음",
                    onClick = onNextClick,
                    enabled = isChecked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}

// ────────────────────────────────────────────
// Preview
// ────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "서비스 약관 상세 - 미동의")
@Composable
private fun ServiceTermsDetailScreenUncheckedPreview() {
    BrifeTheme {
        ServiceTermsDetailScreen(
            isChecked = false,
            onCheckChange = {},
            onNextClick = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "서비스 약관 상세 - 동의 완료")
@Composable
private fun ServiceTermsDetailScreenCheckedPreview() {
    BrifeTheme {
        ServiceTermsDetailScreen(
            isChecked = true,
            onCheckChange = {},
            onNextClick = {},
            onBackClick = {}
        )
    }
}
