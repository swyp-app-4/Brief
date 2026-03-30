package com.example.brife.feature.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brife.data.repository.ArchiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArchiveViewModel(
    private val repository: ArchiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

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
}
