package com.example.brife.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brife.data.local.OnboardingLocalStorage
import com.example.brife.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val onboardingLocalStorage: OnboardingLocalStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            val localInterests = onboardingLocalStorage.getSelectedCategoryIds()
                .mapNotNull { categoryItemFromId(it) }

            // isLoggedIn은 API 성공 여부가 아니라 토큰 존재 여부로 판단
            val loggedIn = userRepository.isLoggedIn()

            if (!loggedIn) {
                _uiState.value = ProfileUiState(
                    isLoggedIn = false,
                    interests = localInterests
                )
                return@launch
            }

            // 로그인 상태 → 프로필 API 호출 (실패해도 isLoggedIn은 true 유지)
            userRepository.getMyProfile()
                .onSuccess { profile ->
                    _uiState.value = ProfileUiState(
                        isLoggedIn = true,
                        userName = profile.nickname,
                        userEmail = profile.email,
                        interests = localInterests
                    )
                }
                .onFailure {
                    // API 실패해도 토큰이 있으면 로그인 상태 유지
                    _uiState.value = ProfileUiState(
                        isLoggedIn = true,
                        interests = localInterests
                    )
                }
        }
    }
}
