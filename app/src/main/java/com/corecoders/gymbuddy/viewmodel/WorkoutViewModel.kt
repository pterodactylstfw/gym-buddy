package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.Workout
import com.corecoders.gymbuddy.data.dao.WorkoutDao
import com.corecoders.gymbuddy.data.dao.MuscleSetCount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class WorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {
    val allWorkouts = workoutDao.getAllWorkouts()

    fun startNewWorkout(workoutName: String) {
        viewModelScope.launch {
            val newWorkout = Workout(name = workoutName, date = System.currentTimeMillis())
            workoutDao.insertWorkout(newWorkout)
        }
    }

    val workoutsCount: StateFlow<Int> = workoutDao.getWorkoutsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalVolume: StateFlow<Double> = workoutDao.getTotalVolume()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val maxWeightLifted: StateFlow<Double> = workoutDao.getMaxWeightLifted()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalDurationMinutes: StateFlow<Int> = workoutDao.getTotalDuration()
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val muscleSetCounts: StateFlow<List<MuscleSetCount>> = workoutDao.getMuscleSetCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Logica pentru zilele de antrenament din această săptămână
    val weeklyAttendanceDays: StateFlow<Int> = workoutDao.getDistinctWorkoutDaysInRange(getStartOfWeekTimestamp())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private fun getStartOfWeekTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.clear(Calendar.MINUTE)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MILLISECOND)
        
        // Setăm prima zi a săptămânii (Luni)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        return calendar.timeInMillis
    }
}
