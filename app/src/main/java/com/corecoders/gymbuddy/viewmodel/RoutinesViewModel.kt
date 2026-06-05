package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.Exercise
import com.corecoders.gymbuddy.data.Routine
import com.corecoders.gymbuddy.data.RoutineExercise
import com.corecoders.gymbuddy.data.dao.ExerciseDao
import com.corecoders.gymbuddy.data.dao.RoutineDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RoutinesViewModel(
    private val routineDao: RoutineDao,
    private val exerciseDao: ExerciseDao
) : ViewModel() {

    val allRoutines: StateFlow<List<Routine>> = routineDao.getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createRoutine(name: String, description: String = "") {
        viewModelScope.launch {
            routineDao.insertRoutine(Routine(name = name, description = description))
        }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            routineDao.deleteRoutine(routine)
        }
    }

    // --- LOGICĂ PENTRU DETALII RUTINĂ ---

    fun getRoutine(routineId: Int): Flow<Routine?> = routineDao.getRoutineById(routineId)

    fun getExercisesForRoutine(routineId: Int): Flow<List<Exercise>> {
        return routineDao.getExercisesForRoutine(routineId).flatMapLatest { routineExercises ->
            val ids = routineExercises.map { it.exerciseId }
            if (ids.isEmpty()) flowOf(emptyList())
            else exerciseDao.getExercisesByIds(ids)
        }
    }

    fun addExerciseToRoutine(routineId: Int, exercise: Exercise) {
        viewModelScope.launch {
            val maxOrder = routineDao.getMaxOrderForRoutine(routineId) ?: 0
            routineDao.insertRoutineExercise(
                RoutineExercise(
                    routineId = routineId,
                    exerciseId = exercise.id,
                    order = maxOrder + 1
                )
            )
        }
    }

    fun removeExerciseFromRoutine(routineId: Int, exerciseId: String) {
        viewModelScope.launch {
            routineDao.deleteExerciseFromRoutine(routineId, exerciseId)
        }
    }
}

class RoutinesViewModelFactory(
    private val routineDao: RoutineDao,
    private val exerciseDao: ExerciseDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutinesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoutinesViewModel(routineDao, exerciseDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
