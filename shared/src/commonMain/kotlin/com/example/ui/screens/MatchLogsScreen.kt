package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.MatchLog
import com.example.data.model.Student
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
fun MatchLogsScreen(
    viewModel: MainViewModel,
    matchLogs: List<MatchLog>,
    students: List<Student>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showLogDialog by remember { mutableStateOf(false) }

    val studentMap = remember(students) { students.associateBy { it.id } }

    val filteredLogs = remember(matchLogs, searchQuery) {
        if (searchQuery.isBlank()) matchLogs
        else matchLogs.filter { log ->
            val player = studentMap[log.studentId]?.name ?: ""
            player.contains(searchQuery, ignoreCase = true) ||
                    log.opponentNames.contains(searchQuery, ignoreCase = true) ||
                    (log.partnerName?.contains(searchQuery, ignoreCase = true) == true)
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
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Sports, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ACADEMY MATCH RECORDS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Recent Singles & Doubles competition results",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter matches by player, opponent or partner...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CourtGreenDark) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardSurface,
                unfocusedContainerColor = CardSurface,
                focusedBorderColor = CourtGreenPrimary,
                unfocusedBorderColor = CardBorderLight
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredLogs.isEmpty()) {
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
                    Text(
                        text = "No match records found.",
                        color = TextSlate,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    val studentName = studentMap[log.studentId]?.name ?: "Unknown Player"

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
                                Text(
                                    text = studentName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CourtGreenDark
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = if (log.outcome.equals("Win", true)) ScoreGreen else ScoreRed
                                ) {
                                    Text(
                                        text = log.outcome.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${log.type} Match  •  ${log.date}",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSlate,
                                fontWeight = FontWeight.Medium
                            )

                            if (!log.partnerName.isNullOrEmpty()) {
                                Text(
                                    text = "Partner: ${log.partnerName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSlate
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "vs. ${log.opponentNames}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = CourtGreenDark
                            )

                            Text(
                                text = "Score: ${log.score}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CourtGreenPrimary
                            )

                            if (log.performanceNotes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Notes: ${log.performanceNotes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSlate
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { showLogDialog = true },
            enabled = students.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("LOG MATCH RESULT", fontWeight = FontWeight.Bold)
        }
    }

    if (showLogDialog) {
        var selectedStudent by remember { mutableStateOf<Student?>(null) }
        var menuOpen by remember { mutableStateOf(false) }
        AddMatchLogDialog(
            studentId = selectedStudent?.id,
            onDismiss = { showLogDialog = false },
            onSave = { match ->
                viewModel.addMatchLog(match)
                showLogDialog = false
            },
            headerContent = {
                Box {
                    OutlinedButton(
                        onClick = { menuOpen = true },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CourtGreenDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedStudent?.name ?: "Select player *",
                            color = CourtGreenDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = CourtGreenDark)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        students.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.name) },
                                onClick = {
                                    selectedStudent = st
                                    menuOpen = false
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}
