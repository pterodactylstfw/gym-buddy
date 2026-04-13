package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.Workout
import com.corecoders.gymbuddy.data.dao.WorkoutDao
import kotlinx.coroutines.launch

class WorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {
    val allWorkouts = workoutDao.getAllWorkouts()

    fun startNewWorkout(workoutName: String) {
        viewModelScope.launch {
            val newWorkout = Workout(name = workoutName, date = System.currentTimeMillis())
            workoutDao.insertWorkout(newWorkout)
        }
    }
}