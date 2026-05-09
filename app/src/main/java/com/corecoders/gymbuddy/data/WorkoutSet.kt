package com.corecoders.gymbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sets")
data class WorkoutSet (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workoutId: Int,
    val exerciseId: String,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val setType: String = "N", // N: Normal, W: Warm-up, D: Drop Set, F: Failure
    val isCompleted: Boolean = false
)