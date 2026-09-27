package com.example.pdf

import com.example.data.model.Attendance
import com.example.data.model.DailyEvaluation
import com.example.data.model.MatchLog
import com.example.data.model.Skill
import com.example.data.model.Student
import com.example.data.model.TournamentLog
import com.example.util.Dates
import com.example.util.ScoreJson
import kotlinx.datetime.LocalDate

enum class ReportTimeframe(val displayName: String, val shortName: String, val days: Int) {
    WEEKLY("Weekly Report", "7 Days", 7),
    FORTNIGHTLY("15-Day Report", "15 Days", 15),
    MONTHLY("Monthly Report", "30 Days", 30)
}

/** All scores below are normalised to a 0-10 scale so skills with different ranges can be compared. */
data class SkillStat(
    val skill: Skill,
    val studentAvg: Double?,
    val batchAvg: Double?,
    val min: Double?,
    val max: Double?,
    val sessions: Int,
    val growth: Double?
)

data class DailyPoint(val date: String, val studentAvg: Double?, val batchAvg: Double?)

data class StandingEntry(val studentId: String, val name: String, val avg: Double)

data class AttendanceSummary(val present: Int, val absent: Int, val byDate: Map<String, Boolean>) {
    val marked: Int get() = present + absent
    val percent: Double? get() = if (marked > 0) present * 100.0 / marked else null
}

data class MatchSummary(
    val played: Int,
    val won: Int,
    val singlesPlayed: Int,
    val singlesWon: Int,
    val doublesPlayed: Int,
    val doublesWon: Int
) {
    val lost: Int get() = played - won
    val winPercent: Double? get() = if (played > 0) won * 100.0 / played else null
}

data class StudentReport(
    val student: Student,
    val timeframe: ReportTimeframe,
    val periodDates: List<String>,
    val overallAvg: Double?,
    val batchAvg: Double?,
    val growth: Double?,
    val rank: Int?,
    val standings: List<StandingEntry>,
    val skillStats: List<SkillStat>,
    val dailyTrend: List<DailyPoint>,
    val attendance: AttendanceSummary,
    val matchSummary: MatchSummary,
    val matches: List<MatchLog>,
    val tournaments: List<TournamentLog>,
    val coachNotes: List<Pair<String, String>>
) {
    val fromDate: String get() = periodDates.first()
    val toDate: String get() = periodDates.last()
    val rankedCount: Int get() = standings.size
}

object ReportAnalytics {

    private const val BATCH_DRILL_NOTE = "Batch drill evaluation"

    fun periodDates(timeframe: ReportTimeframe, today: LocalDate = Dates.today()): List<String> =
        (timeframe.days - 1 downTo 0).map { back -> Dates.minusDays(today, back).toString() }

    /** Parses an evaluation's JSON into skillId -> score normalised to 0-10. */
    fun normalisedScores(eval: DailyEvaluation, skillsById: Map<String, Skill>): Map<String, Double> {
        val out = mutableMapOf<String, Double>()
        for ((k, v) in ScoreJson.decode(eval.skillScoresJson)) {
            val skill = skillsById[k] ?: continue // skill was deleted
            val max = skill.scoreRangeMax.coerceAtLeast(1)
            out[k] = (v / max * 10.0).coerceIn(0.0, 10.0)
        }
        return out
    }

