package com.swyp.brife.feature.archive

import com.swyp.brife.data.model.ArchiveFolderResponse
import com.swyp.brife.data.model.ArchiveItemResponse

data class ArchiveUiState(
    val folders: List<ArchiveFolderUiModel> = emptyList(),
    val favoriteArchiveId: Long = 0L,
    val favoriteItemCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val isSearchLoading: Boolean = false,
    val searchFolders: List<ArchiveFolderResponse> = emptyList(),
    val searchItems: List<ArchiveItemResponse> = emptyList(),
    val searchNewsItems: List<ArchiveNewsItem> = emptyList(),
    val hasSearchCompleted: Boolean = false,
    val searchErrorMessage: String? = null,
    val recentSearchQueries: List<String> = emptyList()
)
