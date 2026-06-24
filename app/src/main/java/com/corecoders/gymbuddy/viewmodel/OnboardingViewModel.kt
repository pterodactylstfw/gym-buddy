package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.UserPreferences
import com.corecoders.gymbuddy.data.SocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OnboardingViewModel(private val userPreferences: UserPreferences) : ViewModel() {
    private val socialRepository = SocialRepository()

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

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        viewModelScope.launch {
            try {
                val currentAge = userPreferences.ageFlow.first()
                _age.value = if (currentAge > 0) currentAge else null

                val currentGender = userPreferences.genderFlow.first()
                _gender.value = currentGender

                val currentWeight = userPreferences.weightFlow.first()
                _weight.value = if (currentWeight > 0f) currentWeight else null

                val currentTargetWeight = userPreferences.targetWeightFlow.first()
                _targetWeight.value = if (currentTargetWeight > 0f) currentTargetWeight else null

                val currentHeight = userPreferences.heightFlow.first()
                _height.value = if (currentHeight > 0) currentHeight else null

                val currentGoal = userPreferences.fitnessGoalFlow.first()
                _fitnessGoal.value = currentGoal

                val currentExp = userPreferences.experienceLevelFlow.first()
                _experienceLevel.value = currentExp

                val currentFreq = userPreferences.trainingFrequencyFlow.first()
                _trainingFrequency.value = if (currentFreq > 0) currentFreq else null
            } catch (e: Exception) {
                // ignore
            }
        }
    }

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
            val finalAge = _age.value ?: 0
            val finalGender = _gender.value
            val finalWeight = _weight.value ?: 0f
            val finalTargetWeight = _targetWeight.value ?: 0f
            val finalHeight = _height.value ?: 0
            val finalFitnessGoal = _fitnessGoal.value
            val finalExperienceLevel = _experienceLevel.value
            val finalTrainingFrequency = _trainingFrequency.value ?: 0

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

            // Save to Firestore
            socialRepository.saveOnboardingDetails(
                age = finalAge,
                gender = finalGender,
                weight = finalWeight,
                targetWeight = finalTargetWeight,
                height = finalHeight,
                fitnessGoal = finalFitnessGoal,
                experienceLevel = finalExperienceLevel,
                trainingFrequency = finalTrainingFrequency
            )

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
