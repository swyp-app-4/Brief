package com.example.brife.feature.archive

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brife.data.repository.ArchiveRepository
import com.example.brife.feature.home.LongFormImageProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArchiveDetailViewModel(
    private val archiveId: Long,
    private val isFavorite: Boolean,
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
                    val mapped = items.map { item ->
                        async {
                            val detail = repository.getNewsDetail(item.contentId).getOrNull()
                            if (detail != null) {
                                ArchiveNewsItem(
                                    archiveItemId = item.id,
                                    title = detail.title.ifBlank { "뉴스 #${item.contentId}" },
                                    summary = "",
                                    time = detail.publishedDate.ifBlank { item.savedAt.ifBlank { "-" } },
                                    company = "${detail.sourceCount}개 언론사",
                                    category = detail.groupName,
                                    subCategory = detail.categoryName,
                                    imageUrl = LongFormImageProvider.getStableImageRes(
                                        detail.groupName,
                                        detail.categoryName,
                                        item.contentId
                                    ),
                                    newsId = item.contentId
                                )
                            } else {
                                ArchiveNewsItem(
                                    archiveItemId = item.id,
                                    title = "뉴스 #${item.contentId}",
                                    summary = "",
                                    time = item.savedAt,
                                    company = "",
                                    category = "",
                                    subCategory = "",
                                    imageUrl = LongFormImageProvider.getStableImageRes("", "", item.contentId),
                                    newsId = item.contentId
                                )
                            }
                        }
                    }.awaitAll()
                    _newsItems.value = mapped
                }
                .onFailure { e ->
                    _newsItems.value = emptyList()
                }
            _isLoading.value = false
        }
    }

    fun deleteItems(
        selectedIds: Set<Long>,
        onCompleted: (List<Long>) -> Unit = {}
    ) {
        viewModelScope.launch {

            val deletedNewsIds = mutableListOf<Long>()
            selectedIds.forEach { itemId ->
                val newsId = _newsItems.value.firstOrNull { it.archiveItemId == itemId }?.newsId
                val result = repository.deleteArchiveItem(archiveId, itemId)

                if (result.isSuccess) {
                    if (newsId != null) {
                        deletedNewsIds += newsId
                    }
                    _newsItems.value = _newsItems.value.filter { it.archiveItemId != itemId }
                } else {
                }
            }

            onCompleted(deletedNewsIds)
        }
    }
}
