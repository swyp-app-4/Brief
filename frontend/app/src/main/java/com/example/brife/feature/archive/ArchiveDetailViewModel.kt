package com.example.brife.feature.archive

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brife.feature.home.LongFormImageProvider
import com.example.brife.data.repository.ArchiveRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArchiveDetailViewModel(
    private val archiveId: Long,
    private val repository: ArchiveRepository
) : ViewModel() {

    private val _newsItems = MutableStateFlow<List<ArchiveNewsItem>>(emptyList())
    val newsItems: StateFlow<List<ArchiveNewsItem>> = _newsItems.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadItems()
    }

    fun loadItems(sort: String = "LATEST") {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getItems(archiveId, sort)
                .onSuccess { items ->
                    Log.d("ArchiveDetailVM", "아이템 ${items.size}개 로드 → 뉴스 상세 병렬 조회")
                    val mapped = items.mapIndexed { index, item ->
                        async {
                            val detail = repository.getNewsDetail(item.contentId).getOrNull()
                            if (detail != null) {
                                ArchiveNewsItem(
                                    archiveItemId = item.id,
                                    title = detail.title,
                                    summary = "",
                                    time = detail.publishedDate,
                                    company = "${detail.sourceCount}개 언론사",
                                    imageUrl = LongFormImageProvider.getStableImageRes(
                                        detail.groupName,
                                        index
                                    ),
                                    newsId = item.contentId
                                )
                            } else {
                                // 뉴스 상세 조회 실패 시 최소 정보로 표시
                                Log.w("ArchiveDetailVM", "뉴스 상세 조회 실패: contentId=${item.contentId}")
                                ArchiveNewsItem(
                                    archiveItemId = item.id,
                                    title = "뉴스 #${item.contentId}",
                                    summary = "",
                                    time = item.savedAt,
                                    company = "",
                                    imageUrl = LongFormImageProvider.getStableImageRes("", index),
                                    newsId = item.contentId
                                )
                            }
                        }
                    }.awaitAll()
                    _newsItems.value = mapped
                }
                .onFailure { e ->
                    Log.e("ArchiveDetailVM", "아이템 로드 실패: ${e.message}")
                    _newsItems.value = emptyList()
                }
            _isLoading.value = false
        }
    }

    fun deleteItems(selectedIds: Set<Long>) {
        viewModelScope.launch {
            Log.d("ArchiveDetail", "삭제 요청 selectedIds=$selectedIds, archiveId=$archiveId")
            Log.d(
                "ArchiveDetail",
                "현재 목록=${_newsItems.value.map { "archiveItemId=${it.archiveItemId}, newsId=${it.newsId}, title=${it.title}" }}"
            )

            selectedIds.forEach { itemId ->
                val result = repository.deleteArchiveItem(archiveId, itemId)

                if (result.isSuccess) {
                    _newsItems.value = _newsItems.value.filter { it.archiveItemId != itemId }
                    Log.d("ArchiveDetail", "삭제 성공: itemId=$itemId")
                } else {
                    Log.e("ArchiveDetail", "삭제 실패: itemId=$itemId, error=${result.exceptionOrNull()}")
                }
            }
        }
    }
}
