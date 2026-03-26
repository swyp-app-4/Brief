package com.example.brife.feature.home


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import com.example.brife.R
import com.example.brife.data.local.BookmarkFolderUiModel
import com.example.brife.ui.component.AppText
import com.example.brife.ui.theme.PrimaryNormal
import com.example.brife.ui.theme.TextBody
import com.example.brife.ui.theme.TextTitle
import androidx.compose.ui.tooling.preview.Preview
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.TextSubtitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsBookmarkBottomSheet(
    folders: List<BookmarkFolderUiModel>,
    onDismissRequest: () -> Unit,
    onMyFolderClick: () -> Unit,
    onAddFolderClick: () -> Unit,
    onFolderBookmarkClick: (BookmarkFolderUiModel) -> Unit
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





@Preview(showBackground = true, name = "FolderBookmarkRow - Selected")
@Composable
private fun FolderBookmarkRowSelectedPreview() {
    BrifeTheme {
        Box(
            modifier = Modifier.background(Color.White)
        ) {
            FolderBookmarkRow(
                folder = BookmarkFolderUiModel(
                    id = 1L,
                    name = "즐겨찾기",
                    newsCount = 0,
                    isSelected = true
                ),
                onBookmarkClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "FolderBookmarkRow - Count")
@Composable
private fun FolderBookmarkRowCountPreview() {
    BrifeTheme {
        Box(
            modifier = Modifier.background(Color.White)
        ) {
            FolderBookmarkRow(
                folder = BookmarkFolderUiModel(
                    id = 2L,
                    name = "전쟁",
                    newsCount = 12,
                    isSelected = false
                ),
                onBookmarkClick = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "NewsBookmarkBottomSheet")
@Composable
private fun NewsBookmarkBottomSheetPreview() {
    BrifeTheme {
        NewsBookmarkBottomSheet(
            folders = listOf(
                BookmarkFolderUiModel(
                    id = 1L,
                    name = "즐겨찾기",
                    newsCount = 0,
                    isSelected = true
                ),
                BookmarkFolderUiModel(
                    id = 2L,
                    name = "전쟁",
                    newsCount = 3,
                    isSelected = false
                ),
                BookmarkFolderUiModel(
                    id = 3L,
                    name = "경제",
                    newsCount = 7,
                    isSelected = false
                )
            ),
            onDismissRequest = {},
            onMyFolderClick = {},
            onAddFolderClick = {},
            onFolderBookmarkClick = {}
        )
    }
}