package com.example.brife.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.theme.BorderDefault
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.PrimaryNormal
import com.example.brife.ui.theme.TextBody
import com.example.brife.ui.theme.TextSubtitle
import com.example.brife.ui.theme.TextTitle

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    uiState: ProfileUiState = mockGuestProfileState,
    onLoginClick: () -> Unit = {},
    onResetInterestClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // 프로필 아바타 — 상단 중앙 고정
        Image(
            painter = painterResource(id = R.drawable.img_profile_avatar),
            contentDescription = "프로필 아바타",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoggedIn) {
            LoggedInContent(
                uiState = uiState,
                onResetInterestClick = onResetInterestClick
            )
        } else {
            GuestContent(
                uiState = uiState,
                onLoginClick = onLoginClick,
                onResetInterestClick = onResetInterestClick
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ────────────────────────────────────────────
// 비로그인 콘텐츠
// ────────────────────────────────────────────

@Composable
private fun GuestContent(
    uiState: ProfileUiState,
    onLoginClick: () -> Unit,
    onResetInterestClick: () -> Unit
) {
    AppText(
        text = "로그인을 해주세요",
        style = MaterialTheme.typography.bodyLarge,
        color = TextSubtitle,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))

    // 로그인 버튼: white background, PrimaryNormal stroke, radius 30
    Button(
        onClick = onLoginClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = PrimaryNormal
        ),
        border = BorderStroke(1.dp, PrimaryNormal),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        AppText(
            text = "로그인",
            style = MaterialTheme.typography.labelLarge,
            color = PrimaryNormal
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    InterestBox(interests = uiState.interests)

    Spacer(modifier = Modifier.height(12.dp))

    ResetInterestBox(onClick = onResetInterestClick)
}

// ────────────────────────────────────────────
// 로그인 콘텐츠
// ────────────────────────────────────────────

@Composable
private fun LoggedInContent(
    uiState: ProfileUiState,
    onResetInterestClick: () -> Unit
) {
    AppText(
        text = uiState.userName,
        style = MaterialTheme.typography.titleSmall,
        color = TextTitle,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    InterestBox(interests = uiState.interests)

    Spacer(modifier = Modifier.height(12.dp))

    MemberInfoBox(
        userName = uiState.userName,
        userEmail = uiState.userEmail
    )

    Spacer(modifier = Modifier.height(12.dp))

    ResetInterestBox(onClick = onResetInterestClick)
}

// ────────────────────────────────────────────
// 공통 컴포넌트
// ────────────────────────────────────────────

/**
 * 관심사 박스
 * - 가로 한 줄, 각 항목(아이콘 + 카테고리명) 사이 세로 구분선
 * - 1~3개 항목 대응
 */
@Composable
private fun InterestBox(interests: List<ProfileCategoryItem>) {
    if (interests.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .border(1.dp, BorderDefault, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        interests.forEachIndexed { index, item ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.name,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                AppText(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextBody,
                    textAlign = TextAlign.Center
                )
            }

            // 마지막 항목 이후에는 구분선 없음
            if (index < interests.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(BorderDefault)
                )
            }
        }
    }
}

/**
 * 회원정보 박스 (로그인 상태 전용)
 * - "회원정보" 레이블 + 이름 / 계정 이메일 행
 */
@Composable
private fun MemberInfoBox(
    userName: String,
    userEmail: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDefault, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        AppText(
            text = "회원정보",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSubtitle
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = "이름",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSubtitle
            )
            AppText(
                text = userName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextBody
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = "계정 이메일",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSubtitle
            )
            AppText(
                text = userEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = TextBody
            )
        }
    }
}

/**
 * 관심사 재설정 박스
 * - InterestBox와 동일한 border/radius/background 스타일
 * - 높이는 InterestBox보다 낮게 (padding 조정)
 */
@Composable
private fun ResetInterestBox(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderDefault, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            text = "관심사 재설정",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSubtitle
        )
        Image(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = "관심사 재설정 이동",
            modifier = Modifier.size(20.dp)
        )
    }
}

// ────────────────────────────────────────────
// Preview
// ────────────────────────────────────────────

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "1. 비로그인 상태"
)
@Composable
fun ProfileScreenGuestPreview() {
    BrifeTheme {
        ProfileScreen(uiState = mockGuestProfileState)
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "2. 로그인 상태"
)
@Composable
fun ProfileScreenLoggedInPreview() {
    BrifeTheme {
        ProfileScreen(uiState = mockLoggedInProfileState)
    }
}
