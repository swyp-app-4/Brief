package com.example.brife.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brife.data.remote.NetworkModule
import com.example.brife.data.repository.AuthRepository

@Composable
fun LoginRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val repository = AuthRepository(NetworkModule.authApiService)
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onNavigateToHome()
        }
    }

    LaunchedEffect(uiState.showTermsBottomSheet, uiState.isLoading) {
        if (!uiState.showTermsBottomSheet &&
            !uiState.isLoading &&
            uiState.isNewUser &&
            uiState.pendingAccessToken != null
        ) {
            onNavigateToOnboarding()
        }
    }

    LaunchedEffect(uiState.isTermsSuccess) {
        if (uiState.isTermsSuccess) {
            onNavigateToOnboarding()
        }
    }

    LoginScreen(
        uiState = uiState,
        onKakaoClick = {
            // 실제로는 카카오 SDK 토큰 받아와야 함
            viewModel.loginWithKakao("kakao_sdk_access_token")
        },
        onNaverClick = {
            viewModel.loginWithNaver("naver_sdk_access_token")
        },
        onGoogleClick = {
            viewModel.loginWithGoogle("google_sdk_id_token")
        },
        onDismissTerms = {
            viewModel.dismissTermsBottomSheet()
        },
        onAgreeTerms = {
            viewModel.agreeTerms()
        }
    )
}