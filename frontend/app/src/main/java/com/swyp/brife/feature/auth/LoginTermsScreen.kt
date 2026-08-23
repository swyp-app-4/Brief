package com.swyp.brife.feature.auth

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.brife.R
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.component.PrimaryButton
import com.swyp.brife.ui.theme.BgDefault
import com.swyp.brife.ui.theme.ComponentDefault
import com.swyp.brife.ui.theme.DarkBackground
import com.swyp.brife.ui.theme.DarkBlue300
import com.swyp.brife.ui.theme.DarkBlue700
import com.swyp.brife.ui.theme.DarkComponentDefault
import com.swyp.brife.ui.theme.InterestSelectedLight
import com.swyp.brife.ui.theme.PrimaryNormal

@Composable
fun LoginTermsScreen(
    isLoading: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit = {},
    onServiceDetailClick: () -> Unit = {},
    onPrivacyDetailClick: () -> Unit = {},
    onAllAgreeClick: () -> Unit = {},
    initialServiceAgree: Boolean = false,
    initialPrivacyAgree: Boolean = false,
) {
    val allAgree = initialServiceAgree && initialPrivacyAgree
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground
    val screenBackground = if (isDarkTheme) DarkBackground else BgDefault

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = screenBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackground)
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

                // 전체 동의: 두 약관 모두 확인 완료 시 자동 체크
                // 미완료 상태에서 클릭 시 순차 플로우 시작
                TermsRow(
                    text = "전체 동의",
                    checked = allAgree,
                    highlightBox = true,
                    showArrow = false,
                    onCheckedChange = { if (!allAgree) onAllAgreeClick() }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 서비스 이용약관: 클릭하면 ServiceTermsDetailScreen으로 이동
                TermsRow(
                    text = "서비스 이용약관 필수 동의",
                    checked = initialServiceAgree,
                    onCheckedChange = { onServiceDetailClick() },
                    onDetailClick = { onServiceDetailClick() }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 개인정보 처리방침: 클릭하면 PrivacyTermsDetailScreen으로 이동
                TermsRow(
                    text = "개인정보 처리방침 필수 동의",
                    checked = initialPrivacyAgree,
                    onCheckedChange = { onPrivacyDetailClick() },
                    onDetailClick = { onPrivacyDetailClick() }
                )

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


@Composable
fun TermsRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onDetailClick: () -> Unit = {},
    highlightBox: Boolean = false,
    showArrow: Boolean = true
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground
    val rowShape = RoundedCornerShape(20.dp)

    val backgroundColor = when {
        isDarkTheme && highlightBox && checked -> DarkBlue300
        isDarkTheme && highlightBox -> DarkComponentDefault
        highlightBox && checked -> InterestSelectedLight
        highlightBox && !checked -> ComponentDefault
        else -> Color.Transparent
    }

    val borderModifier = if (highlightBox && checked) {
        Modifier.border(
            width = 2.dp,
            color = if (isDarkTheme) DarkBlue700 else PrimaryNormal,
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
                id = when {
                    checked -> R.drawable.ic_aftercheck
                    isDarkTheme -> R.drawable.check_disabled_darkmode
                    else -> R.drawable.ic_beforecheck
                }
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

@Preview(showBackground = true, showSystemUi = true, name = "LoginTermsScreen - 전체 동의 완료")
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
