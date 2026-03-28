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
                    // 비로그인 또는 API 실패 시 로컬 관심사만 표시
                    _uiState.value = ProfileUiState(
                        isLoggedIn = false,
                        interests = localInterests
                    )
                }
        }
    }
}
