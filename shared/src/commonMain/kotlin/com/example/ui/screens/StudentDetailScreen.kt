package com.example.ui.screens

import com.example.ui.components.imageExists
import com.example.ui.components.rememberLocalImagePainter
import com.example.ui.components.rememberDatePicker
import com.example.platform.rememberImagePicker
import com.example.util.Dates
import com.example.util.ScoreJson
import com.example.util.fmt1
import kotlin.math.round
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Attendance
import com.example.data.model.DailyEvaluation
import com.example.data.model.MatchLog
import com.example.data.model.Skill
import com.example.data.model.Student
import com.example.data.model.TournamentLog
import com.example.ui.components.WeeklyPerformanceTrendChart
import com.example.ui.theme.AvatarTeal
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CourtGreenDark
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CourtGreenPrimary
import com.example.ui.theme.ScoreGreen
import com.example.ui.theme.ScoreRed
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TextSlate
import com.example.ui.viewmodel.MainViewModel

@Composable
fun StudentDetailScreen(
    viewModel: MainViewModel,
    student: Student?,
    skills: List<Skill>,
    evaluations: List<DailyEvaluation>,
    matchLogs: List<MatchLog>,
    tournamentLogs: List<TournamentLog>,
    students: List<Student>,
    allEvaluations: List<DailyEvaluation>,
    attendance: List<Attendance>,
    isGeneratingReport: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (student == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Player profile not found.")
        }
        return
    }

    var selectedTabIndex by remember(student.id) { mutableStateOf(viewModel.detailInitialTab) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPdfReportDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Scores", "Matches", "Progress", "Report")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP APP BAR
        Surface(
            color = CardSurface,
            border = BorderStroke(1.dp, CardBorderLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CourtGreenDark)
                    }
                    Text(
                        text = "PLAYER ASSESSMENT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CourtGreenDark,
                        letterSpacing = 0.5.sp
                    )
                }

                // EXPORT PDF & SHARE BUTTON
                Button(
                    onClick = { showPdfReportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TealContainer),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = CourtGreenDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF Report", color = CourtGreenDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // PLAYER PROFILE CARD
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, CardBorderLight),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (imageExists(student.profilePicPath)) {
                    Image(
                        painter = rememberLocalImagePainter(student.profilePicPath),
                        contentDescription = student.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(AvatarTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase(),
                            color = CourtGreenDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CourtGreenDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Age: ${student.age} yrs  •  Blood: ${student.bloodGroup}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSlate,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Parent: ${student.parentPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSlate
                    )
                }

                IconButton(
                    onClick = { showEditProfileDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(TealContainer)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = CourtGreenDark, modifier = Modifier.size(18.dp))
                }
            }
        }

        // TABS
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = CardSurface,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = CourtGreenPrimary,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (selectedTabIndex == index) CourtGreenDark else TextSlate
                        )
                    }
                )
            }
        }

        // TAB CONTENTS
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> DailyScoresTab(viewModel, student, skills, evaluations)
                1 -> MatchAndTournamentLogsTab(viewModel, student, matchLogs, tournamentLogs)
                2 -> PerformanceChartsTab(evaluations)
                3 -> StudentReportTab(
                    student = student,
                    students = students,
                    skills = skills,
                    allEvaluations = allEvaluations,
                    attendance = attendance,
                    matchLogs = matchLogs,
                    tournamentLogs = tournamentLogs,
                    isGenerating = isGeneratingReport,
                    onGeneratePdf = { tf -> viewModel.generatePdfReport(student, tf) }
                )
            }
        }
    }

    if (showEditProfileDialog) {
        EditStudentProfileDialog(
            student = student,
            onSaveImage = { bytes, prefix -> viewModel.saveImage(bytes, prefix) },
            onDismiss = { showEditProfileDialog = false },
            onSave = { updatedStudent ->
                viewModel.updateStudent(updatedStudent)
                showEditProfileDialog = false
            }
        )
    }

    if (showPdfReportDialog) {
        PdfReportTimeframeDialog(
            onDismiss = { showPdfReportDialog = false },
            onSelectTimeframe = { timeframe ->
                showPdfReportDialog = false
                viewModel.generatePdfReport(student, timeframe)
            }
        )
    }
}

