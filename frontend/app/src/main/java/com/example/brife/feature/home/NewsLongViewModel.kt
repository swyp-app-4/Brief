package com.example.brife.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brife.data.local.BookmarkFolderUiModel
import com.example.brife.data.model.NewsDetailSection
import com.example.brife.data.repository.ArchiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsLongViewModel(
    private val archiveRepository: ArchiveRepository
) : ViewModel() {

    private val _folders = MutableStateFlow<List<BookmarkFolderUiModel>>(emptyList())
    val folders: StateFlow<List<BookmarkFolderUiModel>> = _folders.asStateFlow()

    private val _isSavingFolders = MutableStateFlow(false)
    val isSavingFolders: StateFlow<Boolean> = _isSavingFolders.asStateFlow()

    private val _sections = MutableStateFlow<List<NewsDetailSection>>(emptyList())
    val sections: StateFlow<List<NewsDetailSection>> = _sections.asStateFlow()

    private val _groupName = MutableStateFlow("")
    val groupName: StateFlow<String> = _groupName.asStateFlow()

    private val _categoryName = MutableStateFlow("")
    val categoryName: StateFlow<String> = _categoryName.asStateFlow()

    private val _summaryPoints = MutableStateFlow<List<String>>(emptyList())
    val summaryPoints: StateFlow<List<String>> = _summaryPoints.asStateFlow()

    private val _articleCount = MutableStateFlow(0)
    val articleCount: StateFlow<Int> = _articleCount.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _isSectionsLoading = MutableStateFlow(false)
    val isSectionsLoading: StateFlow<Boolean> = _isSectionsLoading.asStateFlow()

    private val _sectionsError = MutableStateFlow(false)
    val sectionsError: StateFlow<Boolean> = _sectionsError.asStateFlow()

    fun loadFolders(preselectedArchiveId: Long? = null, newsId: Long? = null) {
        viewModelScope.launch {
            Log.d(
                "NewsLongViewModel",
                "loadFolders start: preselectedArchiveId=$preselectedArchiveId, newsId=$newsId"
            )
            fetchBookmarkFolders(preselectedArchiveId, newsId)
                .onSuccess { mappedFolders ->
                    _folders.value = mappedFolders
                    Log.d(
                        "NewsLongViewModel",
                        "loadFolders success: count=${mappedFolders.size}"
                    )
                }
                .onFailure { error ->
                    Log.e("NewsLongViewModel", "loadFolders failed: ${error.message}", error)
                }
        }
    }

    fun loadSections(newsId: Long) {
        viewModelScope.launch {
            _isSectionsLoading.value = true
            _sectionsError.value = false
            archiveRepository.getNewsDetail(newsId)
                .onSuccess { detail ->
                    _sections.value = detail.sections
                    _groupName.value = detail.groupName
                    _categoryName.value = detail.categoryName
                    _summaryPoints.value = detail.summaryList
                    _articleCount.value = detail.sourceCount
                    _title.value = detail.title
                    _isSectionsLoading.value = false
                }
                .onFailure { error ->
                    Log.e("NewsLongViewModel", "loadSections failed: newsId=$newsId", error)
                    _isSectionsLoading.value = false
                    _sectionsError.value = true
                }
        }
    }

    fun saveToFolders(
        newsId: Long,
        targetFolders: List<BookmarkFolderUiModel>,
        onComplete: (Boolean, Boolean) -> Unit = { _, _ -> }
    ) {
        if (_isSavingFolders.value) {
            onComplete(false, _folders.value.any { it.isSelected })
            return
        }

        viewModelScope.launch {
            _isSavingFolders.value = true

            val currentFoldersById = _folders.value.associateBy { it.id }
            val currentSelectedFolders = currentFoldersById.values.filter { it.isSelected }
            val currentSelectedIds = currentSelectedFolders.map { it.id }.toSet()
            val targetSelectedIds = targetFolders
                .filter { it.isSelected }
                .map { it.id }
                .toSet()

            val foldersToAdd = targetFolders.filter { folder ->
                folder.isSelected && folder.id !in currentSelectedIds
            }
            val foldersToRemove = currentSelectedFolders.filter { folder ->
                folder.id !in targetSelectedIds
            }

            var hasFailure = false

            foldersToAdd.forEach { folder ->
                val result = if (folder.isFavorite) {
                    archiveRepository.addToFavorites(newsId)
                } else {
                    archiveRepository.addToFolder(folder.id, newsId)
                }
                if (result.isFailure) {
                    hasFailure = true
                    Log.e(
                        "NewsLongViewModel",
                        "addToFolder failed: archiveId=${folder.id}, error=${result.exceptionOrNull()?.message}"
                    )
                }
            }

            foldersToRemove.forEach { folder ->
                val archiveItemId = folder.archiveItemId
                if (archiveItemId == null || folder.id == 0L) {
                    hasFailure = true
                    Log.e(
                        "NewsLongViewModel",
                        "removeFromFolder skipped: archiveId=${folder.id}, itemId=$archiveItemId"
                    )
                    return@forEach
                }

                val result = archiveRepository.deleteArchiveItem(folder.id, archiveItemId)
                if (result.isFailure) {
                    hasFailure = true
                    Log.e(
                        "NewsLongViewModel",
                        "removeFromFolder failed: archiveId=${folder.id}, itemId=$archiveItemId, error=${result.exceptionOrNull()?.message}"
                    )
                }
            }

            fetchBookmarkFolders(newsId = newsId)
                .onSuccess { refreshedFolders ->
                    _folders.value = refreshedFolders
                }
                .onFailure { error ->
                    hasFailure = true
                    Log.e("NewsLongViewModel", "reloadFolders failed after save", error)
                }

            _isSavingFolders.value = false
            onComplete(!hasFailure, _folders.value.any { it.isSelected })
        }
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch {
            Log.d("NewsLongViewModel", "createFolder: name=$folderName")
            archiveRepository.createFolder(folderName)
                .onSuccess { newFolder ->
                    _folders.value = _folders.value + BookmarkFolderUiModel(
                        id = newFolder.archiveId,
                        name = newFolder.folderName,
                        newsCount = 0,
                        isSelected = true,
                        isFavorite = false
                    )
                }
                .onFailure { error ->
                    Log.e("NewsLongViewModel", "createFolder failed: ${error.message}", error)
                }
        }
    }

    private suspend fun fetchBookmarkFolders(
        preselectedArchiveId: Long? = null,
        newsId: Long? = null
    ): Result<List<BookmarkFolderUiModel>> {
        return archiveRepository.getFolders().map { archiveFolders ->
            val savedArchiveIds = mutableSetOf<Long>()
            val archiveItemIdsByArchiveId = mutableMapOf<Long, Long>()

            when {
                preselectedArchiveId != null -> {
                    savedArchiveIds += preselectedArchiveId
                }

                newsId != null -> {
                    archiveFolders.forEach { folder ->
                        val savedItem = archiveRepository.getItems(folder.archiveId)
                            .getOrNull()
                            .orEmpty()
                            .firstOrNull { it.contentId == newsId }

                        if (savedItem != null) {
                            savedArchiveIds += folder.archiveId
                            archiveItemIdsByArchiveId[folder.archiveId] = savedItem.id
                        }
                    }
                }
            }

            val mappedFolders = archiveFolders
                .sortedByDescending { it.isFavorite }
                .map { folder ->
                    BookmarkFolderUiModel(
                        id = folder.archiveId,
                        name = folder.folderName,
                        newsCount = folder.itemCount,
                        isSelected = folder.archiveId in savedArchiveIds,
                        isFavorite = folder.isFavorite,
                        archiveItemId = archiveItemIdsByArchiveId[folder.archiveId]
                    )
                }

            if (mappedFolders.none { it.isFavorite }) {
                listOf(
                    BookmarkFolderUiModel(
                        id = 0L,
                        name = "즐겨찾기",
                        newsCount = 0,
                        isSelected = false,
                        isFavorite = true
                    )
                ) + mappedFolders
            } else {
                mappedFolders
            }
        }
    }
}
