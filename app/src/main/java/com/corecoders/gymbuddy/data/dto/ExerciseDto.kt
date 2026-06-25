package com.corecoders.gymbuddy.data.dto

data class ExerciseDto(
    val exerciseId: String,
    val name: String,
    val imageUrl: String,
    val targetMuscles: List<String>?,
    val bodyParts: List<String>?
)