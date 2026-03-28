package com.example.brife.feature.archive

data class ArchiveUiState(
    val folders: List<ArchiveFolderUiModel> = emptyList(),
    val favoriteArchiveId: Long = 0L,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
