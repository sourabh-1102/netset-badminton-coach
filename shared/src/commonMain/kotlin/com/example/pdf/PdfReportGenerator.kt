package com.example.pdf

import com.example.data.model.AcademyConfig
import com.example.platform.ImageClip
import com.example.platform.PdfCanvas
import com.example.platform.PdfWriter
import com.example.platform.PlatformFiles
import com.example.platform.createPdfWriter
import com.example.resources.Res
import com.example.util.Dates
import com.example.util.fmt1
import org.jetbrains.compose.resources.ExperimentalResourceApi

/** Draws the student performance report; the same layout is used on Android and iOS. */
class PdfReportGenerator {

    private val pageW = 595f
    private val pageH = 842f
    private val margin = 28f
    private val contentRight = pageW - margin
    private val footerTop = pageH - 62f

    private val white = 0xFFFFFFFF.toInt()
    private val black = 0xFF000000.toInt()
    private val darkGray = 0xFF444444.toInt()
    private val navy = 0xFF14284B.toInt()
    private val teal = 0xFF2F7F8F.toInt()
    private val tealLight = 0xFFA9CDD4.toInt()
    private val otherGray = 0xFFCBD5E1.toInt()
    private val tileBg = 0xFFF1F5F9.toInt()
    private val border = 0xFFE2E8F0.toInt()
    private val textPrimary = 0xFF1E293B.toInt()
    private val textSecondary = 0xFF64748B.toInt()
    private val headerSub = 0xFFDCE6F2.toInt()
    private val headerSub2 = 0xFFB8C7DA.toInt()
    private val green = 0xFF15803D.toInt()
    private val red = 0xFFB91C1C.toInt()
    private val amber = 0xFFB45309.toInt()

    private lateinit var writer: PdfWriter
    private lateinit var c: PdfCanvas
    private var pageNo = 0
    private var y = 0f
    private lateinit var report: StudentReport
    private lateinit var academy: AcademyConfig
    private var nexonvateMark: ByteArray? = null

