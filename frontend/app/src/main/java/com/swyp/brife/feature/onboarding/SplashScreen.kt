package com.swyp.brife.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swyp.brife.R
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.Pretendard
import com.swyp.brife.ui.theme.SulphurPoint
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinish: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        delay(1500)
        onFinish()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF444E5E)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 로고와 텍스트를 가로로 배치
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.brife_logo),
                contentDescription = "Brife Logo",
                modifier = Modifier.size(56.dp) // 텍스트 높이에 맞춰 크기 조정
            )
            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Brief",
                color = Color(0xFFFFFFFF),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = SulphurPoint,
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp
                )
            )

        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "3초 스캔, 3분 몰입\n내 관심사로 정리되는 뉴스",
            color = Color(0xFFFFFFFF).copy(alpha = 0.9f),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )



    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    BrifeTheme {
        SplashScreen()
    }
}