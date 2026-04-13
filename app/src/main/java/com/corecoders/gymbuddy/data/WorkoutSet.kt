package com.corecoders.gymbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sets")
data class WorkoutSet (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workoutId: Int,
    val exerciseId: Int,
    val setNumber: Int,
    val weight: Double,
    val reps: Int,
    val isCompleted: Boolean = false
)