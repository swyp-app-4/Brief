package com.swyp.brife.feature.setting

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.swyp.brife.R
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.component.AppTopBar2
import com.swyp.brife.ui.theme.BorderDefault
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.Positive
import com.swyp.brife.ui.theme.PrimaryNormal
import com.swyp.brife.ui.theme.TextBody
import com.swyp.brife.ui.theme.TextCaption
import com.swyp.brife.ui.theme.TextSubtitle
import com.swyp.brife.ui.theme.TextTitle


@Composable
fun SettingScreen(
    loginMethod: String,
    appVersion: String,
    onBackClick: () -> Unit,
    isLoggedIn: Boolean = true,
    isWithdrawing: Boolean = false,
    isWithdrawn: Boolean = false,
    withdrawErrorMessage: String? = null,
    onWithdrawErrorDismiss: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onAlarmSettingClick: () -> Unit = {},
    onWidgetSettingClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onInquiryClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showLogoutSheet by remember { mutableStateOf(false) }
    var showWithdrawSheet by remember { mutableStateOf(false) }
    var showWithdrawConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isWithdrawn) {
        if (isWithdrawn) {
            Toast.makeText(context, "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
            onWithdrawErrorDismiss()
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            AppTopBar2(
                title = "설정",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 32.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingSection(title = "앱 설정") {
                SettingNavItem(label = "알림 설정", onClick = onAlarmSettingClick)
                SettingNavItem(label = "위젯 설정", onClick = onWidgetSettingClick)
//                SettingNavItem(label = "테마", 라디오버튼)
            }

            SettingSection(title = "정보") {
                SettingNavItem(label = "서비스 이용약관", onClick = onTermsClick)
                SettingNavItem(label = "개인정보 처리방침", onClick = onPrivacyClick)
                SettingInfoItem(label = "현재 버전", trailingText = appVersion)
            }

            SettingSection(title = "지원") {
                SettingNavItem(label = "1:1 문의", onClick = onInquiryClick)
            }

            SettingSection(title = "계정") {
                if (isLoggedIn) {
                    SettingInfoItem(label = "로그인 방식", trailingText = loginMethod)
                    SettingTextItem(
                        label = "로그아웃",
                        onClick = { showLogoutSheet = true },
                        color = TextSubtitle
                    )
                    SettingTextItem(
                        label = if (isWithdrawing) "탈퇴 처리 중..." else "회원탈퇴",
                        onClick = {
                            if (!isWithdrawing) {
                                showWithdrawConfirmDialog = true
                            }
                        },
                        color = TextCaption
                    )
                } else {
                    SettingTextItem(
                        label = "로그인",
                        onClick = onLoginClick,
                        color = Positive
                    )
                }
            }
        }

        if (showLogoutSheet) {
            SettingConfirmBottomSheet(
                illustRes = R.drawable.img_logout_character,
                message = "로그아웃 하시겠어요?",
                primaryText = "조금 더 둘러볼게요",
                actionText = "로그아웃",
                onPrimaryClick = { showLogoutSheet = false },
                onActionClick = {
                    showLogoutSheet = false
                    onLogoutClick()
                },
                onDismissRequest = { showLogoutSheet = false }
            )
        }

        if (showWithdrawConfirmDialog) {
            WithdrawConfirmDialog(
                onDismissRequest = { showWithdrawConfirmDialog = false },
                onCancelClick = { showWithdrawConfirmDialog = false },
                onConfirmClick = {
                    showWithdrawConfirmDialog = false
                    showWithdrawSheet = true
                }
            )
        }

        if (showWithdrawSheet) {
            SettingConfirmBottomSheet(
                illustRes = R.drawable.img_deleteuser,
                message = "계정을 삭제하시겠어요?",
                primaryText = "조금 더 둘러볼게요",
                actionText = "탈퇴하기",
                onPrimaryClick = { showWithdrawSheet = false },
                onActionClick = {
                    showWithdrawSheet = false
                    onWithdrawClick()
                },
                onDismissRequest = { showWithdrawSheet = false }
            )
        }

        if (withdrawErrorMessage != null) {
            AlertDialog(
                onDismissRequest = onWithdrawErrorDismiss,
                title = {
                    AppText(
                        text = "회원탈퇴 실패",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextTitle
                    )
                },
                text = {
                    AppText(
                        text = withdrawErrorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSubtitle
                    )
                },
                confirmButton = {
                    TextButton(onClick = onWithdrawErrorDismiss) {
                        AppText(
                            text = "확인",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryNormal
                        )
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
private fun WithdrawConfirmDialog(
    onDismissRequest: () -> Unit,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            AppText(
                text = "정말로 탈퇴하시겠습니까?",
                style = MaterialTheme.typography.titleSmall,
                color = TextTitle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            AppText(
                text = "탈퇴를 진행하려면 한 번 더 확인이 필요합니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSubtitle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                AppText(
                    text = "확인",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextCaption
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelClick) {
                AppText(
                    text = "취소",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryNormal
                )
            }
        }
    )
}

@Composable
private fun SettingSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderDefault),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {
            AppText(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = TextBody,
                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun SettingNavItem(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSubtitle
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_next),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingInfoItem(label: String, trailingText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSubtitle
        )
        AppText(
            text = trailingText,
            style = MaterialTheme.typography.bodyMedium,
            color = TextCaption
        )
    }
}

@Composable
private fun SettingTextItem(label: String, onClick: () -> Unit, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}


