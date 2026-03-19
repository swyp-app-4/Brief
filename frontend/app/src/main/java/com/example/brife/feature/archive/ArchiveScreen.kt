package com.example.brife.feature.archive

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.ui.component.AppText
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.ComponentDefault
import com.example.brife.ui.theme.InterestSelected
import com.example.brife.ui.theme.PrimaryNormal
import com.example.brife.feature.archive.component.CreateFolderBottomSheet
@Composable
fun ArchiveScreen(
    onNavigateToDetail: (String) -> Unit, // 상세 화면 이동 콜백 추가
    modifier: Modifier = Modifier // MainScreen에서 전달받은 padding을 적용하기 위함
) {

    // 1. 추가된 폴더 리스트 상태 (초기값은 빈 리스트)
    var folders by remember { mutableStateOf(listOf<String>()) }
    val folderCount = folders.size + 1 // 즐겨찾기 기본 포함
    var showBottomSheet by remember { mutableStateOf(false) }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()) // 폴더가 많아지면 스크롤 가능
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // 1. 폴더 개수 텍스트 (숫자 부분만 PrimaryNormal 적용)
        AppText(
            text = buildAnnotatedString {
                append("폴더 ")
                withStyle(style = SpanStyle(color = PrimaryNormal)) {
                    append("$folderCount")
                }
                append("개")
            },
            style = MaterialTheme.typography.titleSmall,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 폴더 버튼 영역 (2개 가로 배치)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 왼쪽: 폴더 추가 버튼
            ArchiveFolderCard(
                modifier = Modifier.weight(1f),
                onClick = { showBottomSheet = true }
            ) {
                // 가운데 아이콘 배치
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_folderadd),
                        contentDescription = "폴더 추가",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // 오른쪽: 즐겨찾기 폴더
            ArchiveFolderCard(
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToDetail("즐겨찾기") }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 중앙 상단에는 텍스트 (선택 사항, 필요 없으면 제거 가능)
                    AppText(
                        text = "즐겨찾기",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.TopStart)
                    )

                    // 우측 하단 아이콘 배치
                    Image(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = "즐겨찾기",
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
        }
        // 3. 동적으로 추가되는 폴더 영역 (2개씩 배치)
        folders.chunked(2).forEach { rowFolders ->
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowFolders.forEach { folderName ->
                    ArchiveFolderCard(
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToDetail(folderName) }
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            AppText(
                                text = folderName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.TopStart)
                            )
                        }
                    }
                }
                // 홀수 개일 경우 빈 공간을 채워 정렬 유지
                if (rowFolders.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (showBottomSheet) {
            CreateFolderBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                onSave = { name ->
                    folders = folders + name // 리스트에 추가
                    showBottomSheet = false
                },
                currentFolderCount = folderCount,
                existingFolders = folders + "즐겨찾기" // 중복 체크용 리스트 전달
            )
        }
    }
}

/**
 * InterestCard 스타일을 계승한 아카이브 전용 폴더 카드
 */
@Composable
fun ArchiveFolderCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false, //선택 상태 추가 (상세 화면 진입 시 or 필요 시)
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) InterestSelected else ComponentDefault
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}




