package com.example.ui.screens

import kotlin.math.abs
import kotlin.math.round
import com.example.util.fmt1
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Attendance
import com.example.data.model.Skill
import com.example.data.model.Student
import com.example.ui.theme.AvatarBlue
import com.example.ui.theme.AvatarGold
import com.example.ui.theme.AvatarTeal
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CourtGreenDark
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CourtGreenPrimary
import com.example.ui.theme.ScoreGreen
import com.example.ui.theme.ScoreOrange
import com.example.ui.theme.ScoreRed
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TextSlate
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DrillModeScreen(
    viewModel: MainViewModel,
    students: List<Student>,
    todayAttendance: List<Attendance>,
    skills: List<Skill>,
    selectedDate: String,
    onManageDrills: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarColors = listOf(AvatarTeal, AvatarGold, AvatarBlue)

    // Filter list of ONLY today's PRESENT students
    val presentStudentIds = remember(todayAttendance) {
        todayAttendance.filter { it.isPresent }.map { it.studentId }.toSet()
    }

    val presentStudents = remember(students, presentStudentIds) {
        students.filter { presentStudentIds.contains(it.id) }
    }

    var selectedSkillId by remember { mutableStateOf(skills.firstOrNull()?.id ?: "") }

    // Synchronize selectedSkillId if skills change
    LaunchedEffect(skills) {
        if (selectedSkillId.isEmpty() && skills.isNotEmpty()) {
            selectedSkillId = skills.first().id
        }
    }

    val activeSkill = remember(skills, selectedSkillId) {
        skills.firstOrNull { it.id == selectedSkillId } ?: skills.firstOrNull()
    }

    // Map of studentId -> score value
    val scoresMap = remember { mutableStateMapOf<String, Double>() }

    val maxScore = activeSkill?.scoreRangeMax ?: 10
    val defaultDrillScore = round(maxScore * 0.7 * 2) / 2.0

    // Fresh default scores whenever the drill changes, so one drill's values don't leak into another
    LaunchedEffect(activeSkill?.id) { scoresMap.clear() }
    LaunchedEffect(activeSkill?.id, presentStudents) {
        presentStudents.forEach { s ->
            if (!scoresMap.containsKey(s.id)) {
                scoresMap[s.id] = defaultDrillScore
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // TOP HEADER
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CourtGreenPrimary,
            contentColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "DRILL MODE: BATCH SCORING",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Rapidly grade all ${presentStudents.size} present players in under a minute!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // DRILL SELECTOR PILLS + MANAGE
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SELECT DRILL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSlate,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f)
            )
            androidx.compose.material3.TextButton(onClick = onManageDrills) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = CourtGreenPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add / Edit Drills", color = CourtGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        if (skills.isEmpty()) {
            Text(
                text = "No drills yet. Tap \"Add / Edit Drills\" to create one.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSlate
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(skills) { skill ->
                val isSelected = skill.id == selectedSkillId
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSkillId = skill.id },
                    label = { Text(skill.skillName, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CourtGreenPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = TealContainer,
                        labelColor = CourtGreenDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = CardBorderLight,
                        selectedBorderColor = CourtGreenPrimary
                    ),
                    shape = CircleShape
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CLASS SCORING LIST
        if (presentStudents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, CardBorderLight),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No players marked PRESENT for today ($selectedDate).",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = CourtGreenDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Go to Home tab to mark attendance first.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSlate
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(presentStudents, key = { _, s -> s.id }) { index, student ->
                    val currentScore = (scoresMap[student.id] ?: defaultDrillScore).coerceIn(1.0, maxScore.toDouble())
                    val avatarBg = avatarColors[index % avatarColors.size]

                    DrillStudentScoreCard(
                        student = student,
                        score = currentScore,
                        maxScore = maxScore,
                        avatarBg = avatarBg,
                        onScoreChange = { newScore ->
                            scoresMap[student.id] = newScore
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SAVE ALL BUTTON
            Button(
                onClick = {
                    if (activeSkill != null) {
                        viewModel.saveBatchDrillScores(
                            activeSkill.id,
                            presentStudents.associate { it.id to (scoresMap[it.id] ?: defaultDrillScore) }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SAVE SCORES FOR ${activeSkill?.skillName?.uppercase() ?: ""}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun DrillStudentScoreCard(
    student: Student,
    score: Double,
    maxScore: Int,
    avatarBg: Color,
    onScoreChange: (Double) -> Unit
) {
    val scoreColor = when {
        score >= 8.0 -> ScoreGreen
        score >= 5.0 -> ScoreOrange
        else -> ScoreRed
    }

    val initials = student.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()

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
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(avatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = CourtGreenDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CourtGreenDark
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = scoreColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${score.fmt1()} / $maxScore",
                        color = scoreColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Slider
            Slider(
                value = score.toFloat(),
                onValueChange = { onScoreChange(round(it * 10) / 10.0) },
                valueRange = 1f..maxScore.toFloat(),
                steps = (maxScore - 1) * 2 - 1, // 0.5 step resolution
                colors = SliderDefaults.colors(
                    thumbColor = CourtGreenDark,
                    activeTrackColor = CourtGreenPrimary,
                    inactiveTrackColor = TealContainer
                )
            )

            // Quick Tap score buttons (1, 3, 5, 7, 8, 9, 10)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(1.0, 3.0, 5.0, 7.0, 8.0, 9.0, 10.0).forEach { quickScore ->
                    val isSelected = abs(score - quickScore) < 0.1
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) CourtGreenPrimary else TealContainer,
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { onScoreChange(quickScore) },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${quickScore.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else CourtGreenDark
                            )
                        }
                    }
                }
            }
        }
    }
}
