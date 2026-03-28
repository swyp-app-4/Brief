package com.example.brife.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.brife.data.local.SearchHistoryLocalStorage
import com.example.brife.data.repository.ExploreRepository

class ExploreViewModelFactory(
    private val searchHistoryStorage: SearchHistoryLocalStorage,
    private val exploreRepository: ExploreRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExploreViewModel::class.java)) {
            return ExploreViewModel(searchHistoryStorage, exploreRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
