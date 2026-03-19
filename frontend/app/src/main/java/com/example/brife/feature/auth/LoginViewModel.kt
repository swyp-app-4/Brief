package com.example.brife.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brife.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun loginWithKakao(accessToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.loginWithKakao(accessToken)
                .onSuccess { response ->
                    handleLoginSuccess(
                        accessToken = response.accessToken,
                        isNewUser = response.isNewUser
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message
                    )
                }
        }
    }

    fun loginWithNaver(accessToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.loginWithNaver(accessToken)
                .onSuccess { response ->
                    handleLoginSuccess(
                        accessToken = response.accessToken,
                        isNewUser = response.isNewUser
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message
                    )
                }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.loginWithGoogle(idToken)
                .onSuccess { response ->
                    handleLoginSuccess(
                        accessToken = response.accessToken,
                        isNewUser = response.isNewUser
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message
                    )
                }
        }
    }

    private fun handleLoginSuccess(
        accessToken: String,
        isNewUser: Boolean
    ) {
        if (isNewUser) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isNewUser = true,
                showTermsBottomSheet = true,
                pendingAccessToken = accessToken
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isLoginSuccess = true,
                isNewUser = false
            )
        }
    }

    fun agreeTerms() {
        val accessToken = _uiState.value.pendingAccessToken ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.agreeTerms(accessToken)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showTermsBottomSheet = false,
                        isTermsSuccess = true
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message
                    )
                }
        }
    }

    fun dismissTermsBottomSheet() {
        _uiState.value = _uiState.value.copy(
            showTermsBottomSheet = false
        )
    }
}