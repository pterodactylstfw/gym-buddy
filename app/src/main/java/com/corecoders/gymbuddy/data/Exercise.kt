package com.corecoders.gymbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val targetMuscle: String,
    val gifUrl: String
)