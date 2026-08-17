package com.swyp.brife.feature.archive.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.theme.BrifeTheme
import com.swyp.brife.ui.theme.CtaDisabled
import com.swyp.brife.ui.theme.Negative
import com.swyp.brife.ui.theme.PrimaryButtonTextStyle
import com.swyp.brife.ui.theme.TextSubtitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveMoreBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onRenameClick: () -> Unit,  // 1차: 미구현, 표시만
    onDeleteClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // 폴더명 수정 (1차: 표시만)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRenameClick() }
                    .padding(vertical = 16.dp)
            ) {
                AppText(
                    text = "폴더명 수정",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSubtitle
                )
            }

            // 폴더 삭제
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDeleteClick() }
                    .padding(vertical = 16.dp)
            ) {
                AppText(
                    text = "폴더 삭제",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Negative
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 취소 버튼 — PrimaryButton 스타일, CtaDisabled 색상
            Button(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CtaDisabled),
                contentPadding = PaddingValues(0.dp)
            ) {
                AppText(
                    text = "취소",
                    style = PrimaryButtonTextStyle,
                    color = Color.White
                )
            }
        }
    }
}

// Preview 2. 더보기 바텀시트가 열린 상태
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "2. 더보기 바텀시트 열린 상태")
@Composable
fun ArchiveMoreBottomSheetPreview() {
    BrifeTheme {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ArchiveMoreBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {},
            onRenameClick = {},
            onDeleteClick = {}
        )
    }
}
