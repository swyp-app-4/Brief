package com.example.brife.feature.onboarding

import com.example.brife.data.model.CategoryResponse

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val categories: List<CategoryResponse> = emptyList(),
    val selectedCategoryIds: List<Long> = emptyList(),
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)