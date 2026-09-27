package com.example.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.data.model.Attendance
import com.example.data.model.DailyEvaluation
import com.example.data.model.MatchLog
import com.example.data.model.Skill
import com.example.data.model.Student
import com.example.data.model.TournamentLog
import com.example.platform.PlatformFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [
        Student::class,
        Skill::class,
        DailyEvaluation::class,
        Attendance::class,
        MatchLog::class,
        TournamentLog::class
    ],
    version = 1,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun skillDao(): SkillDao
    abstract fun dailyEvaluationDao(): DailyEvaluationDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun matchLogDao(): MatchLogDao
    abstract fun tournamentLogDao(): TournamentLogDao

    companion object {
        const val DATABASE_NAME = "badminton_coach.db"

        private var instance: AppDatabase? = null

        fun get(): AppDatabase = instance ?: open().also { instance = it }

        private fun open(): AppDatabase =
            platformDatabaseBuilder(PlatformFiles.databasePath(DATABASE_NAME))
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()

        /** Closes the open database so its file can be replaced (used by restore). */
        fun closeAndReset() {
            instance?.close()
            instance = null
        }
    }
}

/** Android needs a Context to build the database; iOS only needs the path. */
expect fun platformDatabaseBuilder(path: String): RoomDatabase.Builder<AppDatabase>

// Room generates the actual implementations for each platform
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
