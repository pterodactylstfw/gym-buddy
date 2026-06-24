package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    val darkMode: StateFlow<Boolean?> = userPreferences.darkModeFlow.stateIn(
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

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.updateDarkMode(enabled)
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
