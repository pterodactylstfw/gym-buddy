package com.corecoders.gymbuddy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.corecoders.gymbuddy.data.dao.ExerciseDao
import com.corecoders.gymbuddy.data.dao.RoutineDao
import com.corecoders.gymbuddy.data.dao.WorkoutDao

@Database(
    entities = [Workout::class, WorkoutSet::class, Exercise::class, Routine::class, RoutineExercise::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun workoutDao() : WorkoutDao
    abstract fun exerciseDao() : ExerciseDao
    abstract fun routineDao() : RoutineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gymbuddy_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
