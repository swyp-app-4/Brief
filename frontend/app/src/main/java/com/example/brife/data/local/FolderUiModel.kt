package com.example.brife.data.local

data class BookmarkFolderUiModel(
    val id: Long,
    val name: String,
    val newsCount: Int = 0,
    val isSelected: Boolean = false,
    val isFavorite: Boolean = false
)