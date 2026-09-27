package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.example.platform.renderPdfPages
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CourtGreenDark
import com.example.ui.theme.CourtGreenDeep
import com.example.ui.viewmodel.GeneratedReport

private val WhatsAppGreen = Color(0xFF25D366)

@Composable
fun ReportPreviewDialog(
    generated: GeneratedReport,
    onDismiss: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onShareOther: () -> Unit
) {
    var pages by remember(generated.path) { mutableStateOf<List<ImageBitmap>?>(null) }

    LaunchedEffect(generated.path) {
        pages = renderPdfPages(generated.path)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFE2E8F0)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(color = CourtGreenDeep, contentColor = Color.White) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Report Preview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "${generated.report.student.name} • ${generated.report.timeframe.displayName}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    val rendered = pages
                    if (rendered == null) {
                        CircularProgressIndicator(color = CourtGreenDark)
                    } else if (rendered.isEmpty()) {
                        Text("Could not render the preview.", color = Color.DarkGray)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                        ) {
                            items(rendered) { bmp ->
                                Image(
                                    bitmap = bmp,
                                    contentDescription = "Report page",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(bmp.width.toFloat() / bmp.height)
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFCBD5E1))
                                )
                            }
                        }
                    }
                }

                Surface(color = CardSurface, shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onShareWhatsApp,
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            shape = CircleShape,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onShareOther,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, CourtGreenDark),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text("Share / Save", color = CourtGreenDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
