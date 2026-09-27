package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AcademyConfig
import com.example.data.model.Attendance
import com.example.data.model.DailyEvaluation
import com.example.data.model.MatchLog
import com.example.data.model.Skill
import com.example.data.model.Student
import com.example.data.model.TournamentLog
import com.example.platform.KeyValueStore
import com.example.platform.PlatformFiles
import com.example.util.Dates
import com.example.util.ScoreJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import kotlin.math.round
import kotlin.random.Random

class BadmintonRepository {
    // Bumped after a restore so every Flow re-subscribes to the reopened database
    private val dbGeneration = MutableStateFlow(0)
    private val db: AppDatabase get() = AppDatabase.get()

    private val prefs = KeyValueStore("academy_prefs")

    private fun <T> dbFlow(query: (AppDatabase) -> Flow<T>): Flow<T> =
        dbGeneration.flatMapLatest { query(db) }

    // Flow Accessors
    val allStudents: Flow<List<Student>> = dbFlow { it.studentDao().getAllStudents() }
    val allSkills: Flow<List<Skill>> = dbFlow { it.skillDao().getAllSkills() }
    val allMatchLogs: Flow<List<MatchLog>> = dbFlow { it.matchLogDao().getAllMatchLogs() }
    val allEvaluations: Flow<List<DailyEvaluation>> = dbFlow { it.dailyEvaluationDao().getAllEvaluations() }

    fun getStudentFlow(studentId: String): Flow<Student?> = dbFlow { it.studentDao().getStudentFlowById(studentId) }

    fun getAttendanceForDate(dateStr: String): Flow<List<Attendance>> =
        dbFlow { it.attendanceDao().getAttendanceForDate(dateStr) }

    fun getAttendanceForStudent(studentId: String): Flow<List<Attendance>> =
        dbFlow { it.attendanceDao().getAttendanceForStudent(studentId) }

    fun getEvaluationsForStudent(studentId: String): Flow<List<DailyEvaluation>> =
        dbFlow { it.dailyEvaluationDao().getEvaluationsForStudent(studentId) }

    fun getMatchLogsForStudent(studentId: String): Flow<List<MatchLog>> =
        dbFlow { it.matchLogDao().getMatchLogsForStudent(studentId) }

    fun getTournamentLogsForStudent(studentId: String): Flow<List<TournamentLog>> =
        dbFlow { it.tournamentLogDao().getTournamentLogsForStudent(studentId) }

    // Students
    suspend fun insertStudent(student: Student) = db.studentDao().insertStudent(student)
    suspend fun updateStudent(student: Student) = db.studentDao().updateStudent(student)
    suspend fun deleteStudent(studentId: String) {
        db.studentDao().deleteStudentById(studentId)
        db.dailyEvaluationDao().deleteEvaluationsForStudent(studentId)
        db.attendanceDao().deleteAttendanceForStudent(studentId)
        db.matchLogDao().deleteMatchLogsForStudent(studentId)
        db.tournamentLogDao().deleteTournamentLogsForStudent(studentId)
    }
    suspend fun getStudentById(id: String): Student? = db.studentDao().getStudentById(id)

    // Skills (drills / criteria)
    suspend fun insertSkill(skill: Skill) = db.skillDao().insertSkill(skill)
    suspend fun updateSkill(skill: Skill) = db.skillDao().updateSkill(skill)
    suspend fun deleteSkill(skill: Skill) = db.skillDao().deleteSkill(skill)
    suspend fun getAllSkillsList(): List<Skill> = db.skillDao().getAllSkillsList()

    // Evaluations: one per student per day
    suspend fun insertEvaluation(evaluation: DailyEvaluation) =
        db.dailyEvaluationDao().upsertForDay(evaluation.copy(id = evaluationId(evaluation.studentId, evaluation.date)))
    suspend fun getEvaluationsListForStudent(studentId: String): List<DailyEvaluation> =
        db.dailyEvaluationDao().getEvaluationsListForStudent(studentId)
    suspend fun getAllEvaluationsList(): List<DailyEvaluation> = db.dailyEvaluationDao().getAllEvaluationsList()

