package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.Routine
import com.corecoders.gymbuddy.data.Workout
import com.corecoders.gymbuddy.data.dao.RoutineDao
import com.corecoders.gymbuddy.data.dao.WorkoutDao
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*

class ProfileViewModel(
    private val workoutDao: WorkoutDao,
    private val routineDao: RoutineDao
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

    fun signOut() {
        auth.signOut()
    }
}

class ProfileViewModelFactory(
    private val workoutDao: WorkoutDao,
    private val routineDao: RoutineDao
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(workoutDao, routineDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
