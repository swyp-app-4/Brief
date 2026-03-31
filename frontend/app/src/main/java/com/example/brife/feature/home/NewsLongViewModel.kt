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

    // preselectedArchiveId: ArchiveDetail에서 진입 시 해당 폴더를 isSelected=true로 초기화
    // newsId: Home/Explore 진입 시 이미 저장된 폴더를 자동 감지하여 isSelected=true로 설정
    fun loadFolders(preselectedArchiveId: Long? = null, newsId: Long? = null) {
        viewModelScope.launch {
            Log.d("NewsLongViewModel", "폴더 목록 로드 시작 (preselect=$preselectedArchiveId, newsId=$newsId)")
            archiveRepository.getFolders()
                .onSuccess { archiveFolders ->
                    Log.d("NewsLongViewModel", "폴더 목록 로드 성공: ${archiveFolders.size}개")

                    // 이미 저장된 폴더 ID 집합 결정
                    val savedArchiveIds: Set<Long> = when {
                        // ArchiveDetail 진입: 바로 해당 폴더 ID 사용
                        preselectedArchiveId != null -> setOf(preselectedArchiveId)
                        // Home/Explore 진입: 각 폴더 아이템을 조회하여 해당 newsId 포함 여부 확인
                        newsId != null -> archiveFolders.mapNotNull { folder ->
                            val result = archiveRepository.getItems(folder.archiveId)
                            if (result.isSuccess && result.getOrNull()?.any { it.contentId == newsId } == true) {
                                folder.archiveId
                            } else null
                        }.toSet()
                        else -> emptySet()
                    }

                    val mapped = archiveFolders
                        .sortedByDescending { it.isFavorite }
                        .map { folder ->
                            BookmarkFolderUiModel(
                                id = folder.archiveId,
                                name = folder.folderName,
                                newsCount = folder.itemCount,
                                isSelected = folder.archiveId in savedArchiveIds,
                                isFavorite = folder.isFavorite
                            )
                        }
                    // 즐겨찾기 폴더가 없으면 최상단에 폴백으로 추가
                    _folders.value = if (mapped.none { it.isFavorite }) {
                        listOf(
                            BookmarkFolderUiModel(
                                id = 0L,
                                name = "즐겨찾기",
                                newsCount = 0,
                                isSelected = false,
                                isFavorite = true
                            )
                        ) + mapped
                    } else {
                        mapped
                    }
                }
                .onFailure { e ->
                    Log.e("NewsLongViewModel", "폴더 목록 로드 실패: ${e.message}")
                }
        }
    }

    fun loadSections(newsId: Long) {
        viewModelScope.launch {
            archiveRepository.getNewsDetail(newsId)
                .onSuccess { detail ->
                    _sections.value = detail.sections
                    _groupName.value = detail.groupName
                    _categoryName.value = detail.categoryName
                    _summaryPoints.value = detail.summaryList
                    _articleCount.value = detail.sourceCount
                }
                .onFailure { e ->
                    Log.e("NewsLongViewModel", "섹션 로드 실패: ${e.message}")
                }
        }
    }

    fun saveToFolders(newsId: Long, selectedFolders: List<BookmarkFolderUiModel>) {
        if (selectedFolders.isEmpty()) return
        viewModelScope.launch {
            Log.d("NewsLongViewModel", "저장 시작: newsId=$newsId, 선택된 폴더 수=${selectedFolders.size}")
            selectedFolders.forEach { folder ->
                Log.d("NewsLongViewModel", "저장 요청: archiveId=${folder.id}, isFavorite=${folder.isFavorite}")
                val result = if (folder.isFavorite) {
                    archiveRepository.addToFavorites(newsId)
                } else {
                    archiveRepository.addToFolder(folder.id, newsId)
                }
                result
                    .onSuccess {
                        Log.d("NewsLongViewModel", "저장 성공: archiveId=${folder.id}")
                    }
                    .onFailure { e ->
                        Log.e("NewsLongViewModel", "저장 실패: archiveId=${folder.id}, error=${e.message}")
                    }
            }
        }
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch {
            Log.d("NewsLongViewModel", "폴더 생성: name=$folderName")
            archiveRepository.createFolder(folderName)
                .onSuccess { newFolder ->
                    Log.d("NewsLongViewModel", "폴더 생성 성공: archiveId=${newFolder.archiveId}")
                    // 새로 생성된 폴더를 자동 선택 상태로 목록에 추가
                    _folders.value = _folders.value + BookmarkFolderUiModel(
                        id = newFolder.archiveId,
                        name = newFolder.folderName,
                        newsCount = 0,
                        isSelected = true,
                        isFavorite = false
                    )
                }
                .onFailure { e ->
                    Log.e("NewsLongViewModel", "폴더 생성 실패: ${e.message}")
                }
        }
    }
}
