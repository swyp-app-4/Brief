package com.example.brife.feature.archive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.brife.ui.theme.CtaDisabled
import com.example.brife.ui.theme.Negative
import com.example.brife.ui.theme.PrimaryNormal
import com.example.brife.feature.archive.component.CreateFolderBottomSheet
import com.example.brife.ui.component.AppNavigationBar
import com.example.brife.ui.component.AppTopBar
import com.example.brife.ui.theme.InterestSelectedLight

@Composable
fun ArchiveScreen(
    modifier: Modifier = Modifier,
    folders: List<ArchiveFolderUiModel>,
    favoriteArchiveId: Long = 0L,
    onFolderAdd: (String) -> Unit,
    onNavigateToDetail: (archiveId: Long, folderName: String) -> Unit,
    isDeleteMode: Boolean = false,
    selectedFolderIds: Set<Long> = emptySet(),
    onToggleFolderSelect: (Long) -> Unit = {},
    onCancelDelete: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    // 수정 모드
    isRenameMode: Boolean = false,
    onFolderRename: (archiveId: Long, newName: String) -> Unit = { _, _ -> },
    onCancelRename: () -> Unit = {},
) {
    val folderCount = folders.size + 1 // 즐겨찾기 기본 포함
    var showCreateSheet by remember { mutableStateOf(false) }
    // 수정 모드 로컬 상태
    var showRenameSheet by remember { mutableStateOf(false) }
    var renamingFolderName by remember { mutableStateOf("") }
    var renamingFolderId by remember { mutableStateOf(0L) }

    Box(modifier = modifier.fillMaxSize()) {

        // 스크롤 가능한 메인 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 폴더 개수 텍스트 (숫자만 PrimaryNormal)
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

            // 첫 번째 행: 폴더 추가 버튼 + 즐겨찾기
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 폴더 추가 버튼 (삭제/수정 모드에서는 비활성)
                ArchiveFolderCard(
                    modifier = Modifier.weight(1f),
                    onClick = { if (!isDeleteMode && !isRenameMode) showCreateSheet = true }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_folderadd),
                            contentDescription = "폴더 추가",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // 즐겨찾기 폴더 — 삭제·수정 불가, 해당 모드에서 클릭 no-op
                ArchiveFolderCard(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (!isDeleteMode && !isRenameMode)
                            onNavigateToDetail(favoriteArchiveId, "즐겨찾기")
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        AppText(
                            text = "즐겨찾기",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.TopStart)
                        )
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

            // 동적 폴더 (2개씩 배치)
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
                                AppText(
                                    text = folder.folderName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.align(Alignment.TopStart)
                                )
                            }
                        }
                    }
                    if (rowFolders.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 삭제 모드일 때 하단 버튼이 콘텐츠를 가리지 않도록 공간 확보
            if (isDeleteMode) {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // 삭제 모드 하단 취소/삭제 버튼 (AppNavigationBar 자리를 대체)
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
                // 취소 버튼
                Button(
                    onClick = onCancelDelete,
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

                // 삭제 버튼 (선택된 폴더 없으면 비활성)
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
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AppText(
                        text = "삭제",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        // 폴더 생성 바텀시트
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

        // 폴더명 수정 바텀시트
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
                currentFolderCount = 0, // 수정 모드: 생성 제한 체크 불필요
                existingFolders = (folders.map { it.folderName } + "즐겨찾기").filter { it != renamingFolderName }
            )
        }
    }
}

/**
 * 아카이브 전용 폴더 카드
 * - selected = true 시 InterestSelected 배경 (삭제 선택 상태에서 사용)
 */
@Composable
fun ArchiveFolderCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) InterestSelectedLight else ComponentDefault
        ),
        border = if (selected) BorderStroke(1.dp, PrimaryNormal) else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}
