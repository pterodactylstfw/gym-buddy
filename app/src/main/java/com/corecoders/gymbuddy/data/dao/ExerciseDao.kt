package com.corecoders.gymbuddy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.corecoders.gymbuddy.data.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<Exercise>)

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :searchQuery || '%'")
    fun searchExercises(searchQuery: String): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<Exercise>>
}