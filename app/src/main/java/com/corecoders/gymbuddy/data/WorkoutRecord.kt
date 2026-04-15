package com.corecoders.gymbuddy.data

data class WorkoutRecord(
    val documentId: String = "",
    val workoutId: Int = 0,
    val userId: String = "",
    val exerciseName: String = "",
    val weight: Double = 0.0,
    val reps: Int = 0,
    val date: Long = System.currentTimeMillis()
)