    // Attendance: one mark per student per day
    suspend fun setAttendance(attendance: Attendance) =
        db.attendanceDao().upsertForDay(attendance.copy(id = attendanceId(attendance.studentId, attendance.date)))
    suspend fun saveBatchAttendance(attendanceList: List<Attendance>) = attendanceList.forEach { setAttendance(it) }
    suspend fun getAttendanceListForStudent(studentId: String): List<Attendance> =
        db.attendanceDao().getAttendanceListForStudent(studentId)

    // Matches / tournaments
    suspend fun insertMatchLog(matchLog: MatchLog) = db.matchLogDao().insertMatchLog(matchLog)
    suspend fun deleteMatchLog(matchLog: MatchLog) = db.matchLogDao().deleteMatchLog(matchLog)
    suspend fun getMatchLogsListForStudent(studentId: String): List<MatchLog> = db.matchLogDao().getMatchLogsListForStudent(studentId)

    suspend fun insertTournamentLog(tournamentLog: TournamentLog) = db.tournamentLogDao().insertTournamentLog(tournamentLog)
    suspend fun deleteTournamentLog(tournamentLog: TournamentLog) = db.tournamentLogDao().deleteTournamentLog(tournamentLog)
    suspend fun getTournamentLogsListForStudent(studentId: String): List<TournamentLog> =
        db.tournamentLogDao().getTournamentLogsListForStudent(studentId)

    // Academy Config
    fun getAcademyConfig(): AcademyConfig = AcademyConfig(
        academyName = prefs.getString("academy_name", DEFAULT_ACADEMY_NAME),
        coachName = prefs.getString("coach_name", DEFAULT_COACH_NAME),
        coachPhone = prefs.getString("coach_phone", ""),
        coachEmail = prefs.getString("coach_email", ""),
        logoPath = prefs.getString("academy_logo_path", "")
    )

    fun saveAcademyConfig(config: AcademyConfig) {
        prefs.putString("academy_name", config.academyName)
        prefs.putString("coach_name", config.coachName)
        prefs.putString("coach_phone", config.coachPhone)
        prefs.putString("coach_email", config.coachEmail)
        prefs.putString("academy_logo_path", config.logoPath)
    }

    /** Saves picked image bytes into app storage and returns the new file path. */
    fun saveImage(bytes: ByteArray, prefix: String): String? {
        val path = "${PlatformFiles.filesDir()}/${prefix}_${Dates.nowMillis()}.jpg"
        return if (PlatformFiles.writeBytes(path, bytes)) path else null
    }

    // Default Initialization & Sample Data
    suspend fun initializeDefaultSkillsIfEmpty() {
        if (db.skillDao().getAllSkillsList().isEmpty()) {
            db.skillDao().insertSkills(
                listOf(
                    Skill(id = "skill_smash", skillName = "Smash Accuracy", displayOrder = 1),
                    Skill(id = "skill_footwork", skillName = "Footwork & Agility", displayOrder = 2),
                    Skill(id = "skill_backhand", skillName = "Backhand Clear", displayOrder = 3),
                    Skill(id = "skill_net", skillName = "Net Play & Tumbles", displayOrder = 4),
                    Skill(id = "skill_serve", skillName = "Serve Precision", displayOrder = 5),
                    Skill(id = "skill_stamina", skillName = "Physical Stamina", displayOrder = 6)
                )
            )
        }
    }

    /** True exactly once: on the very first launch with an empty roster, so testers see sample data. */
    suspend fun claimFirstLaunchDemoSeed(): Boolean {
        if (prefs.getBoolean("demo_seed_done", false)) return false
        prefs.putBoolean("demo_seed_done", true)
        return db.studentDao().getAllStudents().first().isEmpty()
    }

    /** Wipes every student, score, attendance and match record (drills reset to defaults). */
    suspend fun clearAllData() {
        db.studentDao().deleteAll()
        db.dailyEvaluationDao().deleteAll()
        db.attendanceDao().deleteAll()
        db.matchLogDao().deleteAll()
        db.tournamentLogDao().deleteAll()
        db.skillDao().deleteAll()
        initializeDefaultSkillsIfEmpty()
    }

