package com.contoh.rentalhp

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.contoh.rentalhp.ui.common.UserMessageException
import com.contoh.rentalhp.util.PhotoUploadUtil
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Random
import kotlin.math.abs
import kotlin.math.max

@SdkSuppress(minSdkVersion = 29)
@RunWith(AndroidJUnit4::class)
class PhotoUploadTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun largeContentUri_isDownscaledAndCompressedBelowServerLimit() = runBlocking {
        val uri = GalleryTestPhoto.create(context, 2048, 2048, noise = true)
        var result: File? = null
        try {
            val sourceSize = context.contentResolver.openAssetFileDescriptor(uri, "r")!!.use { it.length }
            assertTrue("The fixture must exceed the upload limit before processing", sourceSize > PhotoUploadUtil.MAX_UPLOAD_BYTES)
            result = PhotoUploadUtil.prepare(context, uri)
            assertTrue(result.length() in 1 until PhotoUploadUtil.MAX_UPLOAD_BYTES)
            assertTrue(result.name.endsWith(".jpg"))
            val bitmap = BitmapFactory.decodeFile(result.absolutePath)
            try {
                assertTrue(max(bitmap.width, bitmap.height) <= 1280)
            } finally {
                bitmap.recycle()
            }
        } finally {
            result?.delete()
            context.contentResolver.delete(uri, null, null)
        }
    }

    @Test
    fun allExifOrientations_areAppliedToPixels() = runBlocking {
        val expectedCorners = listOf(
            listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW),
            listOf(Color.GREEN, Color.RED, Color.YELLOW, Color.BLUE),
            listOf(Color.YELLOW, Color.BLUE, Color.GREEN, Color.RED),
            listOf(Color.BLUE, Color.YELLOW, Color.RED, Color.GREEN),
            listOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW),
            listOf(Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN),
            listOf(Color.YELLOW, Color.GREEN, Color.BLUE, Color.RED),
            listOf(Color.GREEN, Color.YELLOW, Color.RED, Color.BLUE)
        )
        for (orientation in 1..8) {
            val uri = GalleryTestPhoto.create(context, 640, 480, orientation)
            var result: File? = null
            try {
                result = PhotoUploadUtil.prepare(context, uri)
                val bitmap = BitmapFactory.decodeFile(result.absolutePath)
                try {
                    assertEquals(if (orientation >= 5) 480 else 640, bitmap.width)
                    assertEquals(if (orientation >= 5) 640 else 480, bitmap.height)
                    val actual = listOf(
                        bitmap.getPixel(bitmap.width / 4, bitmap.height / 4),
                        bitmap.getPixel(bitmap.width * 3 / 4, bitmap.height / 4),
                        bitmap.getPixel(bitmap.width / 4, bitmap.height * 3 / 4),
                        bitmap.getPixel(bitmap.width * 3 / 4, bitmap.height * 3 / 4)
                    )
                    actual.zip(expectedCorners[orientation - 1]).forEach { (pixel, expected) ->
                        assertTrue("Incorrect EXIF orientation $orientation", abs(Color.red(pixel) - Color.red(expected)) < 40)
                        assertTrue(abs(Color.green(pixel) - Color.green(expected)) < 40)
                        assertTrue(abs(Color.blue(pixel) - Color.blue(expected)) < 40)
                    }
                } finally {
                    bitmap.recycle()
                }
            } finally {
                result?.delete()
                context.contentResolver.delete(uri, null, null)
            }
        }
    }

    @Test
    fun nonImageType_isRejectedBeforeUpload() = runBlocking {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "rental-hp-validation-test.gif")
            put(MediaStore.Images.Media.MIME_TYPE, "image/gif")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/RentalHpUiTests")
        }
        val uri = requireNotNull(context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        try {
            try {
                PhotoUploadUtil.prepare(context, uri)
                throw AssertionError("GIF input must be rejected")
            } catch (error: UserMessageException) {
                assertEquals(context.getString(R.string.invalid_image_type), error.message)
            }
        } finally {
            context.contentResolver.delete(uri, null, null)
        }
    }
}

@RequiresApi(29)
object GalleryTestPhoto {
    fun create(
        context: Context,
        width: Int,
        height: Int,
        orientation: Int = ExifInterface.ORIENTATION_NORMAL,
        noise: Boolean = false
    ): Uri {
        val source = File.createTempFile("gallery-ui-test-", ".jpg", context.cacheDir)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        var uri: Uri? = null
        try {
            if (noise) {
                val random = Random(7)
                val pixels = IntArray(width * height) { Color.BLACK or (random.nextInt() and 0xFFFFFF) }
                bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            } else {
                val canvas = Canvas(bitmap)
                val paint = Paint()
                listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW).forEachIndexed { index, color ->
                    paint.color = color
                    val left = if (index % 2 == 0) 0f else width / 2f
                    val top = if (index < 2) 0f else height / 2f
                    canvas.drawRect(left, top, left + width / 2f, top + height / 2f, paint)
                }
            }
            source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
            ExifInterface(source).apply {
                setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
                saveAttributes()
            }
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, source.name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/RentalHpUiTests")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val inserted = requireNotNull(context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
            uri = inserted
            requireNotNull(context.contentResolver.openOutputStream(inserted)).use { output ->
                source.inputStream().use { it.copyTo(output) }
            }
            context.contentResolver.update(inserted, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null)
            return inserted
        } catch (error: Throwable) {
            uri?.let { context.contentResolver.delete(it, null, null) }
            throw error
        } finally {
            bitmap.recycle()
            source.delete()
        }
    }
}
