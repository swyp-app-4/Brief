package com.example.brife.feature.auth

data class LoginUiState(
    val isLoading: Boolean = false,
    val showTermsBottomSheet: Boolean = false,
    val pendingAccessToken: String? = null,
    val isLoginSuccess: Boolean = false,
    val isNewUser: Boolean = false,
    val errorMessage: String? = null,
    val isTermsSuccess: Boolean = false
)