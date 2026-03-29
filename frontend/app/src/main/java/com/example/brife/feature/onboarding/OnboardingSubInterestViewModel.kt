package com.example.brife.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brife.data.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingSubInterestViewModel(
    private val repository: OnboardingRepository,
    private val selectedParentCategoryIds: List<Long>
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingSubInterestUiState())
    val uiState: StateFlow<OnboardingSubInterestUiState> = _uiState.asStateFlow()

    init {
        loadSubCategories()
    }

    private fun loadSubCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.getSubCategories(selectedParentCategoryIds)
                .onSuccess { subCategories ->
                    // API가 이미 categoryGroupIds 기준으로 필터링해서 반환하므로 별도 필터 불필요
                    val sections = subCategories
                        .groupBy { it.parentCategoryId }
                        .map { (parentId, items) ->
                            SubCategorySection(
                                categoryId = parentId,
                                categoryName = items.firstOrNull()?.parentCategoryName.orEmpty(),
                                subCategories = items
                            )
                        }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        sections = sections
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

    fun toggleSubCategory(subCategoryId: Long) {
        val current = _uiState.value.selectedSubCategoryIds.toMutableList()

        if (current.contains(subCategoryId)) {
            current.remove(subCategoryId)
        } else {
            current.add(subCategoryId)
        }

        _uiState.value = _uiState.value.copy(
            selectedSubCategoryIds = current
        )
    }

    fun submitSubInterests() {
        val selectedIds = _uiState.value.selectedSubCategoryIds

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                errorMessage = null
            )

            repository.saveSubInterests(
                subCategoryIds = selectedIds,
                parentCategoryIds = selectedParentCategoryIds
            )
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