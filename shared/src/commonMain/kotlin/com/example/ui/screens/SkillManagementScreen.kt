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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import com.example.data.model.Skill
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CourtGreenDark
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CourtGreenPrimary
import com.example.ui.theme.ScoreRed
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TextSlate
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SkillManagementScreen(
    viewModel: MainViewModel,
    skills: List<Skill>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddSkillDialog by remember { mutableStateOf(false) }
    var editingSkill by remember { mutableStateOf<Skill?>(null) }
    var deletingSkill by remember { mutableStateOf<Skill?>(null) }

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
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "DRILLS & SCORING CRITERIA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Add, rename, rescale or remove drills. Tap + to add.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(skills, key = { it.id }) { skill ->
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(1.dp, CardBorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = skill.skillName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CourtGreenDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = TealContainer
                                ) {
                                    Text(
                                        text = "Scale: ${skill.scoreRangeMin} to ${skill.scoreRangeMax}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CourtGreenDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            IconButton(onClick = { editingSkill = skill }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CourtGreenPrimary)
                            }
                            IconButton(onClick = { deletingSkill = skill }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ScoreRed)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddSkillDialog = true },
            containerColor = CourtGreenPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Skill")
        }

        if (showAddSkillDialog) {
            AddSkillDialog(
                onDismiss = { showAddSkillDialog = false },
                onAdd = { name, min, max ->
                    viewModel.addSkill(name, min, max)
                    showAddSkillDialog = false
                }
            )
        }

        editingSkill?.let { skill ->
            AddSkillDialog(
                existing = skill,
                onDismiss = { editingSkill = null },
                onAdd = { name, min, max ->
                    viewModel.updateSkill(skill.copy(skillName = name, scoreRangeMin = min, scoreRangeMax = max))
                    editingSkill = null
                }
            )
        }

        deletingSkill?.let { skill ->
            AlertDialog(
                onDismissRequest = { deletingSkill = null },
                containerColor = CardSurface,
                title = { Text("Delete \"${skill.skillName}\"?", fontWeight = FontWeight.Bold, color = CourtGreenDark) },
                text = { Text("This drill will be removed from scoring and future reports. Past scores for it will no longer be shown.", color = TextSlate) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteSkill(skill)
                            deletingSkill = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ScoreRed),
                        shape = CircleShape
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { deletingSkill = null }) { Text("Cancel", color = TextSlate) }
                }
            )
        }
    }
}

@Composable
fun AddSkillDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Int) -> Unit,
    existing: Skill? = null
) {
    var skillName by remember { mutableStateOf(existing?.skillName ?: "") }
    var scoreRangeMax by remember { mutableStateOf(existing?.scoreRangeMax ?: 10) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = CardSurface,
        title = { Text(if (existing == null) "Add Drill" else "Edit Drill", fontWeight = FontWeight.Bold, color = CourtGreenDark) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = skillName,
                    onValueChange = { skillName = it },
                    label = { Text("Drill name (e.g. Smash, Stamina, Footwork)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Text("Score Range Max:", fontWeight = FontWeight.SemiBold, color = CourtGreenDark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = scoreRangeMax == 5, onClick = { scoreRangeMax = 5 })
                    Text("1 to 5", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(20.dp))
                    RadioButton(selected = scoreRangeMax == 10, onClick = { scoreRangeMax = 10 })
                    Text("1 to 10", fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (skillName.isNotBlank()) {
                        onAdd(skillName.trim(), 1, scoreRangeMax)
                    }
                },
                enabled = skillName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
                shape = CircleShape
            ) {
                Text(if (existing == null) "Add Drill" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSlate) }
        }
    )
}
