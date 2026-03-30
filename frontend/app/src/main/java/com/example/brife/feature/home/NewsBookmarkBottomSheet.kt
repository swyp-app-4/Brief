package com.example.brife.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.data.local.BookmarkFolderUiModel
import com.example.brife.feature.archive.component.CreateFolderBottomSheet
import com.example.brife.ui.component.AppText
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.CtaDisabled
import com.example.brife.ui.theme.PrimaryNormal
import com.example.brife.ui.theme.TextSubtitle
import com.example.brife.ui.theme.TextTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsBookmarkBottomSheet(
    folders: List<BookmarkFolderUiModel>,
    onDismissRequest: () -> Unit,
    onMyFolderClick: () -> Unit,
    onAddFolderClick: () -> Unit,
    onFolderBookmarkClick: (BookmarkFolderUiModel) -> Unit,
    onSaveClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = Color.White,
        contentColor = Color.Black,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 8.dp, bottom = 20.dp)
        ) {
            // 내 폴더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMyFolderClick() }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText(
                    text = "내 폴더",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextTitle
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_longform_arrow_right),
                    contentDescription = "내 폴더로 이동",
                    tint = Color.Unspecified
                )
            }

            // 새로운 폴더 추가
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddFolderClick() }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_longform_add),
                    contentDescription = "새로운 폴더 추가",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                AppText(
                    text = "새로운 폴더 추가",
                    modifier = Modifier.padding(start = 10.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = PrimaryNormal
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = Color(0xFFE9EDF2)
            )

            // 폴더 목록 — 스크롤 가능, 높이 제한
            Column(
                modifier = Modifier
                    .heightIn(max = 280.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                folders.forEachIndexed { index, folder ->
                    FolderBookmarkRow(
                        folder = folder,
                        onBookmarkClick = { onFolderBookmarkClick(folder) }
                    )
                    if (index != folders.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = Color(0xFFE9EDF2)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 취소 / 저장 버튼 — CreateFolderBottomSheet와 동일한 스타일
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CtaDisabled),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AppText(
                        text = "취소",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNormal),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AppText(
                        text = "저장",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderBookmarkRow(
    folder: BookmarkFolderUiModel,
    onBookmarkClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val folderLabel = if (folder.newsCount > 0) "${folder.name} (${folder.newsCount})" else folder.name

        AppText(
            text = folderLabel,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSubtitle
        )

        IconButton(onClick = onBookmarkClick) {
            Icon(
                painter = painterResource(
                    id = if (folder.isSelected) {
                        R.drawable.ic_longform_bookmark_active
                    } else {
                        R.drawable.ic_longform_bookmark_inactive
                    }
                ),
                contentDescription = if (folder.isSelected) "즐겨찾기 해제" else "즐겨찾기 추가",
                tint = Color.Unspecified
            )
        }
    }
}
