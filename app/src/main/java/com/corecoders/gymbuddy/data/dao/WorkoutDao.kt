package com.corecoders.gymbuddy.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.corecoders.gymbuddy.data.Workout
import com.corecoders.gymbuddy.data.WorkoutSet
import kotlinx.coroutines.flow.Flow

data class MuscleSetCount(
    val bodyPart: String,
    val setCount: Int
)

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(workoutSet: WorkoutSet)

    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY date DESC")
    fun getAllWorkouts(userId: String): Flow<List<Workout>>

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId")
    fun getSetsforWorkout(workoutId: Int): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Int): Workout?

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId")
    suspend fun getSetsForWorkoutSync(workoutId: Int): List<WorkoutSet>

    // În WorkoutDao.kt
    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId")
    fun getWorkoutsCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(DISTINCT (date / 86400000)) FROM workouts WHERE date >= :startOfWeek AND userId = :userId")
    fun getDistinctWorkoutDaysInRange(startOfWeek: Long, userId: String): Flow<Int>

    @Query("SELECT SUM(ws.weight * ws.reps) FROM workout_sets ws INNER JOIN workouts w ON ws.workoutId = w.id WHERE ws.isCompleted = 1 AND w.userId = :userId")
    fun getTotalVolume(userId: String): Flow<Double?>

    @Query("SELECT MAX(ws.weight) FROM workout_sets ws INNER JOIN workouts w ON ws.workoutId = w.id WHERE ws.isCompleted = 1 AND w.userId = :userId")
    fun getMaxWeightLifted(userId: String): Flow<Double?>

    @Query("SELECT SUM(durationMinutes) FROM workouts WHERE userId = :userId")
    fun getTotalDuration(userId: String): Flow<Int?>

    @Query("SELECT e.bodyPart, COUNT(ws.id) as setCount FROM workout_sets ws INNER JOIN exercises e ON ws.exerciseId = e.id INNER JOIN workouts w ON ws.workoutId = w.id WHERE ws.isCompleted = 1 AND w.userId = :userId GROUP BY e.bodyPart ORDER BY setCount DESC")
    fun getMuscleSetCounts(userId: String): Flow<List<MuscleSetCount>>

    @Query("DELETE FROM workouts")
    suspend fun clearWorkouts()

    @Query("DELETE FROM workout_sets")
    suspend fun clearWorkoutSets()
    
    @Query("UPDATE workouts SET userId = :newUserId WHERE userId = ''")
    suspend fun assignOrphanWorkouts(newUserId: String)

    @Query("""
        SELECT ws.* FROM workout_sets ws 
        INNER JOIN workouts w ON ws.workoutId = w.id 
        WHERE ws.exerciseId = :exerciseId AND w.id = (
            SELECT workoutId FROM workout_sets 
            INNER JOIN workouts ON workout_sets.workoutId = workouts.id 
            WHERE exerciseId = :exerciseId 
            ORDER BY date DESC LIMIT 1
        )
        ORDER BY ws.setNumber ASC
    """)
    suspend fun getLastWorkoutSetsForExercise(exerciseId: String): List<WorkoutSet>
}