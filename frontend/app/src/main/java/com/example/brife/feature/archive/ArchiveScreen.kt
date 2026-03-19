package com.example.brife.feature.archive

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.example.brife.ui.theme.PrimaryNormal

@Composable
fun ArchiveScreen(

    modifier: Modifier = Modifier // MainScreen에서 전달받은 padding을 적용하기 위함
) {

    val folderCount = 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
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
            style = MaterialTheme.typography.titleMedium,
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
                onClick = { /* TODO: 폴더 추가 로직 */ }
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
                onClick = { /* TODO: 즐겨찾기 이동 로직 */ }
            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                    // 중앙 상단에는 텍스트 (선택 사항, 필요 없으면 제거 가능)
                    AppText(
                        text = "즐겨찾기",
                        style = MaterialTheme.typography.bodyLarge,
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
    }
}

/**
 * InterestCard 스타일을 계승한 아카이브 전용 폴더 카드
 */
@Composable
fun ArchiveFolderCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ComponentDefault // InterestCard와 동일한 배경색
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}




