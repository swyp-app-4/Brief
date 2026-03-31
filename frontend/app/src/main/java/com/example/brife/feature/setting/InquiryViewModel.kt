package com.example.brife.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brife.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InquiryUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val initialName: String = "",
    val initialEmail: String = ""
)

class InquiryViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InquiryUiState())
    val uiState: StateFlow<InquiryUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            val loggedIn = userRepository.isLoggedIn()
            if (!loggedIn) {
                _uiState.value = InquiryUiState(isLoggedIn = false, isLoading = false)
                return@launch
            }

            // 로그인 상태 → GET /users/me 로 nickname, email 로드
            userRepository.getMyProfile()
                .onSuccess { profile ->
                    _uiState.value = InquiryUiState(
                        isLoggedIn = true,
                        isLoading = false,
                        initialName = profile.nickname,
                        initialEmail = profile.email
                    )
                }
                .onFailure {
                    // API 실패해도 로그인 상태는 유지, 필드는 빈 값으로 직접 입력 유도
                    _uiState.value = InquiryUiState(
                        isLoggedIn = true,
                        isLoading = false,
                        initialName = "",
                        initialEmail = ""
                    )
                }
        }
    }
}