@Composable
fun DailyScoresTab(
    viewModel: MainViewModel,
    student: Student,
    skills: List<Skill>,
    evaluations: List<DailyEvaluation>
) {
    var selectedDateStr by remember { mutableStateOf(Dates.todayIso()) }
    var overallNote by remember { mutableStateOf("") }

    // Map of skillId to score value
    val scoresMap = remember { mutableStateMapOf<String, Double>() }

    // Load evaluation if exists for selectedDateStr
    LaunchedEffect(selectedDateStr, evaluations) {
        val existing = evaluations.firstOrNull { it.date == selectedDateStr }
        scoresMap.clear()
        if (existing != null) {
            overallNote = existing.overallNote
            scoresMap.putAll(ScoreJson.decode(existing.skillScoresJson))
        } else {
            overallNote = ""
            skills.forEach { skill ->
                scoresMap[skill.id] = defaultScore(skill)
            }
        }
    }

    val showDatePicker = rememberDatePicker(selectedDateStr) { selectedDateStr = it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Date Selector Bar
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = TealContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp), tint = CourtGreenDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Assessment: $selectedDateStr",
                        fontWeight = FontWeight.Bold,
                        color = CourtGreenDark,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = showDatePicker,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, CourtGreenDark)
                ) {
                    Text("Change Date", color = CourtGreenDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SCORE EACH CRITERIA",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextSlate
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Skills Sliders List
        for (skill in skills) {
            val currentScore = (scoresMap[skill.id] ?: defaultScore(skill))
                .coerceIn(skill.scoreRangeMin.toDouble(), skill.scoreRangeMax.toDouble())

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, CardBorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = skill.skillName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CourtGreenDark
                        )
                        Surface(
                            shape = CircleShape,
                            color = TealContainer
                        ) {
                            Text(
                                text = "${currentScore.fmt1()} / ${skill.scoreRangeMax}",
                                fontWeight = FontWeight.Bold,
                                color = CourtGreenDark,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Slider(
                        value = currentScore.toFloat(),
                        onValueChange = { scoresMap[skill.id] = (round(it * 10) / 10.0) },
                        valueRange = skill.scoreRangeMin.toFloat()..skill.scoreRangeMax.toFloat(),
                        steps = ((skill.scoreRangeMax - skill.scoreRangeMin) * 2 - 1).coerceAtLeast(0), // 0.5 increments
                        colors = SliderDefaults.colors(
                            thumbColor = CourtGreenDark,
                            activeTrackColor = CourtGreenPrimary,
                            inactiveTrackColor = TealContainer
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = overallNote,
            onValueChange = { overallNote = it },
            label = { Text("Coach Assessment Notes") },
            placeholder = { Text("e.g. Excellent footwork, needs improvement in net drops...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = RoundedCornerShape(20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.saveSingleStudentEvaluation(
                    studentId = student.id,
                    dateStr = selectedDateStr,
                    scores = skills.associate { sk ->
                        sk.id to (scoresMap[sk.id] ?: defaultScore(sk))
                            .coerceIn(sk.scoreRangeMin.toDouble(), sk.scoreRangeMax.toDouble())
                    },
                    overallNote = overallNote
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SAVE ASSESSMENT SCORES", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun MatchAndTournamentLogsTab(
    viewModel: MainViewModel,
    student: Student,
    matchLogs: List<MatchLog>,
    tournamentLogs: List<TournamentLog>
) {
    var showAddMatchDialog by remember { mutableStateOf(false) }
    var showAddTournamentDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // MATCH LOGS SECTION
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MATCH LOGS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSlate
                )

                Button(
                    onClick = { showAddMatchDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Match", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (matchLogs.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, CardBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No match logs recorded yet.",
                        modifier = Modifier.padding(16.dp),
                        color = TextSlate
                    )
                }
            }
        } else {
            items(matchLogs, key = { it.id }) { match ->
                MatchLogCard(match = match, onDelete = { viewModel.deleteMatchLog(match) })
            }
        }

        // TOURNAMENT HIGHLIGHTS SECTION
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOURNAMENT ACHIEVEMENTS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSlate
                )

                Button(
                    onClick = { showAddTournamentDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Record", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (tournamentLogs.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, CardBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No tournament records added yet.",
                        modifier = Modifier.padding(16.dp),
                        color = TextSlate
                    )
                }
            }
        } else {
            items(tournamentLogs, key = { it.id }) { tournament ->
                TournamentLogCard(
                    tournament = tournament,
                    onDelete = { viewModel.deleteTournamentLog(tournament) }
                )
            }
        }
    }

    if (showAddMatchDialog) {
        AddMatchLogDialog(
            studentId = student.id,
            onDismiss = { showAddMatchDialog = false },
            onSave = { newMatch ->
                viewModel.addMatchLog(newMatch)
                showAddMatchDialog = false
            }
        )
    }

    if (showAddTournamentDialog) {
        AddTournamentLogDialog(
            studentId = student.id,
            onDismiss = { showAddTournamentDialog = false },
            onSave = { newTourney ->
                viewModel.addTournamentLog(newTourney)
                showAddTournamentDialog = false
            }
        )
    }
}

