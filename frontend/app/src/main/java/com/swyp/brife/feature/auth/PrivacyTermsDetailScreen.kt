package com.swyp.brife.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.brife.R
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.component.AppTopBar2
import com.swyp.brife.ui.component.PrimaryButton
import com.swyp.brife.ui.theme.BorderDefault
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.Negative
import com.swyp.brife.ui.theme.TextBody
import com.swyp.brife.ui.theme.TextSubtitle

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
                // ── 최상단 제목 (박스 밖) ──
                AppText(
                    text = "개인정보 처리 동의서",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── 첫 번째 박스 ──
                PrivacyTermsBox(
                    text = "Brife(이하 '회사'라고 합니다)은(는) 개인정보보호법 등 관련 법령상의 개인정보 보호 규정을 준수하며 귀하의 개인정보 보호에 최선을 다하고 있습니다. 회사는 개인정보 보호법에 근거하여 다음과 같은 내용으로 개인정보를 수집 및 처리하려고 합니다.\n다음의 내용을 자세히 읽어보시고 모든 내용을 이해하신 후에 동의 여부를 결정해주시기 바랍니다."
                )

                Spacer(modifier = Modifier.height(30.dp))

                // ── 제 1조 ──
                AppText(
                    text = "  제 1조 (개인정보 수집 및 이용 목적)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSubtitle,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyTermsBox(
                    text = "이용자가 제공한 모든 정보는 다음의 목적을 위해 활용하며, 목적 이외의 용도로는 사용되지 않습니다.\n • 본인 확인"
                )

                Spacer(modifier = Modifier.height(30.dp))

                // ── 제 2조 ──
                AppText(
                    text = "  제 2조 (개인정보 수집 및 이용 항목)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSubtitle,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyTermsBox(
                    text = "회사는 개인정보 수집 목적을 위하여 다음과 같은 정보를 수집합니다.\n • 이메일"
                )

                Spacer(modifier = Modifier.height(30.dp))

                // ── 제 3조 ──
                AppText(
                    text = "  제 3조 (개인정보 보유 및 이용기간)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSubtitle,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyTermsBox(
                    text = "1. 수집한 개인정보는 수집•이용 동의일로부터 개인정보 수집•이용 목적을 달성할 때까지 보관 및 이용합니다.\n2. 개인 정보 보유기간의 경과, 처리목적의 달성 등 개인정보가 불필요하게 되었을 때에는 지체없이 해당 개인정보를 파기합니다."
                )

                Spacer(modifier = Modifier.height(30.dp))

                // ── 제 4조 ──
                AppText(
                    text = "제 4조 (동의 거부 관리)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextSubtitle,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyTermsBox(
                    text = "귀하는 본 안내에 따른 개인정보 수집•이용에 대하여 동의를 거부하실 수 있으며, 이에 따른 불이익은 없습니다."
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
                        text = buildAnnotatedString {
                            append("개인정보 수집 및 이용에 동의합니다 ")

                            withStyle(
                                style = SpanStyle(color = Negative)
                            ) {
                                append("(필수)")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSubtitle,
                        modifier = Modifier.weight(1f)
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

// 반복되는 border 박스 — 본문 설명 텍스트를 감싸는 컨테이너
@Composable
private fun PrivacyTermsBox(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(1.dp, BorderDefault),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        AppText(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextBody,
            modifier = Modifier.fillMaxWidth()
        )
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
