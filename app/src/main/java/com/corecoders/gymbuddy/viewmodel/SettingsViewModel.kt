package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.UserPreferences
import android.util.Log
import com.corecoders.gymbuddy.services.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ServerStatus {
    CHECKING, OK, DOWN
}

class SettingsViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _serverStatus = MutableStateFlow(ServerStatus.CHECKING)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus.asStateFlow()

    private val _serverError = MutableStateFlow<String?>(null)
    val serverError: StateFlow<String?> = _serverError.asStateFlow()

    init {
        checkServerStatus()
    }

    fun checkServerStatus() {
        viewModelScope.launch {
            _serverStatus.value = ServerStatus.CHECKING
            _serverError.value = null
            try {
                ApiClient.exerciseApi.checkLiveness()
                _serverStatus.value = ServerStatus.OK
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Server liveness check failed", e)
                _serverError.value = e.localizedMessage ?: e.toString()
                _serverStatus.value = ServerStatus.DOWN
            }
        }
    }

    val themeMode: StateFlow<String?> = userPreferences.themeModeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val unitSystemMetric: StateFlow<Boolean> = userPreferences.unitSystemMetricFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val autoCompleteSet: StateFlow<Boolean> = userPreferences.autoCompleteSetFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun selectThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferences.updateThemeMode(mode)
        }
    }

    fun toggleUnitSystem(metric: Boolean) {
        viewModelScope.launch {
            userPreferences.updateUnitSystem(metric)
        }
    }

    fun toggleAutoCompleteSet(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.updateAutoCompleteSet(enabled)
        }
    }
}

class SettingsViewModelFactory(private val userPreferences: UserPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
