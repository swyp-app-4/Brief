package com.example.brife.feature.archive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
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
import com.example.brife.feature.archive.component.CreateFolderBottomSheet
import com.example.brife.ui.component.AppText
import com.example.brife.ui.theme.BorderDefault
import com.example.brife.ui.theme.BrifeTheme
import com.example.brife.ui.theme.ComponentDefault
import com.example.brife.ui.theme.CtaDisabled
import com.example.brife.ui.theme.InterestSelectedLight
import com.example.brife.ui.theme.Negative
import com.example.brife.ui.theme.PrimaryNormal
import com.example.brife.ui.theme.TextBody

@Composable
fun ArchiveScreen(
    modifier: Modifier = Modifier,
    folders: List<ArchiveFolderUiModel>,
    favoriteArchiveId: Long = 0L,
    favoriteItemCount: Int = 0,
    onFolderAdd: (String) -> Unit,
    onNavigateToDetail: (archiveId: Long, folderName: String) -> Unit,
    isDeleteMode: Boolean = false,
    selectedFolderIds: Set<Long> = emptySet(),
    onToggleFolderSelect: (Long) -> Unit = {},
    onCancelDelete: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    isRenameMode: Boolean = false,
    onFolderRename: (archiveId: Long, newName: String) -> Unit = { _, _ -> },
    onCancelRename: () -> Unit = {},
    showTopBar: Boolean = true
) {
    val folderCount = folders.size + 1
    val isSelectionMode = isDeleteMode || isRenameMode
    val selectionGuideText = when {
        isDeleteMode -> "삭제할 폴더를 선택해주세요"
        isRenameMode -> "이름을 수정할 폴더를 선택해주세요"
        else -> ""
    }

    var showCreateSheet by remember { mutableStateOf(false) }
    var showRenameSheet by remember { mutableStateOf(false) }
    var renamingFolderName by remember { mutableStateOf("") }
    var renamingFolderId by remember { mutableStateOf(0L) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            if (isSelectionMode) {
                ArchiveSelectionModeBanner(
                    text = selectionGuideText,
                    onCancelClick = {
                        if (isDeleteMode) onCancelDelete() else onCancelRename()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArchiveFolderCard(
                    modifier = Modifier.weight(1f),
                    isSelectionMode = isSelectionMode,
                    enabled = !isSelectionMode,
                    onClick = { if (!isSelectionMode) showCreateSheet = true }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_folderadd),
                            contentDescription = "폴더 추가",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                ArchiveFolderCard(
                    modifier = Modifier.weight(1f),
                    isSelectionMode = isSelectionMode,
                    enabled = !isSelectionMode,
                    onClick = {
                        if (!isSelectionMode) {
                            onNavigateToDetail(favoriteArchiveId, "즐겨찾기")
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.TopStart),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AppText(
                                text = "즐겨찾기",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            AppText(
                                text = "${favoriteItemCount}개",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextBody
                            )
                        }
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

            folders.chunked(2).forEach { rowFolders ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowFolders.forEach { folder ->
                        ArchiveFolderCard(
                            modifier = Modifier.weight(1f),
                            selected = isDeleteMode && folder.archiveId in selectedFolderIds,
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                when {
                                    isDeleteMode -> onToggleFolderSelect(folder.archiveId)
                                    isRenameMode -> {
                                        renamingFolderName = folder.folderName
                                        renamingFolderId = folder.archiveId
                                        showRenameSheet = true
                                    }
                                    else -> onNavigateToDetail(folder.archiveId, folder.folderName)
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.align(Alignment.TopStart),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    AppText(
                                        text = folder.folderName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    AppText(
                                        text = "${folder.itemCount}개",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextBody
                                    )
                                }

                                if (isRenameMode) {
                                    AppText(
                                        text = "선택",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrimaryNormal,
                                        modifier = Modifier.align(Alignment.BottomEnd)
                                    )
                                }
                            }
                        }
                    }
                    if (rowFolders.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isDeleteMode) {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (isDeleteMode) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCancelDelete,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CtaDisabled),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    AppText(
                        text = "취소",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Button(
                    onClick = onConfirmDelete,
                    enabled = selectedFolderIds.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Negative,
                        disabledContainerColor = Negative.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    AppText(
                        text = "삭제",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        if (showCreateSheet) {
            CreateFolderBottomSheet(
                onDismissRequest = { showCreateSheet = false },
                onSave = { name ->
                    onFolderAdd(name)
                    showCreateSheet = false
                },
                currentFolderCount = folderCount,
                existingFolders = folders.map { it.folderName } + "즐겨찾기"
            )
        }

        if (showRenameSheet) {
            CreateFolderBottomSheet(
                title = "폴더명 수정",
                initialFolderName = renamingFolderName,
                onDismissRequest = {
                    showRenameSheet = false
                    onCancelRename()
                },
                onSave = { newName ->
                    onFolderRename(renamingFolderId, newName)
                    showRenameSheet = false
                    onCancelRename()
                },
                currentFolderCount = 0,
                existingFolders = (folders.map { it.folderName } + "즐겨찾기")
                    .filter { it != renamingFolderName }
            )
        }
    }
}

@Composable
private fun ArchiveSelectionModeBanner(
    text: String,
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = InterestSelectedLight,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppText(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onCancelClick) {
            AppText(
                text = "취소",
                style = MaterialTheme.typography.labelLarge,
                color = PrimaryNormal
            )
        }
    }
}

@Composable
fun ArchiveFolderCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    isSelectionMode: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                selected -> InterestSelectedLight
                isSelectionMode -> ComponentDefault.copy(alpha = 0.95f)
                else -> ComponentDefault
            }
        ),
        border = when {
            selected -> BorderStroke(1.dp, PrimaryNormal)
            isSelectionMode -> BorderStroke(1.dp, BorderDefault)
            else -> null
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}
