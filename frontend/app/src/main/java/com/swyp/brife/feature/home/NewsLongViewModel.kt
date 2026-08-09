package com.swyp.brife.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.brife.data.local.BookmarkFolderUiModel
import com.swyp.brife.data.model.NewsDetailSection
import com.swyp.brife.data.model.NewsListItem
import com.swyp.brife.data.model.NewsSourceItemResponse
import com.swyp.brife.data.repository.ArchiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsLongViewModel(
    private val archiveRepository: ArchiveRepository
) : ViewModel() {

    private val _folders = MutableStateFlow<List<BookmarkFolderUiModel>>(emptyList())
    val folders: StateFlow<List<BookmarkFolderUiModel>> = _folders.asStateFlow()

    // 서버에서 실제로 저장이 확인된 폴더 ID 집합 (새로 생성만 된 폴더는 포함하지 않음)
    private val _savedFolderIds = MutableStateFlow<Set<Long>>(emptySet())

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

    private val _sources = MutableStateFlow<List<NewsSourceItemResponse>>(emptyList())
    val sources: StateFlow<List<NewsSourceItemResponse>> = _sources.asStateFlow()

    private val _isSourcesLoading = MutableStateFlow(false)
    val isSourcesLoading: StateFlow<Boolean> = _isSourcesLoading.asStateFlow()

    private val _sourcesError = MutableStateFlow(false)
    val sourcesError: StateFlow<Boolean> = _sourcesError.asStateFlow()

    private val _similarNews = MutableStateFlow<List<NewsListItem>>(emptyList())
    val similarNews: StateFlow<List<NewsListItem>> = _similarNews.asStateFlow()

    private val _isSimilarNewsLoading = MutableStateFlow(false)
    val isSimilarNewsLoading: StateFlow<Boolean> = _isSimilarNewsLoading.asStateFlow()

    private val _similarNewsError = MutableStateFlow<String?>(null)
    val similarNewsError: StateFlow<String?> = _similarNewsError.asStateFlow()

    private val _showSourcesBottomSheet = MutableStateFlow(false)
    val showSourcesBottomSheet: StateFlow<Boolean> = _showSourcesBottomSheet.asStateFlow()

    fun loadSources(newsId: Long) {
        viewModelScope.launch {
            _isSourcesLoading.value = true
            _sourcesError.value = false
            archiveRepository.getNewsSources(newsId)
                .onSuccess { list ->
                    _sources.value = list
                    _isSourcesLoading.value = false
                }
                .onFailure {
                    _isSourcesLoading.value = false
                    _sourcesError.value = true
                }
        }
    }

    fun loadSimilarNews(newsId: Long) {
        viewModelScope.launch {
            _isSimilarNewsLoading.value = true
            _similarNewsError.value = null
            _similarNews.value = emptyList()

            try {
                archiveRepository.getSimilarNews(newsId)
                    .onSuccess { list ->
                        _similarNews.value = list
                    }
                    .onFailure { error ->
                        _similarNews.value = emptyList()
                        _similarNewsError.value = error.message
                    }
            } finally {
                _isSimilarNewsLoading.value = false
            }
        }
    }

    fun setShowSourcesBottomSheet(show: Boolean) {
        _showSourcesBottomSheet.value = show
    }

    fun loadFolders(preselectedArchiveId: Long? = null, newsId: Long? = null) {
        viewModelScope.launch {

            fetchBookmarkFolders(preselectedArchiveId, newsId)
                .onSuccess { mappedFolders ->
                    _folders.value = mappedFolders
                    _savedFolderIds.value = mappedFolders.filter { it.isSelected }.map { it.id }.toSet()

                }
                .onFailure { error ->
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

            // _savedFolderIds: 서버에 실제로 저장이 확인된 폴더 ID 집합
            // createFolder 후 _folders.value에 추가된 폴더는 여기에 포함되지 않으므로
            // 새 폴더가 foldersToAdd에 올바르게 포함됨
            val currentSelectedIds = _savedFolderIds.value
            val targetSelectedIds = targetFolders
                .filter { it.isSelected }
                .map { it.id }
                .toSet()

            val foldersToAdd = targetFolders.filter { folder ->
                folder.isSelected && folder.id !in currentSelectedIds
            }
            // 서버 기준으로 저장된 폴더들 중 이번에 해제된 것만 제거 대상으로 삼음
            val currentSelectedFolders = _folders.value.filter { it.id in currentSelectedIds }
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

                }
            }

            foldersToRemove.forEach { folder ->
                val resolvedArchiveId = resolveArchiveId(folder)
                val resolvedArchiveItemId = resolveArchiveItemId(
                    folder = folder,
                    archiveId = resolvedArchiveId,
                    newsId = newsId
                )

                if (resolvedArchiveId == null || resolvedArchiveItemId == null) {
                    hasFailure = true

                    return@forEach
                }

                val result = archiveRepository.deleteArchiveItem(
                    resolvedArchiveId,
                    resolvedArchiveItemId
                )
                if (result.isFailure) {
                    hasFailure = true

                }
            }

            fetchBookmarkFolders(newsId = newsId)
                .onSuccess { refreshedFolders ->
                    _folders.value = refreshedFolders
                    _savedFolderIds.value = refreshedFolders.filter { it.isSelected }.map { it.id }.toSet()
                }
                .onFailure { error ->
                    hasFailure = true
                }

            _isSavingFolders.value = false
            onComplete(!hasFailure, _folders.value.any { it.isSelected })
        }
    }

    fun createFolder(
        folderName: String,
        onSuccess: (BookmarkFolderUiModel) -> Unit = {}
    ) {
        viewModelScope.launch {
            archiveRepository.createFolder(folderName)
                .onSuccess { newFolder ->
                    val createdFolder = BookmarkFolderUiModel(
                        id = newFolder.archiveId,
                        name = newFolder.folderName,
                        newsCount = 0,
                        isSelected = true,  // UI에서 즉시 선택 상태로 표시 (LaunchedEffect 타이밍 방어)
                        isFavorite = false
                        // _savedFolderIds에는 추가하지 않음 — 아직 서버에 저장된 게 아니므로
                        // saveToFolders에서 foldersToAdd에 올바르게 포함됨
                    )
                    _folders.value = _folders.value + createdFolder
                    onSuccess(createdFolder)
                }
                .onFailure { error ->
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

                preselectedArchiveId != null -> {
                    savedArchiveIds += preselectedArchiveId
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

    private suspend fun resolveArchiveId(folder: BookmarkFolderUiModel): Long? {
        if (folder.id != 0L) return folder.id
        if (!folder.isFavorite) return null

        return archiveRepository.getFolders()
            .getOrNull()
            ?.firstOrNull { it.isFavorite }
            ?.archiveId
    }

    private suspend fun resolveArchiveItemId(
        folder: BookmarkFolderUiModel,
        archiveId: Long?,
        newsId: Long
    ): Long? {
        if (archiveId == null) return null
        if (folder.archiveItemId != null) return folder.archiveItemId

        return archiveRepository.getItems(archiveId)
            .getOrNull()
            ?.firstOrNull { it.contentId == newsId }
            ?.id
    }
}
