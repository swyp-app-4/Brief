package com.example.brife.feature.auth

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.brife.ui.component.PrimaryButton
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.theme.BgDefault
import com.example.brife.ui.theme.ComponentDefault
import com.example.brife.ui.theme.InterestSelected
import com.example.brife.ui.theme.PrimaryNormal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTermsScreen(
    isLoading: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit = {},
    onServiceDetailClick: () -> Unit = {},
    onPrivacyDetailClick: () -> Unit = {},
    onAgeDetailClick: () -> Unit = {},
) {
    var serviceAgree by remember { mutableStateOf(false) }
    var privacyAgree by remember { mutableStateOf(false) }
    var ageAgree by remember {mutableStateOf(false)}

    val allAgree = serviceAgree && privacyAgree && ageAgree

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgDefault
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDefault)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            AppText(
                text = "시작 전에 약관을\n확인해 주세요",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = if (isLoading) "처리 중..." else "다음",
                onClick = onNext,
                enabled = allAgree && !isLoading,
                modifier = Modifier.fillMaxWidth()
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
        Modifier.fillMaxWidth()
    }

    Row(
        modifier = containerModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
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
}




