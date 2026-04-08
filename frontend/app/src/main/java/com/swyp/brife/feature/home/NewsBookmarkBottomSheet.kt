package com.swyp.brife.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.swyp.brife.R
import com.swyp.brife.data.local.BookmarkFolderUiModel
import com.swyp.brife.ui.component.AppText
import com.swyp.brife.ui.theme.CtaDisabled
import com.swyp.brife.ui.theme.PrimaryNormal
import com.swyp.brife.ui.theme.TextSubtitle
import com.swyp.brife.ui.theme.TextTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsBookmarkBottomSheet(
    folders: List<BookmarkFolderUiModel>,
    isSaving: Boolean = false,
    onDismissRequest: () -> Unit,
    onMyFolderClick: () -> Unit,
    onAddFolderClick: () -> Unit,
    onFolderBookmarkClick: (BookmarkFolderUiModel) -> Unit,
    onSaveClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            if (!isSaving) onDismissRequest()
        },
        sheetState = sheetState,
        dragHandle = null,
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 8.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSaving) { onMyFolderClick() }
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSaving) { onAddFolderClick() }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_longform_add),
                    contentDescription = "새 폴더 추가",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                AppText(
                    text = "새 폴더 추가",
                    modifier = Modifier.padding(start = 10.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = PrimaryNormal
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = Color(0xFFE9EDF2)
            )

            Column(
                modifier = Modifier
                    .heightIn(max = 280.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                folders.forEachIndexed { index, folder ->
                    FolderBookmarkRow(
                        folder = folder,
                        enabled = !isSaving,
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismissRequest,
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CtaDisabled),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AppText(
                        text = if (isSaving) "저장 중..." else "취소",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Button(
                    onClick = onSaveClick,
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNormal),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AppText(
                        text = if (isSaving) "저장 중..." else "저장",
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
    enabled: Boolean,
    onBookmarkClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val folderLabel = if (folder.newsCount > 0) {
            "${folder.name} (${folder.newsCount})"
        } else {
            folder.name
        }

        AppText(
            text = folderLabel,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSubtitle
        )

        IconButton(
            enabled = enabled,
            onClick = onBookmarkClick
        ) {
            Icon(
                painter = painterResource(
                    id = if (folder.isSelected) {
                        R.drawable.ic_longform_bookmark_active
                    } else {
                        R.drawable.ic_longform_bookmark_inactive
                    }
                ),
                contentDescription = if (folder.isSelected) "저장 해제" else "저장",
                tint = Color.Unspecified
            )
        }
    }
}
