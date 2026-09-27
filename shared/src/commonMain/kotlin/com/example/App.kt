package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.example.platform.SystemBarsAppearance
import com.example.ui.theme.LocalThemeToggle
import com.example.ui.theme.ThemeToggle
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.platform.PlatformBackHandler
import com.example.ui.components.BadmintonBottomNavBar
import com.example.ui.components.ReportPreviewDialog
import com.example.ui.screens.DrillModeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MatchLogsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SkillManagementScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.theme.SmashAssessTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.NavDestination

/** Root of the NetSet app, shared by Android and iOS. */
@Composable
fun App(
    viewModel: MainViewModel = viewModel { MainViewModel() },
    startScreen: String? = null
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val dark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    SystemBarsAppearance(dark)
    SmashAssessTheme(darkTheme = dark) {
        CompositionLocalProvider(LocalThemeToggle provides ThemeToggle(dark) { viewModel.setDarkMode(it) }) {
            MainAppContent(viewModel, startScreen)
        }
    }
}

@Composable
private fun MainAppContent(viewModel: MainViewModel, startScreen: String?) {
    val currentNav by viewModel.currentNav.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val skills by viewModel.skills.collectAsStateWithLifecycle()
    val todayAttendance by viewModel.todayAttendance.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    val selectedStudent by viewModel.selectedStudent.collectAsStateWithLifecycle()
    val selectedStudentEvaluations by viewModel.selectedStudentEvaluations.collectAsStateWithLifecycle()
    val selectedStudentMatchLogs by viewModel.selectedStudentMatchLogs.collectAsStateWithLifecycle()
    val selectedStudentTournamentLogs by viewModel.selectedStudentTournamentLogs.collectAsStateWithLifecycle()
    val selectedStudentAttendance by viewModel.selectedStudentAttendance.collectAsStateWithLifecycle()
    val allEvaluations by viewModel.allEvaluations.collectAsStateWithLifecycle()
    val generatedReport by viewModel.generatedReport.collectAsStateWithLifecycle()
    val isGeneratingReport by viewModel.isGeneratingReport.collectAsStateWithLifecycle()

    val allMatchLogs by viewModel.allMatchLogs.collectAsStateWithLifecycle()
    val academyConfig by viewModel.academyConfig.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Optional deep link used for automated preview screenshots (e.g. "-netsetScreen report")
    LaunchedEffect(startScreen, students.isNotEmpty()) {
        if (startScreen != null && students.isNotEmpty()) viewModel.openStartScreen(startScreen)
    }

    PlatformBackHandler(enabled = currentNav != NavDestination.HOME) {
        viewModel.navigateTo(
            when (currentNav) {
                NavDestination.STUDENT_DETAIL -> NavDestination.STUDENTS
                NavDestination.SKILL_MANAGEMENT -> viewModel.skillManagementReturn
                else -> NavDestination.HOME
            }
        )
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            viewModel.clearToast()
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BadmintonBottomNavBar(
                currentNav = currentNav,
                onNavigate = { destination -> viewModel.navigateTo(destination) }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentNav) {
                NavDestination.HOME -> HomeScreen(
                    viewModel = viewModel,
                    students = students,
                    todayAttendance = todayAttendance,
                    selectedDate = selectedDate,
                    onNavigate = { viewModel.navigateTo(it) },
                    onStudentClick = { studentId -> viewModel.openStudentDetail(studentId) }
                )
                NavDestination.STUDENTS -> StudentsScreen(
                    viewModel = viewModel,
                    students = students,
                    onStudentClick = { studentId -> viewModel.openStudentDetail(studentId) }
                )
                NavDestination.STUDENT_DETAIL -> StudentDetailScreen(
                    viewModel = viewModel,
                    student = selectedStudent,
                    skills = skills,
                    evaluations = selectedStudentEvaluations,
                    matchLogs = selectedStudentMatchLogs,
                    tournamentLogs = selectedStudentTournamentLogs,
                    students = students,
                    allEvaluations = allEvaluations,
                    attendance = selectedStudentAttendance,
                    isGeneratingReport = isGeneratingReport,
                    onBack = { viewModel.navigateTo(NavDestination.STUDENTS) }
                )
                NavDestination.DRILL_MODE -> DrillModeScreen(
                    viewModel = viewModel,
                    students = students,
                    todayAttendance = todayAttendance,
                    skills = skills,
                    selectedDate = selectedDate,
                    onManageDrills = { viewModel.openSkillManagement(NavDestination.DRILL_MODE) }
                )
                NavDestination.MATCH_LOGS -> MatchLogsScreen(
                    viewModel = viewModel,
                    matchLogs = allMatchLogs,
                    students = students
                )
                NavDestination.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    academyConfig = academyConfig,
                    onOpenSkillManagement = { viewModel.openSkillManagement(NavDestination.SETTINGS) }
                )
                NavDestination.SKILL_MANAGEMENT -> SkillManagementScreen(
                    viewModel = viewModel,
                    skills = skills,
                    onBack = { viewModel.navigateTo(viewModel.skillManagementReturn) }
                )
            }
        }
    }

    generatedReport?.let { generated ->
        ReportPreviewDialog(
            generated = generated,
            onDismiss = { viewModel.dismissGeneratedReport() },
            onShareWhatsApp = { viewModel.shareReportToWhatsApp(generated) },
            onShareOther = { viewModel.shareReport(generated) }
        )
    }
}
