package com.corecoders.gymbuddy.services

import ExerciseDto
import com.corecoders.gymbuddy.data.dto.ExerciseApiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Model pentru răspunsurile care returnează doar liste de string-uri (muscles, bodyparts, etc.)
data class StringListResponse(
    val success: Boolean,
    val data: List<String>
)

// Model pentru detalii despre un singur exercițiu
data class SingleExerciseResponse(
    val success: Boolean,
    val data: ExerciseDto
)

// Clasa pentru răspunsul complet
data class CategoryResponse(
    val success: Boolean,
    val data: List<CategoryDto>
)

// Clasa pentru obiectele din interiorul listei "data"
data class CategoryDto(
    val name: String
)


data class BodyPartResponse(
    val success: Boolean,
    val data: List<BodyPartDto>
)

data class BodyPartDto(
    val name: String,
    val imageUrl: String? = null // Îl punem opțional, în caz că vrem să afișăm iconițe în viitor
)

interface ExerciseApiService {

    // 1. Verifică dacă API-ul este online
    @GET("api/v1/liveness")
    suspend fun checkLiveness(): Map<String, Any>

    // 2. Căutare globală (ex: "chest press")
    @GET("api/v1/exercises/search")
    suspend fun searchExercises(
        @Query("search") query: String,
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null
    ): ExerciseApiResponse

    // 3. Detalii despre un exercițiu specific după ID
    @GET("api/v1/exercises/{exerciseId}")
    suspend fun getExerciseById(
        @Path("exerciseId") exerciseId: String
    ): SingleExerciseResponse

    // 4. Lista completă / Filtrare după nume sau keywords
    @GET("api/v1/exercises")
    suspend fun getAllExercises(
        @Query("limit") limit: Int = 1000,
        @Query("cursor") cursor: String? = null
    ): ExerciseApiResponse

    // 5. Lista tuturor mușchilor
    @GET("api/v1/muscles")
    suspend fun getMuscles(): CategoryResponse

    // 6. Lista tuturor părților corpului
    @GET("api/v1/bodyparts")
    suspend fun getBodyParts(): BodyPartResponse

    // 7. Lista tuturor echipamentelor (Dumbbell, Barbell, etc.)
    @GET("api/v1/equipments")
    suspend fun getEquipments(): StringListResponse

    // 8. Lista tipurilor de exerciții (Strength, Cardio, etc.)
    @GET("api/v1/exercisetypes")
    suspend fun getExerciseTypes(): StringListResponse
}