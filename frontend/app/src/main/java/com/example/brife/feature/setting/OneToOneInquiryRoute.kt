package com.example.brife.feature.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brife.data.local.AuthLocalStorage
import com.example.brife.data.remote.NetworkModule
import com.example.brife.data.repository.UserRepository

@Composable
fun OneToOneInquiryRoute(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: InquiryViewModel = viewModel(
        factory = InquiryViewModelFactory(
            remember {
                UserRepository(
                    api = NetworkModule.userApiService,
                    authLocalStorage = AuthLocalStorage(context)
                )
            }
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    OneToOneInquiryScreen(
        isLoggedIn = uiState.isLoggedIn,
        initialName = uiState.initialName,
        initialEmail = uiState.initialEmail,
        onBackClick = onBackClick
    )
}
