package com.corecoders.gymbuddy.data.dto

data class ExerciseApiResponse(
    val success: Boolean,
    val meta: MetaDto,
    val data: List<ExerciseDto>
)

data class MetaDto(
    val total: Int,
    val hasNextPage: Boolean,
    val nextCursor: String?
)