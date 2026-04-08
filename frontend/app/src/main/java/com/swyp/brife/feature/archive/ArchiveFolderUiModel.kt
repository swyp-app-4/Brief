package com.swyp.brife.feature.archive

data class ArchiveFolderUiModel(
    val archiveId: Long,
    val folderName: String,
    val itemCount: Int = 0,
    val isFavorite: Boolean = false
)
