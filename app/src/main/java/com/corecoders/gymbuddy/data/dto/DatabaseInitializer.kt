package com.corecoders.gymbuddy.data.dto

import android.content.Context
import com.corecoders.gymbuddy.data.Exercise
import com.corecoders.gymbuddy.data.dao.ExerciseDao
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ExerciseDTO(
    val id: String,
    val name: String,
    val primaryMuscles: List<String>,
    val images: List<String>
)

object DatabaseInitializer {
    fun populateDatabase(context: Context, exerciseDao: ExerciseDao) {
        CoroutineScope(Dispatchers.IO).launch {
            val currentExercises = exerciseDao.getAllExercises().first()

            if (currentExercises.isEmpty()) {
                val jsonString =
                    context.assets.open("exercises.json").bufferedReader().use { it.readText() }

                val type = object : TypeToken<List<ExerciseDTO>>() {}.type
                val dtoList: List<ExerciseDTO> = Gson().fromJson(jsonString, type)

                val roomExercises = dtoList.map { dto ->
                    Exercise(
                        id = dto.id,
                        name = dto.name,
                        targetMuscle = dto.primaryMuscles.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                        gifUrl = if (dto.images.isNotEmpty()) {
                            "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/${dto.images[0]}"
                        } else {
                            ""
                        }
                    )
                }
                exerciseDao.insertAll(roomExercises)
                println("SUCCESS: Am adăugat ${roomExercises.size} exerciții în baza de date!")
            }
        }
    }
}
