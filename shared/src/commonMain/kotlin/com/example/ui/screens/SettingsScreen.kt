package com.example.ui.screens

import com.example.ui.components.imageExists
import com.example.ui.components.rememberLocalImagePainter
import com.example.platform.rememberImagePicker
import com.example.platform.rememberFilePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademyConfig
import com.example.ui.components.NetSetLogo
import com.example.ui.components.NexonvateLogo
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CourtGreenDark
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CourtGreenPrimary
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TextSlate
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    academyConfig: AcademyConfig,
    onOpenSkillManagement: () -> Unit,
    modifier: Modifier = Modifier
) {

    var academyName by remember(academyConfig) { mutableStateOf(academyConfig.academyName) }
    var coachName by remember(academyConfig) { mutableStateOf(academyConfig.coachName) }
    var coachPhone by remember(academyConfig) { mutableStateOf(academyConfig.coachPhone) }
    var coachEmail by remember(academyConfig) { mutableStateOf(academyConfig.coachEmail) }

    var backupStatusText by remember { mutableStateOf<String?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val pickRestoreFile = rememberFilePicker { bytes ->
        viewModel.restoreDatabase(bytes) { success ->
            backupStatusText = if (success) "Backup restored successfully!" else "Failed to restore: not a NetSet backup file."
        }
    }

    val pickLogo = rememberImagePicker { bytes ->
        val path = viewModel.saveImage(bytes, "academy_logo")
        if (path != null) {
            viewModel.saveAcademyConfig(
                academyConfig.copy(
                    academyName = academyName.trim(),
                    coachName = coachName.trim(),
                    coachPhone = coachPhone.trim(),
                    coachEmail = coachEmail.trim(),
                    logoPath = path
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER
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
                    Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "COACH & ACADEMY PROFILE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Customize logo, PDF header info, parameters and backup data",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // ACADEMY LOGO UPLOAD CARD
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, CardBorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ACADEMY LOGO (FOR PDF REPORT)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CourtGreenDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (imageExists(academyConfig.logoPath)) {
                            Image(
                                painter = rememberLocalImagePainter(academyConfig.logoPath),
                                contentDescription = "Academy Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, CardBorderLight, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(TealContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, tint = CourtGreenDark, modifier = Modifier.size(28.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = if (academyConfig.logoPath.isNotEmpty()) "Logo Attached" else "No Logo Uploaded",
                                fontWeight = FontWeight.Bold,
                                color = CourtGreenDark
                            )
                            Text(
                                text = "Header branding for PDF reports",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSlate
                            )
                        }
                    }

                    Button(
                        onClick = pickLogo,
                        colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (academyConfig.logoPath.isNotEmpty()) "Change" else "Upload", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ACADEMY DETAILS CARD
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, CardBorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ACADEMY & COACH INFO (FOR PDF REPORT)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CourtGreenDark
                )

                OutlinedTextField(
                    value = academyName,
                    onValueChange = { academyName = it },
                    label = { Text("Academy / Club Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = coachName,
                    onValueChange = { coachName = it },
                    label = { Text("Head Coach Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = coachPhone,
                        onValueChange = { coachPhone = it },
                        label = { Text("Coach Phone") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = coachEmail,
                        onValueChange = { coachEmail = it },
                        label = { Text("Coach Email") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Button(
                    onClick = {
                        viewModel.saveAcademyConfig(
                            academyConfig.copy(
                                academyName = academyName.trim(),
                                coachName = coachName.trim(),
                                coachPhone = coachPhone.trim(),
                                coachEmail = coachEmail.trim()
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Coach & Academy Profile", fontWeight = FontWeight.Bold)
                }
            }
        }

        // SKILL PARAMETERS MANAGE CARD
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CUSTOM SKILL PARAMETERS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CourtGreenDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Add, edit or adjust rating ranges (1-5 or 1-10)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSlate
                    )
                }

                OutlinedButton(
                    onClick = onOpenSkillManagement,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, CourtGreenDark)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp), tint = CourtGreenDark)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manage", color = CourtGreenDark, fontWeight = FontWeight.Bold)
                }
            }
        }

        // DATA SECURITY & OFFLINE BACKUP CARD
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, CardBorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = CourtGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OFFLINE DATA BACKUP & RESTORE",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CourtGreenDark
                    )
                }

                Text(
                    text = "Your data is stored 100% locally on your device with no internet dependence. Export a backup file (save it to Drive, Files or WhatsApp) to prevent data loss, or restore from a previously saved backup.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSlate
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.exportDatabaseBackup { ok ->
                                backupStatusText = if (ok) "Backup created - choose where to save it." else "Failed to export backup."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CourtGreenPrimary),
                        shape = CircleShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export .db", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = pickRestoreFile,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, CourtGreenDark),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp), tint = CourtGreenDark)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore .db", fontSize = 12.sp, color = CourtGreenDark, fontWeight = FontWeight.Bold)
                    }
                }

                if (backupStatusText != null) {
                    Text(
                        text = backupStatusText!!,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = CourtGreenPrimary
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = CardBorderLight)

                // DEMO SEED BUTTON
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PRE-FILL DEMO DATA",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = CourtGreenDark
                        )
                        Text(
                            text = "Add 6 sample students with scores, attendance & matches",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSlate
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.seedDemoData() },
                        shape = CircleShape,
                        border = BorderStroke(1.dp, CourtGreenDark)
                    ) {
                        Text("Seed Demo", color = CourtGreenDark, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CLEAR ALL DATA",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = com.example.ui.theme.ScoreRed
                        )
                        Text(
                            text = "Remove demo/all students before starting real use",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSlate
                        )
                    }

                    OutlinedButton(
                        onClick = { showClearConfirm = true },
                        shape = CircleShape,
                        border = BorderStroke(1.dp, com.example.ui.theme.ScoreRed)
                    ) {
                        Text("Clear", color = com.example.ui.theme.ScoreRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ABOUT / LICENSING
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, CardBorderLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NetSetLogo(size = 72.dp)
                Text("NetSet - The Badminton Connect", fontWeight = FontWeight.Bold, color = CourtGreenDark)
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = CardBorderLight)
                NexonvateLogo(height = 96.dp)
                Text("Licensed by NEXONVATE", fontWeight = FontWeight.Bold, color = CourtGreenDark)
                Text("Powered by NEXONVATE", style = MaterialTheme.typography.bodySmall, color = TextSlate)
            }
        }
    }

    if (showClearConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = CardSurface,
            title = { Text("Clear all data?", fontWeight = FontWeight.Bold, color = CourtGreenDark) },
            text = { Text("All students, scores, attendance and match records will be permanently deleted. Export a backup first if you need it.", color = TextSlate) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.ScoreRed),
                    shape = CircleShape
                ) { Text("Delete everything") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showClearConfirm = false }) { Text("Cancel", color = TextSlate) }
            }
        )
    }
}