    fun build(
        student: Student,
        students: List<Student>,
        skills: List<Skill>,
        allEvaluations: List<DailyEvaluation>,
        studentAttendance: List<Attendance>,
        studentMatches: List<MatchLog>,
        studentTournaments: List<TournamentLog>,
        timeframe: ReportTimeframe,
        today: LocalDate = Dates.today()
    ): StudentReport {
        val dates = periodDates(timeframe, today)
        val from = dates.first()
        val to = dates.last()
        val inPeriod: (String) -> Boolean = { it in from..to }
        val skillsById = skills.associateBy { it.id }

        val periodEvals = allEvaluations.filter { inPeriod(it.date) }
        val scoresByEval = periodEvals.associateWith { normalisedScores(it, skillsById) }
        val studentEvals = periodEvals.filter { it.studentId == student.id }.sortedBy { it.date }

        // Per-skill statistics
        val skillStats = skills.map { skill ->
            val mine = studentEvals.mapNotNull { scoresByEval[it]?.get(skill.id) }
            val batch = periodEvals.mapNotNull { scoresByEval[it]?.get(skill.id) }
            SkillStat(
                skill = skill,
                studentAvg = mine.averageOrNull(),
                batchAvg = batch.averageOrNull(),
                min = mine.minOrNull(),
                max = mine.maxOrNull(),
                sessions = mine.size,
                growth = if (mine.size >= 2) mine.last() - mine.first() else null
            )
        }

        // Overall standings: each student's average of every normalised score in the period
        val studentIds = students.map { it.id }.toSet()
        val standings = periodEvals
            .filter { it.studentId in studentIds }
            .groupBy { it.studentId }
            .mapNotNull { (sid, evals) ->
                val all = evals.flatMap { scoresByEval[it]?.values ?: emptyList() }
                val avg = all.averageOrNull() ?: return@mapNotNull null
                StandingEntry(sid, students.first { it.id == sid }.name, avg)
            }
            .sortedByDescending { it.avg }

        val overallAvg = standings.firstOrNull { it.studentId == student.id }?.avg
        val rank = standings.indexOfFirst { it.studentId == student.id }.takeIf { it >= 0 }?.plus(1)
        val batchAvg = periodEvals.flatMap { scoresByEval[it]?.values ?: emptyList() }.averageOrNull()

        // Daily trend (student vs batch)
        val evalsByDate = periodEvals.groupBy { it.date }
        val dailyTrend = dates.map { d ->
            val dayEvals = evalsByDate[d].orEmpty()
            DailyPoint(
                date = d,
                studentAvg = dayEvals.filter { it.studentId == student.id }
                    .flatMap { scoresByEval[it]?.values ?: emptyList() }.averageOrNull(),
                batchAvg = dayEvals.flatMap { scoresByEval[it]?.values ?: emptyList() }.averageOrNull()
            )
        }

        // Growth: average of the later half of the student's sessions minus the earlier half
        val dayAverages = dailyTrend.mapNotNull { it.studentAvg }
        val growth = if (dayAverages.size >= 2) {
            val half = dayAverages.size / 2
            dayAverages.drop(dayAverages.size - half).average() - dayAverages.take(half).average()
        } else null

        // Attendance (latest mark per day wins in case of legacy duplicates)
        val attendanceByDate = studentAttendance.filter { inPeriod(it.date) }
            .groupBy { it.date }
            .mapValues { (_, marks) -> marks.last().isPresent }
        val attendance = AttendanceSummary(
            present = attendanceByDate.count { it.value },
            absent = attendanceByDate.count { !it.value },
            byDate = attendanceByDate
        )

        // Matches
        val matches = studentMatches.filter { inPeriod(it.date) }.sortedByDescending { it.date }
        fun List<MatchLog>.wins() = count { it.outcome.equals("Win", ignoreCase = true) }
        val singles = matches.filter { it.type.equals("Singles", ignoreCase = true) }
        val doubles = matches.filter { it.type.equals("Doubles", ignoreCase = true) }
        val matchSummary = MatchSummary(
            played = matches.size,
            won = matches.wins(),
            singlesPlayed = singles.size,
            singlesWon = singles.wins(),
            doublesPlayed = doubles.size,
            doublesWon = doubles.wins()
        )

        val coachNotes = studentEvals
            .filter { it.overallNote.isNotBlank() && it.overallNote != BATCH_DRILL_NOTE }
            .sortedByDescending { it.date }
            .map { it.date to it.overallNote }
            .distinctBy { it.second }

        return StudentReport(
            student = student,
            timeframe = timeframe,
            periodDates = dates,
            overallAvg = overallAvg,
            batchAvg = batchAvg,
            growth = growth,
            rank = rank,
            standings = standings,
            skillStats = skillStats,
            dailyTrend = dailyTrend,
            attendance = attendance,
            matchSummary = matchSummary,
            matches = matches,
            tournaments = studentTournaments.filter { inPeriod(it.date) }.sortedByDescending { it.date },
            coachNotes = coachNotes
        )
    }

    fun performanceLabel(avg: Double?): String = when {
        avg == null -> "No data"
        avg >= 9.0 -> "Excellent"
        avg >= 7.5 -> "Good"
        avg >= 5.0 -> "Average"
        else -> "Needs focus"
    }

    private fun List<Double>.averageOrNull(): Double? = if (isEmpty()) null else average()
}

/** Changes within +/-0.3 points are treated as steady so small day-to-day noise isn't flagged. */
fun growthLabel(growth: Double?): String = when {
    growth == null -> "Need 2+ sessions"
    growth >= 0.3 -> "Improving"
    growth > -0.3 -> "Steady"
    else -> "Dropping"
}
