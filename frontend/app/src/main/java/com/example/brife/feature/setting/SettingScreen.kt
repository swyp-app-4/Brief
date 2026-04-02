package com.example.brife.feature.setting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.AppTopBar2
import com.example.brife.ui.theme.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

data class SettingUiState(
    val loginMethod: String = "Google",  // "Google" | "Naver" | "Kakao"
    val appVersion: String = "1.0.0"
)

@Composable
fun SettingScreen(
    uiState: SettingUiState,
    onBackClick: () -> Unit,
    isLoggedIn: Boolean = true,
    isWithdrawing: Boolean = false,
    isWithdrawn: Boolean = false,
    withdrawErrorMessage: String? = null,
    onWithdrawErrorDismiss: () -> Unit = {},
    onLoginClick: () -> Unit = {},
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

    // 회원탈퇴 완료 토스트 출력 및 상태 리셋
    LaunchedEffect(isWithdrawn) {
        if (isWithdrawn) {
            Toast.makeText(context, "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
            onWithdrawErrorDismiss() // 전역 상태(isWithdrawn)를 false로 리셋하여 중복 출력 방지
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
            // 앱 설정
            SettingSection(title = "앱 설정") {
                SettingNavItem(label = "위젯 설정", onClick = onWidgetSettingClick)
            }

            // 정보
            SettingSection(title = "정보") {
                SettingNavItem(label = "서비스 이용약관", onClick = onTermsClick)
                SettingNavItem(label = "개인정보 처리방침", onClick = onPrivacyClick)
                SettingInfoItem(label = "현재 버전", trailingText = uiState.appVersion)
            }

            // 지원
            SettingSection(title = "지원") {
                SettingNavItem(label = "1:1 문의", onClick = onInquiryClick)
            }

            // 계정
            SettingSection(title = "계정") {
                if (isLoggedIn) {
                    SettingInfoItem(label = "로그인 방식", trailingText = uiState.loginMethod)
                    SettingTextItem(label = "로그아웃", onClick = { showLogoutSheet = true }, color = TextSubtitle)
                    SettingTextItem(
                        label = if (isWithdrawing) "탈퇴 처리 중..." else "회원탈퇴",
                        onClick = { if (!isWithdrawing) showWithdrawSheet = true },
                        color = if (isWithdrawing) TextCaption else Negative
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
                primaryText = "조금 더 써볼게요",
                actionText = "로그아웃",
                onPrimaryClick = { showLogoutSheet = false },
                onActionClick = {
                    showLogoutSheet = false
                    onLogoutClick()
                },
                onDismissRequest = { showLogoutSheet = false }
            )
        }

        if (showWithdrawSheet) {
            SettingConfirmBottomSheet(
                illustRes = R.drawable.img_deleteuser,
                message = "계정을 삭제하시겠어요?",
                primaryText = "조금 더 써볼게요",
                actionText = "탈퇴하기",
                onPrimaryClick = { showWithdrawSheet = false },
                onActionClick = {
                    showWithdrawSheet = false
                    onWithdrawClick()
                },
                onDismissRequest = { showWithdrawSheet = false }
            )
        }

        // 탈퇴 실패 에러 다이얼로그
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
            // 섹션 제목 — 박스 내부 첫 줄
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

// ic_next 아이콘이 있는 일반 이동 항목
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

// 오른쪽에 텍스트 값만 표시하는 항목 (버전, 로그인 방식)
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

// 오른쪽 아이콘 없는 텍스트 버튼 항목 (로그아웃, 회원탈퇴)
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

// ────────────────────────────────────────────
// Preview
// ────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "1. 기본 설정 화면 (Google 로그인)")
@Composable
fun SettingScreenPreview() {
    BrifeTheme {
        SettingScreen(
            uiState = SettingUiState(loginMethod = "Google", appVersion = "1.0.0"),
            isLoggedIn = true,
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "3. 비로그인 설정 화면")
@Composable
fun SettingScreenGuestPreview() {
    BrifeTheme {
        SettingScreen(
            uiState = SettingUiState(appVersion = "1.0.0"),
            isLoggedIn = false,
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "2. 카카오 로그인 방식 표시")
@Composable
fun SettingScreenKakaoPreview() {
    BrifeTheme {
        SettingScreen(
            uiState = SettingUiState(loginMethod = "Kakao", appVersion = "1.0.0"),
            onBackClick = {}
        )
    }
}
