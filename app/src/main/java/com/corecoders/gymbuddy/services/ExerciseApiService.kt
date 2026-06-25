package com.corecoders.gymbuddy.services

import com.corecoders.gymbuddy.data.dto.ExerciseDto
import com.corecoders.gymbuddy.data.dto.ExerciseApiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Model pentru raspunsurile care returneaza doar liste de string-uri
data class StringListResponse(
    val success: Boolean,
    val data: List<String>
)

// Model pentru detalii despre un singur exercițiu
data class SingleExerciseResponse(
    val success: Boolean,
    val data: ExerciseDto
)

// Clasa pentru raspunsul complet
data class CategoryResponse(
    val success: Boolean,
    val data: List<CategoryDto>
)

// Clasa pentru obiectele din interiorul listei data
data class CategoryDto(
    val name: String
)


data class BodyPartResponse(
    val success: Boolean,
    val data: List<BodyPartDto>
)

data class BodyPartDto(
    val name: String,
    val imageUrl: String? = null
)

interface ExerciseApiService {

    // Verifica daca API-ul este online
    @GET("api/v1/liveness")
    suspend fun checkLiveness(): Map<String, Any>

    // Cautare globala
    @GET("api/v1/exercises/search")
    suspend fun searchExercises(
        @Query("search") query: String,
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null
    ): ExerciseApiResponse

    // Detalii despre un exercitiu specificat
    @GET("api/v1/exercises/{exerciseId}")
    suspend fun getExerciseById(
        @Path("exerciseId") exerciseId: String
    ): SingleExerciseResponse

    // Lista completa
    @GET("api/v1/exercises")
    suspend fun getAllExercises(
        @Query("limit") limit: Int = 1000,
        @Query("cursor") cursor: String? = null
    ): ExerciseApiResponse

    // Lista tuturor muschilor
    @GET("api/v1/muscles")
    suspend fun getMuscles(): CategoryResponse

    // Lista tuturor partilor corpului
    @GET("api/v1/bodyparts")
    suspend fun getBodyParts(): BodyPartResponse

    // Lista tuturor echipamentelor
    @GET("api/v1/equipments")
    suspend fun getEquipments(): StringListResponse

    // Lista tipurilor de exercitii
    @GET("api/v1/exercisetypes")
    suspend fun getExerciseTypes(): StringListResponse
}