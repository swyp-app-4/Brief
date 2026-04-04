package com.example.brife.feature.auth

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brife.R
import com.example.brife.ui.theme.BgDefault
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.TextBody
import com.example.brife.ui.component.AppText

private val SulphurPoint = FontFamily(
    Font(R.font.sulphur_point_bold, FontWeight.Bold)
)


@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onKakaoClick: () -> Unit = {},
    onNaverClick: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    onDismissTerms: () -> Unit = {},
    onAgreeTerms: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = BgDefault
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDefault)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoSection()

            Spacer(modifier = Modifier.height(56.dp))

            SocialLoginDivider()

            Spacer(modifier = Modifier.height(24.dp))

            SocialLoginButton(
                text = if (uiState.isLoading) "로그인 중..." else "카카오로 로그인",
                backgroundColor = Color(0xFFFEE500),
                contentColor = Color(0xFF191919),
                iconRes = R.drawable.ic_kakao,
                enabled = !uiState.isLoading,
                onClick = {
                    if (!uiState.isLoading) {
                        onKakaoClick()
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SocialLoginButton(
                text = if (uiState.isLoading) "로그인 중..." else "네이버로 로그인",
                backgroundColor = Color(0xFF03C75A),
                contentColor = Color.White,
                iconRes = R.drawable.ic_naver,
                enabled = !uiState.isLoading,
                onClick = onNaverClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            SocialLoginButton(
                text = if (uiState.isLoading) "로그인 중..." else "Google로 로그인",
                backgroundColor = Color.White,
                contentColor = Color(0xFF464646),
                borderColor = Color(0xFFE3E5E8),
                iconRes = R.drawable.ic_google,
                enabled = !uiState.isLoading,
                onClick = onGoogleClick
            )

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                AppText(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

        }
    }
}


@Composable
private fun LogoSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.brife_logo),
                contentDescription = "Brife Logo",
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Brife",
                color = Color(0xFF464646),
                fontFamily = SulphurPoint,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        AppText(
            text = "간편한 지식 습득을 경험해요",
            style = MaterialTheme.typography.titleSmall,
            color = TextBody
        )
    }
}


@Composable
private fun SocialLoginDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color(0xFFCCCCCC))
        )

        AppText(
            text = " 소셜 로그인 ",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF767676),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color(0xFFCCCCCC))
        )
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    @DrawableRes iconRes: Int? = null,
    borderColor: Color = Color.Transparent,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = if (borderColor != Color.Transparent) {
            androidx.compose.foundation.BorderStroke(1.dp, borderColor)
        } else {
            null
        },
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterStart)
                )
            }

            AppText(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    BrifeTheme {
        LoginScreen(
            uiState = LoginUiState()

        )
    }
}
