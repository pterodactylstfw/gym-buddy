package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.Workout
import com.corecoders.gymbuddy.data.dao.WorkoutDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {
    val allWorkouts = workoutDao.getAllWorkouts()

    fun startNewWorkout(workoutName: String) {
        viewModelScope.launch {
            val newWorkout = Workout(name = workoutName, date = System.currentTimeMillis())
            workoutDao.insertWorkout(newWorkout)
        }
    }

    // În WorkoutViewModel.kt
    val workoutsCount: StateFlow<Int> = workoutDao.getWorkoutsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalVolume: StateFlow<Double> = workoutDao.getTotalVolume()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}