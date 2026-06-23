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
import com.google.firebase.auth.FirebaseAuth

import com.corecoders.gymbuddy.data.AuthManager
import kotlinx.coroutines.flow.flatMapLatest

class WorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {
    
    val allWorkouts = AuthManager.currentUserIdFlow().flatMapLatest { userId ->
        workoutDao.getAllWorkouts(userId)
    }

    fun startNewWorkout(workoutName: String) {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val newWorkout = Workout(userId = currentUserId, name = workoutName, date = System.currentTimeMillis())
            workoutDao.insertWorkout(newWorkout)
        }
    }

    val workoutsCount: StateFlow<Int> = AuthManager.currentUserIdFlow().flatMapLatest { userId ->
        workoutDao.getWorkoutsCount(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalVolume: StateFlow<Double> = AuthManager.currentUserIdFlow().flatMapLatest { userId ->
        workoutDao.getTotalVolume(userId).map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val maxWeightLifted: StateFlow<Double> = AuthManager.currentUserIdFlow().flatMapLatest { userId ->
        workoutDao.getMaxWeightLifted(userId).map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalDurationMinutes: StateFlow<Int> = AuthManager.currentUserIdFlow().flatMapLatest { userId ->
        workoutDao.getTotalDuration(userId).map { it ?: 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val muscleSetCounts: StateFlow<List<MuscleSetCount>> = AuthManager.currentUserIdFlow().flatMapLatest { userId ->
        workoutDao.getMuscleSetCounts(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Logica pentru zilele de antrenament din această săptămână
    val weeklyAttendanceDays: StateFlow<Int> = AuthManager.currentUserIdFlow().flatMapLatest { userId ->
        workoutDao.getDistinctWorkoutDaysInRange(getStartOfWeekTimestamp(), userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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
