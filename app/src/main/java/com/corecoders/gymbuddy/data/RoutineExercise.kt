package com.corecoders.gymbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_exercises")
data class RoutineExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val routineId: Int,
    val exerciseId: String,
    val order: Int
)
