package com.swyp.brife.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.brife.data.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class OnboardingViewModel(
    private val repository: OnboardingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.getCategories()
                .onSuccess { categories ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categories = categories
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message
                    )
                }
        }
    }

    fun toggleCategory(categoryId: Long) {
        val current = _uiState.value.selectedCategoryIds.toMutableList()

        if (current.contains(categoryId)) {
            current.remove(categoryId)
        } else {
            current.add(categoryId)
        }

        _uiState.value = _uiState.value.copy(
            selectedCategoryIds = current
        )
    }

    fun submitInterests() {
        val selectedIds = _uiState.value.selectedCategoryIds
        Log.d("OnboardingVM", "submitInterests called: $selectedIds")
        if (selectedIds.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "관심사를 1개 이상 선택해주세요."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                errorMessage = null
            )

            repository.saveInterests(selectedIds)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        isSuccess = true
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = throwable.message
                    )
                }
        }
    }
}