package com.example.brife.feature.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.brife.ui.component.AppTopBar2
import com.example.brife.ui.theme.BrifeTheme

@Composable
fun OneToOneInquiryScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            AppTopBar2(
                title = "1:1 문의",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        )
        // TODO: 1:1 문의 UI 구현 예정
    }
}

// ────────────────────────────────────────────
// Preview
// ────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "1:1 문의 화면")
@Composable
private fun OneToOneInquiryScreenPreview() {
    BrifeTheme {
        OneToOneInquiryScreen(
            onBackClick = {}
        )
    }
}
