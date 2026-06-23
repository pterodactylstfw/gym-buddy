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
    version = 5,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun workoutDao() : WorkoutDao
    abstract fun exerciseDao() : ExerciseDao
    abstract fun routineDao() : RoutineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workouts ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE routines ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gymbuddy_database"
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
