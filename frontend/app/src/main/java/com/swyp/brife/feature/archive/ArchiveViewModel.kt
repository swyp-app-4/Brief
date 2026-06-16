package com.swyp.brife.feature.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.brife.data.repository.ArchiveRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArchiveViewModel(
    private val repository: ArchiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val maxRecentSearchCount = 10

    init {
        loadFolders()
    }

    fun loadFolders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repository.getFolders()
                .onSuccess { all ->
                    val favorite = all.find { it.isFavorite }
                    val regular = all.filter { !it.isFavorite }
                    _uiState.value = _uiState.value.copy(
                        folders = regular,
                        favoriteArchiveId = favorite?.archiveId ?: 0L,
                        favoriteItemCount = favorite?.itemCount ?: 0,
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message
                    )
                }
        }
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch {
            repository.createFolder(folderName)
                .onSuccess { newFolder ->
                    _uiState.value = _uiState.value.copy(
                        folders = _uiState.value.folders + newFolder
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = it.message)
                }
        }
    }

    fun deleteFolders(archiveIds: Set<Long>) {
        viewModelScope.launch {
            archiveIds.forEach { id -> repository.deleteFolder(id) }
            _uiState.value = _uiState.value.copy(
                folders = _uiState.value.folders.filter { it.archiveId !in archiveIds }
            )
        }
    }

    fun renameFolder(archiveId: Long, newName: String) {
        viewModelScope.launch {
            repository.renameFolder(archiveId, newName)
                .onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(
                        folders = _uiState.value.folders.map {
                            if (it.archiveId == archiveId) updated else it
                        }
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = it.message)
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(searchQuery = query)

        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            resetSearchResults()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            searchArchive(trimmed)
        }
    }

    fun clearSearchQuery() {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(searchQuery = "")
        resetSearchResults()
    }

    fun submitSearch() {
        searchJob?.cancel()
        val trimmed = _uiState.value.searchQuery.trim()
        if (trimmed.isBlank()) {
            resetSearchResults()
            return
        }
        searchJob = viewModelScope.launch {
            searchArchive(trimmed)
        }
    }

    fun clearRecentSearches() {
        _uiState.value = _uiState.value.copy(recentSearchQueries = emptyList())
    }

    fun removeRecentSearch(query: String) {
        _uiState.value = _uiState.value.copy(
            recentSearchQueries = _uiState.value.recentSearchQueries.filterNot { it == query }
        )
    }

    fun onRecentSearchClick(query: String) {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob = viewModelScope.launch {
            searchArchive(query.trim())
        }
    }

    private suspend fun searchArchive(query: String) {
        if (query.isBlank()) {
            resetSearchResults()
            return
        }

        if (isSpecialCharOnly(query)) {
            _uiState.value = _uiState.value.copy(
                isSearchLoading = false,
                searchFolders = emptyList(),
                searchItems = emptyList(),
                searchErrorMessage = "SPECIAL_CHAR_ONLY"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isSearchLoading = true,
            searchErrorMessage = null
        )

        repository.searchArchive(query)
            .onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isSearchLoading = false,
                    searchFolders = response.folders,
                    searchItems = response.items,
                    searchErrorMessage = null,
                    recentSearchQueries = addRecentQuery(query)
                )
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(
                    isSearchLoading = false,
                    searchFolders = emptyList(),
                    searchItems = emptyList(),
                    searchErrorMessage = it.message
                )
            }
    }

    private fun resetSearchResults() {
        _uiState.value = _uiState.value.copy(
            isSearchLoading = false,
            searchFolders = emptyList(),
            searchItems = emptyList(),
            searchErrorMessage = null
        )
    }

    private fun addRecentQuery(query: String): List<String> {
        return (listOf(query) + _uiState.value.recentSearchQueries.filterNot { it == query })
            .take(maxRecentSearchCount)
    }

    private fun isSpecialCharOnly(query: String): Boolean {
        val specialChars = "!@#\$%^&*()+=[]{}|;':\",./<>?\\`~"
        val compact = query.filterNot { it.isWhitespace() }
        return compact.isNotEmpty() && compact.all { it in specialChars }
    }
}
