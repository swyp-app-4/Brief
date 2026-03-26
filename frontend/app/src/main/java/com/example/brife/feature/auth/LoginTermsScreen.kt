package com.example.brife.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.PrimaryButton
import com.example.brife.ui.theme.BgDefault
import com.example.brife.ui.theme.ComponentDefault
import com.example.brife.ui.theme.InterestSelected
import com.example.brife.ui.theme.PrimaryNormal

@Composable
fun LoginTermsScreen(
    isLoading: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit = {},
    onServiceDetailClick: () -> Unit = {},
    onPrivacyDetailClick: () -> Unit = {},
    onAgeDetailClick: () -> Unit = {},
    initialServiceAgree: Boolean = false,
    initialPrivacyAgree: Boolean = false,
    initialAgeAgree: Boolean = false,
) {
    // 각 약관은 상세 페이지에서 동의 체크 + "다음" 버튼을 눌러야 confirmed 됨
    var serviceDetailConfirmed by remember { mutableStateOf(initialServiceAgree) }
    var privacyDetailConfirmed by remember { mutableStateOf(initialPrivacyAgree) }

    var showServiceDetail by remember { mutableStateOf(false) }
    var showPrivacyDetail by remember { mutableStateOf(false) }

    // 두 상세 약관 동의를 모두 완료해야 "다음" 버튼 활성화
    val allAgree = serviceDetailConfirmed && privacyDetailConfirmed

    when {
        showServiceDetail -> TermsDetailPage(
            title = "서비스 이용약관",
            onConfirm = {
                serviceDetailConfirmed = true
                showServiceDetail = false
            },
            onBack = { showServiceDetail = false }
        )

        showPrivacyDetail -> TermsDetailPage(
            title = "개인정보 처리방침",
            onConfirm = {
                privacyDetailConfirmed = true
                showPrivacyDetail = false
            },
            onBack = { showPrivacyDetail = false }
        )

        else -> Surface(
            modifier = Modifier.fillMaxSize(),
            color = BgDefault
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgDefault)
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(80.dp))

                    AppText(
                        text = "시작 전에 약관을\n확인해 주세요",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // 전체 동의: 두 약관이 모두 상세 확인 완료된 경우에만 체크됨 (직접 토글 불가)
                    TermsRow(
                        text = "전체 동의",
                        checked = allAgree,
                        highlightBox = true,
                        showArrow = false,
                        onCheckedChange = { /* 상세 페이지 완료 시 자동 반영, 직접 토글 불가 */ }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 서비스 이용약관: 클릭하면 상세 페이지로 이동
                    TermsRow(
                        text = "서비스 이용약관 필수 동의",
                        checked = serviceDetailConfirmed,
                        onCheckedChange = { showServiceDetail = true },
                        onDetailClick = { showServiceDetail = true }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 개인정보 처리방침: 클릭하면 상세 페이지로 이동
                    TermsRow(
                        text = "개인정보 처리방침 필수 동의",
                        checked = privacyDetailConfirmed,
                        onCheckedChange = { showPrivacyDetail = true },
                        onDetailClick = { showPrivacyDetail = true }
                    )

                    // 만 14세 이상 확인 항목은 hidden 처리

                    Spacer(modifier = Modifier.weight(1f))
                }

                PrimaryButton(
                    text = if (isLoading) "처리 중..." else "다음",
                    onClick = onNext,
                    enabled = allAgree && !isLoading,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    }
}

// 서비스 이용약관 / 개인정보 처리방침 상세 페이지
// 동의 체크박스와 "다음" 버튼을 모두 눌러야 onConfirm 호출
@Composable
private fun TermsDetailPage(
    title: String,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    var agreed by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgDefault
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDefault)
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                // 뒤로 가기 버튼
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "뒤로",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppText(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 약관 내용 (API 연결 전 placeholder)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    AppText(
                        text = "$title 내용이 여기에 표시됩니다.\n\n" +
                                "서비스 제공자 및 이용자의 권리, 의무, 책임사항, " +
                                "서비스 이용조건 및 절차 등에 관한 기본적인 사항을 규정합니다.\n\n" +
                                "본 약관에 동의하시면 서비스를 이용하실 수 있습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 동의 체크 행 — 여기서 체크해야 "다음" 버튼 활성화
                TermsRow(
                    text = "(필수) $title 동의",
                    checked = agreed,
                    showArrow = false,
                    onCheckedChange = { agreed = it }
                )

                Spacer(modifier = Modifier.height(80.dp)) // PrimaryButton 공간 확보
            }

            // "다음" 버튼: 동의 체크 시에만 활성화
            PrimaryButton(
                text = "다음",
                onClick = onConfirm,
                enabled = agreed,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }
}

@Composable
fun TermsRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onDetailClick: () -> Unit = {},
    highlightBox: Boolean = false,
    showArrow: Boolean = true
) {
    val rowShape = RoundedCornerShape(20.dp)

    val backgroundColor = when {
        highlightBox && checked -> InterestSelected
        highlightBox && !checked -> ComponentDefault
        else -> Color.Transparent
    }

    val borderModifier = if (highlightBox && checked) {
        Modifier.border(
            width = 2.dp,
            color = PrimaryNormal,
            shape = rowShape
        )
    } else {
        Modifier
    }

    val containerModifier = if (highlightBox) {
        Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(backgroundColor)
            .then(borderModifier)
    } else {
        Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(Color.Transparent)
    }

    Row(
        modifier = containerModifier
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(
                id = if (checked) R.drawable.ic_aftercheck else R.drawable.ic_beforecheck
            ),
            contentDescription = if (checked) "선택됨" else "선택 안됨",
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        AppText(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        if (showArrow) {
            Image(
                painter = painterResource(id = R.drawable.ic_next),
                contentDescription = "상세 약관 보기",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onDetailClick() }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginTermsScreenPreview() {
    LoginTermsScreen(
        isLoading = false,
        onNext = {},
        onBack = {}
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginTermsScreenAllCheckedPreview() {
    LoginTermsScreen(
        isLoading = false,
        onNext = {},
        onBack = {},
        initialServiceAgree = true,
        initialPrivacyAgree = true
    )
}
