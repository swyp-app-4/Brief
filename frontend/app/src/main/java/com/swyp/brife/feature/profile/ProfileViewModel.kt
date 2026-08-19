package com.swyp.brife.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.annotation.DrawableRes
import com.swyp.brife.data.local.OnboardingLocalStorage
import com.swyp.brife.data.repository.UserRepository
import com.swyp.brife.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val onboardingLocalStorage: OnboardingLocalStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            Log.d("ProfileDebug", "loadProfile start")

            val localInterests = onboardingLocalStorage.getSelectedCategoryIds()
                .mapNotNull { categoryItemFromId(it) }

            // 서버 프로필 응답을 기다리지 않고 최신 로컬 관심사를 먼저 표시한다.
            _uiState.value = _uiState.value.copy(interests = localInterests)

            // isLoggedIn은 API 성공 여부가 아니라 토큰 존재 여부로 판단
            val loggedIn = userRepository.isLoggedIn()
            Log.d("ProfileDebug", "loadProfile isLoggedIn=$loggedIn")

            if (!loggedIn) {
                Log.d("ProfileDebug", "loadProfile guest branch")
                _uiState.value = ProfileUiState(
                    isLoggedIn = false,
                    profileImageRes = R.drawable.img_profile_avatar,
                    interests = localInterests
                )
                return@launch
            }

            // 로그인 상태 → 프로필 API 호출 (실패해도 isLoggedIn은 true 유지)
            userRepository.getMyProfile()
                .onSuccess { profile ->
                    Log.d(
                        "ProfileDebug",
                        "loadProfile success hasName=${profile.nickname.isNotBlank()}, hasProfileImage=${profile.profileImageUrl != null}"
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        userName = profile.nickname,
                        userEmail = profile.email,
                        profileImageUrl = profile.profileImageUrl,
                        profileImageRes = profileImageResFromUrl(profile.profileImageUrl)
                    )
                }
                .onFailure {
                    Log.d(
                        "ProfileDebug",
                        "loadProfile failure message=${it.message}, keepLoggedIn=true"
                    )
                    // API 실패해도 토큰이 있으면 로그인 상태 유지
                    _uiState.value = _uiState.value.copy(isLoggedIn = true)
                }
        }
    }

    fun updateLocalProfileImage(
        profileImageUrl: String,
        @DrawableRes profileImageRes: Int
    ) {
        _uiState.value = _uiState.value.copy(
            profileImageUrl = profileImageUrl,
            profileImageRes = profileImageRes
        )
    }
}
