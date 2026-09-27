package com.example.ui.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import com.example.platform.PlatformFiles
import com.example.util.Dates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap

/** True when [path] points at an existing image file in app storage. */
fun imageExists(path: String): Boolean = path.isNotEmpty() && PlatformFiles.exists(path)

/** Loads an image file from app storage off the main thread; transparent until ready. */
@Composable
fun rememberLocalImagePainter(path: String): Painter {
    val bitmap by produceState<ImageBitmap?>(null, path) {
        value = withContext(Dispatchers.IO) {
            try {
                PlatformFiles.readBytes(path)?.decodeToImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }
    return remember(bitmap) { bitmap?.let { BitmapPainter(it) } ?: ColorPainter(Color.Transparent) }
}

/**
 * Returns a function that opens a Material date picker preset to [initialIso];
 * the chosen date is delivered as an ISO string (yyyy-MM-dd).
 */
@Composable
fun rememberDatePicker(initialIso: String, onPicked: (String) -> Unit): () -> Unit {
    var show by remember { mutableStateOf(false) }
    if (show) {
        val state = rememberDatePickerState(initialSelectedDateMillis = Dates.isoToPickerMillis(initialIso))
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onPicked(Dates.pickerMillisToIso(it)) }
                    show = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = state)
        }
    }
    return { show = true }
}
