package com.example.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/** App-private file storage. Paths are absolute. */
expect object PlatformFiles {
    fun filesDir(): String
    fun cacheDir(): String
    fun databasePath(name: String): String
    fun exists(path: String): Boolean
    fun readBytes(path: String): ByteArray?
    fun writeBytes(path: String, bytes: ByteArray): Boolean
    fun delete(path: String): Boolean
}

/** Small persistent key/value store (SharedPreferences on Android, NSUserDefaults on iOS). */
expect class KeyValueStore(name: String) {
    fun getString(key: String, default: String): String
    fun putString(key: String, value: String)
    fun getBoolean(key: String, default: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
}

/** Opens the system share sheet (or WhatsApp directly where the platform allows it). */
expect object Sharing {
    fun shareFile(path: String, mimeType: String, message: String)
    fun shareToWhatsApp(path: String, mimeType: String, message: String, phone: String)
}

/** Returns a launcher that lets the user pick a photo; the chosen image's bytes are delivered. */
@Composable
expect fun rememberImagePicker(onPicked: (ByteArray) -> Unit): () -> Unit

/** Returns a launcher that lets the user pick any file (used to restore backups). */
@Composable
expect fun rememberFilePicker(onPicked: (ByteArray) -> Unit): () -> Unit

/** Keeps status/navigation bar icons readable for the app's current theme. */
@Composable
expect fun SystemBarsAppearance(dark: Boolean)

@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)

/** Renders each page of a PDF file to an image for the in-app preview. */
expect suspend fun renderPdfPages(path: String): List<ImageBitmap>

/** Platform PDF writer; the report layout itself is drawn in common code through [PdfCanvas]. */
expect fun createPdfWriter(): PdfWriter

interface PdfWriter {
    fun startPage(width: Float, height: Float): PdfCanvas
    fun finishPage()
    fun saveTo(path: String): Boolean
}

enum class ImageClip { NONE, CIRCLE, ROUNDED }

/** Colors are ARGB ints (e.g. 0xFF14284B.toInt()). Text y is the baseline. */
interface PdfCanvas {
    fun rect(left: Float, top: Float, right: Float, bottom: Float, color: Int)
    fun roundRect(left: Float, top: Float, right: Float, bottom: Float, radius: Float, color: Int)
    fun ovalStroke(left: Float, top: Float, right: Float, bottom: Float, color: Int, width: Float)
    fun oval(left: Float, top: Float, right: Float, bottom: Float, color: Int)
    fun line(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, width: Float, dashed: Boolean = false)
    fun polyline(points: List<Pair<Float, Float>>, color: Int, width: Float, dashed: Boolean = false)
    fun circle(cx: Float, cy: Float, radius: Float, color: Int)
    fun text(text: String, x: Float, baseline: Float, size: Float, color: Int, bold: Boolean = false, letterSpacing: Float = 0f)
    fun measureText(text: String, size: Float, bold: Boolean = false, letterSpacing: Float = 0f): Float
    /** Draws encoded image bytes (PNG/JPEG) into the box, center-cropped, optionally clipped. */
    fun image(bytes: ByteArray, left: Float, top: Float, right: Float, bottom: Float, clip: ImageClip, radius: Float = 0f, centerCrop: Boolean = true)
}
