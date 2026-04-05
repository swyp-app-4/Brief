package com.example.brife.feature.archive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.remote.NetworkModule
import com.example.brife.data.repository.ArchiveRepository

@Composable
fun ArchiveRoute(
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean,
    sessionVersion: Int = 0,
    reloadVersion: Int = 0,
    isDeleteMode: Boolean,
    isRenameMode: Boolean,
    onDeleteModeExit: () -> Unit,
    onRenameModeExit: () -> Unit,
    onNavigateToDetail: (archiveId: Long, folderName: String) -> Unit
) {
    if (!isLoggedIn) {
        ArchiveScreen(
            modifier = modifier,
            folders = emptyList(),
            favoriteArchiveId = 0L,
            favoriteItemCount = 0,
            onFolderAdd = {},
            onNavigateToDetail = onNavigateToDetail,
            isDeleteMode = false,
            selectedFolderIds = emptySet(),
            onToggleFolderSelect = {},
            onCancelDelete = onDeleteModeExit,
            onConfirmDelete = {},
            isRenameMode = false,
            onFolderRename = { _, _ -> },
            onCancelRename = onRenameModeExit
        )
        return
    }

    val context = LocalContext.current
    val authStorage = remember { AuthLocalStorage(context) }
    val repository = remember {
        ArchiveRepository(
            api = NetworkModule.archiveApiService,
            newsApi = NetworkModule.newsApiService, // 이 인자를 추가하세요
            authLocalStorage = authStorage
        )
    }
    val viewModel: ArchiveViewModel = viewModel(
        key = "archive_$sessionVersion",
        factory = ArchiveViewModelFactory(repository)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionVersion, reloadVersion, isLoggedIn) {
        if (isLoggedIn) {
            viewModel.loadFolders()
        }
    }

    var selectedFolderIds by remember { mutableStateOf(setOf<Long>()) }

    ArchiveScreen(
        modifier = modifier,
        folders = uiState.folders,
        favoriteArchiveId = uiState.favoriteArchiveId,
        favoriteItemCount = uiState.favoriteItemCount,
        onFolderAdd = { name -> viewModel.createFolder(name) },
        onNavigateToDetail = onNavigateToDetail,
        isDeleteMode = isDeleteMode,
        selectedFolderIds = selectedFolderIds,
        onToggleFolderSelect = { id ->
            selectedFolderIds = if (id in selectedFolderIds)
                selectedFolderIds - id
            else
                selectedFolderIds + id
        },
        onCancelDelete = {
            selectedFolderIds = emptySet()
            onDeleteModeExit()
        },
        onConfirmDelete = {
            viewModel.deleteFolders(selectedFolderIds)
            selectedFolderIds = emptySet()
            onDeleteModeExit()
        },
        isRenameMode = isRenameMode,
        onFolderRename = { archiveId, newName -> viewModel.renameFolder(archiveId, newName) },
        onCancelRename = onRenameModeExit
    )
}
