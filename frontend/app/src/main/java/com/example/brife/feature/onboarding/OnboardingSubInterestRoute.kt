package com.example.brife.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.local.OnboardingLocalStorage
import com.example.brife.data.remote.NetworkModule
import com.example.brife.data.repository.OnboardingRepository

@Composable
fun OnboardingSubInterestRoute(
    selectedParentCategoryIds: List<Long>,
    onNextClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = OnboardingRepository(
        api = NetworkModule.onboardingApiService,
        localStorage = OnboardingLocalStorage(context),
        authLocalStorage = AuthLocalStorage(context)
    )

    val viewModel: OnboardingSubInterestViewModel = viewModel(
        factory = OnboardingSubInterestViewModelFactory(
            repository = repository,
            selectedParentCategoryIds = selectedParentCategoryIds
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNextClick()
        }
    }

    OnboardingSubInterestScreen(
        uiState = uiState,
        onSubCategoryClick = { subCategoryId ->
            viewModel.toggleSubCategory(subCategoryId)
        },
        onSkipClick = {
            onNextClick()
        },
        onSubmitClick = {
            viewModel.submitSubInterests()
        }
    )
}