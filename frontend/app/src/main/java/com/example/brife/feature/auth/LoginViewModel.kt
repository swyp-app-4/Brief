package com.example.brife.feature.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.local.OnboardingLocalStorage
import com.example.brife.data.repository.AuthRepository
import com.example.brife.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository,
    private val authLocalStorage: AuthLocalStorage,
    private val onboardingLocalStorage: OnboardingLocalStorage,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun loginWithKakao(accessToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            _uiState.value = _uiState.value.copy(pendingLoginMethod = "kakao")
            repository.loginWithKakao(accessToken)
                .onSuccess { response ->
                    handleLoginSuccess(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
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

            _uiState.value = _uiState.value.copy(pendingLoginMethod = "naver")
            repository.loginWithNaver(accessToken)
                .onSuccess { response ->
                    handleLoginSuccess(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
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

            _uiState.value = _uiState.value.copy(pendingLoginMethod = "google")
            repository.loginWithGoogle(idToken)
                .onSuccess { response ->
                    handleLoginSuccess(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
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

    fun onExternalLoginError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    private fun handleLoginSuccess(
        accessToken: String,
        refreshToken: String,
        isNewUser: Boolean
    ) {
        if (isNewUser && !authLocalStorage.hasAgreedTerms()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isNewUser = true,
                pendingAccessToken = accessToken,
                pendingRefreshToken = refreshToken,
                needTermsAgreement = true
            )
        } else {
            completeLogin(
                accessToken = accessToken,
                refreshToken = refreshToken,
                persistTermsAgreement = true
            )
        }
    }

    private fun completeLogin(
        accessToken: String,
        refreshToken: String,
        persistTermsAgreement: Boolean
    ) {
        val categoryIds = onboardingLocalStorage.getSelectedSubCategoryIds()
        val groupIds = onboardingLocalStorage.getSelectedCategoryIds()

        if (categoryIds.isEmpty() && groupIds.isEmpty()) {
            persistAuthState(accessToken, refreshToken, persistTermsAgreement)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isLoginSuccess = true,
                isNewUser = false
            )
            return
        }

        viewModelScope.launch {
            val interestResult = userRepository.updateInterests(accessToken, categoryIds, groupIds)
            if (interestResult.isSuccess) {
                Log.d("LoginViewModel", "login sync interests success: categoryIds=$categoryIds, groupIds=$groupIds")
                persistAuthState(accessToken, refreshToken, persistTermsAgreement)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoginSuccess = true,
                    isNewUser = false,
                    errorMessage = null
                )
            } else {
                val errMsg = interestResult.exceptionOrNull()?.message ?: "unknown error"
                Log.e("LoginViewModel", "login sync interests failed: $errMsg")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoginSuccess = false,
                    isNewUser = false,
                    errorMessage = "관심사 동기화 실패 ($errMsg)"
                )
            }
        }
    }

    fun agreeTerms() {
        if (_uiState.value.isLoading) return
        val accessToken = _uiState.value.pendingAccessToken ?: return
        val refreshToken = _uiState.value.pendingRefreshToken.orEmpty()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.agreeTerms(accessToken)
                .onSuccess {
                    completeTermsLogin(accessToken, refreshToken)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message
                    )
                }
        }
    }

    private fun completeTermsLogin(
        accessToken: String,
        refreshToken: String
    ) {
        val categoryIds = onboardingLocalStorage.getSelectedSubCategoryIds()
        val groupIds = onboardingLocalStorage.getSelectedCategoryIds()

        viewModelScope.launch {
            if (categoryIds.isNotEmpty() || groupIds.isNotEmpty()) {
                val interestResult = userRepository.updateInterests(accessToken, categoryIds, groupIds)
                if (interestResult.isFailure) {
                    val errMsg = interestResult.exceptionOrNull()?.message ?: "unknown error"
                    Log.e("LoginViewModel", "terms sync interests failed: $errMsg")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "관심사 저장 실패 ($errMsg)"
                    )
                    return@launch
                }
                Log.d("LoginViewModel", "terms sync interests success: categoryIds=$categoryIds, groupIds=$groupIds")
            }

            persistAuthState(
                accessToken = accessToken,
                refreshToken = refreshToken,
                persistTermsAgreement = true
            )
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                needTermsAgreement = false,
                isTermsSuccess = true
            )
        }
    }

    private fun persistAuthState(
        accessToken: String,
        refreshToken: String,
        persistTermsAgreement: Boolean
    ) {
        if (persistTermsAgreement) {
            authLocalStorage.saveTermsAgreement(true)
        }
        authLocalStorage.saveAccessToken(accessToken)
        if (refreshToken.isNotBlank()) {
            authLocalStorage.saveRefreshToken(refreshToken)
        }
        _uiState.value.pendingLoginMethod.takeIf { it.isNotEmpty() }?.let {
            authLocalStorage.saveLoginMethod(it)
        }
    }

    fun consumeTermsNavigation() {
        _uiState.value = _uiState.value.copy(
            needTermsAgreement = false
        )
    }
}
