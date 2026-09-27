package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AcademyConfig
import com.example.data.model.Attendance
import com.example.data.model.DailyEvaluation
import com.example.data.model.MatchLog
import com.example.data.model.Skill
import com.example.data.model.Student
import com.example.data.model.TournamentLog
import com.example.data.repository.BadmintonRepository
import com.example.pdf.PdfReportGenerator
import com.example.pdf.ReportAnalytics
import com.example.pdf.ReportTimeframe
import com.example.pdf.StudentReport
import com.example.platform.Sharing
import com.example.util.Dates
import com.example.util.ScoreJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GeneratedReport(val path: String, val report: StudentReport)

enum class NavDestination {
    HOME, STUDENTS, DRILL_MODE, MATCH_LOGS, SETTINGS, STUDENT_DETAIL, SKILL_MANAGEMENT
}

class MainViewModel : ViewModel() {
    val repository = BadmintonRepository()
    private val pdfGenerator = PdfReportGenerator()

    private val _currentNav = MutableStateFlow(NavDestination.HOME)
    val currentNav: StateFlow<NavDestination> = _currentNav.asStateFlow()

    private val _selectedStudentId = MutableStateFlow<String?>(null)
    val selectedStudentId: StateFlow<String?> = _selectedStudentId.asStateFlow()

    private val _selectedDate = MutableStateFlow(Dates.todayIso())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _academyConfig = MutableStateFlow(repository.getAcademyConfig())
    val academyConfig: StateFlow<AcademyConfig> = _academyConfig.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Flows
    val students: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val skills: StateFlow<List<Skill>> = repository.allSkills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMatchLogs: StateFlow<List<MatchLog>> = repository.allMatchLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEvaluations: StateFlow<List<DailyEvaluation>> = repository.allEvaluations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _generatedReport = MutableStateFlow<GeneratedReport?>(null)
    val generatedReport: StateFlow<GeneratedReport?> = _generatedReport.asStateFlow()

    private val _isGeneratingReport = MutableStateFlow(false)
    val isGeneratingReport: StateFlow<Boolean> = _isGeneratingReport.asStateFlow()

