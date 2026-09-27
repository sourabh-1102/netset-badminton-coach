package com.example.ui.screens

import com.example.ui.components.imageExists
import com.example.ui.components.rememberLocalImagePainter
import com.example.ui.components.rememberDatePicker
import com.example.ui.theme.ThemeToggleButton
import com.example.util.Dates
import com.example.util.pad2
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.foundation.Image
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.layout.ContentScale
import com.example.ui.components.NetSetLogo
import com.example.data.model.Attendance
import com.example.data.model.Student
import com.example.ui.theme.AvatarBlue
import com.example.ui.theme.AvatarGold
import com.example.ui.theme.AvatarTeal
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CourtGreenDark
import com.example.ui.theme.PillTrack
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CourtGreenPrimary
import com.example.ui.theme.ScoreRedContainer
import com.example.ui.theme.ScoreRedOnContainer
import com.example.ui.theme.SlateOutline
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TextSlate
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.NavDestination

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    students: List<Student>,
    todayAttendance: List<Attendance>,
    selectedDate: String,
    onNavigate: (NavDestination) -> Unit,
    onStudentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val academyConfig by viewModel.academyConfig.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Map attendance by studentId
    val attendanceMap = remember(todayAttendance) {
        todayAttendance.associate { it.studentId to it.isPresent }
    }

    val totalStudents = students.size
    val presentCount = students.count { attendanceMap[it.id] == true }
    val absentCount = totalStudents - presentCount

    val filteredStudents = remember(students, searchQuery) {
        if (searchQuery.isBlank()) students
        else students.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val showDatePicker = rememberDatePicker(selectedDate) { viewModel.setSelectedDate(it) }
    val formattedDisplayDate = remember(selectedDate) { Dates.longHeader(selectedDate) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // TOP HEADER: App Bar style header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NetSetLogo(size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = academyConfig.academyName,
                    style = MaterialTheme.typography.labelMedium,
                    color = CourtGreenPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = formattedDisplayDate.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSlate,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Daily Attendance",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = CourtGreenDark
                )
            }

            ThemeToggleButton()
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                onClick = showDatePicker,
                shape = CircleShape,
                color = TealContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        tint = CourtGreenDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // STATS ROW: 3 Pills (Total, Present, Absent)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Total Stat
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = TealContainer),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TOTAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CourtGreenDark.copy(alpha = 0.7f)
                    )
                    Text(
                        text = totalStudents.pad2(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = CourtGreenDark
                    )
                }
            }

            // Present Stat
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CourtGreenPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PRESENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = presentCount.pad2(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Absent Stat
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, SlateOutline),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ABSENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSlate.copy(alpha = 0.7f)
                    )
                    Text(
                        text = absentCount.pad2(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSlate
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ROSTER HEADER & ACTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ACTIVE ROSTER",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TextSlate
            )

            TextButton(
                onClick = {
                    students.forEach { viewModel.toggleAttendance(it.id, true) }
                }
            ) {
                Text(
                    text = "Select All",
                    fontWeight = FontWeight.Bold,
                    color = CourtGreenPrimary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search player name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSlate) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ROSTER LIST
        if (filteredStudents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (totalStudents == 0) "No students added yet." else "No players match search.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSlate
                    )
                    if (totalStudents == 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.seedDemoData() },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pre-fill Demo Students")
                        }
                    }
                }
            }
        } else {
            val avatarColors = listOf(AvatarTeal, AvatarGold, AvatarBlue)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(filteredStudents, key = { _, s -> s.id }) { index, student ->
                    val isPresent = attendanceMap[student.id] ?: false
                    val avatarBg = avatarColors[index % avatarColors.size]

                    StudentAttendanceCard(
                        student = student,
                        isPresent = isPresent,
                        avatarBg = avatarBg,
                        onToggle = { newStatus ->
                            viewModel.toggleAttendance(student.id, newStatus)
                        },
                        onClick = { onStudentClick(student.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // START DRILL SESSION MAIN CTA
        Button(
            onClick = { onNavigate(NavDestination.DRILL_MODE) },
            colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "START SKILL DRILLS ($presentCount PRESENT)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun StudentAttendanceCard(
    student: Student,
    isPresent: Boolean,
    avatarBg: Color,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorderLight),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar: profile photo, or initials when none
                if (imageExists(student.profilePicPath)) {
                    Image(
                        painter = rememberLocalImagePainter(student.profilePicPath),
                        contentDescription = student.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                } else Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = CourtGreenDark,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CourtGreenDark
                    )
                    Text(
                        text = "Age ${student.age} • ${student.bloodGroup}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSlate
                    )
                }
            }

            // Pill Toggle Bar (P / A)
            Surface(
                shape = CircleShape,
                color = PillTrack,
                modifier = Modifier.padding(2.dp)
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    // P button
                    Surface(
                        onClick = { onToggle(true) },
                        shape = CircleShape,
                        color = if (isPresent) CardSurface else Color.Transparent,
                        shadowElevation = if (isPresent) 2.dp else 0.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "P",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isPresent) CourtGreenPrimary else TextSlate.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // A button
                    Surface(
                        onClick = { onToggle(false) },
                        shape = CircleShape,
                        color = if (!isPresent) ScoreRedContainer else Color.Transparent,
                        shadowElevation = if (!isPresent) 2.dp else 0.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (!isPresent) ScoreRedOnContainer else TextSlate.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
