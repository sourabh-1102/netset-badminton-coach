package com.example.platform

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

/** Holds the application context and current activity for the Android actuals. */
object AndroidPlatform {
    lateinit var appContext: Context
        private set
    private var activityRef: WeakReference<Activity>? = null

    fun init(activity: Activity) {
        appContext = activity.applicationContext
        activityRef = WeakReference(activity)
    }

    /** Prefer the live activity for starting UI; fall back to the app context with NEW_TASK. */
    fun startActivity(intent: Intent) {
        val activity = activityRef?.get()
        if (activity != null && !activity.isFinishing) {
            activity.startActivity(intent)
        } else {
            appContext.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}

actual object PlatformFiles {
    actual fun filesDir(): String = AndroidPlatform.appContext.filesDir.absolutePath
    actual fun cacheDir(): String = AndroidPlatform.appContext.cacheDir.absolutePath
    actual fun databasePath(name: String): String = AndroidPlatform.appContext.getDatabasePath(name).absolutePath
    actual fun exists(path: String): Boolean = File(path).exists()
    actual fun readBytes(path: String): ByteArray? = try {
        File(path).takeIf { it.exists() }?.readBytes()
    } catch (e: Exception) {
        null
    }

    actual fun writeBytes(path: String, bytes: ByteArray): Boolean = try {
        File(path).parentFile?.mkdirs()
        FileOutputStream(path).use { it.write(bytes) }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    actual fun delete(path: String): Boolean = File(path).delete()
}

actual class KeyValueStore actual constructor(name: String) {
    private val prefs = AndroidPlatform.appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    actual fun getString(key: String, default: String): String = prefs.getString(key, default) ?: default
    actual fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()
    actual fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)
    actual fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
}

actual object Sharing {
    private fun uriFor(path: String): Uri {
        val ctx = AndroidPlatform.appContext
        return FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", File(path))
    }

    private fun sendIntent(path: String, mimeType: String, message: String) =
        Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uriFor(path))
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    actual fun shareFile(path: String, mimeType: String, message: String) {
        AndroidPlatform.startActivity(Intent.createChooser(sendIntent(path, mimeType, message), "Share"))
    }

    /** Opens WhatsApp directly (to the parent's chat when a number is known), else the share sheet. */
    actual fun shareToWhatsApp(path: String, mimeType: String, message: String, phone: String) {
        val digits = phone.filter { it.isDigit() }
        val jid = when {
            digits.length == 10 -> "91$digits"
            digits.length > 10 -> digits
            else -> null
        }
        for (pkg in listOf("com.whatsapp", "com.whatsapp.w4b")) {
            try {
                val intent = sendIntent(path, mimeType, message).setPackage(pkg)
                if (jid != null) intent.putExtra("jid", "$jid@s.whatsapp.net")
                AndroidPlatform.startActivity(intent)
                return
            } catch (_: ActivityNotFoundException) {
            }
        }
        shareFile(path, mimeType, message)
    }
}

private fun readUri(uri: Uri): ByteArray? = try {
    AndroidPlatform.appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
} catch (e: Exception) {
    null
}

@Composable
actual fun rememberImagePicker(onPicked: (ByteArray) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(::readUri)?.let(onPicked)
    }
    return { launcher.launch("image/*") }
}

@Composable
actual fun rememberFilePicker(onPicked: (ByteArray) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(::readUri)?.let(onPicked)
    }
    return { launcher.launch("*/*") }
}

