package com.swyp.brife.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swyp.brife.data.repository.OnboardingRepository

class OnboardingSubInterestViewModelFactory(
    private val repository: OnboardingRepository,
    private val selectedParentCategoryIds: List<Long>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingSubInterestViewModel::class.java)) {
            return OnboardingSubInterestViewModel(
                repository = repository,
                selectedParentCategoryIds = selectedParentCategoryIds
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}