package com.swyp.brife.feature.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swyp.brife.data.repository.ArchiveRepository

class ArchiveDetailViewModelFactory(
    private val archiveId: Long,
    private val isFavorite: Boolean,
    private val repository: ArchiveRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArchiveDetailViewModel::class.java)) {
            return ArchiveDetailViewModel(archiveId, isFavorite, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