@Composable
actual fun SystemBarsAppearance(dark: Boolean) {
    val view = androidx.compose.ui.platform.LocalView.current
    androidx.compose.runtime.SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = BackHandler(enabled, onBack)

actual suspend fun renderPdfPages(path: String): List<ImageBitmap> = withContext(Dispatchers.IO) {
    try {
        ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                (0 until renderer.pageCount).map { i ->
                    renderer.openPage(i).use { page ->
                        val bmp = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                        bmp.eraseColor(android.graphics.Color.WHITE)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bmp.asImageBitmap()
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

actual fun createPdfWriter(): PdfWriter = AndroidPdfWriter()

private class AndroidPdfWriter : PdfWriter {
    private val doc = PdfDocument()
    private var page: PdfDocument.Page? = null
    private var count = 0

    override fun startPage(width: Float, height: Float): PdfCanvas {
        count++
        val p = doc.startPage(PdfDocument.PageInfo.Builder(width.toInt(), height.toInt(), count).create())
        page = p
        return AndroidPdfCanvas(p.canvas)
    }

    override fun finishPage() {
        page?.let { doc.finishPage(it) }
        page = null
    }

    override fun saveTo(path: String): Boolean = try {
        File(path).parentFile?.mkdirs()
        FileOutputStream(path).use { doc.writeTo(it) }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    } finally {
        doc.close()
    }
}

private class AndroidPdfCanvas(private val canvas: Canvas) : PdfCanvas {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val regular = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    private val boldFace = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

    private fun fill(color: Int) {
        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.FILL; paint.color = color
    }

    private fun stroke(color: Int, width: Float, dashed: Boolean = false) {
        paint.reset(); paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE; paint.color = color; paint.strokeWidth = width
        paint.strokeJoin = Paint.Join.ROUND
        if (dashed) paint.pathEffect = DashPathEffect(floatArrayOf(5f, 4f), 0f)
    }

    override fun rect(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        fill(color); canvas.drawRect(left, top, right, bottom, paint)
    }

    override fun roundRect(left: Float, top: Float, right: Float, bottom: Float, radius: Float, color: Int) {
        fill(color); canvas.drawRoundRect(RectF(left, top, right, bottom), radius, radius, paint)
    }

    override fun ovalStroke(left: Float, top: Float, right: Float, bottom: Float, color: Int, width: Float) {
        stroke(color, width); canvas.drawOval(RectF(left, top, right, bottom), paint)
    }

    override fun oval(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        fill(color); canvas.drawOval(RectF(left, top, right, bottom), paint)
    }

    override fun line(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, width: Float, dashed: Boolean) {
        stroke(color, width, dashed); canvas.drawLine(x1, y1, x2, y2, paint)
    }

    override fun polyline(points: List<Pair<Float, Float>>, color: Int, width: Float, dashed: Boolean) {
        if (points.isEmpty()) return
        val path = Path().apply {
            moveTo(points[0].first, points[0].second)
            points.drop(1).forEach { lineTo(it.first, it.second) }
        }
        stroke(color, width, dashed); canvas.drawPath(path, paint)
    }

    override fun circle(cx: Float, cy: Float, radius: Float, color: Int) {
        fill(color); canvas.drawCircle(cx, cy, radius, paint)
    }

    private fun prepText(size: Float, color: Int, bold: Boolean, letterSpacing: Float) {
        fill(color)
        paint.textSize = size
        paint.typeface = if (bold) boldFace else regular
        paint.letterSpacing = letterSpacing
    }

    override fun text(text: String, x: Float, baseline: Float, size: Float, color: Int, bold: Boolean, letterSpacing: Float) {
        prepText(size, color, bold, letterSpacing)
        canvas.drawText(text, x, baseline, paint)
    }

    override fun measureText(text: String, size: Float, bold: Boolean, letterSpacing: Float): Float {
        prepText(size, 0, bold, letterSpacing)
        return paint.measureText(text)
    }

    override fun image(bytes: ByteArray, left: Float, top: Float, right: Float, bottom: Float, clip: ImageClip, radius: Float, centerCrop: Boolean) {
        val bmp = decodeSampled(bytes, maxOf(right - left, bottom - top).toInt() * 3) ?: return
        val dst = RectF(left, top, right, bottom)
        val src = if (centerCrop) {
            val side = minOf(bmp.width, bmp.height)
            val l = (bmp.width - side) / 2
            val t = (bmp.height - side) / 2
            Rect(l, t, l + side, t + side)
        } else null
        canvas.save()
        when (clip) {
            ImageClip.CIRCLE -> canvas.clipPath(Path().apply { addOval(dst, Path.Direction.CW) })
            ImageClip.ROUNDED -> canvas.clipPath(Path().apply { addRoundRect(dst, radius, radius, Path.Direction.CW) })
            ImageClip.NONE -> Unit
        }
        fill(android.graphics.Color.WHITE)
        canvas.drawBitmap(bmp, src, dst, paint)
        canvas.restore()
    }

    private fun decodeSampled(bytes: ByteArray, target: Int): Bitmap? = try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= target && bounds.outHeight / (sample * 2) >= target) sample *= 2
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
    } catch (e: Exception) {
        null
    }
}
