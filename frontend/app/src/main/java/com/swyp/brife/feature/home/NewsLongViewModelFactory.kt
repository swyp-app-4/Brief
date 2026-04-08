package com.swyp.brife.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swyp.brife.data.repository.ArchiveRepository

class NewsLongViewModelFactory(
    private val archiveRepository: ArchiveRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsLongViewModel::class.java)) {
            return NewsLongViewModel(archiveRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
