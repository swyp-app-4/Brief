package com.example.brife.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brife.data.remote.NetworkModule
import com.example.brife.data.repository.OnboardingRepository

@Composable
fun OnboardingInterestRoute(
    onNextClick: () -> Unit = {}
) {
    val repository = OnboardingRepository(NetworkModule.onboardingApiService)
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNextClick()
        }
    }

    OnboardingInterestScreen(
        uiState = uiState,
        onCategoryClick = { categoryId ->
            if (
                uiState.selectedCategoryIds.size < 3 ||
                uiState.selectedCategoryIds.contains(categoryId)
            ) {
                viewModel.toggleCategory(categoryId)
            }
        },
        onSubmitClick = {
            viewModel.submitInterests()
        }
    )
}