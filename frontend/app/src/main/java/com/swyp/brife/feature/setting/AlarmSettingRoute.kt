package com.swyp.brife.feature.setting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swyp.brife.data.local.AuthLocalStorage
import com.swyp.brife.data.remote.NetworkModule
import com.swyp.brife.data.repository.NotificationSettingsRepository

@Composable
fun AlarmSettingRoute(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember {
        NotificationSettingsRepository(
            api = NetworkModule.notificationSettingsApiService,
            authLocalStorage = AuthLocalStorage(context)
        )
    }
    val viewModel: AlarmSettingViewModel = viewModel(
        factory = AlarmSettingViewModelFactory(repository)
    )
    val uiState by viewModel.uiState.collectAsState()

    AlarmSettingScreen(
        onBackClick = onBackClick,
        dailyNewsEnabled = uiState.dailyNewsEnabled,
        time8am = uiState.time8am,
        time12pm = uiState.time12pm,
        time6pm = uiState.time6pm,
        time10pm = uiState.time10pm,
        isLoading = uiState.isLoading,
        isSaving = uiState.isSaving,
        onDailyNewsChanged = viewModel::onDailyNewsChanged,
        onTime8amChanged = viewModel::onTime8amChanged,
        onTime12pmChanged = viewModel::onTime12pmChanged,
        onTime6pmChanged = viewModel::onTime6pmChanged,
        onTime10pmChanged = viewModel::onTime10pmChanged
    )
}
