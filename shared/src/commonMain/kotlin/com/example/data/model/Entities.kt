package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.util.Dates
import kotlin.uuid.Uuid

@Entity(tableName = "students")
data class Student(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val name: String,
    val age: Int,
    val bloodGroup: String,
    val address: String,
    val parentPhone: String,
    val profilePicPath: String = "",
    val dateJoined: Long = Dates.nowMillis()
)

@Entity(tableName = "skills")
data class Skill(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val skillName: String,
    val scoreRangeMin: Int = 1,
    val scoreRangeMax: Int = 10,
    val displayOrder: Int = 0
)

@Entity(tableName = "daily_evaluations")
data class DailyEvaluation(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val studentId: String,
    val date: String, // ISO date format: yyyy-MM-dd
    val skillScoresJson: String, // JSON representation of Map<String, Double>
    val overallNote: String = ""
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val studentId: String,
    val date: String, // ISO date format: yyyy-MM-dd
    val isPresent: Boolean
)

@Entity(tableName = "match_logs")
data class MatchLog(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val studentId: String,
    val date: String, // ISO date format: yyyy-MM-dd
    val type: String, // "Singles" or "Doubles"
    val partnerName: String? = null,
    val opponentNames: String,
    val score: String, // e.g., "21-18, 19-21, 21-15"
    val outcome: String, // "Win" or "Loss"
    val performanceNotes: String = ""
)

@Entity(tableName = "tournament_logs")
data class TournamentLog(
    @PrimaryKey val id: String = Uuid.random().toString(),
    val studentId: String,
    val tournamentName: String,
    val date: String, // ISO date format: yyyy-MM-dd
    val category: String,
    val roundReached: String,
    val achievements: String = ""
)

data class AcademyConfig(
    val academyName: String = "My Badminton Academy",
    val coachName: String = "Head Coach",
    val coachPhone: String = "",
    val coachEmail: String = "",
    val logoPath: String = ""
)