@Composable
fun MatchLogCard(match: MatchLog, onDelete: () -> Unit) {
    val isWin = match.outcome.equals("Win", true)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (isWin) ScoreGreen else ScoreRed
                    ) {
                        Text(
                            text = match.outcome.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${match.type} - ${match.date}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CourtGreenDark
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ScoreRed)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (!match.partnerName.isNullOrEmpty()) {
                Text(
                    text = "Partner: ${match.partnerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSlate
                )
            }
            Text(
                text = "Opponents: ${match.opponentNames}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = CourtGreenDark
            )
            Text(
                text = "Score: ${match.score}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = CourtGreenPrimary
            )
            if (match.performanceNotes.isNotEmpty()) {
                Text(
                    text = "Notes: ${match.performanceNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSlate
                )
            }
        }
    }
}

@Composable
fun TournamentLogCard(tournament: TournamentLog, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorderLight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = CourtGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tournament.tournamentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CourtGreenDark
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ScoreRed)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Category: ${tournament.category}  |  Round: ${tournament.roundReached}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = CourtGreenDark
            )
            Text(
                text = "Achievements: ${tournament.achievements}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSlate
            )
            Text(
                text = "Date: ${tournament.date}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSlate
            )
        }
    }
}

@Composable
fun PerformanceChartsTab(evaluations: List<DailyEvaluation>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        WeeklyPerformanceTrendChart(evaluations = evaluations)
    }
}

