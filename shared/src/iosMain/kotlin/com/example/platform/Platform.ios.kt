package com.example.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import platform.CoreGraphics.CGContextRestoreGState
import platform.CoreGraphics.CGContextSaveGState
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSMutableData
import platform.Foundation.NSNumber
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.numberWithDouble
import platform.Foundation.writeToFile
import platform.PDFKit.PDFDocument
import platform.PDFKit.kPDFDisplayBoxMediaBox
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.NSKernAttributeName
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIBezierPath
import platform.UIKit.UIColor
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIViewController
import platform.UIKit.drawAtPoint
import platform.UIKit.sizeWithAttributes
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

// ---------------------------------------------------------------- byte helpers

internal fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val out = ByteArray(size)
    if (size > 0) out.usePinned { memcpy(it.addressOf(0), bytes, length) }
    return out
}

@OptIn(BetaInteropApi::class)
internal fun ByteArray.toNSData(): NSData =
    if (isEmpty()) NSData() else usePinned { NSData.create(bytes = it.addressOf(0), length = size.toULong()) }

private fun directory(kind: ULong): String =
    (NSSearchPathForDirectoriesInDomains(kind, NSUserDomainMask, true).first() as String)

internal fun topViewController(): UIViewController? {
    var vc = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (vc?.presentedViewController != null) vc = vc.presentedViewController
    return vc
}

// ---------------------------------------------------------------- files & settings

actual object PlatformFiles {
    actual fun filesDir(): String = directory(NSDocumentDirectory)
    actual fun cacheDir(): String = directory(NSCachesDirectory)
    actual fun databasePath(name: String): String = "${directory(NSDocumentDirectory)}/$name"
    actual fun exists(path: String): Boolean = NSFileManager.defaultManager.fileExistsAtPath(path)
    actual fun readBytes(path: String): ByteArray? = NSData.dataWithContentsOfFile(path)?.toByteArray()

    actual fun writeBytes(path: String, bytes: ByteArray): Boolean {
        val dir = path.substringBeforeLast('/')
        NSFileManager.defaultManager.createDirectoryAtPath(dir, withIntermediateDirectories = true, attributes = null, error = null)
        return bytes.toNSData().writeToFile(path, atomically = true)
    }

    actual fun delete(path: String): Boolean =
        exists(path) && NSFileManager.defaultManager.removeItemAtPath(path, error = null)
}

actual class KeyValueStore actual constructor(name: String) {
    private val defaults = NSUserDefaults(suiteName = name)
    actual fun getString(key: String, default: String): String = defaults.stringForKey(key) ?: default
    actual fun putString(key: String, value: String) = defaults.setObject(value, forKey = key)
    actual fun getBoolean(key: String, default: Boolean): Boolean =
        if (defaults.objectForKey(key) == null) default else defaults.boolForKey(key)
    actual fun putBoolean(key: String, value: Boolean) = defaults.setBool(value, forKey = key)
}

// ---------------------------------------------------------------- sharing

actual object Sharing {
    actual fun shareFile(path: String, mimeType: String, message: String) {
        val top = topViewController() ?: return
        val sheet = UIActivityViewController(activityItems = listOf(NSURL.fileURLWithPath(path)), applicationActivities = null)
        // iPad presents the share sheet as a popover anchored to the screen centre
        sheet.popoverPresentationController?.let { pop ->
            pop.sourceView = top.view
            top.view.bounds.useContents { pop.sourceRect = CGRectMake(size.width / 2, size.height / 2, 1.0, 1.0) }
        }
        top.presentViewController(sheet, animated = true, completion = null)
    }

    /** iOS doesn't allow attaching a file to a specific WhatsApp chat; the share sheet lists WhatsApp. */
    actual fun shareToWhatsApp(path: String, mimeType: String, message: String, phone: String) =
        shareFile(path, mimeType, message)
}

// ---------------------------------------------------------------- pickers

private class ImagePickerDelegate(private val onResult: (ByteArray) -> Unit) : NSObject(), PHPickerViewControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return
        result.itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, _ ->
            // Re-encode as JPEG so HEIC photos decode everywhere
            val jpeg = data?.let { UIImage(data = it) }?.let { UIImageJPEGRepresentation(it, 0.85) }
            val bytes = jpeg?.toByteArray() ?: return@loadDataRepresentationForTypeIdentifier
            dispatch_async(dispatch_get_main_queue()) { onResult(bytes) }
        }
    }
}

