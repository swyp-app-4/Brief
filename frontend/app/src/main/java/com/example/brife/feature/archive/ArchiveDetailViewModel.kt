package com.example.brife.feature.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brife.R
import com.example.brife.data.repository.ArchiveRepository
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
                    // TODO: 2차 연동 시 GET /news/{id} 호출로 실제 제목/요약 채우기
                    _newsItems.value = items.map { item ->
                        ArchiveNewsItem(
                            title = "",
                            summary = "뉴스 #${item.contentId}",
                            time = item.savedAt,
                            company = "",
                            imageUrl = R.drawable.homescreen_bg,
                            newsId = item.contentId
                        )
                    }
                }
                .onFailure {
                    _newsItems.value = emptyList()
                }
            _isLoading.value = false
        }
    }
}