    val todayAttendance: StateFlow<List<Attendance>> = _selectedDate
        .flatMapLatest { dateStr -> repository.getAttendanceForDate(dateStr) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedStudent: StateFlow<Student?> = _selectedStudentId
        .flatMapLatest { id -> if (id != null) repository.getStudentFlow(id) else flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedStudentEvaluations: StateFlow<List<DailyEvaluation>> = _selectedStudentId
        .flatMapLatest { id -> if (id != null) repository.getEvaluationsForStudent(id) else flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedStudentAttendance: StateFlow<List<Attendance>> = _selectedStudentId
        .flatMapLatest { id -> if (id != null) repository.getAttendanceForStudent(id) else flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedStudentMatchLogs: StateFlow<List<MatchLog>> = _selectedStudentId
        .flatMapLatest { id -> if (id != null) repository.getMatchLogsForStudent(id) else flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedStudentTournamentLogs: StateFlow<List<TournamentLog>> = _selectedStudentId
        .flatMapLatest { id -> if (id != null) repository.getTournamentLogsForStudent(id) else flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initializeDefaultSkillsIfEmpty()
            if (repository.claimFirstLaunchDemoSeed()) {
                repository.seedSampleData()
            }
        }
    }

    fun navigateTo(destination: NavDestination) {
        _currentNav.value = destination
    }

    /** Screen to return to when leaving drill/skill management (Drills tab or Coach tab). */
    var skillManagementReturn: NavDestination = NavDestination.SETTINGS
        private set

    fun openSkillManagement(from: NavDestination) {
        skillManagementReturn = from
        _currentNav.value = NavDestination.SKILL_MANAGEMENT
    }

    /** Tab to show when the student profile opens (0 = Scores ... 3 = Report). */
    var detailInitialTab: Int = 0
        private set

    private var startScreenHandled = false

    /** Jumps to a named screen once; used by automated preview runs. */
    fun openStartScreen(name: String) {
        if (startScreenHandled) return
        startScreenHandled = true
        val firstStudent = students.value.firstOrNull() ?: return
        when (name) {
            "students" -> navigateTo(NavDestination.STUDENTS)
            "profile" -> openStudentDetail(firstStudent.id)
            "report" -> openStudentDetail(firstStudent.id, tab = 3)
            "pdf" -> {
                openStudentDetail(firstStudent.id, tab = 3)
                generatePdfReport(firstStudent, ReportTimeframe.FORTNIGHTLY)
            }
            "drills" -> navigateTo(NavDestination.DRILL_MODE)
            "managedrills" -> openSkillManagement(NavDestination.DRILL_MODE)
            "matches" -> navigateTo(NavDestination.MATCH_LOGS)
            "coach" -> navigateTo(NavDestination.SETTINGS)
        }
    }

    fun openStudentDetail(studentId: String, tab: Int) {
        detailInitialTab = tab
        _selectedStudentId.value = studentId
        _currentNav.value = NavDestination.STUDENT_DETAIL
    }

    fun openStudentDetail(studentId: String) {
        detailInitialTab = 0
        _selectedStudentId.value = studentId
        _currentNav.value = NavDestination.STUDENT_DETAIL
    }

    fun setSelectedDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun toggleAttendance(studentId: String, isPresent: Boolean) {
        viewModelScope.launch {
            val dateStr = _selectedDate.value
            repository.setAttendance(
                Attendance(studentId = studentId, date = dateStr, isPresent = isPresent)
            )
        }
    }

    fun saveBatchAttendance(attendanceMap: Map<String, Boolean>) {
        viewModelScope.launch {
            val dateStr = _selectedDate.value
            val list = attendanceMap.map { (studentId, isPresent) ->
                Attendance(studentId = studentId, date = dateStr, isPresent = isPresent)
            }
            repository.saveBatchAttendance(list)
            _toastMessage.value = "Attendance updated for ${list.size} students"
        }
    }

    fun saveSingleStudentEvaluation(
        studentId: String,
        dateStr: String,
        scores: Map<String, Double>,
        overallNote: String
    ) {
        viewModelScope.launch {
            val eval = DailyEvaluation(
                studentId = studentId,
                date = dateStr,
                skillScoresJson = ScoreJson.encode(scores),
                overallNote = overallNote
            )
            repository.insertEvaluation(eval)
            _toastMessage.value = "Scores saved successfully!"
        }
    }

    fun saveBatchDrillScores(
        skillId: String,
        scoresMap: Map<String, Double> // studentId -> score
    ) {
        viewModelScope.launch {
            val dateStr = _selectedDate.value
            val studentIds = scoresMap.keys

            for (studentId in studentIds) {
                val newScore = scoresMap[studentId] ?: continue
                // Fetch existing evaluation for today if present
                val existing = repository.getEvaluationsListForStudent(studentId)
                    .firstOrNull { it.date == dateStr }

                val currentMap = existing?.let { ScoreJson.decode(it.skillScoresJson) }.orEmpty().toMutableMap()
                currentMap[skillId] = newScore

                val updatedEval = DailyEvaluation(
                    studentId = studentId,
                    date = dateStr,
                    skillScoresJson = ScoreJson.encode(currentMap),
                    overallNote = existing?.overallNote ?: "Batch drill evaluation"
                )
                repository.insertEvaluation(updatedEval)
            }

            _toastMessage.value = "Drill scores recorded for ${scoresMap.size} students!"
        }
    }

    fun addStudent(student: Student) {
        viewModelScope.launch {
            repository.insertStudent(student)
            _toastMessage.value = "Student ${student.name} added"
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
            _toastMessage.value = "Student details updated"
        }
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
            _selectedStudentId.value = null
            _currentNav.value = NavDestination.STUDENTS
            _toastMessage.value = "Student removed"
        }
    }

    fun addSkill(skillName: String, min: Int, max: Int) {
        viewModelScope.launch {
            val currentList = repository.getAllSkillsList()
            val skill = Skill(
                skillName = skillName,
                scoreRangeMin = min,
                scoreRangeMax = max,
                displayOrder = currentList.size + 1
            )
            repository.insertSkill(skill)
            _toastMessage.value = "Drill '$skillName' added"
        }
    }

    fun updateSkill(skill: Skill) {
        viewModelScope.launch {
            repository.updateSkill(skill)
            _toastMessage.value = "Drill updated"
        }
    }

    fun deleteSkill(skill: Skill) {
        viewModelScope.launch {
            repository.deleteSkill(skill)
            _toastMessage.value = "Drill deleted"
        }
    }

    fun addMatchLog(matchLog: MatchLog) {
        viewModelScope.launch {
            repository.insertMatchLog(matchLog)
            _toastMessage.value = "Match log added"
        }
    }

    fun deleteMatchLog(matchLog: MatchLog) {
        viewModelScope.launch {
            repository.deleteMatchLog(matchLog)
            _toastMessage.value = "Match log removed"
        }
    }

    fun addTournamentLog(tournamentLog: TournamentLog) {
        viewModelScope.launch {
            repository.insertTournamentLog(tournamentLog)
            _toastMessage.value = "Tournament record added"
        }
    }

    fun deleteTournamentLog(tournamentLog: TournamentLog) {
        viewModelScope.launch {
            repository.deleteTournamentLog(tournamentLog)
            _toastMessage.value = "Tournament record removed"
        }
    }

    fun saveAcademyConfig(config: AcademyConfig) {
        repository.saveAcademyConfig(config)
        _academyConfig.value = config
        _toastMessage.value = "Academy settings saved"
    }

    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedSampleData()
            _toastMessage.value = "Demo students & assessments seeded successfully!"
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _selectedStudentId.value = null
            _toastMessage.value = "All data cleared"
        }
    }

    /** Exports a backup file and opens the share sheet so it can be saved to Drive / Files / WhatsApp. */
    fun exportDatabaseBackup(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val path = repository.exportDatabaseBackup()
            if (path != null) {
                Sharing.shareFile(path, "application/octet-stream", "NetSet data backup")
            }
            onComplete(path != null)
        }
    }

