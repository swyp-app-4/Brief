package com.example.brife.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun LoginTermsRoute(
    viewModel: LoginViewModel,
    onNavigateToOnboarding: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isTermsSuccess) {
        if (uiState.isTermsSuccess) {
            onNavigateToOnboarding()
        }
    }

    LoginTermsScreen(
        isLoading = uiState.isLoading,
        onNext = {
            viewModel.agreeTerms()
        },
        onBack = onBackClick
    )
}