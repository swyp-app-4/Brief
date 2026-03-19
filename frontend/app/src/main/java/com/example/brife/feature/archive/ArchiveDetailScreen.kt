// C:/Users/SAMSUNG/Desktop/brife/frontend/app/src/main/java/com/example/brife/feature/archive/ArchiveDetailScreen.kt

package com.example.brife.feature.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.brife.feature.home.HomeNewsCardContent
import com.example.brife.feature.home.HomeNewsCardItem
import com.example.brife.ui.component.AppTopBar // 기존에 만들어둔 탑바가 있다면 활용

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveDetailScreen(
    folderName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 임시 데이터 (실제로는 ViewModel 등에서 받아와야 함)
    val newsItems = listOf(
        HomeNewsCardItem(
            category = "경제",
            title = "기준금리 동결 속 소비 회복 기대감 확대",
            notice = "AI가 분석하여 핵심만 재구성한 요약본입니다",
            summary = "한국은행의 기준금리 동결 이후 시장은 당분간 안정세를 유지할 것으로 전망되고 있다.",
            insight = "금리 흐름은 대출, 소비, 투자 심리에 직접 영향을 미칩니다."
        ),
        // 추가 데이터...
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = folderName, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF3F3F3) // 리스트 배경색 (카드와 대비되도록)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(newsItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    HomeNewsCardContent(item = item)
                }
            }
        }
    }
}