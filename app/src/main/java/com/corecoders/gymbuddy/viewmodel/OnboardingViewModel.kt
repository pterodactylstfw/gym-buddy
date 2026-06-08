package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _age = MutableStateFlow<Int?>(null)
    val age: StateFlow<Int?> = _age.asStateFlow()

    private val _gender = MutableStateFlow<String>("")
    val gender: StateFlow<String> = _gender.asStateFlow()

    private val _weight = MutableStateFlow<Float?>(null)
    val weight: StateFlow<Float?> = _weight.asStateFlow()

    private val _targetWeight = MutableStateFlow<Float?>(null)
    val targetWeight: StateFlow<Float?> = _targetWeight.asStateFlow()

    private val _height = MutableStateFlow<Int?>(null)
    val height: StateFlow<Int?> = _height.asStateFlow()

    private val _fitnessGoal = MutableStateFlow<String>("")
    val fitnessGoal: StateFlow<String> = _fitnessGoal.asStateFlow()

    private val _experienceLevel = MutableStateFlow<String>("")
    val experienceLevel: StateFlow<String> = _experienceLevel.asStateFlow()

    private val _trainingFrequency = MutableStateFlow<Int?>(null)
    val trainingFrequency: StateFlow<Int?> = _trainingFrequency.asStateFlow()

    fun updateAge(newAge: Int) { _age.value = newAge }
    fun updateGender(newGender: String) { _gender.value = newGender }
    fun updateWeight(newWeight: Float) { _weight.value = newWeight }
    fun updateTargetWeight(newTarget: Float) { _targetWeight.value = newTarget }
    fun updateHeight(newHeight: Int) { _height.value = newHeight }
    fun updateFitnessGoal(newGoal: String) { _fitnessGoal.value = newGoal }
    fun updateExperienceLevel(newLevel: String) { _experienceLevel.value = newLevel }
    fun updateTrainingFrequency(newFreq: Int) { _trainingFrequency.value = newFreq }

    fun saveOnboardingData(onComplete: () -> Unit) {
        viewModelScope.launch {
            userPreferences.updateProfileData(
                age = _age.value,
                weight = _weight.value,
                height = _height.value,
                targetWeight = _targetWeight.value,
                trainingFrequency = _trainingFrequency.value,
                fitnessGoal = _fitnessGoal.value.takeIf { it.isNotEmpty() },
                experienceLevel = _experienceLevel.value.takeIf { it.isNotEmpty() },
                gender = _gender.value.takeIf { it.isNotEmpty() }
            )
            userPreferences.updateOnboardingCompleted(true)
            onComplete()
        }
    }
}

class OnboardingViewModelFactory(private val userPreferences: UserPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnboardingViewModel(userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
