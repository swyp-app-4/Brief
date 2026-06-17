package com.swyp.brife.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swyp.brife.data.repository.NotificationSettingsRepository

class AlarmSettingViewModelFactory(
    private val repository: NotificationSettingsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmSettingViewModel::class.java)) {
            return AlarmSettingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
