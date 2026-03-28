package com.example.brife.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brife.data.local.shortsampleHomeNews
import com.example.brife.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecommendedNews()
    }

    fun loadRecommendedNews() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            homeRepository.getRecommendedNews()
                .onSuccess { newsList ->
                    _uiState.value = HomeUiState(newsList = newsList, isLoading = false)
                }
                .onFailure {
                    // API 실패 시 mock 데이터로 fallback (비로그인 또는 네트워크 오류)
                    _uiState.value = HomeUiState(newsList = shortsampleHomeNews, isLoading = false)
                }
        }
    }
}
