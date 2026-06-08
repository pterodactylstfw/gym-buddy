package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.Routine
import com.corecoders.gymbuddy.data.Workout
import com.corecoders.gymbuddy.data.dao.RoutineDao
import com.corecoders.gymbuddy.data.dao.WorkoutDao
import com.corecoders.gymbuddy.data.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val workoutDao: WorkoutDao,
    private val routineDao: RoutineDao,
    private val userPreferences: UserPreferences
): ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    val userEmail: String = auth.currentUser?.email ?: "Unknown User"

    val allWorkouts: StateFlow<List<Workout>> = workoutDao.getAllWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalWorkouts: StateFlow<Int> = allWorkouts
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val allRoutines: StateFlow<List<Routine>> = routineDao.getAllRoutines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalVolume: StateFlow<Double> = workoutDao.getTotalVolume()
        .map { it ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val age = userPreferences.ageFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val weight = userPreferences.weightFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)
    val height = userPreferences.heightFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)
    val experienceLevel = userPreferences.experienceLevelFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")
    val fitnessGoal = userPreferences.fitnessGoalFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    fun signOut() {
        viewModelScope.launch {
            userPreferences.clearProfileData()
            auth.signOut()
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch {
                        userPreferences.clearProfileData()
                        onSuccess()
                    }
                } else {
                    onError(task.exception?.localizedMessage ?: "Failed to delete account.")
                }
            }
        } else {
            onError("No authenticated user found.")
        }
    }
}

class ProfileViewModelFactory(
    private val workoutDao: WorkoutDao,
    private val routineDao: RoutineDao,
    private val userPreferences: UserPreferences
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(workoutDao, routineDao, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
