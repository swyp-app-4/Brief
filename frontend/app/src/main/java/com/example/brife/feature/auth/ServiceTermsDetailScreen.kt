package com.example.brife.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.AppTopBar2
import com.example.brife.ui.component.PrimaryButton
import com.example.brife.ui.theme.BorderDefault
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.Negative
import com.example.brife.ui.theme.TextBody
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
                // 각 조항의 독립적인 접기/펼치기 상태 (기본: 펼침)
                var expanded1 by remember { mutableStateOf(true) }
                var expanded2 by remember { mutableStateOf(true) }
                var expanded3 by remember { mutableStateOf(true) }
                var expanded4 by remember { mutableStateOf(true) }

                ServiceTermsClause(
                    title = "  제 1조(목적)",
                    content = "이 약관은 Brife (이하 '회사'라고 합니다)가 제공하는 제반 서비스의 이용과 관련하여 회사와 회원과의 권리, 의무 및 책임사항, 기타 필요한 사항을 규정함을 목적으로 합니다.",
                    expanded = expanded1,
                    onToggle = { expanded1 = !expanded1 }
                )

                Spacer(modifier = Modifier.height(40.dp))

                ServiceTermsClause(
                    title = "  제 2조(정의)",
                    content = "이 약관에서 사용하는 주요 용어의 정의는 다음과 같습니다.\n1. '서비스'라 함은 구현되는 단말기(PC, TV, 휴대형 단말기 등의 각종 유무선 장치를 포함)와 상관없이 '이용자' 가 이용할 수 있는 회사가 제공하는 제반 서비스를 의미합니다.\n2. '이용자'란 이 약관에 따라 회사가 제공하는 서비스를 받는 '개인회원' , '기업회원' 및 '비회원'을말합니다.\n3. '개인회원'은 회사에 개인정보를 제공하여 회원 등록을 한 사람으로, 회사로부터 지속적으로 정보를 제공받고' 회사' 가 제공하는 서비스를 계속 이용할 수 있는 자를 말합니다.\n4. '기업회원'은 회사에 기업 정보 및 개인정보를 제공하여 회원 등록을 한 사람으로, 회사로부터 지속적으로 정보를 제공받고 회사가 제공하는 서비스를 계속 이용할 수 있는 자를 말합니다.\n5. '비회원' 은 회원가입 없이 회사가 제공하는 서비스를 이용하는 자를 말합니다.\n6. '아이디(ID)'라 함은 회원의 식별과 서비스 이용을 위하여 회원이 정하고 회사가 승인하는 문자 또는 문자와 숫자의 조합을 의미합니다.\n7. '비밀번호'라 함은 회원이 부여받은 아이디와 일치되는 회원임을 확인하고 비밀의 보호를 위해 회원 자신이 정한 문자(특수문자 포함)와 숫자의 조합을 의미합니다.",
                    expanded = expanded2,
                    onToggle = { expanded2 = !expanded2 }
                )

                Spacer(modifier = Modifier.height(40.dp))

                ServiceTermsClause(
                    title = "  제 3조(정의)",
                    content = "이 약관에서 정하지 아니한 사항에 관해서는 법령 또는 회사 가정한 서비스의 개별약관, 운영 정책 및 규칙 등(이하 세부 지침)의 규정에 따릅니다또한 본 약관과 세부 지침이 충돌할 때는 세부 지침에 따릅니다.",
                    expanded = expanded3,
                    onToggle = { expanded3 = !expanded3 }
                )

                Spacer(modifier = Modifier.height(40.dp))

                ServiceTermsClause(
                    title = "  제 4조(약관의 효력과 변경)",
                    content = "1. 이 약관은 Brief(이)가 제공하는 모든 인터넷서비스에 게시하여 공시합니다. '회사'는 '전자상거래 등에서의 소비자 보호에 관한 법률(이하 '전자상거래법'이라 함)', '약관의 규제에 관한 법률(이하 '약관 규제법'이라 함)', '전자문서 및 전자거래 기본법(이하' 전자문서법'이라 함)', '\"전자금융거래법\"', '정보통신망 이용촉진 및 정보보호 등에 관한 법률(이하' 정보통신망법'이라 함)', '소비자기본법' 등관계법령(이하'관계 법령'이라 함)에 위배되지않는범위 내에서 이 약관을 변경할 수 있으며, 회사는 약관이 변경될 때에 변경된 약관의 내용과 시행일을 정하여, 그 시행일로부터 최소 7일(이용자에게 불리하거나 중대한 사항의 변경은 30일) 이전부터 시행일 후 상당한 기간 동안 공지하고, 기존 이용자에게는 변경된 약관, 적용일자 및 변경사유(변경될 내용 중 중요사항에 대한 설명을 포함)를 별도의 전자적 수단(전자우편, 문자메시지, 서비스 내 전자쪽지발송, 알림메시지를 띄우는 등의 방법)으로 개별 통지합니다. 변경된 약관은 공지하거나 통지한 시행일로부터 효력이 발생합니다.\n2. 회사가 제1 항에 따라 개정 약관을 공지 또는 통지하는 경우 변경에 동의하지 아니한 경우 공지일 또는 통지받은 날로부터 7일(이용자에게 불리하거나 중대한 사항의 변경인 경우에는 30일) 이내에 계약을 해지할 수 있으며, 계약 해지의 의사표시를 하지 아니한 경우에는 변경에 동의한 것으로 본다.'",
                    expanded = expanded4,
                    onToggle = { expanded4 = !expanded4 }
                )

                Spacer(modifier = Modifier.height(16.dp))
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
                        text = buildAnnotatedString {
                            append("서비스 이용약관에 동의합니다 ")

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

// 접기/펼치기 가능한 조항 단위 — 제목 row + 아이콘 + 박스 내용
@Composable
private fun ServiceTermsClause(
    title: String,
    content: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextSubtitle,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_setting_arrow_up),
                contentDescription = if (expanded) "접기" else "펼치기",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, BorderDefault), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                AppText(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextBody,
                    modifier = Modifier.fillMaxWidth()
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
