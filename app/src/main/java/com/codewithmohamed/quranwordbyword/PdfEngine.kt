package com.codewithmohamed.quranwordbyword

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.*
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

/** All native PDF access is serialized on one worker, including shutdown. */
class PdfEngine(private val context: Context) {
    private val executor = Executors.newSingleThreadExecutor()
    private val dispatcher = executor.asCoroutineDispatcher()
    private var renderer: PdfRenderer? = null
    suspend fun open() = withContext(dispatcher) {
        val file = File(context.filesDir, "bundled-quran-960.pdf")
        if (!file.exists()) {
            val temp = File(context.filesDir, "quran-${UUID.randomUUID()}.tmp")
            try {
                context.assets.open("quran-960.pdf").use { input -> temp.outputStream().use { input.copyTo(it) } }
                ensureActive()
                java.nio.file.Files.move(temp.toPath(), file.toPath(), java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING)
            } finally { temp.delete() }
        }
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        try { renderer = PdfRenderer(fd) } catch (e: Exception) { fd.close(); throw e }
        require(renderer!!.pageCount == 960) { "Included Qur’an page count is invalid" }
    }
    suspend fun render(page: Int, width: Int): Bitmap = withContext(dispatcher) {
        require(page in 1..960)
        renderer!!.openPage(page - 1).use { pdf ->
            val requested = width.coerceIn(800, 2200)
            val scale = minOf(requested.toDouble() / pdf.width, kotlin.math.sqrt(4_500_000.0 / (pdf.width.toDouble()*pdf.height)))
            val bitmap = Bitmap.createBitmap((pdf.width*scale).toInt().coerceAtLeast(1),
                (pdf.height*scale).toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
            try { bitmap.eraseColor(Color.WHITE); pdf.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY); bitmap }
            catch (e: Throwable) { bitmap.recycle(); throw e }
        }
    }
    /** Render visible viewport at screen resolution instead of stretching a small preview. */
    suspend fun renderViewport(page: Int, previewWidth: Int, previewScale: Float,
                               x: Float, y: Float, width: Int, height: Int): Bitmap = withContext(dispatcher) {
        renderer!!.openPage(page - 1).use { pdf ->
            val quality = minOf(1.5f, kotlin.math.sqrt(4_500_000f / (width.toFloat()*height)))
            val bitmap = Bitmap.createBitmap((width*quality).toInt().coerceAtLeast(1),
                (height*quality).toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
            val matrix = Matrix().apply {
                setScale(previewWidth.toFloat()/pdf.width*previewScale*quality,
                    previewWidth.toFloat()/pdf.width*previewScale*quality)
                postTranslate(x*quality, y*quality)
            }
            try { bitmap.eraseColor(Color.WHITE); pdf.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY); bitmap }
            catch (e: Throwable) { bitmap.recycle(); throw e }
        }
    }
    fun close() {
        // Queued after any in-flight render; ownership never crosses threads.
        executor.execute { renderer?.close(); renderer = null; dispatcher.close() }
        executor.shutdown()
    }
}