    fun restoreDatabase(bytes: ByteArray, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.restoreDatabase(bytes)
            if (success) {
                _selectedStudentId.value = null
                _currentNav.value = NavDestination.HOME
                _toastMessage.value = "Backup restored successfully!"
            } else {
                _toastMessage.value = "That file is not a valid NetSet backup."
            }
            onComplete(success)
        }
    }

    /** Stores a picked photo in app storage; returns its path or null. */
    fun saveImage(bytes: ByteArray, prefix: String): String? = repository.saveImage(bytes, prefix)

    suspend fun buildReport(student: Student, timeframe: ReportTimeframe): StudentReport =
        withContext(Dispatchers.IO) {
            ReportAnalytics.build(
                student = student,
                students = students.value.ifEmpty { listOf(student) },
                skills = repository.getAllSkillsList().sortedWith(compareBy({ it.displayOrder }, { it.skillName })),
                allEvaluations = repository.getAllEvaluationsList(),
                studentAttendance = repository.getAttendanceListForStudent(student.id),
                studentMatches = repository.getMatchLogsListForStudent(student.id),
                studentTournaments = repository.getTournamentLogsListForStudent(student.id),
                timeframe = timeframe
            )
        }

    /** Builds the analysis + PDF; the UI shows an in-app preview with WhatsApp / share actions. */
    fun generatePdfReport(student: Student, timeframe: ReportTimeframe) {
        if (_isGeneratingReport.value) return
        viewModelScope.launch {
            _isGeneratingReport.value = true
            try {
                val report = buildReport(student, timeframe)
                val path = withContext(Dispatchers.Default) {
                    pdfGenerator.generateReport(report, academyConfig.value)
                }
                if (path != null) {
                    _generatedReport.value = GeneratedReport(path, report)
                } else {
                    _toastMessage.value = "Failed to generate PDF report"
                }
            } finally {
                _isGeneratingReport.value = false
            }
        }
    }

    fun dismissGeneratedReport() {
        _generatedReport.value = null
    }

    private fun shareMessage(generated: GeneratedReport) =
        "Hello! Here is the ${generated.report.timeframe.displayName.lowercase()} for ${generated.report.student.name} " +
            "from ${academyConfig.value.academyName}.\n\nPowered by NEXONVATE"

    fun shareReportToWhatsApp(generated: GeneratedReport) {
        Sharing.shareToWhatsApp(generated.path, "application/pdf", shareMessage(generated), generated.report.student.parentPhone)
    }

    fun shareReport(generated: GeneratedReport) {
        Sharing.shareFile(generated.path, "application/pdf", shareMessage(generated))
    }
}