    suspend fun seedSampleData() {
        initializeDefaultSkillsIfEmpty()
        val skills = db.skillDao().getAllSkillsList()
        val now = Dates.nowMillis()
        val day = 86_400_000L

        val sampleStudents = listOf(
            Student(id = "s_1", name = "Aarav Patel", age = 15, bloodGroup = "O+", address = "12 Park Avenue, Sector 4", parentPhone = "+91 98765 11111", dateJoined = now - day * 60),
            Student(id = "s_2", name = "Ananya Sen", age = 14, bloodGroup = "A+", address = "88 Green Glen, Phase 2", parentPhone = "+91 98765 22222", dateJoined = now - day * 90),
            Student(id = "s_3", name = "Rohan Verma", age = 16, bloodGroup = "B+", address = "45 Sports Complex Rd", parentPhone = "+91 98765 33333", dateJoined = now - day * 120),
            Student(id = "s_4", name = "Diya Sharma", age = 13, bloodGroup = "AB+", address = "104 Sun City Towers", parentPhone = "+91 98765 44444", dateJoined = now - day * 45),
            Student(id = "s_5", name = "Kabir Singh", age = 17, bloodGroup = "O-", address = "7 Stadium Road, Gate 3", parentPhone = "+91 98765 55555", dateJoined = now - day * 180),
            Student(id = "s_6", name = "Meera Iyer", age = 12, bloodGroup = "B-", address = "23 Lake View Colony", parentPhone = "+91 98765 66666", dateJoined = now - day * 30)
        )
        sampleStudents.forEach { db.studentDao().insertStudent(it) }

        // Attendance for the last 30 days (each student misses a few sessions)
        for (d in 0..29) {
            val dStr = Dates.isoDaysAgo(d)
            sampleStudents.forEachIndexed { idx, student ->
                val present = (d + idx * 3) % (5 + idx) != 0 && !(d == 0 && idx == 3)
                db.attendanceDao().insertAttendance(
                    Attendance(id = attendanceId(student.id, dStr), studentId = student.id, date = dStr, isPresent = present)
                )
            }
        }

        // 8 weeks of evaluations, every other day
        for (i in 0..56 step 2) {
            val evalDateStr = Dates.isoDaysAgo(i)
            for (student in sampleStudents) {
                val scores = skills.associate { skill ->
                    val base = when (student.id) {
                        "s_1" -> 7.5 + ((i % 7) * 0.2)
                        "s_2" -> 8.5 - ((i % 5) * 0.1)
                        "s_3" -> 6.0 + ((i % 10) * 0.3)
                        "s_4" -> 5.5 + ((i % 4) * 0.4)
                        "s_6" -> 4.5 + ((56 - i) * 0.05) // steadily improving newcomer
                        else -> 7.0
                    }
                    val max = skill.scoreRangeMax.toDouble()
                    val score = ((base + Random.nextDouble() * 1.5) / 10.0 * max).coerceIn(1.0, max)
                    skill.id to round(score * 10) / 10.0
                }
                db.dailyEvaluationDao().insertEvaluation(
                    DailyEvaluation(
                        id = evaluationId(student.id, evalDateStr),
                        studentId = student.id,
                        date = evalDateStr,
                        skillScoresJson = ScoreJson.encode(scores),
                        overallNote = "Solid court coverage and active footwork during drill set."
                    )
                )
            }
        }

        val todayStr = Dates.todayIso()
        listOf(
            MatchLog(id = "m_1", studentId = "s_1", date = todayStr, type = "Singles", opponentNames = "Vikram Malhotra", score = "21-18, 19-21, 21-15", outcome = "Win", performanceNotes = "Excellent cross-court smash accuracy in third set."),
            MatchLog(id = "m_2", studentId = "s_1", date = Dates.isoDaysAgo(3), type = "Doubles", partnerName = "Rohan Verma", opponentNames = "Karan / Parth", score = "21-14, 21-16", outcome = "Win", performanceNotes = "Great rotation and net control."),
            MatchLog(id = "m_3", studentId = "s_2", date = todayStr, type = "Singles", opponentNames = "Neha Gupta", score = "21-12, 21-19", outcome = "Win", performanceNotes = "Dominant backhand clear rallies."),
            MatchLog(id = "m_4", studentId = "s_3", date = Dates.isoDaysAgo(2), type = "Singles", opponentNames = "Arjun Kapoor", score = "18-21, 20-22", outcome = "Loss", performanceNotes = "Unforced errors on net drops. Need to focus on patience."),
            MatchLog(id = "m_5", studentId = "s_1", date = Dates.isoDaysAgo(6), type = "Singles", opponentNames = "Kabir Singh", score = "15-21, 21-19, 18-21", outcome = "Loss", performanceNotes = "Tired in the decider; stamina work needed."),
            MatchLog(id = "m_6", studentId = "s_1", date = Dates.isoDaysAgo(10), type = "Singles", opponentNames = "Sahil Mehta", score = "21-11, 21-13", outcome = "Win", performanceNotes = "Controlled the net well."),
            MatchLog(id = "m_7", studentId = "s_2", date = Dates.isoDaysAgo(5), type = "Doubles", partnerName = "Diya Sharma", opponentNames = "Isha / Meera", score = "21-17, 16-21, 21-18", outcome = "Win", performanceNotes = "Strong front-court coverage."),
            MatchLog(id = "m_8", studentId = "s_4", date = Dates.isoDaysAgo(5), type = "Doubles", partnerName = "Ananya Sen", opponentNames = "Isha / Meera", score = "21-17, 16-21, 21-18", outcome = "Win", performanceNotes = "Good serve returns."),
            MatchLog(id = "m_9", studentId = "s_5", date = Dates.isoDaysAgo(6), type = "Singles", opponentNames = "Aarav Patel", score = "21-15, 19-21, 21-18", outcome = "Win", performanceNotes = "Patient rallies paid off."),
            MatchLog(id = "m_10", studentId = "s_3", date = Dates.isoDaysAgo(12), type = "Singles", opponentNames = "Dev Nair", score = "21-19, 21-17", outcome = "Win", performanceNotes = "Improved smash placement.")
        ).forEach { db.matchLogDao().insertMatchLog(it) }

        listOf(
            TournamentLog(id = "t_1", studentId = "s_1", tournamentName = "State Junior Badminton Open", date = Dates.isoDaysAgo(20), category = "Under-17 Singles", roundReached = "Finals", achievements = "Runner Up Silver Medalist"),
            TournamentLog(id = "t_2", studentId = "s_2", tournamentName = "District Academy League", date = Dates.isoDaysAgo(40), category = "Under-15 Girls Singles", roundReached = "Winner", achievements = "Gold Medal & Best Player Trophy")
        ).forEach { db.tournamentLogDao().insertTournamentLog(it) }
    }

