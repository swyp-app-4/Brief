package com.example.brife.feature.setting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.component.PrimaryButton
import com.example.brife.ui.theme.*

data class SettingUiState(
    val loginMethod: String = "Google",  // "Google" | "Naver" | "Kakao"
    val appVersion: String = "1.0.0"
)

@Composable
fun SettingScreen(
    uiState: SettingUiState,
    onBackClick: () -> Unit,
    onWidgetSettingClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onInquiryClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showLogoutSheet by remember { mutableStateOf(false) }
    var showWithdrawSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .background(Color.White)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "뒤로가기",
                        tint = Color.Unspecified
                    )
                }
                AppText(
                    text = "설정",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextTitle
                )
            }
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
                SettingInfoItem(label = "로그인 방식", trailingText = uiState.loginMethod)
                SettingTextItem(label = "로그아웃", onClick = { showLogoutSheet = true }, color = TextSubtitle)
                SettingTextItem(label = "회원탈퇴", onClick = { showWithdrawSheet = true }, color = Negative)
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

// HomeToLoginBottomSheet와 동일한 구조 — 일러스트·텍스트·버튼 텍스트만 파라미터로 분리
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingConfirmBottomSheet(
    illustRes: Int,
    message: String,
    primaryText: String,        // 윗버튼 ("조금 더 써볼게요")
    actionText: String,         // 아랫버튼 ("로그아웃" / "탈퇴하기")
    onPrimaryClick: () -> Unit, // 윗버튼: 시트 닫고 현재 화면 유지
    onActionClick: () -> Unit,  // 아랫버튼: 동작 확정
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .padding(horizontal = 24.dp)
                    .padding(top = 0.dp, bottom = 32.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(120.dp))

                AppText(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(25.dp))

                PrimaryButton(
                    text = primaryText,
                    onClick = onPrimaryClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onActionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CtaDisabled,
                        contentColor = Color.White
                    )
                ) {
                    AppText(
                        text = actionText,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }

            // 일러스트 — 바텀시트 경계선에 걸쳐서 올라오도록 배치
            Image(
                painter = painterResource(id = illustRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(200.dp)
            )
        }
    }
}

// ────────────────────────────────────────────
// Preview
// ────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "3. 로그아웃 바텀시트 열린 상태")
@Composable
fun SettingLogoutSheetPreview() {
    BrifeTheme {
        SettingConfirmBottomSheet(
            illustRes = R.drawable.img_logout_character,
            message = "로그아웃 하시겠어요?",
            primaryText = "조금 더 써볼게요",
            actionText = "로그아웃",
            onPrimaryClick = {},
            onActionClick = {},
            onDismissRequest = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "4. 회원탈퇴 바텀시트 열린 상태")
@Composable
fun SettingWithdrawSheetPreview() {
    BrifeTheme {
        SettingConfirmBottomSheet(
            illustRes = R.drawable.img_deleteuser,
            message = "계정을 삭제하시겠어요?",
            primaryText = "조금 더 써볼게요",
            actionText = "탈퇴하기",
            onPrimaryClick = {},
            onActionClick = {},
            onDismissRequest = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "1. 기본 설정 화면 (Google 로그인)")
@Composable
fun SettingScreenPreview() {
    BrifeTheme {
        SettingScreen(
            uiState = SettingUiState(loginMethod = "Google", appVersion = "1.0.0"),
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
