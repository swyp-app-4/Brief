package com.example.brife.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.theme.BgDefault
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.TextBody

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialLoginIconButton(
                    iconRes = R.drawable.ic_naver_login,
                    contentDescription = "네이버 로그인",
                    enabled = !uiState.isLoading,
                    onClick = onNaverClick
                )

                SocialLoginIconButton(
                    iconRes = R.drawable.ic_kakao_login,
                    contentDescription = "카카오 로그인",
                    enabled = !uiState.isLoading,
                    onClick = {
                        if (!uiState.isLoading) {
                            onKakaoClick()
                        }
                    }
                )

                SocialLoginIconButton(
                    iconRes = R.drawable.ic_google_login,
                    contentDescription = "구글 로그인",
                    enabled = !uiState.isLoading,
                    onClick = onGoogleClick
                )
            }

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
        Spacer(modifier = Modifier.height(180.dp))
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
private fun SocialLoginIconButton(
    iconRes: Int,
    contentDescription: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Login - Default")
@Composable
private fun LoginScreenDefaultPreview() {
    BrifeTheme {
        LoginScreen(
            uiState = LoginUiState()
        )
    }
}
