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
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _uiState.value = _uiState.value.copy(pendingLoginMethod = "kakao")

            Log.d("INTEREST_DEBUG", "loginWithKakao: accessToken.length=${accessToken.length}")

            repository.loginWithKakao(accessToken)
                .onSuccess { response ->
                    Log.d("INTEREST_DEBUG", "kakao login success: isNewUser=${response.isNewUser}")
                    handleLoginSuccess(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
                        isNewUser = response.isNewUser
                    )
                }
                .onFailure { throwable ->
                    Log.e("INTEREST_DEBUG", "kakao login failed: ${throwable.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message
                    )
                }
        }
    }

    fun loginWithNaver(accessToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _uiState.value = _uiState.value.copy(pendingLoginMethod = "naver")

            Log.d("INTEREST_DEBUG", "loginWithNaver: accessToken.length=${accessToken.length}")

            repository.loginWithNaver(accessToken)
                .onSuccess { response ->
                    Log.d("INTEREST_DEBUG", "naver login success: isNewUser=${response.isNewUser}")
                    handleLoginSuccess(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
                        isNewUser = response.isNewUser
                    )
                }
                .onFailure { throwable ->
                    Log.e("INTEREST_DEBUG", "naver login failed: ${throwable.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message
                    )
                }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _uiState.value = _uiState.value.copy(pendingLoginMethod = "google")

            Log.d("INTEREST_DEBUG", "loginWithGoogle: idToken.length=${idToken.length}")

            repository.loginWithGoogle(idToken)
                .onSuccess { response ->
                    Log.d("INTEREST_DEBUG", "google login success: isNewUser=${response.isNewUser}")
                    handleLoginSuccess(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
                        isNewUser = response.isNewUser
                    )
                }
                .onFailure { throwable ->
                    Log.e("INTEREST_DEBUG", "google login failed: ${throwable.message}")
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
        val hasAgreed = authLocalStorage.hasAgreedTerms()
        Log.d("INTEREST_DEBUG", "handleLoginSuccess: isNewUser=$isNewUser, hasAgreedTerms=$hasAgreed")

        if (isNewUser && !hasAgreed) {
            // 신규회원 + 약관 미동의 → 약관 화면으로
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isNewUser = true,
                pendingAccessToken = accessToken,
                pendingRefreshToken = refreshToken,
                needTermsAgreement = true
            )
        } else {
            // 기존회원 OR (신규회원이지만 기기에 약관 동의 기록 있음)
            completeLogin(
                accessToken = accessToken,
                refreshToken = refreshToken,
                persistTermsAgreement = true,
                isNewUser = isNewUser
            )
        }
    }

    /**
     * 로그인 완료 처리.
     * ① 토큰 저장(persistAuthState) + 로그인 성공 상태를 먼저 설정 → 로그인 자체를 완료
     * ② 관심사 동기화는 로그인 성공 이후 best-effort 처리 → 실패해도 로그인 유지
     */
    private fun completeLogin(
        accessToken: String,
        refreshToken: String,
        persistTermsAgreement: Boolean,
        isNewUser: Boolean = false
    ) {
        val subCategoryIds = onboardingLocalStorage.getSelectedSubCategoryIds()
        val groupIds = onboardingLocalStorage.getSelectedCategoryIds()

        Log.d("INTEREST_DEBUG", "completeLogin: isNewUser=$isNewUser, subCategoryIds=$subCategoryIds, groupIds=$groupIds")
        Log.d("INTEREST_DEBUG", "completeLogin: accessToken.isNotBlank=${accessToken.isNotBlank()}")

        // ① 로그인 완료 먼저 처리 (토큰 저장 + 성공 상태)
        persistAuthState(accessToken, refreshToken, persistTermsAgreement)
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isLoginSuccess = true,
            isNewUser = false,
            errorMessage = null
        )

        // ② 로컬 관심사가 없으면 동기화 불필요
        if (subCategoryIds.isEmpty() && groupIds.isEmpty()) {
            Log.d("INTEREST_DEBUG", "completeLogin: no local interests, skipping sync")
            return
        }

        // ③ 관심사 동기화는 로그인 이후 별도 실행 (실패해도 로그인 상태 유지)
        viewModelScope.launch {
            Log.d("INTEREST_DEBUG", "completeLogin sync start: isNewUser=$isNewUser, body={categoryIds=$subCategoryIds, groupIds=$groupIds}")

            val interestResult = if (isNewUser) {
                // 신규회원: POST /users/me/interests (최초 저장)
                userRepository.saveInterests(accessToken, subCategoryIds, groupIds)
            } else {
                // 기존회원: PUT /users/me/interests (재설정)
                userRepository.updateInterests(accessToken, subCategoryIds, groupIds)
            }

            if (interestResult.isSuccess) {
                Log.d("INTEREST_DEBUG", "completeLogin interest sync success")
            } else {
                val errMsg = interestResult.exceptionOrNull()?.message ?: "unknown"
                Log.e("INTEREST_DEBUG", "completeLogin interest sync failed (login already succeeded): $errMsg")
                // 로그인은 이미 완료 — 관심사 동기화 실패는 로그인을 막지 않음
                // 사용자는 이미 홈으로 이동했으며, 프로필에서 관심사 재설정 가능
            }
        }
    }

    fun agreeTerms() {
        if (_uiState.value.isLoading) return
        val accessToken = _uiState.value.pendingAccessToken ?: return
        val refreshToken = _uiState.value.pendingRefreshToken.orEmpty()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            Log.d("INTEREST_DEBUG", "agreeTerms: calling POST /auth/terms")

            repository.agreeTerms(accessToken)
                .onSuccess {
                    Log.d("INTEREST_DEBUG", "agreeTerms: POST /auth/terms success")
                    completeTermsLogin(accessToken, refreshToken)
                }
                .onFailure { throwable ->
                    Log.e("INTEREST_DEBUG", "agreeTerms: POST /auth/terms failed: ${throwable.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message
                    )
                }
        }
    }

    /**
     * 약관 동의 완료 후 처리.
     * ① 토큰 저장 + 약관 동의 성공 상태를 먼저 설정 → 로그인 자체를 완료
     * ② 관심사 동기화는 이후 best-effort 처리 → 실패해도 로그인 유지
     * 약관 동의 직후는 항상 신규회원이므로 POST /users/me/interests 사용
     */
    private fun completeTermsLogin(
        accessToken: String,
        refreshToken: String
    ) {
        val subCategoryIds = onboardingLocalStorage.getSelectedSubCategoryIds()
        val groupIds = onboardingLocalStorage.getSelectedCategoryIds()

        Log.d("INTEREST_DEBUG", "completeTermsLogin: subCategoryIds=$subCategoryIds, groupIds=$groupIds")
        Log.d("INTEREST_DEBUG", "completeTermsLogin: accessToken.isNotBlank=${accessToken.isNotBlank()}")

        // ① 약관 동의 완료 먼저 처리 (토큰 저장 + 성공 상태)
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

        // ② 로컬 관심사가 없으면 동기화 불필요
        if (subCategoryIds.isEmpty() && groupIds.isEmpty()) {
            Log.d("INTEREST_DEBUG", "completeTermsLogin: no local interests, skipping sync")
            return
        }

        // ③ 관심사 동기화는 로그인 이후 별도 실행 (신규회원 → POST)
        viewModelScope.launch {
            Log.d("INTEREST_DEBUG", "completeTermsLogin sync start: body={categoryIds=$subCategoryIds, groupIds=$groupIds}")

            val interestResult = userRepository.saveInterests(accessToken, subCategoryIds, groupIds)

            if (interestResult.isSuccess) {
                Log.d("INTEREST_DEBUG", "completeTermsLogin interest sync success")
            } else {
                val errMsg = interestResult.exceptionOrNull()?.message ?: "unknown"
                Log.e("INTEREST_DEBUG", "completeTermsLogin interest sync failed (login already succeeded): $errMsg")
                // 로그인은 이미 완료 — 관심사 동기화 실패는 로그인을 막지 않음
            }
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
        Log.d("INTEREST_DEBUG", "persistAuthState: token saved, isLoggedIn=${authLocalStorage.isLoggedIn()}")
    }

    fun consumeTermsNavigation() {
        _uiState.value = _uiState.value.copy(needTermsAgreement = false)
    }
}