@Composable
fun AddMatchLogDialog(
    studentId: String?,
    onDismiss: () -> Unit,
    onSave: (MatchLog) -> Unit,
    headerContent: @Composable () -> Unit = {}
) {
    var type by remember { mutableStateOf("Singles") }
    var partnerName by remember { mutableStateOf("") }
    var opponentNames by remember { mutableStateOf("") }
    var score by remember { mutableStateOf("21-18, 21-15") }
    var outcome by remember { mutableStateOf("Win") }
    var notes by remember { mutableStateOf("") }

    var matchDate by remember { mutableStateOf(Dates.todayIso()) }
    val showMatchDatePicker = rememberDatePicker(matchDate) { matchDate = it }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = CardSurface,
        title = { Text("Log Match Result", fontWeight = FontWeight.Bold, color = CourtGreenDark) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                headerContent()

                OutlinedButton(
                    onClick = showMatchDatePicker,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CourtGreenDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = CourtGreenDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Match date: $matchDate", color = CourtGreenDark, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = type == "Singles", onClick = { type = "Singles" })
                    Text("Singles")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = type == "Doubles", onClick = { type = "Doubles" })
                    Text("Doubles")
                }

                if (type == "Doubles") {
                    OutlinedTextField(
                        value = partnerName,
                        onValueChange = { partnerName = it },
                        label = { Text("Doubles Partner Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                OutlinedTextField(
                    value = opponentNames,
                    onValueChange = { opponentNames = it },
                    label = { Text("Opponent Name(s) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = score,
                    onValueChange = { score = it },
                    label = { Text("Score (e.g. 21-18, 19-21, 21-15)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = outcome == "Win", onClick = { outcome = "Win" })
                    Text("WIN", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = outcome == "Loss", onClick = { outcome = "Loss" })
                    Text("LOSS", fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Performance Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (opponentNames.isNotBlank() && studentId != null) {
                        val match = MatchLog(
                            studentId = studentId,
                            date = matchDate,
                            type = type,
                            partnerName = if (type == "Doubles") partnerName else null,
                            opponentNames = opponentNames.trim(),
                            score = score.trim(),
                            outcome = outcome,
                            performanceNotes = notes.trim()
                        )
                        onSave(match)
                    }
                },
                enabled = opponentNames.isNotBlank() && studentId != null,
                colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
                shape = CircleShape
            ) {
                Text("Save Match Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSlate) }
        }
    )
}

@Composable
fun AddTournamentLogDialog(
    studentId: String,
    onDismiss: () -> Unit,
    onSave: (TournamentLog) -> Unit
) {
    var tournamentName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Under-17 Singles") }
    var roundReached by remember { mutableStateOf("Finals") }
    var achievements by remember { mutableStateOf("Gold Medalist") }

    val todayDate = Dates.todayIso()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = CardSurface,
        title = { Text("Record Tournament Result", fontWeight = FontWeight.Bold, color = CourtGreenDark) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = tournamentName,
                    onValueChange = { tournamentName = it },
                    label = { Text("Tournament Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. U-15 Girls Singles)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = roundReached,
                    onValueChange = { roundReached = it },
                    label = { Text("Round Reached (e.g. Semi-Finals, Winner)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = achievements,
                    onValueChange = { achievements = it },
                    label = { Text("Achievements & Medals") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tournamentName.isNotBlank()) {
                        val t = TournamentLog(
                            studentId = studentId,
                            tournamentName = tournamentName.trim(),
                            date = todayDate,
                            category = category.trim(),
                            roundReached = roundReached.trim(),
                            achievements = achievements.trim()
                        )
                        onSave(t)
                    }
                },
                enabled = tournamentName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
                shape = CircleShape
            ) {
                Text("Save Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSlate) }
        }
    )
}

@Composable
fun EditStudentProfileDialog(
    student: Student,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit,
    onSaveImage: (ByteArray, String) -> String?
) {
    var name by remember { mutableStateOf(student.name) }
    var ageStr by remember { mutableStateOf(student.age.toString()) }
    var bloodGroup by remember { mutableStateOf(student.bloodGroup) }
    var parentPhone by remember { mutableStateOf(student.parentPhone) }
    var address by remember { mutableStateOf(student.address) }
    var profilePicPath by remember { mutableStateOf(student.profilePicPath) }

    val pickPhoto = rememberImagePicker { bytes ->
        onSaveImage(bytes, "player_${student.id}")?.let { profilePicPath = it }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = CardSurface,
        title = { Text("Edit Player Profile", fontWeight = FontWeight.Bold, color = CourtGreenDark) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar preview and change photo button
                Box(contentAlignment = Alignment.BottomEnd) {
                    if (imageExists(profilePicPath)) {
                        Image(
                            painter = rememberLocalImagePainter(profilePicPath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(AvatarTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase(),
                                color = CourtGreenDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = pickPhoto,
                        shape = CircleShape,
                        modifier = Modifier.height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Change Photo", modifier = Modifier.size(14.dp), tint = CourtGreenDark)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Photo", fontSize = 10.sp, color = CourtGreenDark, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Player Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = { ageStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Age *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = bloodGroup,
                        onValueChange = { bloodGroup = it },
                        label = { Text("Blood Group") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                OutlinedTextField(
                    value = parentPhone,
                    onValueChange = { parentPhone = it },
                    label = { Text("Parent Phone Number *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Residential Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val updated = student.copy(
                            name = name.trim(),
                            age = ageStr.toIntOrNull() ?: student.age,
                            bloodGroup = bloodGroup.trim(),
                            parentPhone = parentPhone.trim(),
                            address = address.trim(),
                            profilePicPath = profilePicPath
                        )
                        onSave(updated)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
                shape = CircleShape
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSlate)
            }
        }
    )
}

@Composable
fun PdfReportTimeframeDialog(
    onDismiss: () -> Unit,
    onSelectTimeframe: (com.example.pdf.ReportTimeframe) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = CardSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = CourtGreenDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate PDF Report", fontWeight = FontWeight.Bold, color = CourtGreenDark)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select report timeframe for this player:", style = MaterialTheme.typography.bodyMedium, color = TextSlate)

                com.example.pdf.ReportTimeframe.entries.forEach { tf ->
                    OutlinedButton(
                        onClick = { onSelectTimeframe(tf) },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CourtGreenDark),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tf.displayName, fontWeight = FontWeight.Bold, color = CourtGreenDark)
                            Text("Last ${tf.shortName}", fontSize = 12.sp, color = TextSlate)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSlate)
            }
        }
    )
}

private fun defaultScore(skill: Skill): Double =
    round((skill.scoreRangeMin + skill.scoreRangeMax) / 2.0 * 2) / 2.0
