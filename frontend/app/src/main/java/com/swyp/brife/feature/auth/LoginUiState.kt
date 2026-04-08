package com.swyp.brife.feature.auth

data class LoginUiState(
    val isLoading: Boolean = false,
    val pendingAccessToken: String? = null,
    val pendingRefreshToken: String? = null,
    val pendingLoginMethod: String = "",
    val isLoginSuccess: Boolean = false,
    val isNewUser: Boolean = false,
    val errorMessage: String? = null,
    val isTermsSuccess: Boolean = false,
    val needTermsAgreement: Boolean = false,
    val termsNavigationNonce: Int = 0
)


