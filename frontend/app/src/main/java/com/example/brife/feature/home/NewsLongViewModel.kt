package com.example.brife.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brife.data.local.BookmarkFolderUiModel
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

    fun loadFolders() {
        viewModelScope.launch {
            Log.d("NewsLongViewModel", "폴더 목록 로드 시작")
            archiveRepository.getFolders()
                .onSuccess { archiveFolders ->
                    Log.d("NewsLongViewModel", "폴더 목록 로드 성공: ${archiveFolders.size}개")
                    _folders.value = archiveFolders.map { folder ->
                        BookmarkFolderUiModel(
                            id = folder.archiveId,
                            name = folder.folderName,
                            newsCount = folder.itemCount,
                            isSelected = false,
                            isFavorite = folder.isFavorite
                        )
                    }
                }
                .onFailure { e ->
                    Log.e("NewsLongViewModel", "폴더 목록 로드 실패: ${e.message}")
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
