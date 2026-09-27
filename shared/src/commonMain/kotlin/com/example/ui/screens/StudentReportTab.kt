package com.example.ui.screens

import com.example.util.fmt1
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Attendance
import com.example.data.model.DailyEvaluation
import com.example.data.model.MatchLog
import com.example.data.model.Skill
import com.example.data.model.Student
import com.example.data.model.TournamentLog
import com.example.pdf.ReportAnalytics
import com.example.pdf.ReportTimeframe
import com.example.pdf.StudentReport
import com.example.pdf.growthLabel
import com.example.ui.theme.BatchBarColor
import com.example.ui.theme.ScoreBadText
import com.example.ui.theme.ScoreGoodText
import com.example.ui.theme.ScoreMidText
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CourtGreenDark
import com.example.ui.theme.PillTrack
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CourtGreenPrimary
import com.example.ui.theme.ScoreGreen
import com.example.ui.theme.ScoreOrange
import com.example.ui.theme.ScoreRed
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TextSlate

private val OtherPlayerColor = Color(0xFFCBD5E1)

@Composable
fun StudentReportTab(
    student: Student,
    students: List<Student>,
    skills: List<Skill>,
    allEvaluations: List<DailyEvaluation>,
    attendance: List<Attendance>,
    matchLogs: List<MatchLog>,
    tournamentLogs: List<TournamentLog>,
    isGenerating: Boolean,
    onGeneratePdf: (ReportTimeframe) -> Unit
) {
    var timeframe by rememberSaveable { mutableStateOf(ReportTimeframe.WEEKLY) }

    val report = remember(student, students, skills, allEvaluations, attendance, matchLogs, tournamentLogs, timeframe) {
        ReportAnalytics.build(
            student = student,
            students = students.ifEmpty { listOf(student) },
            skills = skills,
            allEvaluations = allEvaluations,
            studentAttendance = attendance,
            studentMatches = matchLogs,
            studentTournaments = tournamentLogs,
            timeframe = timeframe
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReportTimeframe.entries.forEach { tf ->
                FilterChip(
                    selected = timeframe == tf,
                    onClick = { timeframe = tf },
                    label = { Text(tf.shortName, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CourtGreenPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        KpiGrid(report)

        ReportCard(title = "SKILLS vs BATCH AVERAGE", legend = listOf("You" to CourtGreenPrimary, "Batch" to BatchBarColor)) {
            SkillComparisonBars(report)
        }

        ReportCard(title = "WHERE ${student.name.substringBefore(' ').uppercase()} STANDS", legend = listOf("You" to CourtGreenPrimary, "Others" to OtherPlayerColor)) {
            StandingBars(report)
        }

        ReportCard(title = "ATTENDANCE & MATCHES") {
            val a = report.attendance
            val m = report.matchSummary
            SummaryLine("Sessions attended", "${a.present} / ${a.marked}" + (a.percent?.let { "  (${it.toInt()}%)" } ?: ""))
            SummaryLine("Matches played", m.played.toString())
            SummaryLine("Won / Lost", "${m.won} / ${m.lost}" + (m.winPercent?.let { "  (${it.toInt()}% won)" } ?: ""))
            SummaryLine("Singles (W-L)", "${m.singlesWon}-${m.singlesPlayed - m.singlesWon}")
            SummaryLine("Doubles (W-L)", "${m.doublesWon}-${m.doublesPlayed - m.doublesWon}")
            if (report.tournaments.isNotEmpty()) SummaryLine("Tournaments", report.tournaments.size.toString())
        }

        Button(
            onClick = { onGeneratePdf(timeframe) },
            enabled = !isGenerating,
            colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
            } else {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("EXPORT ${timeframe.shortName.uppercase()} PDF & SHARE", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun KpiGrid(r: StudentReport) {
    val tiles = listOf(
        Triple("OVERALL", r.overallAvg?.let { fmt1(it) + "/10" } ?: "--", ReportAnalytics.performanceLabel(r.overallAvg)),
        Triple("GROWTH", r.growth?.let { (if (it >= 0) "+" else "") + fmt1(it) } ?: "--",
            growthLabel(r.growth)),
        Triple("BATCH RANK", r.rank?.let { "#$it of ${r.rankedCount}" } ?: "--", r.batchAvg?.let { "Batch avg ${fmt1(it)}" } ?: "No batch data"),
        Triple("ATTENDANCE", r.attendance.percent?.let { "${it.toInt()}%" } ?: "--", "${r.attendance.present}/${r.attendance.marked} sessions")
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (label, value, sub) ->
                    val valueColor = if (label == "GROWTH" && r.growth != null) {
                        if (r.growth > -0.3) ScoreGoodText else ScoreBadText
                    } else CourtGreenDark
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = TealContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CourtGreenDark.copy(alpha = 0.7f))
                            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = valueColor)
                            Text(sub, fontSize = 11.sp, color = TextSlate, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(
    title: String,
    legend: List<Pair<String, Color>> = emptyList(),
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CourtGreenDark,
                    modifier = Modifier.weight(1f)
                )
                legend.forEach { (label, color) ->
                    Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(label, fontSize = 11.sp, color = TextSlate)
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            content()
        }
    }
}

@Composable
private fun SkillComparisonBars(r: StudentReport) {
    if (r.skillStats.isEmpty()) {
        Text("No skill criteria defined yet.", color = TextSlate, fontSize = 13.sp)
        return
    }
    r.skillStats.forEach { s ->
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(s.skill.skillName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CourtGreenDark,
                    modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(s.studentAvg?.let { fmt1(it) } ?: "--", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = scoreColor(s.studentAvg))
                Text("  / ${s.batchAvg?.let { fmt1(it) } ?: "--"}", fontSize = 12.sp, color = TextSlate)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Bar(fraction = (s.studentAvg ?: 0.0) / 10.0, color = CourtGreenPrimary, height = 8)
            Spacer(modifier = Modifier.height(3.dp))
            Bar(fraction = (s.batchAvg ?: 0.0) / 10.0, color = BatchBarColor, height = 5)
        }
    }
}

@Composable
private fun Bar(fraction: Double, color: Color, height: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .background(PillTrack, RoundedCornerShape(4.dp))
    ) {
        if (fraction > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0.0, 1.0).toFloat())
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun StandingBars(r: StudentReport) {
    if (r.standings.isEmpty()) {
        Text("No assessments recorded in this period.", color = TextSlate, fontSize = 13.sp)
        return
    }
    val me = r.student.id
    val batchAvg = r.batchAvg
    val myColor = CourtGreenPrimary
    val batchLineColor = CourtGreenPrimary
    Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
        val n = r.standings.size
        val slot = size.width / n
        val barW = minOf(slot * 0.6f, 36.dp.toPx())
        r.standings.forEachIndexed { i, s ->
            val h = size.height * (s.avg / 10.0).toFloat()
            val cx = slot * i + slot / 2f
            drawRoundRect(
                color = if (s.studentId == me) myColor else OtherPlayerColor,
                topLeft = Offset(cx - barW / 2f, size.height - h),
                size = Size(barW, h),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
        if (batchAvg != null) {
            val by = size.height - size.height * (batchAvg / 10.0).toFloat()
            drawLine(
                color = batchLineColor,
                start = Offset(0f, by),
                end = Offset(size.width, by),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
            )
        }
    }
    Text(
        text = r.rank?.let { rank ->
            "Rank #$rank of ${r.rankedCount}" + (r.overallAvg?.let { a -> batchAvg?.let { b -> " • ${if (a >= b) "+" else ""}${fmt1(a - b)} vs batch avg (dashed line)" } } ?: "")
        } ?: "No assessments for this player in this period.",
        fontSize = 12.sp,
        color = TextSlate
    )
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, color = TextSlate, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CourtGreenDark)
    }
}

@Composable
private fun scoreColor(v: Double?): Color = when {
    v == null -> TextSlate
    v >= 7.5 -> ScoreGoodText
    v >= 5.0 -> ScoreMidText
    else -> ScoreBadText
}

private fun fmt1(v: Double) = v.fmt1()
