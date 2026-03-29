package com.example.brife.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        loadHomeNews()
    }

    fun loadHomeNews() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            homeRepository.getHomeNews()
                .onSuccess { newsList ->
                    Log.d("HomeViewModel", "uiState 반영: ${newsList.size}개")
                    _uiState.value = HomeUiState(newsList = newsList, isLoading = false)
                }
                .onFailure { e ->
                    Log.e("HomeViewModel", "뉴스 로드 실패: ${e.message}", e)
                    _uiState.value = HomeUiState(
                        newsList = emptyList(),
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
        }
    }
}
