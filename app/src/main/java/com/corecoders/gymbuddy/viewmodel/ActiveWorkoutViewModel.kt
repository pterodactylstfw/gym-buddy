package com.corecoders.gymbuddy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.corecoders.gymbuddy.data.Exercise
import com.corecoders.gymbuddy.data.Workout
import com.corecoders.gymbuddy.data.dao.WorkoutDao
import com.corecoders.gymbuddy.data.WorkoutSet
import com.corecoders.gymbuddy.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
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

class ActiveWorkoutViewModel(
    private val workoutDao: WorkoutDao,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val unitSystemMetric = userPreferences.unitSystemMetricFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    private val _workoutName = MutableStateFlow(getDynamicWorkoutName())
    val workoutName = _workoutName.asStateFlow()

    private val _activeExercises = MutableStateFlow<List<ActiveExercise>>(emptyList())
    val activeExercises = _activeExercises.asStateFlow()

    private var startTime: Long = System.currentTimeMillis()

    private val _elapsedTime = MutableStateFlow("00:00")
    val elapsedTime = _elapsedTime.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    private val _restTimeRemaining = MutableStateFlow<Int?>(null)
    val restTimeRemaining = _restTimeRemaining.asStateFlow()

    private var restTimerJob: kotlinx.coroutines.Job? = null

    private val _previousSets = MutableStateFlow<Map<String, List<WorkoutSet>>>(emptyMap())
    val previousSets = _previousSets.asStateFlow()

    init {
        startTimer()
    }

    fun startTimer() {
        timerJob?.cancel()
        startTime = System.currentTimeMillis()
        timerJob = viewModelScope.launch {
            while (true) {
                val elapsedMs = System.currentTimeMillis() - startTime
                val seconds = (elapsedMs / 1000) % 60
                val minutes = (elapsedMs / 60000) % 60
                val hours = (elapsedMs / 3600000)
                _elapsedTime.value = if (hours > 0) {
                    String.format("%02d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format("%02d:%02d", minutes, seconds)
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun startRestTimer(durationSeconds: Int = 90) {
        restTimerJob?.cancel()
        _restTimeRemaining.value = durationSeconds
        restTimerJob = viewModelScope.launch {
            var remaining = durationSeconds
            while (remaining > 0) {
                kotlinx.coroutines.delay(1000)
                remaining--
                _restTimeRemaining.value = remaining
            }
            _restTimeRemaining.value = null
        }
    }

    fun stopRestTimer() {
        restTimerJob?.cancel()
        _restTimeRemaining.value = null
    }

    fun loadPreviousSets(exerciseId: String) {
        viewModelScope.launch {
            try {
                val sets = workoutDao.getLastWorkoutSetsForExercise(exerciseId)
                val currentMap = _previousSets.value.toMutableMap()
                currentMap[exerciseId] = sets
                _previousSets.value = currentMap
            } catch (e: Exception) {
                // ignore
            }
        }
    }

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
        loadPreviousSets(exercise.id)
    }

    fun removeExercise(index: Int) {
        val currentList = _activeExercises.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _activeExercises.value = currentList
        }
    }

    fun startWorkoutFromRoutine(routine: com.corecoders.gymbuddy.data.Routine, exercises: List<Exercise>) {
        _workoutName.value = routine.name
        startTime = System.currentTimeMillis()
        _activeExercises.value = exercises.map { exercise ->
            ActiveExercise(exercise = exercise, sets = listOf(ActiveSet()))
        }
        startTimer()
        exercises.forEach { loadPreviousSets(it.id) }
    }

    fun addSetToExercise(exerciseIndex: Int) {
        val currentList = _activeExercises.value.toMutableList()
        val currentExercise = currentList[exerciseIndex]
        val currentSets = currentExercise.sets.toMutableList()

        val lastSet = currentSets.lastOrNull()
        if (lastSet != null) {
            val lastIdx = currentSets.lastIndex
            currentSets[lastIdx] = lastSet.copy(isCompleted = true)
            startRestTimer()
        }

        val newSet = if (lastSet != null) {
            ActiveSet(weight = lastSet.weight, reps = lastSet.reps, setType = lastSet.setType, isCompleted = false)
        } else {
            ActiveSet()
        }

        currentSets.add(newSet)
        currentList[exerciseIndex] = currentExercise.copy(sets = currentSets)
        _activeExercises.value = currentList
    }

    fun removeSetFromExercise(exerciseIndex: Int, setIndex: Int) {
        val currentList = _activeExercises.value.toMutableList()
        val currentExercise = currentList[exerciseIndex]
        val currentSets = currentExercise.sets.toMutableList()

        if (setIndex in currentSets.indices && currentSets.size > 1) {
            currentSets.removeAt(setIndex)
            currentList[exerciseIndex] = currentExercise.copy(sets = currentSets)
            _activeExercises.value = currentList
        }
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

        if (isCompleted == true && !oldSet.isCompleted) {
            startRestTimer()
        }
    }

    fun finishWorkout(onFinished: (Int?) -> Unit) {
        val isMetric = unitSystemMetric.value
        val validSets = _activeExercises.value.flatMap { activeExercise ->
            val prevSets = _previousSets.value[activeExercise.exercise.id] ?: emptyList()
            activeExercise.sets.mapIndexed { index, activeSet ->
                val prevSet = prevSets.getOrNull(index)
                val weightVal = activeSet.weight.ifEmpty { 
                    prevSet?.let { 
                        val displayW = if (isMetric) it.weight else it.weight * 2.20462
                        if (displayW % 1 == 0.0) displayW.toInt().toString() else "%.1f".format(displayW)
                    } ?: "" 
                }
                val repsVal = activeSet.reps.ifEmpty { 
                    prevSet?.reps?.toString() ?: "" 
                }
                Triple(activeSet, weightVal, repsVal)
            }
        }.filter { (activeSet, weightVal, repsVal) ->
            (activeSet.isCompleted || (weightVal.isNotEmpty() && repsVal.isNotEmpty())) && 
            weightVal.isNotEmpty() && repsVal.isNotEmpty()
        }
        
        if (validSets.isEmpty()) {
            stopTimer()
            stopRestTimer()
            onFinished(null)
            return
        }

        viewModelScope.launch {
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val finalName = if (_workoutName.value.isBlank()) "Workout" else _workoutName.value
            val durationMinutes = ((System.currentTimeMillis() - startTime) / 60000).toInt()
            val newWorkout = Workout(userId = userId, name = finalName, durationMinutes = durationMinutes)
            val workoutId = workoutDao.insertWorkout(newWorkout).toInt()

            _activeExercises.value.forEach { activeExercise ->
                val prevSets = _previousSets.value[activeExercise.exercise.id] ?: emptyList()
                activeExercise.sets.forEachIndexed { index, activeSet ->
                    val prevSet = prevSets.getOrNull(index)
                    
                    val parsedWeight = activeSet.weight.toDoubleOrNull()
                    val finalWeightInKg = if (parsedWeight != null) {
                        if (isMetric) parsedWeight else parsedWeight / 2.20462
                    } else {
                        prevSet?.weight ?: 0.0
                    }

                    val finalReps = activeSet.reps.ifEmpty { 
                        prevSet?.reps?.toString() ?: "" 
                    }

                    if (activeSet.isCompleted || (parsedWeight != null && finalReps.isNotEmpty()) || (prevSet != null && finalReps.isNotEmpty())) {
                        val repsInt = finalReps.toIntOrNull() ?: 0
                        if (finalWeightInKg > 0.0 && repsInt > 0) {
                            val setInfo = WorkoutSet(
                                workoutId = workoutId,
                                exerciseId = activeExercise.exercise.id,
                                setNumber = index + 1,
                                weight = finalWeightInKg,
                                reps = repsInt,
                                setType = activeSet.setType,
                                isCompleted = true
                            )
                            workoutDao.insertSet(setInfo)
                        }
                    }
                }
            }
            stopTimer()
            stopRestTimer()
            onFinished(workoutId)
        }
    }

    fun resetWorkout() {
        _activeExercises.value = emptyList()
        _workoutName.value = getDynamicWorkoutName()
        startTime = System.currentTimeMillis()
        stopRestTimer()
        startTimer()
    }
}

class ActiveWorkoutViewModelFactory(
    private val workoutDao: WorkoutDao,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveWorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActiveWorkoutViewModel(workoutDao, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