    // Backup & Restore
    /** Copies the database into a timestamped backup file and returns its path. */
    suspend fun exportDatabaseBackup(): String? = withContext(Dispatchers.IO) {
        // Close first so all pending WAL pages are checkpointed into the main file
        AppDatabase.closeAndReset()
        val bytes = PlatformFiles.readBytes(PlatformFiles.databasePath(AppDatabase.DATABASE_NAME))
        dbGeneration.value++
        if (bytes == null) return@withContext null
        val path = "${PlatformFiles.cacheDir()}/NetSet_Backup_${Dates.todayIso()}.db"
        if (PlatformFiles.writeBytes(path, bytes)) path else null
    }

    suspend fun restoreDatabase(bytes: ByteArray): Boolean = withContext(Dispatchers.IO) {
        // SQLite files start with this 16-byte header; reject anything else
        if (bytes.size < 16 || bytes.decodeToString(0, 15) != "SQLite format 3") return@withContext false
        AppDatabase.closeAndReset()
        val dbPath = PlatformFiles.databasePath(AppDatabase.DATABASE_NAME)
        PlatformFiles.delete("$dbPath-wal")
        PlatformFiles.delete("$dbPath-shm")
        val ok = PlatformFiles.writeBytes(dbPath, bytes)
        dbGeneration.value++
        ok
    }

    companion object {
        const val DEFAULT_ACADEMY_NAME = "My Badminton Academy"
        const val DEFAULT_COACH_NAME = "Head Coach"

        fun attendanceId(studentId: String, date: String) = "att_${studentId}_$date"
        fun evaluationId(studentId: String, date: String) = "eval_${studentId}_$date"
    }
}
