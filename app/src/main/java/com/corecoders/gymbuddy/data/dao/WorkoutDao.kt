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

    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId")
    fun getSetsforWorkout(workoutId: Int): Flow<List<WorkoutSet>>

    // În WorkoutDao.kt
    @Query("SELECT COUNT(*) FROM workouts")
    fun getWorkoutsCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT (date / 86400000)) FROM workouts WHERE date >= :startOfWeek")
    fun getDistinctWorkoutDaysInRange(startOfWeek: Long): Flow<Int>

    @Query("SELECT SUM(weight * reps) FROM workout_sets WHERE isCompleted = 1")
    fun getTotalVolume(): Flow<Double?>

    @Query("SELECT MAX(weight) FROM workout_sets WHERE isCompleted = 1")
    fun getMaxWeightLifted(): Flow<Double?>

    @Query("SELECT SUM(durationMinutes) FROM workouts")
    fun getTotalDuration(): Flow<Int?>

    @Query("""
        SELECT e.bodyPart, COUNT(ws.id) as setCount
        FROM workout_sets ws
        INNER JOIN exercises e ON ws.exerciseId = e.id
        WHERE ws.isCompleted = 1
        GROUP BY e.bodyPart
        ORDER BY setCount DESC
    """)
    fun getMuscleSetCounts(): Flow<List<MuscleSetCount>>
}