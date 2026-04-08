package com.swyp.brife.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swyp.brife.data.local.AuthLocalStorage
import com.swyp.brife.data.local.OnboardingLocalStorage
import com.swyp.brife.data.remote.NetworkModule
import com.swyp.brife.data.repository.OnboardingRepository

@Composable
fun OnboardingInterestRoute(
    onNextClick: (List<Long>) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = OnboardingRepository(
        api = NetworkModule.onboardingApiService,
        localStorage = OnboardingLocalStorage(context),
        authLocalStorage = AuthLocalStorage(context)
    )
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNextClick(uiState.selectedCategoryIds)
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