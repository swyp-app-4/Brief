package com.example.brife.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.brife.ui.component.PrimaryButton
import androidx.compose.ui.tooling.preview.Preview
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.R
import com.example.brife.ui.component.AppText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTermsBottomSheet(
    onDismiss: () -> Unit,
    onNext: () -> Unit
) {
    var serviceAgree by remember { mutableStateOf(false) }
    var privacyAgree by remember { mutableStateOf(false) }

    val allAgree = serviceAgree && privacyAgree
    val isEnabled = allAgree

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            AppText(
                text = "시작 전에 약관을\n확인해 주세요",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            TermsRow(
                text = "전체 동의",
                checked = allAgree,
                onCheckedChange = { checked ->
                    serviceAgree = checked
                    privacyAgree = checked
                }
            )

            TermsRow(
                text = "서비스 이용약관 필수 동의",
                checked = serviceAgree,
                onCheckedChange = { checked ->
                    serviceAgree = checked
                }
            )

            TermsRow(
                text = "개인정보 처리 방침 필수 동의",
                checked = privacyAgree,
                onCheckedChange = { checked ->
                    privacyAgree = checked
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "다음",
                enabled = isEnabled,
                onClick = onNext
            )
        }
    }
}

@Composable
fun TermsRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onDetailClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(
                id = if (checked) R.drawable.ic_aftercheck else R.drawable.ic_beforecheck
            ),
            contentDescription = if (checked) "선택됨" else "선택 안됨",
            modifier = Modifier
                .size(24.dp)
                .clickable { onCheckedChange(!checked) }
        )

        Spacer(modifier = Modifier.width(12.dp))

        AppText(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
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
fun LoginTermsBottomSheetPreview() {
    BrifeTheme {
        LoginTermsBottomSheetPreviewContainer()
    }
}

@Composable
private fun LoginTermsBottomSheetPreviewContainer() {

    var showSheet by remember { mutableStateOf(true) }

    if (showSheet) {
        LoginTermsBottomSheet(
            onDismiss = { showSheet = false },
            onNext = { showSheet = false }
        )
    }
}