package com.swyp.brife.feature.profile

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.brife.R
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.theme.BorderDefault
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.PrimaryNormal
import com.swyp.brife.ui.theme.TextBody
import com.swyp.brife.ui.theme.TextSubtitle

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    uiState: ProfileUiState = mockGuestProfileState,
    onLoginClick: () -> Unit = {},
    onResetInterestClick: () -> Unit = {},
    onEditProfileImageClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        ProfileImageBox(
            imageRes = uiState.profileImageRes,
            onEditClick = onEditProfileImageClick
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

@Composable
private fun ProfileImageBox(
    imageRes: Int,
    onEditClick: () -> Unit
) {
    Box(modifier = Modifier.size(140.dp)) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "프로필 이미지",
            modifier = Modifier.fillMaxSize()
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_profile_edit),
            contentDescription = "프로필 이미지 수정",
            tint = Color.Unspecified,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-6).dp, y = (-6).dp)
                .size(24.dp)
                .clickable(onClick = onEditClick)
        )
    }
}

@Composable
private fun GuestContent(
    uiState: ProfileUiState,
    onLoginClick: () -> Unit,
    onResetInterestClick: () -> Unit
) {
    AppText(
        text = "로그인을 해주세요",
        style = MaterialTheme.typography.titleMedium,
        color = Black,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = onLoginClick,
        modifier = Modifier
            .height(40.dp)
            .width(80.dp),
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
            style = MaterialTheme.typography.labelSmall,
            color = TextBody
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    InterestBox(interests = uiState.interests)

    Spacer(modifier = Modifier.height(12.dp))

    ResetInterestBox(onClick = onResetInterestClick)
}

@Composable
private fun LoggedInContent(
    uiState: ProfileUiState,
    onResetInterestClick: () -> Unit
) {
    AppText(
        text = uiState.userName,
        style = MaterialTheme.typography.titleMedium,
        color = Black,
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
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                AppText(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextBody,
                    textAlign = TextAlign.Center
                )
            }

            if (index < interests.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .padding(vertical = 12.dp)
                        .background(BorderDefault)
                )
            }
        }
    }
}

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
            style = MaterialTheme.typography.titleSmall,
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
            style = MaterialTheme.typography.labelMedium,
            color = TextSubtitle
        )
        Image(
            painter = painterResource(id = R.drawable.ic_next),
            contentDescription = "관심사 재설정 이동",
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenGuestPreview() {
    BrifeTheme {
        ProfileScreen(uiState = mockGuestProfileState)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenLoggedInPreview() {
    BrifeTheme {
        ProfileScreen(uiState = mockLoggedInProfileState)
    }
}
