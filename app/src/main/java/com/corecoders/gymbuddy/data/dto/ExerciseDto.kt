package com.corecoders.gymbuddy.data.dto

data class ExerciseDto(
    val exerciseId: String,
    val name: String,
    val imageUrl: String,
    val targetMuscles: List<String>?,
    val bodyParts: List<String>?,
    val equipments: List<String>? = null,
    val exerciseType: String? = null,
    val videoUrl: String? = null,
    val overview: String? = null,
    val instructions: List<String>? = null,
    val exerciseTips: List<String>? = null,
    val variations: List<String>? = null
)