    @OptIn(ExperimentalResourceApi::class)
    suspend fun generateReport(report: StudentReport, academyConfig: AcademyConfig): String? {
        return try {
            this.report = report
            this.academy = academyConfig
            nexonvateMark = try { Res.readBytes("drawable/nexonvate_mark.png") } catch (_: Exception) { null }
            writer = createPdfWriter()
            pageNo = 0

            newPage(fullHeader = true)
            drawTitleStrip()
            drawKpiTiles()
            drawSkillComparisonChart()
            drawTrendChart()
            drawStandingChart()
            drawSkillTable()
            drawMatchSection()
            drawAttendanceStrip()
            drawTournaments()
            drawCoachNotes()
            finishPage()

            val safeName = report.student.name.replace(Regex("[^A-Za-z0-9]+"), "_")
            val path = "${PlatformFiles.cacheDir()}/NetSet_${safeName}_${report.timeframe.name}_${report.toDate}.pdf"
            if (writer.saveTo(path)) path else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ---------------------------------------------------------------- pages

    private fun newPage(fullHeader: Boolean) {
        pageNo++
        c = writer.startPage(pageW, pageH)
        y = if (fullHeader) drawFullHeader() else drawCompactHeader()
    }

    private fun finishPage() {
        drawFooter()
        writer.finishPage()
    }

    /** Starts a new page when the next block of [height] would run into the footer. */
    private fun ensureSpace(height: Float) {
        if (y + height > footerTop - 10f) {
            finishPage()
            newPage(fullHeader = false)
        }
    }

    // ---------------------------------------------------------------- header

    private fun drawFullHeader(): Float {
        val h = 108f
        c.rect(0f, 0f, pageW, h, navy)
        c.rect(0f, h - 4f, pageW, h, teal)

        // LEFT: coaching academy + coach
        var textX = margin
        imageBytes(academy.logoPath)?.let { logo ->
            c.rect(margin, 22f, margin + 60f, 82f, white)
            c.image(logo, margin, 22f, margin + 60f, 82f, ImageClip.ROUNDED, radius = 10f)
            textX = margin + 72f
        }
        val leftMax = pageW / 2f - textX + 30f
        text(academy.academyName.ifBlank { "Badminton Academy" }, textX, 44f, 17f, white, bold = true, maxWidth = leftMax)
        text("Coach: ${academy.coachName}", textX, 62f, 10.5f, headerSub, maxWidth = leftMax)
        val contact = listOf(academy.coachPhone, academy.coachEmail).filter { it.isNotBlank() }.joinToString("  |  ")
        if (contact.isNotEmpty()) text(contact, textX, 77f, 8.5f, headerSub2, maxWidth = leftMax)

        // RIGHT: student photo + name
        val photoSize = 66f
        val photoLeft = contentRight - photoSize
        val photo = imageBytes(report.student.profilePicPath)
        if (photo != null) {
            c.image(photo, photoLeft, 20f, contentRight, 20f + photoSize, ImageClip.CIRCLE)
        } else {
            c.oval(photoLeft, 20f, contentRight, 20f + photoSize, teal)
            val initials = report.student.name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
            textCenter(initials.ifEmpty { "P" }, photoLeft + photoSize / 2f, 20f + photoSize / 2f + 8f, 22f, white, bold = true)
        }
        c.ovalStroke(photoLeft, 20f, contentRight, 20f + photoSize, white, 2f)

        val nameRight = photoLeft - 12f
        val rightMax = pageW / 2f - 70f
        textRight(report.student.name, nameRight, 46f, 15f, white, bold = true, maxWidth = rightMax)
        textRight("Age ${report.student.age}  |  Blood ${report.student.bloodGroup}", nameRight, 62f, 9f, headerSub)
        textRight("Joined ${Dates.pretty(Dates.fromMillis(report.student.dateJoined))}", nameRight, 76f, 9f, headerSub2)
        return h + 18f
    }

    private fun drawCompactHeader(): Float {
        val h = 36f
        c.rect(0f, 0f, pageW, h, navy)
        text(academy.academyName, margin, 23f, 10.5f, white, bold = true, maxWidth = 260f)
        textRight("${report.student.name}  |  ${report.timeframe.displayName}", contentRight, 23f, 10f, white, maxWidth = 260f)
        return h + 22f
    }

    private fun drawTitleStrip() {
        text(report.timeframe.displayName.uppercase() + " - PERFORMANCE ANALYSIS", margin, y, 12.5f, navy, bold = true)
        textRight(
            "${Dates.pretty(report.fromDate)} - ${Dates.pretty(report.toDate)}   |   Generated ${Dates.pretty(Dates.today())}",
            contentRight, y, 8.5f, textSecondary
        )
        y += 14f
    }

    // ---------------------------------------------------------------- KPIs

    private fun drawKpiTiles() {
        val gap = 8f
        val count = 5
        val w = (contentRight - margin - gap * (count - 1)) / count
        val h = 64f
        val r = report
        val tiles = listOf(
            Triple("OVERALL SCORE", r.overallAvg?.let { it.fmt1() + "/10" } ?: "--", ReportAnalytics.performanceLabel(r.overallAvg)),
            Triple("GROWTH", r.growth?.let { (if (it >= 0) "+" else "") + it.fmt1() } ?: "--", growthLabel(r.growth)),
            Triple("BATCH RANK", r.rank?.let { "#$it / ${r.rankedCount}" } ?: "--", r.batchAvg?.let { "Batch avg ${it.fmt1()}" } ?: "No batch data"),
            Triple("ATTENDANCE", r.attendance.percent?.let { "${it.toInt()}%" } ?: "--", "${r.attendance.present} of ${r.attendance.marked} sessions"),
            Triple(
                "MATCHES",
                "${r.matchSummary.won}W - ${r.matchSummary.lost}L",
                "${r.matchSummary.played} played" + (r.matchSummary.winPercent?.let { " | ${it.toInt()}% won" } ?: "")
            )
        )
        tiles.forEachIndexed { i, (label, value, sub) ->
            val left = margin + i * (w + gap)
            c.roundRect(left, y, left + w, y + h, 8f, tileBg)
            c.roundRect(left, y, left + 3f, y + h, 2f, if (i == 1 && (r.growth ?: 0.0) <= -0.3) red else teal)
            text(label, left + 10f, y + 16f, 7.5f, textSecondary, bold = true, maxWidth = w - 14f)
            val valueColor = if (i == 1 && r.growth != null) (if (r.growth > -0.3) green else red) else navy
            text(value, left + 10f, y + 38f, 16f, valueColor, bold = true, maxWidth = w - 14f)
            text(sub, left + 10f, y + 53f, 7.5f, textSecondary, maxWidth = w - 14f)
        }
        y += h + 22f
    }

    // ---------------------------------------------------------------- charts

    private fun sectionTitle(title: String, legend: List<Pair<String, Int>> = emptyList()) {
        text(title, margin, y, 11f, navy, bold = true)
        var lx = contentRight
        legend.reversed().forEach { (label, color) ->
            lx -= c.measureText(label, 8f)
            text(label, lx, y, 8f, textSecondary)
            c.roundRect(lx - 14f, y - 7f, lx - 4f, y + 1f, 2f, color)
            lx -= 26f
        }
        y += 6f
        c.line(margin, y, contentRight, y, border, 1f)
        y += 12f
    }

    private fun drawSkillComparisonChart() {
        val stats = report.skillStats
        val rowH = 24f
        ensureSpace(40f + stats.size * rowH + 20f)
        sectionTitle("SKILL SCORES vs BATCH AVERAGE (out of 10)", listOf("This player" to navy, "Batch average" to tealLight))
        if (stats.isEmpty()) {
            text("No skill criteria defined yet.", margin, y + 6f, 9f, textSecondary); y += 24f; return
        }
        val labelW = 128f
        val barL = margin + labelW
        val barR = contentRight - 64f
        val barW = barR - barL
        val top = y

        for (g in 0..5) {
            val gx = barL + barW * g / 5f
            c.line(gx, top - 4f, gx, top + stats.size * rowH, border, 0.7f)
        }
        stats.forEachIndexed { i, s ->
            val ry = top + i * rowH
            text(s.skill.skillName, margin, ry + 12f, 9f, textPrimary, maxWidth = labelW - 8f)
            s.studentAvg?.let { c.roundRect(barL, ry + 3f, barL + barW * (it / 10.0).toFloat(), ry + 11f, 3f, navy) }
            s.batchAvg?.let { c.roundRect(barL, ry + 13f, barL + barW * (it / 10.0).toFloat(), ry + 18f, 2f, tealLight) }
            text(s.studentAvg?.fmt1() ?: "--", barR + 8f, ry + 11f, 9.5f, scoreColor(s.studentAvg), bold = true)
            text("/ ${s.batchAvg?.fmt1() ?: "--"}", barR + 28f, ry + 11f, 8f, textSecondary)
        }
        y = top + stats.size * rowH
        for (g in 0..5) textCenter((g * 2).toString(), barL + barW * g / 5f, y + 9f, 7.5f, textSecondary)
        y += 26f
    }

    private fun drawTrendChart() {
        val chartH = 120f
        ensureSpace(chartH + 60f)
        sectionTitle("DAILY PERFORMANCE TREND", listOf("This player" to navy, "Batch average" to teal))
        val left = margin + 22f
        val right = contentRight - 6f
        val top = y
        val bottom = y + chartH
        val pts = report.dailyTrend
        val n = pts.size
        fun xAt(i: Int) = if (n <= 1) (left + right) / 2f else left + (right - left) * i / (n - 1)
        fun yAt(v: Double) = bottom - (bottom - top) * (v / 10.0).toFloat()

        for (g in 0..5) {
            val gy = yAt(g * 2.0)
            c.line(left, gy, right, gy, border, 0.7f)
            textRight((g * 2).toString(), left - 5f, gy + 3f, 7.5f, textSecondary)
        }

        if (pts.none { it.studentAvg != null || it.batchAvg != null }) {
            textCenter("No assessments recorded in this period", (left + right) / 2f, (top + bottom) / 2f, 9.5f, textSecondary)
        } else {
            val batch = pts.mapIndexedNotNull { i, p -> p.batchAvg?.let { xAt(i) to yAt(it) } }
            if (batch.size > 1) c.polyline(batch, teal, 1.6f, dashed = true)
            val mine = pts.mapIndexedNotNull { i, p -> p.studentAvg?.let { xAt(i) to yAt(it) } }
            if (mine.size > 1) c.polyline(mine, navy, 2.2f)
            mine.forEach { (px, py) ->
                c.circle(px, py, 3.2f, navy)
                c.circle(px, py, 1.4f, white)
            }
        }

        val step = maxOf(1, (n + 7) / 8)
        for (i in 0 until n step step) textCenter(Dates.pretty(pts[i].date, short = true), xAt(i), bottom + 12f, 7.5f, textSecondary)
        y = bottom + 26f
    }

    private fun drawStandingChart() {
        val standings = report.standings
        val chartH = 90f
        ensureSpace(chartH + 58f)
        sectionTitle("WHERE YOU STAND IN THE BATCH", listOf("This player" to navy, "Other players" to otherGray))
        if (standings.isEmpty()) {
            text("No assessments recorded in this period.", margin, y + 6f, 9f, textSecondary); y += 26f; return
        }
        val left = margin + 22f
        val right = contentRight
        val bottom = y + chartH
        for (g in 0..5) {
            val gy = bottom - chartH * g / 5f
            c.line(left, gy, right, gy, border, 0.7f)
            textRight((g * 2).toString(), left - 5f, gy + 3f, 7.5f, textSecondary)
        }
        val slot = (right - left) / standings.size
        val barW = minOf(34f, slot * 0.62f)
        standings.forEachIndexed { i, s ->
            val cx = left + slot * i + slot / 2f
            val bh = chartH * (s.avg / 10.0).toFloat()
            val mine = s.studentId == report.student.id
            c.roundRect(cx - barW / 2f, bottom - bh, cx + barW / 2f, bottom, 3f, if (mine) navy else otherGray)
            if (mine) {
                textCenter(s.avg.fmt1(), cx, bottom - bh - 4f, 8.5f, navy, bold = true)
                textCenter("YOU", cx, bottom + 11f, 7.5f, navy, bold = true)
            } else if (standings.size <= 20) {
                textCenter("#${i + 1}", cx, bottom + 11f, 7f, textSecondary)
            }
        }
        report.batchAvg?.let { avg ->
            val by = bottom - chartH * (avg / 10.0).toFloat()
            c.line(left, by, right, by, teal, 1.2f, dashed = true)
            textRight("batch avg ${avg.fmt1()}", right, by - 3f, 7.5f, teal)
        }
        y = bottom + 28f
        val summary = report.rank?.let {
            "Ranked #$it out of ${report.rankedCount} assessed players in this period" +
                (report.overallAvg?.let { a -> report.batchAvg?.let { b -> " (${if (a >= b) "+" else ""}${(a - b).fmt1()} vs batch average)." } } ?: ".")
        } ?: "This player has no assessments in this period."
        text(summary, margin, y, 9f, textPrimary)
        y += 22f
    }

    // ---------------------------------------------------------------- tables

    private fun tableHeader(cols: List<Pair<String, Float>>) {
        c.rect(margin, y, contentRight, y + 18f, navy)
        cols.forEach { (t, x) -> text(t, x, y + 12.5f, 7.8f, white, bold = true) }
        y += 18f
    }

    private fun tableRowBg(index: Int, h: Float) {
        c.rect(margin, y, contentRight, y + h, if (index % 2 == 0) white else tileBg)
        c.line(margin, y + h, contentRight, y + h, border, 0.6f)
    }

    private fun drawSkillTable() {
        val stats = report.skillStats
        if (stats.isEmpty()) return
        ensureSpace(60f + stats.size * 18f)
        sectionTitle("DETAILED SKILL BREAKDOWN")
        val cols = listOf("SKILL" to margin + 8f, "AVG" to 230f, "MIN" to 285f, "MAX" to 330f, "BATCH" to 375f, "VS BATCH" to 425f, "SESSIONS" to 495f)
        tableHeader(cols)
        stats.forEachIndexed { i, s ->
            tableRowBg(i, 18f)
            val ty = y + 12.5f
            text(s.skill.skillName, cols[0].second, ty, 8.5f, textPrimary, maxWidth = 190f)
            text(s.studentAvg?.fmt1() ?: "--", cols[1].second, ty, 8.5f, scoreColor(s.studentAvg), bold = true)
            text(s.min?.fmt1() ?: "--", cols[2].second, ty, 8.5f, textPrimary)
            text(s.max?.fmt1() ?: "--", cols[3].second, ty, 8.5f, textPrimary)
            text(s.batchAvg?.fmt1() ?: "--", cols[4].second, ty, 8.5f, textPrimary)
            val diff = if (s.studentAvg != null && s.batchAvg != null) s.studentAvg - s.batchAvg else null
            text(
                diff?.let { (if (it >= 0) "+" else "") + it.fmt1() } ?: "--", cols[5].second, ty, 8.5f,
                if (diff == null) textSecondary else if (diff >= 0) green else red, bold = diff != null
            )
            text(s.sessions.toString(), cols[6].second, ty, 8.5f, textPrimary)
            y += 18f
        }
        y += 22f
    }

    private fun drawMatchSection() {
        val m = report.matchSummary
        ensureSpace(90f)
        sectionTitle("MATCH RECORD")
        val line = "Played ${m.played}   |   Won ${m.won}   |   Lost ${m.lost}" +
            (m.winPercent?.let { "   |   Win rate ${it.toInt()}%" } ?: "") +
            "   |   Singles ${m.singlesWon}-${m.singlesPlayed - m.singlesWon}   |   Doubles ${m.doublesWon}-${m.doublesPlayed - m.doublesWon}"
        text(line, margin, y + 2f, 9f, textPrimary, bold = true)
        y += 14f
        if (report.matches.isEmpty()) {
            text("No matches recorded in this period.", margin, y + 6f, 9f, textSecondary); y += 26f; return
        }
        val cols = listOf("DATE" to margin + 8f, "TYPE" to 100f, "OPPONENT / PARTNER" to 160f, "SCORE" to 370f, "RESULT" to 500f)
        tableHeader(cols)
        report.matches.forEachIndexed { i, match ->
            if (y + 18f > footerTop - 10f) {
                ensureSpace(40f); tableHeader(cols)
            }
            tableRowBg(i, 18f)
            val ty = y + 12.5f
            text(Dates.pretty(match.date), cols[0].second, ty, 8.3f, textPrimary)
            text(match.type, cols[1].second, ty, 8.3f, textPrimary)
            val who = "vs ${match.opponentNames}" + (match.partnerName?.takeIf { it.isNotBlank() }?.let { "  (w/ $it)" } ?: "")
            text(who, cols[2].second, ty, 8.3f, textPrimary, maxWidth = 200f)
            text(match.score, cols[3].second, ty, 8.3f, textPrimary, maxWidth = 122f)
            val win = match.outcome.equals("Win", true)
            text(match.outcome.uppercase(), cols[4].second, ty, 8.3f, if (win) green else red, bold = true)
            y += 18f
        }
        y += 22f
    }

    private fun drawAttendanceStrip() {
        val dates = report.periodDates
        val perRow = 15
        val rows = (dates.size + perRow - 1) / perRow
        ensureSpace(50f + rows * 34f)
        sectionTitle("ATTENDANCE", listOf("Present" to green, "Absent" to red, "Not marked" to otherGray))
        val cell = (contentRight - margin) / perRow
        dates.forEachIndexed { i, d ->
            val x = margin + (i % perRow) * cell
            val top = y + (i / perRow) * 34f
            val status = report.attendance.byDate[d]
            c.roundRect(x + 3f, top, x + cell - 3f, top + 16f, 3f, when (status) { true -> green; false -> red; null -> otherGray })
            textCenter(if (status == true) "P" else if (status == false) "A" else "-", x + cell / 2f, top + 11.5f, 8f, white, bold = true)
            textCenter(Dates.pretty(d, short = true), x + cell / 2f, top + 26f, 6.8f, textSecondary)
        }
        y += rows * 34f + 4f
        val a = report.attendance
        text(
            "Present ${a.present}  |  Absent ${a.absent}  |  Attendance ${a.percent?.let { "${it.toInt()}%" } ?: "--"}",
            margin, y + 4f, 9f, textPrimary, bold = true
        )
        y += 26f
    }

    private fun drawTournaments() {
        if (report.tournaments.isEmpty()) return
        ensureSpace(40f + report.tournaments.size * 16f)
        sectionTitle("TOURNAMENTS")
        report.tournaments.forEach { t ->
            ensureSpace(16f)
            text(
                "${Dates.pretty(t.date)}  -  ${t.tournamentName} (${t.category}): ${t.roundReached}" +
                    (if (t.achievements.isNotBlank()) ". ${t.achievements}" else ""),
                margin, y + 4f, 8.8f, textPrimary, maxWidth = contentRight - margin
            )
            y += 16f
        }
        y += 14f
    }

    private fun drawCoachNotes() {
        val notes = report.coachNotes.take(6)
        if (notes.isEmpty()) return
        ensureSpace(40f + notes.size * 16f)
        sectionTitle("COACH'S REMARKS")
        notes.forEach { (d, note) ->
            ensureSpace(16f)
            text(Dates.pretty(d, short = true), margin, y + 4f, 8.5f, teal, bold = true)
            text(note, margin + 48f, y + 4f, 8.8f, textPrimary, maxWidth = contentRight - margin - 48f)
            y += 16f
        }
        y += 10f
    }

    // ---------------------------------------------------------------- footer

    private fun drawFooter() {
        val top = footerTop
        c.line(margin, top, contentRight, top, border, 1f)

        nexonvateMark?.let { c.image(it, margin, top + 11f, margin + 26f, top + 39f, ImageClip.NONE, centerCrop = false) }
        val tx = margin + 34f
        text("NEXONVATE", tx, top + 23f, 12f, black, bold = true, letterSpacing = 0.12f)
        text("INNOVATE || IMPLEMENT || IMPACT", tx, top + 35f, 6.3f, darkGray, letterSpacing = 0.03f)

        textCenter("Generated with NetSet - The Badminton Connect", pageW / 2f + 10f, top + 23f, 7.8f, textSecondary)
        textCenter("Page $pageNo", pageW / 2f + 10f, top + 35f, 7.5f, textSecondary)

        textRight("Licensed by NEXONVATE", contentRight, top + 23f, 9.5f, navy, bold = true)
        textRight("Powered by NEXONVATE", contentRight, top + 36f, 8.5f, textSecondary)
        textRight("(c) ${Dates.year()} NEXONVATE. All rights reserved.", contentRight, top + 48f, 6.8f, textSecondary)
    }

    // ---------------------------------------------------------------- helpers

    private fun imageBytes(path: String): ByteArray? =
        if (path.isBlank() || !PlatformFiles.exists(path)) null else PlatformFiles.readBytes(path)

    /** Truncates with an ellipsis so the text fits in [maxWidth]. */
    private fun fit(s: String, size: Float, bold: Boolean, maxWidth: Float?): String {
        if (maxWidth == null || c.measureText(s, size, bold) <= maxWidth) return s
        var end = s.length
        while (end > 0 && c.measureText(s.take(end) + "...", size, bold) > maxWidth) end--
        return s.take(end).trimEnd() + "..."
    }

    private fun text(s: String, x: Float, baseY: Float, size: Float, color: Int, bold: Boolean = false, maxWidth: Float? = null, letterSpacing: Float = 0f) {
        c.text(fit(s, size, bold, maxWidth), x, baseY, size, color, bold, letterSpacing)
    }

    private fun textRight(s: String, right: Float, baseY: Float, size: Float, color: Int, bold: Boolean = false, maxWidth: Float? = null) {
        val t = fit(s, size, bold, maxWidth)
        c.text(t, right - c.measureText(t, size, bold), baseY, size, color, bold)
    }

    private fun textCenter(s: String, cx: Float, baseY: Float, size: Float, color: Int, bold: Boolean = false) {
        c.text(s, cx - c.measureText(s, size, bold) / 2f, baseY, size, color, bold)
    }

    private fun scoreColor(v: Double?): Int = when {
        v == null -> textSecondary
        v >= 7.5 -> green
        v >= 5.0 -> amber
        else -> red
    }
}
