package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.Exercise
import com.corecoders.gymbuddy.data.Workout
import com.corecoders.gymbuddy.data.dao.WorkoutDao
import com.corecoders.gymbuddy.data.WorkoutSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class ActiveSet(
    val id: String = UUID.randomUUID().toString(),
    val weight: String = "",
    val reps: String = "",
    val setType: String = "N",
    val isCompleted: Boolean = false
)

data class ActiveExercise(
    val exercise: Exercise,
    val sets: List<ActiveSet> = listOf(ActiveSet())
)

class ActiveWorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {

    // Numele antrenamentului, editabil de către utilizator
    private val _workoutName = MutableStateFlow(getDynamicWorkoutName())
    val workoutName = _workoutName.asStateFlow()

    private val _activeExercises = MutableStateFlow<List<ActiveExercise>>(emptyList())
    val activeExercises = _activeExercises.asStateFlow()

    private fun getDynamicWorkoutName(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val period = when (hour) {
            in 5..11 -> "Morning"
            in 12..16 -> "Afternoon"
            in 17..21 -> "Evening"
            else -> "Late Night"
        }
        return "$period Workout"
    }

    fun updateWorkoutName(newName: String) {
        _workoutName.value = newName
    }

    fun addExercise(exercise: Exercise) {
        val currentList = _activeExercises.value.toMutableList()
        currentList.add(ActiveExercise(exercise = exercise))
        _activeExercises.value = currentList
    }

    fun startWorkoutFromRoutine(routine: com.corecoders.gymbuddy.data.Routine, exercises: List<Exercise>) {
        _workoutName.value = routine.name
        _activeExercises.value = exercises.map { exercise ->
            ActiveExercise(exercise = exercise, sets = listOf(ActiveSet()))
        }
    }

    fun addSetToExercise(exerciseIndex: Int) {
        val currentList = _activeExercises.value.toMutableList()
        val currentExercise = currentList[exerciseIndex]

        val lastSet = currentExercise.sets.lastOrNull()
        val newSet = if (lastSet != null) {
            ActiveSet(weight = lastSet.weight, reps = lastSet.reps, setType = lastSet.setType)
        } else {
            ActiveSet()
        }

        val newSets = currentExercise.sets.toMutableList().apply { add(newSet) }
        currentList[exerciseIndex] = currentExercise.copy(sets = newSets)
        _activeExercises.value = currentList
    }

    fun updateSet(exerciseIndex: Int, setIndex: Int, weight: String? = null, reps: String? = null, setType: String? = null, isCompleted: Boolean? = null) {
        val currentList = _activeExercises.value.toMutableList()
        val currentExercise = currentList[exerciseIndex]
        val currentSets = currentExercise.sets.toMutableList()

        val oldSet = currentSets[setIndex]
        currentSets[setIndex] = oldSet.copy(
            weight = weight ?: oldSet.weight,
            reps = reps ?: oldSet.reps,
            setType = setType ?: oldSet.setType,
            isCompleted = isCompleted ?: oldSet.isCompleted
        )

        currentList[exerciseIndex] = currentExercise.copy(sets = currentSets)
        _activeExercises.value = currentList
    }

    fun finishWorkout(onFinished: () -> Unit) {
        val completedSets = _activeExercises.value.flatMap { it.sets }.filter { it.isCompleted && it.weight.isNotEmpty() && it.reps.isNotEmpty() }
        
        if (completedSets.isEmpty()) {
            onFinished()
            return
        }

        viewModelScope.launch {
            val finalName = if (_workoutName.value.isBlank()) "Workout" else _workoutName.value
            val newWorkout = Workout(name = finalName)
            val workoutId = workoutDao.insertWorkout(newWorkout).toInt()

            _activeExercises.value.forEach { activeExercise ->
                activeExercise.sets.forEachIndexed { index, activeSet ->
                    if (activeSet.isCompleted && activeSet.weight.isNotEmpty() && activeSet.reps.isNotEmpty()) {
                        val setInfo = WorkoutSet(
                            workoutId = workoutId,
                            exerciseId = activeExercise.exercise.id,
                            setNumber = index + 1,
                            weight = activeSet.weight.toDoubleOrNull() ?: 0.0,
                            reps = activeSet.reps.toIntOrNull() ?: 0,
                            setType = activeSet.setType,
                            isCompleted = true
                        )
                        workoutDao.insertSet(setInfo)
                    }
                }
            }
            onFinished()
        }
    }

    fun resetWorkout() {
        _activeExercises.value = emptyList()
        _workoutName.value = getDynamicWorkoutName()
    }
}

class ActiveWorkoutViewModelFactory(private val workoutDao: WorkoutDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveWorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActiveWorkoutViewModel(workoutDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
