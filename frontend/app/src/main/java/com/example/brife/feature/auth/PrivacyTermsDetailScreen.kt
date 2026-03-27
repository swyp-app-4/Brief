package com.example.brife.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun PrivacyTermsDetailScreen(
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
                title = "개인정보 처리 동의서",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 스크롤 가능한 본문 영역 (안내 텍스트 + 체크 row 포함)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                AppText(
                    text = "개인정보 처리방침\n\n" +
                            "수집하는 개인정보의 항목 및 수집 방법에 대해 안내드립니다.\n\n" +
                            "제1조 (수집하는 개인정보 항목)\n" +
                            "회사는 서비스 제공을 위해 필요한 최소한의 개인정보를 수집합니다.\n\n" +
                            "제2조 (개인정보 수집 및 이용 목적)\n" +
                            "수집한 개인정보는 다음의 목적을 위해 활용합니다.\n\n" +
                            "제3조 (개인정보의 보유 및 이용 기간)\n" +
                            "이용자의 개인정보는 원칙적으로 개인정보의 수집 및 이용 목적이 달성되면 지체 없이 파기합니다.\n\n" +
                            "제4조 (개인정보의 파기 절차 및 방법)\n" +
                            "이용자의 개인정보는 목적이 달성된 후 별도의 DB로 옮겨져 일정 기간 저장된 후 파기됩니다.\n\n" +
                            "제5조 (이용자 권리)\n" +
                            "이용자는 언제든지 자신의 개인정보를 조회하거나 수정할 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 안내 텍스트 — 스크롤 영역 하단
                AppText(
                    text = "본인은 위의 동의서 내용을 충분히 숙지하였으며,\n위와 같이 개인정보를 수집·이용하는데 동의합니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSubtitle,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 체크 row — 스크롤 영역 내부
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCheckChange(!isChecked) }
                        .padding(vertical = 8.dp),
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
                        text = "개인정보 수집 및 이용에 동의합니다",
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

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 하단 고정 영역 (구분선 + 다음 버튼만)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(color = Color(0xFFE9EDF2))

                PrimaryButton(
                    text = "다음",
                    onClick = onNextClick,
                    enabled = isChecked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    }
}

// ────────────────────────────────────────────
// Preview
// ────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "개인정보 처리 동의서 - 미동의")
@Composable
private fun PrivacyTermsDetailScreenUncheckedPreview() {
    BrifeTheme {
        PrivacyTermsDetailScreen(
            isChecked = false,
            onCheckChange = {},
            onNextClick = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "개인정보 처리 동의서 - 동의 완료")
@Composable
private fun PrivacyTermsDetailScreenCheckedPreview() {
    BrifeTheme {
        PrivacyTermsDetailScreen(
            isChecked = true,
            onCheckChange = {},
            onNextClick = {},
            onBackClick = {}
        )
    }
}
