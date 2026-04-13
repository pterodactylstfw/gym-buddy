package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.util.query
import com.corecoders.gymbuddy.data.Exercise
import com.corecoders.gymbuddy.data.dao.ExerciseDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class ExerciseViewModel(private val exerciseDao: ExerciseDao) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val exercises: StateFlow<List<Exercise>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                exerciseDao.getAllExercises()
            } else {
                exerciseDao.searchExercises(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}

// Fabrica exact ca la WorkoutViewModel
class ExerciseViewModelFactory(private val exerciseDao: ExerciseDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseViewModel(exerciseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}