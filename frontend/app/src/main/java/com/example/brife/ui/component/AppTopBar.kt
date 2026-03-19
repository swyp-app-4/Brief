package com.example.brife.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String? = null,           // 제목 (아카이브 등에서 사용)
    showLogo: Boolean = true,        // 로고 표시 여부 (홈에서 사용)
    showSettings: Boolean = true,    // 설정 아이콘 표시 여부 (홈, 탐색 등에서 사용)
    showSearch: Boolean = false,      // 검색창 표시 여부 (탐색에서 사용)
    onSettingClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            if (title != null) {
                AppText(text = title, style = MaterialTheme.typography.titleLarge)
            }
        },
        navigationIcon = {
            if (showLogo) {
                Image(
                    painter = painterResource(id = R.drawable.brife_logo),
                    contentDescription = "Brife Logo",
                    modifier = Modifier.size(width = 60.dp, height = 20.dp)
                )
            }
        },
        actions = {
            // 탐색 화면일 경우 여기에 검색 아이콘이나 창을 추가 가능
            if (showSearch) {
                IconButton(onClick = { /* 검색 로직 */ }) {
                    Icon(painter = painterResource(id = R.drawable.ic_search), contentDescription = "Search")
                }
            }

            // 설정 아이콘 표시 여부에 따라 제어
            if (showSettings) {
                IconButton(onClick = onSettingClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_setting),
                        contentDescription = "Settings"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun AppTopBarPreview() {
    AppTopBar()
}