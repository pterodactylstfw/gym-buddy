package com.corecoders.gymbuddy.data.dto

import ExerciseDto

// Sau unde ții tu modelele

data class ExerciseApiResponse(
    val success: Boolean,
    val meta: MetaDto, // Aceasta lipsea probabil!
    val data: List<ExerciseDto>
)

data class MetaDto(
    val total: Int,
    val hasNextPage: Boolean,
    val nextCursor: String?
)