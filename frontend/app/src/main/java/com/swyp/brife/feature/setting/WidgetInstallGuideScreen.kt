package com.swyp.brife.feature.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.brife.R
import com.swyp.brife.feature.widget.WidgetPinHelper
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.component.AppTopBar2
import com.swyp.brife.ui.component.PrimaryButton
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.DarkBackground
import com.swyp.brife.ui.theme.brifeColors

@Composable
fun WidgetInstallGuideScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isDarkTheme = MaterialTheme.colorScheme.background == DarkBackground
    val screenBackground = if (isDarkTheme) DarkBackground else Color.White
    val imageRes = if (isDarkTheme) {
        R.drawable.widgetinstall_darkmode
    } else {
        R.drawable.ill_setting_screen2
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackground)
    ) {
        AppTopBar2(
            title = "위젯 설정",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 110.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(424.dp)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 40.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.3f to screenBackground.copy(alpha = 0.5f),
                                0.6f to screenBackground.copy(alpha = 0.95f),
                                1.0f to screenBackground
                            )
                        )
                )
            }

            AppText(
                text = "위젯을 설치하여\n홈화면에서 뉴스를 확인 하세요",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.brifeColors.textTitle,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

            AppText(
                text = "위젯을 설치하고 홈화면에서 빠르게 뉴스를 확인하세요",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.brifeColors.textCaption,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(screenBackground)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp, top = 12.dp)
        ) {
            PrimaryButton(
                text = "위젯 설치하러 가기",
                onClick = { WidgetPinHelper.requestPinWidget(context) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "위젯 설치 가이드")
@Composable
private fun WidgetInstallGuideScreenPreview() {
    BrifeTheme {
        WidgetInstallGuideScreen(
            onBackClick = {}
        )
    }
}