private class FilePickerDelegate(private val onResult: (ByteArray) -> Unit) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
        NSData.dataWithContentsOfURL(url)?.toByteArray()?.let(onResult)
    }
}

@Composable
actual fun rememberImagePicker(onPicked: (ByteArray) -> Unit): () -> Unit {
    val latest by rememberUpdatedState(onPicked)
    val delegate = remember { ImagePickerDelegate { latest(it) } }
    return remember {
        {
            val config = PHPickerConfiguration().apply {
                filter = PHPickerFilter.imagesFilter
                selectionLimit = 1
            }
            val picker = PHPickerViewController(configuration = config)
            picker.delegate = delegate
            topViewController()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

@Composable
actual fun rememberFilePicker(onPicked: (ByteArray) -> Unit): () -> Unit {
    val latest by rememberUpdatedState(onPicked)
    val delegate = remember { FilePickerDelegate { latest(it) } }
    return remember {
        {
            val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeItem), asCopy = true)
            picker.delegate = delegate
            topViewController()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

/** iOS has no system back button; screens provide their own back arrows. */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

// ---------------------------------------------------------------- PDF preview

actual suspend fun renderPdfPages(path: String): List<ImageBitmap> = withContext(Dispatchers.IO) {
    val doc = PDFDocument(uRL = NSURL.fileURLWithPath(path))
    (0 until doc.pageCount.toInt()).mapNotNull { i ->
        val page = doc.pageAtIndex(i.toULong()) ?: return@mapNotNull null
        val (w, h) = page.boundsForBox(kPDFDisplayBoxMediaBox).useContents { size.width to size.height }
        val image = page.thumbnailOfSize(CGSizeMake(w * 2, h * 2), forBox = kPDFDisplayBoxMediaBox)
        UIImagePNGRepresentation(image)?.toByteArray()?.decodeToImageBitmap()
    }
}

// ---------------------------------------------------------------- PDF writing

actual fun createPdfWriter(): PdfWriter = IosPdfWriter()

private class IosPdfWriter : PdfWriter {
    private val data = NSMutableData()
    private var started = false

    override fun startPage(width: Float, height: Float): PdfCanvas {
        if (!started) {
            UIGraphicsBeginPDFContextToData(data, CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()), null)
            started = true
        }
        UIGraphicsBeginPDFPage()
        return IosPdfCanvas()
    }

    override fun finishPage() = Unit // the next BeginPDFPage / EndPDFContext closes the page

    override fun saveTo(path: String): Boolean {
        if (started) UIGraphicsEndPDFContext()
        started = false
        val dir = path.substringBeforeLast('/')
        NSFileManager.defaultManager.createDirectoryAtPath(dir, withIntermediateDirectories = true, attributes = null, error = null)
        return data.writeToFile(path, atomically = true)
    }
}

private fun uiColor(argb: Int): UIColor = UIColor.colorWithRed(
    red = ((argb shr 16) and 0xFF) / 255.0,
    green = ((argb shr 8) and 0xFF) / 255.0,
    blue = (argb and 0xFF) / 255.0,
    alpha = ((argb ushr 24) and 0xFF) / 255.0
)

private fun rect(l: Float, t: Float, r: Float, b: Float) =
    CGRectMake(l.toDouble(), t.toDouble(), (r - l).toDouble(), (b - t).toDouble())

@Suppress("CAST_NEVER_SUCCEEDS")
private class IosPdfCanvas : PdfCanvas {

    private fun font(size: Float, bold: Boolean): UIFont =
        if (bold) UIFont.boldSystemFontOfSize(size.toDouble()) else UIFont.systemFontOfSize(size.toDouble())

    private fun attrs(size: Float, color: Int, bold: Boolean, letterSpacing: Float): Map<Any?, *> = mapOf(
        NSFontAttributeName to font(size, bold),
        NSForegroundColorAttributeName to uiColor(color),
        NSKernAttributeName to NSNumber.numberWithDouble((letterSpacing * size).toDouble())
    )

    private fun stroked(path: UIBezierPath, color: Int, width: Float, dashed: Boolean) {
        path.lineWidth = width.toDouble()
        path.lineJoinStyle = platform.CoreGraphics.CGLineJoin.kCGLineJoinRound
        if (dashed) memScoped { path.setLineDash(allocArrayOf(5.0, 4.0), count = 2, phase = 0.0) }
        uiColor(color).setStroke()
        path.stroke()
    }

    override fun rect(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        uiColor(color).setFill()
        UIBezierPath.bezierPathWithRect(rect(left, top, right, bottom)).fill()
    }

    override fun roundRect(left: Float, top: Float, right: Float, bottom: Float, radius: Float, color: Int) {
        uiColor(color).setFill()
        UIBezierPath.bezierPathWithRoundedRect(rect(left, top, right, bottom), cornerRadius = radius.toDouble()).fill()
    }

    override fun ovalStroke(left: Float, top: Float, right: Float, bottom: Float, color: Int, width: Float) =
        stroked(UIBezierPath.bezierPathWithOvalInRect(rect(left, top, right, bottom)), color, width, false)

    override fun oval(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        uiColor(color).setFill()
        UIBezierPath.bezierPathWithOvalInRect(rect(left, top, right, bottom)).fill()
    }

    override fun line(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, width: Float, dashed: Boolean) {
        val path = UIBezierPath()
        path.moveToPoint(CGPointMake(x1.toDouble(), y1.toDouble()))
        path.addLineToPoint(CGPointMake(x2.toDouble(), y2.toDouble()))
        stroked(path, color, width, dashed)
    }

    override fun polyline(points: List<Pair<Float, Float>>, color: Int, width: Float, dashed: Boolean) {
        if (points.isEmpty()) return
        val path = UIBezierPath()
        path.moveToPoint(CGPointMake(points[0].first.toDouble(), points[0].second.toDouble()))
        points.drop(1).forEach { path.addLineToPoint(CGPointMake(it.first.toDouble(), it.second.toDouble())) }
        stroked(path, color, width, dashed)
    }

    override fun circle(cx: Float, cy: Float, radius: Float, color: Int) =
        oval(cx - radius, cy - radius, cx + radius, cy + radius, color)

    override fun text(text: String, x: Float, baseline: Float, size: Float, color: Int, bold: Boolean, letterSpacing: Float) {
        val f = font(size, bold)
        // drawAtPoint takes the top of the line box; convert from the baseline
        (text as NSString).drawAtPoint(
            CGPointMake(x.toDouble(), baseline.toDouble() - f.ascender),
            withAttributes = attrs(size, color, bold, letterSpacing)
        )
    }

    override fun measureText(text: String, size: Float, bold: Boolean, letterSpacing: Float): Float =
        (text as NSString).sizeWithAttributes(attrs(size, 0, bold, letterSpacing)).useContents { width.toFloat() }

    override fun image(bytes: ByteArray, left: Float, top: Float, right: Float, bottom: Float, clip: ImageClip, radius: Float, centerCrop: Boolean) {
        val img = UIImage(data = bytes.toNSData())
        val (iw, ih) = img.size.useContents { width to height }
        if (iw <= 0.0 || ih <= 0.0) return
        val dst = rect(left, top, right, bottom)
        val dw = (right - left).toDouble()
        val dh = (bottom - top).toDouble()
        // center-crop: scale to fill the box, then clip to it
        val scale = if (centerCrop) maxOf(dw / iw, dh / ih) else minOf(dw / iw, dh / ih)
        val w = iw * scale
        val h = ih * scale
        val drawRect = CGRectMake(left + (dw - w) / 2, top + (dh - h) / 2, w, h)

        val ctx = UIGraphicsGetCurrentContext()
        CGContextSaveGState(ctx)
        when (clip) {
            ImageClip.CIRCLE -> UIBezierPath.bezierPathWithOvalInRect(dst).addClip()
            ImageClip.ROUNDED -> UIBezierPath.bezierPathWithRoundedRect(dst, cornerRadius = radius.toDouble()).addClip()
            ImageClip.NONE -> UIBezierPath.bezierPathWithRect(dst).addClip()
        }
        img.drawInRect(drawRect)
        CGContextRestoreGState(ctx)
    }
}
