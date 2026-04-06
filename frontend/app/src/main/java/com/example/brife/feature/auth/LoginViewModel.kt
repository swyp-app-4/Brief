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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface LoginNavigationEvent {
    data object NavigateToHome : LoginNavigationEvent
    data object NavigateToTerms : LoginNavigationEvent
}

class LoginViewModel(
    private val repository: AuthRepository,
    private val authLocalStorage: AuthLocalStorage,
    private val onboardingLocalStorage: OnboardingLocalStorage,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    private val _navigationEvent = Channel<LoginNavigationEvent>(capacity = Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun loginWithKakao(accessToken: String) {
        viewModelScope.launch {
            resetForLoginAttempt("kakao")


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
            resetForLoginAttempt("naver")


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
            resetForLoginAttempt("google")


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

    private fun resetForLoginAttempt(loginMethod: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            pendingLoginMethod = loginMethod,
            pendingAccessToken = null,
            pendingRefreshToken = null,
            isLoginSuccess = false,
            isNewUser = false,
            errorMessage = null,
            isTermsSuccess = false,
            needTermsAgreement = false
        )
    }

    // LoginViewModel.kt

    private fun handleLoginSuccess(
        accessToken: String,
        refreshToken: String,
        isNewUser: Boolean
    ) {
        // 서버가 신규유저라고 하거나, 로컬에 약관 동의 기록이 없는 경우 반드시 약관 화면으로
        val needToAgree = isNewUser || !authLocalStorage.hasAgreedTerms()

        if (needToAgree) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isNewUser = isNewUser, // 서버 응답 값 유지 (이후 온보딩 분기용)
                pendingAccessToken = accessToken,
                pendingRefreshToken = refreshToken,
                needTermsAgreement = true,
                termsNavigationNonce = _uiState.value.termsNavigationNonce + 1
            )
            _navigationEvent.trySend(LoginNavigationEvent.NavigateToTerms)
        } else {
            completeLogin(
                accessToken = accessToken,
                refreshToken = refreshToken,
                persistTermsAgreement = true,
                isNewUser = false
            )
        }
    }

    /**
     * 로그인 완료 처리.
     * ① 토큰 저장(persistAuthState)
     * ② 관심사 동기화(PUT/POST) → 서버 반영 완료 후 NavigateToHome 발송
     *    → 실패해도 로그인은 유지 (홈으로 이동)
     * 이 순서를 지켜야 홈 뉴스 GET이 관심사 PUT보다 먼저 나가는 레이스 컨디션을 방지
     */
    private fun completeLogin(
        accessToken: String,
        refreshToken: String,
        persistTermsAgreement: Boolean,
        isNewUser: Boolean = false
    ) {
        val subCategoryIds = onboardingLocalStorage.getSelectedSubCategoryIds()
        val groupIds = onboardingLocalStorage.getSelectedCategoryIds()

        // ① 토큰 저장 (성공 상태는 동기화 완료 후 설정)
        persistAuthState(accessToken, refreshToken, persistTermsAgreement)

        // ② 관심사 동기화 → 완료 후 홈으로 이동
        viewModelScope.launch {
            if (subCategoryIds.isNotEmpty() || groupIds.isNotEmpty()) {
                if (isNewUser) {
                    // 신규회원: POST /users/me/interests
                    userRepository.saveInterests(accessToken, subCategoryIds, groupIds)
                } else {
                    // 기존회원: PUT /users/me/interests
                    userRepository.updateInterests(accessToken, subCategoryIds, groupIds)
                }
                // 실패해도 로그인은 계속 진행 (best-effort)
            }

            // ③ 동기화 완료 후 홈으로 이동 → 이 시점에 서버에 최신 관심사 반영됨
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isLoginSuccess = true,
                isNewUser = false,
                errorMessage = null
            )
            _navigationEvent.trySend(LoginNavigationEvent.NavigateToHome)
        }
    }

    fun agreeTerms() {
        if (_uiState.value.isLoading) return
        val accessToken = _uiState.value.pendingAccessToken ?: return
        val refreshToken = _uiState.value.pendingRefreshToken.orEmpty()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)


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

    /**
     * 약관 동의 완료 후 처리.
     * ① 토큰 저장
     * ② 관심사 동기화(POST) → 완료 후 isTermsSuccess = true 설정
     *    → 실패해도 로그인 유지 (best-effort)
     * 약관 동의 직후는 항상 신규회원이므로 POST /users/me/interests 사용
     */
    private fun completeTermsLogin(
        accessToken: String,
        refreshToken: String
    ) {
        val subCategoryIds = onboardingLocalStorage.getSelectedSubCategoryIds()
        val groupIds = onboardingLocalStorage.getSelectedCategoryIds()

        // ① 토큰 저장
        persistAuthState(
            accessToken = accessToken,
            refreshToken = refreshToken,
            persistTermsAgreement = true
        )

        // ② 관심사 동기화 → 완료 후 상태 전환
        viewModelScope.launch {
            if (subCategoryIds.isNotEmpty() || groupIds.isNotEmpty()) {
                // 신규회원: POST /users/me/interests
                userRepository.saveInterests(accessToken, subCategoryIds, groupIds)
                // 실패해도 로그인은 계속 진행 (best-effort)
            }

            // ③ 동기화 완료 후 상태 전환 → LoginTermsRoute가 홈으로 이동
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
        _uiState.value = _uiState.value.copy(needTermsAgreement = false)
    }
}
