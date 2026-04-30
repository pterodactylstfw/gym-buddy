package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.dao.WorkoutDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlin.collections.emptyList

class HistoryViewModel(private val workoutDao: WorkoutDao) : ViewModel() {
    // Luăm Flow-ul din Room și îl transformăm în StateFlow pentru Compose
    val workoutHistory = workoutDao.getAllWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}