package com.example.ui.screens

import com.example.ui.components.imageExists
import com.example.ui.components.rememberLocalImagePainter
import com.example.platform.rememberImagePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.theme.AvatarBlue
import com.example.ui.theme.AvatarGold
import com.example.ui.theme.AvatarTeal
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CourtGreenDark
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CourtGreenPrimary
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TextSlate
import com.example.ui.viewmodel.MainViewModel

@Composable
fun StudentsScreen(
    viewModel: MainViewModel,
    students: List<Student>,
    onStudentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedBloodGroupFilter by remember { mutableStateOf("ALL") }
    var showAddStudentDialog by remember { mutableStateOf(false) }

    val bloodGroups = listOf("ALL", "A+", "B+", "O+", "AB+", "A-", "B-", "O-", "AB-")

    val filteredStudents = remember(students, searchQuery, selectedBloodGroupFilter) {
        students.filter { student ->
            val matchesSearch = student.name.contains(searchQuery, ignoreCase = true) ||
                    student.parentPhone.contains(searchQuery)
            val matchesBlood = selectedBloodGroupFilter == "ALL" || student.bloodGroup.equals(selectedBloodGroupFilter, ignoreCase = true)
            matchesSearch && matchesBlood
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "PLAYER ROSTER & PROFILES",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CourtGreenDark,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSlate) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Blood group filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(bloodGroups) { bg ->
                    FilterChip(
                        selected = selectedBloodGroupFilter == bg,
                        onClick = { selectedBloodGroupFilter = bg },
                        label = { Text(bg, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealContainer,
                            selectedLabelColor = CourtGreenDark,
                            containerColor = CardSurface,
                            labelColor = TextSlate
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedBloodGroupFilter == bg,
                            borderColor = CardBorderLight,
                            selectedBorderColor = CourtGreenPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Roster cards list
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No players found.",
                        color = TextSlate
                    )
                }
            } else {
                val avatarColors = listOf(AvatarTeal, AvatarGold, AvatarBlue)

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(filteredStudents, key = { _, s -> s.id }) { index, student ->
                        val avatarBg = avatarColors[index % avatarColors.size]

                        StudentCardItem(
                            student = student,
                            avatarBg = avatarBg,
                            onClick = { onStudentClick(student.id) }
                        )
                    }
                }
            }
        }

        // FAB Add New Student
        FloatingActionButton(
            onClick = { showAddStudentDialog = true },
            containerColor = CourtGreenPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Student")
        }

        if (showAddStudentDialog) {
            AddStudentDialog(
                onSaveImage = { bytes -> viewModel.saveImage(bytes, "student_photo") },
                onDismiss = { showAddStudentDialog = false },
                onAdd = { newStudent ->
                    viewModel.addStudent(newStudent)
                    showAddStudentDialog = false
                }
            )
        }
    }
}

@Composable
fun StudentCardItem(
    student: Student,
    avatarBg: Color,
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image or Avatar
            if (imageExists(student.profilePicPath)) {
                Image(
                    painter = rememberLocalImagePainter(student.profilePicPath),
                    contentDescription = student.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase(),
                        color = CourtGreenDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CourtGreenDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TealContainer,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = student.bloodGroup,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CourtGreenDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "${student.age} yrs",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSlate
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = TextSlate
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = student.parentPhone,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSlate
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSlate
            )
        }
    }
}

@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onAdd: (Student) -> Unit,
    onSaveImage: (ByteArray) -> String?
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("14") }
    var bloodGroup by remember { mutableStateOf("O+") }
    var parentPhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var profilePicPath by remember { mutableStateOf("") }

    val pickPhoto = rememberImagePicker { bytes ->
        onSaveImage(bytes)?.let { profilePicPath = it }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = CardSurface,
        title = { Text("Add New Player", fontWeight = FontWeight.Bold, color = CourtGreenDark) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Photo selector box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(TealContainer)
                        .clickable { pickPhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageExists(profilePicPath)) {
                        Image(
                            painter = rememberLocalImagePainter(profilePicPath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = CourtGreenDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Select Local Photo", fontSize = 12.sp, color = CourtGreenDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
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
                    label = { Text("Address / City") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val student = Student(
                            name = name.trim(),
                            age = age.toIntOrNull() ?: 14,
                            bloodGroup = bloodGroup.trim().uppercase(),
                            parentPhone = parentPhone.trim(),
                            address = address.trim(),
                            profilePicPath = profilePicPath
                        )
                        onAdd(student)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
                shape = CircleShape
            ) {
                Text("Save Player")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSlate)
            }
        }
    )
}
