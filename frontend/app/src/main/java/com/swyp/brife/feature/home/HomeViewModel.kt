package com.swyp.brife.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.brife.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // B: 뒤로가기 후 홈 카드 위치 복원용 — 내비게이션 전반에서 유지됨
    private val _savedPageIndex = MutableStateFlow(0)
    val savedPageIndex: StateFlow<Int> = _savedPageIndex.asStateFlow()

    fun savePageIndex(index: Int) {
        _savedPageIndex.value = index
    }

    fun loadHomeNews() {
        viewModelScope.launch {
            val currentList = _uiState.value.newsList
            // A: 뉴스가 이미 있으면 리스트를 날리지 않고 백그라운드 갱신
            //    → pageCount가 1로 내려가지 않아 pager 클램핑 방지
            //    뉴스가 없을 때만 isLoading=true로 스켈레톤 표시
            if (currentList.isEmpty()) {
                _uiState.value = HomeUiState(isLoading = true)
            }
            homeRepository.getHomeNews()
                .onSuccess { newsList ->
                    _uiState.value = HomeUiState(newsList = newsList, isLoading = false)
                }
                .onFailure { e ->
                    // A: 실패 시에도 기존 리스트 유지, errorMessage만 갱신
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
        }
    }
}
