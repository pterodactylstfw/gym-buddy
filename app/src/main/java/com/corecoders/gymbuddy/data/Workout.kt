package com.corecoders.gymbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",
    val name: String,
    val date: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 0
)