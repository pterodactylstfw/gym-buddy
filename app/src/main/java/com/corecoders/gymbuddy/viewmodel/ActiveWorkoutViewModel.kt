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
import java.util.UUID

// 1. Definim cum arată un set pe ecran (folosim String pentru că utilizatorul tastează text)
data class ActiveSet(
    val id: String = UUID.randomUUID().toString(),
    val weight: String = "",
    val reps: String = "",
    val isCompleted: Boolean = false
)

// 2. Definim un exercițiu care are o listă de seturi
data class ActiveExercise(
    val exercise: Exercise,
    val sets: List<ActiveSet> = listOf(ActiveSet()) // Începe automat cu un set gol
)

class ActiveWorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {

    // Lista cu exercițiile pe care le face acum la sală
    private val _activeExercises = MutableStateFlow<List<ActiveExercise>>(emptyList())
    val activeExercises = _activeExercises.asStateFlow()

    // --- FUNCȚII PENTRU UI ---

    // Adaugă un exercițiu nou în antrenament (ex: selectat din Catalog)
    fun addExercise(exercise: Exercise) {
        val currentList = _activeExercises.value.toMutableList()
        currentList.add(ActiveExercise(exercise = exercise))
        _activeExercises.value = currentList
    }

    // Adaugă un set nou (rând nou) la un exercițiu
    fun addSetToExercise(exerciseIndex: Int) {
        val currentList = _activeExercises.value.toMutableList()
        val currentExercise = currentList[exerciseIndex]
        val newSets = currentExercise.sets.toMutableList().apply { add(ActiveSet()) }
        currentList[exerciseIndex] = currentExercise.copy(sets = newSets)
        _activeExercises.value = currentList
    }

    // Actualizează ce scrie utilizatorul în căsuțe (kg, repetări) sau bifa
    fun updateSet(exerciseIndex: Int, setIndex: Int, weight: String? = null, reps: String? = null, isCompleted: Boolean? = null) {
        val currentList = _activeExercises.value.toMutableList()
        val currentExercise = currentList[exerciseIndex]
        val currentSets = currentExercise.sets.toMutableList()

        val oldSet = currentSets[setIndex]
        currentSets[setIndex] = oldSet.copy(
            weight = weight ?: oldSet.weight,
            reps = reps ?: oldSet.reps,
            isCompleted = isCompleted ?: oldSet.isCompleted
        )

        currentList[exerciseIndex] = currentExercise.copy(sets = currentSets)
        _activeExercises.value = currentList
    }

    // --- SALVAREA FINALĂ ÎN BAZA DE DATE (ROOM) ---
    fun finishWorkout(workoutName: String, onFinished: () -> Unit) {
        viewModelScope.launch {
            // 1. Creăm antrenamentul general și îi luăm ID-ul
            val newWorkout = Workout(name = workoutName)
            val workoutId = workoutDao.insertWorkout(newWorkout).toInt()

            // 2. Trecem prin toate exercițiile și seturile de pe ecran
            _activeExercises.value.forEach { activeExercise ->
                activeExercise.sets.forEachIndexed { index, activeSet ->
                    // Salvăm doar seturile care au fost completate/bifate
                    if (activeSet.isCompleted && activeSet.weight.isNotEmpty() && activeSet.reps.isNotEmpty()) {
                        val setInfo = WorkoutSet(
                            workoutId = workoutId,
                            exerciseId = activeExercise.exercise.id,
                            setNumber = index + 1,
                            weight = activeSet.weight.toDoubleOrNull() ?: 0.0,
                            reps = activeSet.reps.toIntOrNull() ?: 0,
                            isCompleted = true
                        )
                        workoutDao.insertSet(setInfo)
                    }
                }
            }
            onFinished() // Îi spunem ecranului că am terminat de salvat, ca să închidă pagina
        }
    }
}

// Fabrica standard (la fel ca la celelalte)
class ActiveWorkoutViewModelFactory(private val workoutDao: WorkoutDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveWorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActiveWorkoutViewModel(workoutDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}