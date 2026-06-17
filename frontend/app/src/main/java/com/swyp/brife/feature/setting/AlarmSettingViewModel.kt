package com.swyp.brife.feature.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swyp.brife.data.model.NotificationSettingsRequest
import com.swyp.brife.data.model.NotificationSettingsResponse
import com.swyp.brife.data.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlarmSettingUiState(
    val dailyNewsEnabled: Boolean = false,
    val time8am: Boolean = false,
    val time12pm: Boolean = false,
    val time6pm: Boolean = false,
    val time10pm: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class AlarmSettingViewModel(
    private val repository: NotificationSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmSettingUiState(isLoading = true))
    val uiState: StateFlow<AlarmSettingUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            repository.getNotificationSettings()
                .onSuccess { response ->
                    _uiState.value = response.toUiState()
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message
                    )
                }
        }
    }

    fun onDailyNewsChanged(enabled: Boolean) {
        updateSettings { it.copy(dailyNewsEnabled = enabled) }
    }

    fun onTime8amChanged(enabled: Boolean) {
        updateSettings { it.copy(time8am = enabled) }
    }

    fun onTime12pmChanged(enabled: Boolean) {
        updateSettings { it.copy(time12pm = enabled) }
    }

    fun onTime6pmChanged(enabled: Boolean) {
        updateSettings { it.copy(time6pm = enabled) }
    }

    fun onTime10pmChanged(enabled: Boolean) {
        updateSettings { it.copy(time10pm = enabled) }
    }

    private fun updateSettings(
        reducer: (AlarmSettingUiState) -> AlarmSettingUiState
    ) {
        val previousState = _uiState.value
        if (previousState.isLoading || previousState.isSaving) return

        val nextState = reducer(previousState).copy(isSaving = true, errorMessage = null)
        _uiState.value = nextState

        viewModelScope.launch {
            repository.updateNotificationSettings(nextState.toRequest())
                .onSuccess { response ->
                    _uiState.value = response.toUiState()
                }
                .onFailure { throwable ->
                    _uiState.value = previousState.copy(
                        isSaving = false,
                        errorMessage = throwable.message
                    )
                }
        }
    }
}

private fun NotificationSettingsResponse.toUiState(): AlarmSettingUiState =
    AlarmSettingUiState(
        dailyNewsEnabled = dailyNewsEnabled,
        time8am = time8am,
        time12pm = time12pm,
        time6pm = time6pm,
        time10pm = time10pm,
        isLoading = false,
        isSaving = false,
        errorMessage = null
    )

private fun AlarmSettingUiState.toRequest(): NotificationSettingsRequest =
    NotificationSettingsRequest(
        dailyNewsEnabled = dailyNewsEnabled,
        time8am = time8am,
        time12pm = time12pm,
        time6pm = time6pm,
        time10pm = time10pm
    )
