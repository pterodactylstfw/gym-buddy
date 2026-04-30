package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.dao.WorkoutDao
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(private val workoutDao: WorkoutDao): ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    val userEmail: String = auth.currentUser?.email?: "Unknown User"

    val totalWorkouts: StateFlow<Int> = workoutDao.getAllWorkouts()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun signOut() {
        auth.signOut()
    }
}

class ProfileViewModelFactory(private val workoutDao: WorkoutDao): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(workoutDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}