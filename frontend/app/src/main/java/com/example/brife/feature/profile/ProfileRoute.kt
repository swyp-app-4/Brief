package com.example.brife.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.local.OnboardingLocalStorage
import com.example.brife.data.remote.NetworkModule
import com.example.brife.data.repository.UserRepository

@Composable
fun ProfileRoute(
    onResetInterestClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onEditProfileImageClick: (String, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            userRepository = UserRepository(
                api = NetworkModule.userApiService,
                authLocalStorage = AuthLocalStorage(context)
            ),
            onboardingLocalStorage = OnboardingLocalStorage(context)
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    // 화면이 다시 RESUMED될 때마다 프로필 갱신 (관심사 재설정 후 복귀 포함)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.loadProfile()
        }
    }

    ProfileScreen(
        modifier = modifier,
        uiState = uiState,
        onResetInterestClick = onResetInterestClick,
        onLoginClick = onLoginClick,
        onEditProfileImageClick = {
            onEditProfileImageClick(
                uiState.userName.ifBlank { "브리프" },
                uiState.profileImageRes
            )
        }
    )
}
