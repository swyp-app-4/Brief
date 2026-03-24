package com.example.brife.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String? = null,           // 제목 (아카이브 등에서 사용)
    showLogo: Boolean = true,        // 로고 표시 여부 (홈에서 사용)
    showSettings: Boolean = true,    // 설정 아이콘 표시 여부 (홈에서 사용)
    showSearch: Boolean = false,     // 검색창 표시 여부 (탐색에서 사용)
    onSettingClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    centerTitle: Boolean = false,     // 기본값 추가 (에러 해결 1)
) {
    TopAppBar(
        title = {
            // 내부의 중복된 val centerTitle 선언 삭제 (에러 해결 2)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = if (centerTitle) Alignment.Center else Alignment.CenterStart
            ) {
                if (title != null) {
                    AppText(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = if (centerTitle) TextAlign.Center else TextAlign.Start
                    )
                }
            }
        },
        navigationIcon = {
            if (showLogo) {
                Image(
                    painter = painterResource(id = R.drawable.brife_logo),
                    contentDescription = "Brife Logo",
                    modifier = Modifier.size(width = 60.dp, height = 20.dp)
                )
            } else if (centerTitle) {
                // 중앙 정렬 시 좌측 공간을 확보하여 균형을 맞춤
                Spacer(modifier = Modifier.width(48.dp))
            }
        },
        actions = {
            if (showSearch) {
                IconButton(onClick = { /* 검색 로직 */ }) {
                    Icon(painter = painterResource(id = R.drawable.ic_search), contentDescription = "Search")
                }
            }

            if (showSettings) {
                IconButton(onClick = onSettingClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_setting),
                        contentDescription = "Settings"
                    )
                }
            } else if (centerTitle) {
                // 중앙 정렬 시 우측 공간을 확보하여 균형을 맞춤
                Spacer(modifier = Modifier.width(48.dp))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
    )
}

// --- Preview 영역 ---

@Preview(showBackground = true, name = "1. 홈 화면 (로고 + 설정)")
@Composable
fun HomeTopBarPreview() {
    AppTopBar() // 이제 에러 없이 호출 가능
}

@Preview(showBackground = true, name = "2. 탐색 화면 (검색 + 설정)")
@Composable
fun ExploreTopBarPreview() {
    // 쉼표 오류 수정 (에러 해결 3)
    AppTopBar(showLogo = false, showSearch = true)
}

@Preview(showBackground = true, name = "3. 아카이브 화면 (중앙 텍스트 + 아이콘)")
@Composable
fun ArchiveTopBarPreview() {
    AppTopBar(
        title = "보관함",
        showLogo = false,
        showSettings = false,
        centerTitle = true
    )
}

@Preview(showBackground = true, name = "4. 프로필 화면 (텍스트)")
@Composable
fun ProfileTopBarPreview() {
    AppTopBar(
        showLogo = true,
        showSettings = false,
        centerTitle = false
    )
}