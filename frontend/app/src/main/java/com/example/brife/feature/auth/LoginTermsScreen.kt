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
import androidx.compose.foundation.shape.RoundedCornerShape
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
    var serviceAgree by remember { mutableStateOf(initialServiceAgree) }
    var privacyAgree by remember { mutableStateOf(initialPrivacyAgree) }
    var ageAgree by remember { mutableStateOf(initialAgeAgree) }

    val allAgree = serviceAgree && privacyAgree && ageAgree

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
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(80.dp))

                AppText(
                    text = "시작 전에 약관을\n확인해 주세요",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(40.dp))

                TermsRow(
                    text = "전체 동의",
                    checked = allAgree,
                    highlightBox = true,
                    onCheckedChange = { checked ->
                        serviceAgree = checked
                        privacyAgree = checked
                        ageAgree = checked
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                TermsRow(
                    text = "서비스 이용약관 필수 동의",
                    checked = serviceAgree,
                    onCheckedChange = { serviceAgree = it },
                    onDetailClick = onServiceDetailClick
                )

                Spacer(modifier = Modifier.height(18.dp))

                TermsRow(
                    text = "개인정보 처리방침 필수 동의",
                    checked = privacyAgree,
                    onCheckedChange = { privacyAgree = it },
                    onDetailClick = onPrivacyDetailClick
                )

                Spacer(modifier = Modifier.height(18.dp))

                TermsRow(
                    text = "만 14세 이상 확인",
                    checked = ageAgree,
                    onCheckedChange = { ageAgree = it },
                    onDetailClick = onAgeDetailClick
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
    highlightBox: Boolean = false
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

        Image(
            painter = painterResource(id = R.drawable.ic_next),
            contentDescription = "상세 약관 보기",
            modifier = Modifier
                .size(20.dp)
                .clickable { onDetailClick() }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginTermsScreenPreview() {
    LoginTermsScreen(
        isLoading = false,
        onNext = {},
        onBack = {},
        onServiceDetailClick = {},
        onPrivacyDetailClick = {},
        onAgeDetailClick = {}
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginTermsScreenLoadingPreview() {
    LoginTermsScreen(
        isLoading = true,
        onNext = {},
        onBack = {},
        onServiceDetailClick = {},
        onPrivacyDetailClick = {},
        onAgeDetailClick = {},
        initialServiceAgree = true,
        initialPrivacyAgree = true,
        initialAgeAgree = true
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginTermsScreenAllCheckedPreview() {
    LoginTermsScreen(
        isLoading = false,
        onNext = {},
        onBack = {},
        onServiceDetailClick = {},
        onPrivacyDetailClick = {},
        onAgeDetailClick = {},
        initialServiceAgree = true,
        initialPrivacyAgree = true,
        initialAgeAgree = true
    )
}