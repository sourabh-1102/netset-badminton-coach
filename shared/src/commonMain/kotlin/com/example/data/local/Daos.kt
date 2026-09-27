package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Attendance
import com.example.data.model.DailyEvaluation
import com.example.data.model.MatchLog
import com.example.data.model.Skill
import com.example.data.model.Student
import com.example.data.model.TournamentLog
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: String): Student?

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentFlowById(id: String): Flow<Student?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: String)

    @Query("DELETE FROM students")
    suspend fun deleteAll()
}

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY displayOrder ASC, skillName ASC")
    fun getAllSkills(): Flow<List<Skill>>

    @Query("SELECT * FROM skills")
    suspend fun getAllSkillsList(): List<Skill>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: Skill)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<Skill>)

    @Update
    suspend fun updateSkill(skill: Skill)

    @Delete
    suspend fun deleteSkill(skill: Skill)

    @Query("DELETE FROM skills")
    suspend fun deleteAll()
}

@Dao
interface DailyEvaluationDao {
    @Query("SELECT * FROM daily_evaluations WHERE studentId = :studentId ORDER BY date DESC")
    fun getEvaluationsForStudent(studentId: String): Flow<List<DailyEvaluation>>

    @Query("SELECT * FROM daily_evaluations WHERE studentId = :studentId AND date = :date LIMIT 1")
    suspend fun getEvaluationForStudentAndDate(studentId: String, date: String): DailyEvaluation?

    @Query("SELECT * FROM daily_evaluations WHERE studentId = :studentId")
    suspend fun getEvaluationsListForStudent(studentId: String): List<DailyEvaluation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(evaluation: DailyEvaluation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluations(evaluations: List<DailyEvaluation>)

    @Query("DELETE FROM daily_evaluations WHERE studentId = :studentId")
    suspend fun deleteEvaluationsForStudent(studentId: String)

    @Query("DELETE FROM daily_evaluations WHERE studentId = :studentId AND date = :date")
    suspend fun deleteEvaluationForStudentAndDate(studentId: String, date: String)

    @Query("SELECT * FROM daily_evaluations")
    fun getAllEvaluations(): Flow<List<DailyEvaluation>>

    @Query("SELECT * FROM daily_evaluations")
    suspend fun getAllEvaluationsList(): List<DailyEvaluation>

    /** One evaluation per student per day: replaces any earlier row for that day. */
    @Transaction
    suspend fun upsertForDay(evaluation: DailyEvaluation) {
        deleteEvaluationForStudentAndDate(evaluation.studentId, evaluation.date)
        insertEvaluation(evaluation)
    }

    @Query("DELETE FROM daily_evaluations")
    suspend fun deleteAll()
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    suspend fun getAttendanceListForDate(date: String): List<Attendance>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: String): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(attendanceList: List<Attendance>)

    @Query("DELETE FROM attendance WHERE studentId = :studentId")
    suspend fun deleteAttendanceForStudent(studentId: String)

    @Query("DELETE FROM attendance WHERE studentId = :studentId AND date = :date")
    suspend fun deleteAttendanceForStudentAndDate(studentId: String, date: String)

    @Query("SELECT * FROM attendance WHERE studentId = :studentId")
    suspend fun getAttendanceListForStudent(studentId: String): List<Attendance>

    /** One attendance mark per student per day: re-marking overwrites instead of duplicating. */
    @Transaction
    suspend fun upsertForDay(attendance: Attendance) {
        deleteAttendanceForStudentAndDate(attendance.studentId, attendance.date)
        insertAttendance(attendance)
    }

    @Query("DELETE FROM attendance")
    suspend fun deleteAll()
}

@Dao
interface MatchLogDao {
    @Query("SELECT * FROM match_logs WHERE studentId = :studentId ORDER BY date DESC")
    fun getMatchLogsForStudent(studentId: String): Flow<List<MatchLog>>

    @Query("SELECT * FROM match_logs ORDER BY date DESC")
    fun getAllMatchLogs(): Flow<List<MatchLog>>

    @Query("SELECT * FROM match_logs")
    suspend fun getAllMatchLogsList(): List<MatchLog>

    @Query("SELECT * FROM match_logs WHERE studentId = :studentId")
    suspend fun getMatchLogsListForStudent(studentId: String): List<MatchLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchLog(matchLog: MatchLog)

    @Delete
    suspend fun deleteMatchLog(matchLog: MatchLog)

    @Query("DELETE FROM match_logs WHERE studentId = :studentId")
    suspend fun deleteMatchLogsForStudent(studentId: String)

    @Query("DELETE FROM match_logs")
    suspend fun deleteAll()
}

@Dao
interface TournamentLogDao {
    @Query("SELECT * FROM tournament_logs WHERE studentId = :studentId ORDER BY date DESC")
    fun getTournamentLogsForStudent(studentId: String): Flow<List<TournamentLog>>

    @Query("SELECT * FROM tournament_logs WHERE studentId = :studentId")
    suspend fun getTournamentLogsListForStudent(studentId: String): List<TournamentLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournamentLog(tournamentLog: TournamentLog)

    @Delete
    suspend fun deleteTournamentLog(tournamentLog: TournamentLog)

    @Query("DELETE FROM tournament_logs WHERE studentId = :studentId")
    suspend fun deleteTournamentLogsForStudent(studentId: String)

    @Query("DELETE FROM tournament_logs")
    suspend fun deleteAll()
}
