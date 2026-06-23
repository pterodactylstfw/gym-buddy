package com.corecoders.gymbuddy.data.dao

import androidx.room.*
import com.corecoders.gymbuddy.data.Routine
import com.corecoders.gymbuddy.data.RoutineExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllRoutines(userId: String): Flow<List<Routine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    @Delete
    suspend fun deleteRoutine(routine: Routine)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercise(routineExercise: RoutineExercise)

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY `order` ASC")
    fun getExercisesForRoutine(routineId: Int): Flow<List<RoutineExercise>>
    
    @Query("SELECT * FROM routines WHERE id = :routineId")
    fun getRoutineById(routineId: Int): Flow<Routine?>

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId AND exerciseId = :exerciseId")
    suspend fun deleteExerciseFromRoutine(routineId: Int, exerciseId: String)

    @Query("SELECT MAX(`order`) FROM routine_exercises WHERE routineId = :routineId")
    suspend fun getMaxOrderForRoutine(routineId: Int): Int?

    @Query("DELETE FROM routines")
    suspend fun clearRoutines()

    @Query("DELETE FROM routine_exercises")
    suspend fun clearRoutineExercises()
    
    @Query("UPDATE routines SET userId = :newUserId WHERE userId = ''")
    suspend fun assignOrphanRoutines(newUserId: String)
}
