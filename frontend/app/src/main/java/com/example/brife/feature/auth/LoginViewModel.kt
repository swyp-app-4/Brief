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

    private fun handleLoginSuccess(
        accessToken: String,
        refreshToken: String,
        isNewUser: Boolean
    ) {
        if (isNewUser) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isNewUser = true,
                pendingAccessToken = accessToken,
                pendingRefreshToken = refreshToken,
                needTermsAgreement = true
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isLoginSuccess = true,
                isNewUser = false,
                pendingAccessToken = accessToken,
                pendingRefreshToken = refreshToken
            )
        }
    }

    fun agreeTerms() {
        // 코루틴 진입 전에 가드 — 연타 시 중복 요청 방지
        if (_uiState.value.isLoading) return
        val accessToken = _uiState.value.pendingAccessToken ?: return
        val refreshToken = _uiState.value.pendingRefreshToken

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            repository.agreeTerms(accessToken)
                .onSuccess {
                    authLocalStorage.saveAccessToken(accessToken)
                    if (refreshToken != null) {
                        authLocalStorage.saveRefreshToken(refreshToken)
                    }
                    val loginMethod = _uiState.value.pendingLoginMethod
                    if (loginMethod.isNotEmpty()) {
                        authLocalStorage.saveLoginMethod(loginMethod)
                    }

                    // 비로그인 온보딩에서 로컬에만 저장된 관심사를 서버에 동기화
                    // (온보딩 시점에는 토큰이 없어 API 전송이 스킵됐기 때문)
                    val categoryIds = onboardingLocalStorage.getSelectedSubCategoryIds()
                    val groupIds = onboardingLocalStorage.getSelectedCategoryIds()
                    if (categoryIds.isNotEmpty() || groupIds.isNotEmpty()) {
                        val interestResult = userRepository.updateInterests(categoryIds, groupIds)
                        if (interestResult.isFailure) {
                            // PUT 실패 → 관심사 없이 추천 API가 호출되는 것을 막기 위해 홈 이동 차단
                            val errMsg = interestResult.exceptionOrNull()?.message ?: "알 수 없는 오류"
                            Log.e("LoginViewModel", "관심사 저장 실패: $errMsg")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "관심사 저장 실패 ($errMsg)"
                            )
                            return@onSuccess  // isTermsSuccess 설정 없이 리턴 → 홈 이동 안 함
                        }
                        Log.d("LoginViewModel", "관심사 저장 성공: categoryIds=$categoryIds, groupIds=$groupIds")
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        needTermsAgreement = false,
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

    fun consumeTermsNavigation() {
        _uiState.value = _uiState.value.copy(
            needTermsAgreement = false
        )
    }
}

