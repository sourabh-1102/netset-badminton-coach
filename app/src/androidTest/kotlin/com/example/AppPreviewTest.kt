package com.example

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.repository.BadmintonRepository
import com.example.pdf.PdfReportGenerator
import com.example.pdf.ReportTimeframe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Walks through the main screens with demo data and saves screenshots plus a rendered
 * sample PDF report into the app's files/preview folder (pull with `adb exec-out run-as`).
 */
@RunWith(AndroidJUnit4::class)
class AppPreviewTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val outDir = File(context.filesDir, "preview").apply { mkdirs() }

    private fun shot(name: String) {
        rule.waitForIdle()
        Thread.sleep(700)
        val bmp = instrumentation.uiAutomation.takeScreenshot() ?: return
        FileOutputStream(File(outDir, "$name.png")).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun clickText(text: String) {
        rule.waitUntil(8000) { rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        val node = rule.onAllNodesWithText(text)[0]
        try { node.performScrollTo() } catch (_: Throwable) {} // only works inside scrollable lists
        node.performClick()
        rule.waitForIdle()
    }

    @Test
    fun capturePreview() {
        val repo = BadmintonRepository()
        runBlocking {
            if (repo.allStudents.first().isEmpty()) repo.seedSampleData()
        }
        rule.waitUntil(8000) { rule.onAllNodesWithText("Aarav Patel").fetchSemanticsNodes().isNotEmpty() }
        shot("01_home")
        rule.onNodeWithContentDescription("Switch to", substring = true).performClick(); shot("01b_home_toggled")
        rule.onNodeWithContentDescription("Switch to", substring = true).performClick(); shot("01c_home_toggled_back")
        Thread.sleep(800); shot("01d_home_final")

        clickText("Students"); shot("02_students")
        clickText("Aarav Patel"); shot("03_profile_scores")
        clickText("Report"); shot("05_profile_report")
        clickText("15 Days"); shot("06_profile_report_15d")

        rule.onNodeWithText("EXPORT 15 DAYS PDF & SHARE").performScrollTo().performClick()
        rule.waitUntil(20000) { rule.onAllNodes(hasText("Report Preview")).fetchSemanticsNodes().isNotEmpty() }
        Thread.sleep(2500)
        shot("07_pdf_preview")
        instrumentation.uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
        Thread.sleep(1000)

        // Also render the monthly PDF pages at high resolution
        runBlocking {
            val student = repo.getStudentById("s_1")!!
            val report = com.example.pdf.ReportAnalytics.build(
                student = student,
                students = repo.allStudents.first(),
                skills = repo.getAllSkillsList().sortedBy { it.displayOrder },
                allEvaluations = repo.getAllEvaluationsList(),
                studentAttendance = repo.getAttendanceListForStudent(student.id),
                studentMatches = repo.getMatchLogsListForStudent(student.id),
                studentTournaments = repo.getTournamentLogsListForStudent(student.id),
                timeframe = ReportTimeframe.MONTHLY
            )
            val pdf = File(PdfReportGenerator().generateReport(report, repo.getAcademyConfig())!!)
            pdf.copyTo(File(outDir, "sample_report.pdf"), overwrite = true)
            ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { r ->
                    for (i in 0 until r.pageCount) r.openPage(i).use { p ->
                        val b = Bitmap.createBitmap(p.width * 2, p.height * 2, Bitmap.Config.ARGB_8888)
                        b.eraseColor(android.graphics.Color.WHITE)
                        p.render(b, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        FileOutputStream(File(outDir, "pdf_page_${i + 1}.png")).use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    }
                }
            }
        }

        rule.onAllNodesWithText("Matches").fetchSemanticsNodes().size.let { n -> rule.onAllNodesWithText("Matches")[n - 1].performClick() }
        shot("09_matches")
        clickText("Coach"); shot("10_coach")
        rule.onNodeWithText("Licensed by NEXONVATE").performScrollTo(); shot("11_coach_branding")
        clickText("Drills"); shot("12_drills")
        clickText("Add / Edit Drills"); shot("13_manage_drills")
    }
}
