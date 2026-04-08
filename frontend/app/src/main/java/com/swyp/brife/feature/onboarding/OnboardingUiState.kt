package com.swyp.brife.feature.onboarding

import com.swyp.brife.data.model.CategoryResponse
import com.swyp.brife.data.model.SubCategoryResponse


// 대분류
data class OnboardingUiState(
    val isLoading: Boolean = false,
    val categories: List<CategoryResponse> = emptyList(),
    val selectedCategoryIds: List<Long> = emptyList(),
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)


// 소분류
data class OnboardingSubInterestUiState(
    val isLoading: Boolean = false,
    val sections: List<SubCategorySection> = emptyList(),
    val selectedSubCategoryIds: List<Long> = emptyList(),
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class SubCategorySection(
    val categoryId: Long,
    val categoryName: String,
    val subCategories: List<SubCategoryResponse>
)