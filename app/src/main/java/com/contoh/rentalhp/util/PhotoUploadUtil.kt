package com.contoh.rentalhp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.contoh.rentalhp.R
import com.contoh.rentalhp.ui.common.UserMessageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

object PhotoUploadUtil {
    const val MAX_UPLOAD_BYTES = 2L * 1024 * 1024
    private const val MAX_SOURCE_BYTES = 30L * 1024 * 1024
    private const val MAX_EDGE = 1280
    private val allowedTypes = setOf("image/jpeg", "image/png", "image/webp")

    suspend fun validate(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
        try {
            validateSource(context, uri)
        } catch (_: IOException) {
            throw UserMessageException(context.getString(R.string.image_cannot_read))
        } catch (_: SecurityException) {
            throw UserMessageException(context.getString(R.string.image_cannot_read))
        }
    }

    suspend fun prepare(context: Context, uri: Uri): File {
        var output: File? = null
        try {
            return withContext(Dispatchers.IO) {
                validateSource(context, uri)
                val resolver = context.contentResolver
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                    throw UserMessageException(context.getString(R.string.invalid_image))
                }

                var sampleSize = 1
                while (max(bounds.outWidth, bounds.outHeight) / sampleSize > MAX_EDGE * 2) {
                    sampleSize *= 2
                }
                val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                var bitmap = resolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, options)
                } ?: throw UserMessageException(context.getString(R.string.invalid_image))

                try {
                    val longestSide = max(bitmap.width, bitmap.height)
                    if (longestSide > MAX_EDGE) {
                        val scale = MAX_EDGE.toFloat() / longestSide
                        val scaled = Bitmap.createScaledBitmap(
                            bitmap,
                            (bitmap.width * scale).roundToInt().coerceAtLeast(1),
                            (bitmap.height * scale).roundToInt().coerceAtLeast(1),
                            true
                        )
                        if (scaled !== bitmap) bitmap.recycle()
                        bitmap = scaled
                    }

                    val orientation = resolver.openInputStream(uri)?.use {
                        try {
                            ExifInterface(it).getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL
                            )
                        } catch (_: IOException) {
                            ExifInterface.ORIENTATION_NORMAL
                        }
                    } ?: ExifInterface.ORIENTATION_NORMAL
                    val oriented = orient(bitmap, orientation)
                    if (oriented !== bitmap) bitmap.recycle()
                    bitmap = oriented

                    if (bitmap.hasAlpha()) {
                        val opaque = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.RGB_565)
                        Canvas(opaque).apply {
                            drawColor(Color.WHITE)
                            drawBitmap(bitmap, 0f, 0f, null)
                        }
                        bitmap.recycle()
                        bitmap = opaque
                    }

                    val file = File.createTempFile("alat_", ".jpg", context.cacheDir)
                    output = file
                    var quality = 80
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val compressed = file.outputStream().use {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
                        }
                        if (!compressed || file.length() == 0L) {
                            throw UserMessageException(context.getString(R.string.invalid_image))
                        }
                        if (file.length() < MAX_UPLOAD_BYTES) break
                        if (quality > 50) {
                            quality -= 10
                        } else {
                            val scaled = Bitmap.createScaledBitmap(
                                bitmap,
                                (bitmap.width * 0.8f).roundToInt().coerceAtLeast(1),
                                (bitmap.height * 0.8f).roundToInt().coerceAtLeast(1),
                                true
                            )
                            if (scaled === bitmap) {
                                throw UserMessageException(context.getString(R.string.image_upload_too_large))
                            }
                            bitmap.recycle()
                            bitmap = scaled
                            quality = 80
                        }
                    }
                    file
                } finally {
                    bitmap.recycle()
                }
            }
        } catch (error: Throwable) {
            withContext(NonCancellable + Dispatchers.IO) { output?.delete() }
            if (error is IOException || error is SecurityException) {
                throw UserMessageException(context.getString(R.string.image_cannot_read))
            }
            throw error
        }
    }

    private suspend fun validateSource(context: Context, uri: Uri) {
        val resolver = context.contentResolver
        if (resolver.getType(uri)?.lowercase(Locale.ROOT) !in allowedTypes) {
            throw UserMessageException(context.getString(R.string.invalid_image_type))
        }
        val input = resolver.openInputStream(uri)
            ?: throw UserMessageException(context.getString(R.string.image_cannot_read))
        input.use {
            val buffer = ByteArray(8192)
            var size = 0L
            while (true) {
                currentCoroutineContext().ensureActive()
                val read = it.read(buffer)
                if (read < 0) break
                size += read
                if (size > MAX_SOURCE_BYTES) {
                    throw UserMessageException(context.getString(R.string.image_source_too_large))
                }
            }
            if (size == 0L) throw UserMessageException(context.getString(R.string.invalid_image))
        }
    }

    private fun orient(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
