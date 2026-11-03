package com.contoh.rentalhp.data.model

import com.contoh.rentalhp.data.api.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AlatMultipartTest {
    private lateinit var capturedRequest: Request
    private var capturedBody = ""
    private val textType = "text/plain".toMediaType()
    private val api = Retrofit.Builder()
        .baseUrl("http://localhost/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().addInterceptor { chain ->
            capturedRequest = chain.request()
            capturedBody = Buffer().also { capturedRequest.body?.writeTo(it) }.readUtf8()
            Response.Builder()
                .request(capturedRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("""{"success":true,"message":"Alat tersimpan","data":null}""".toResponseBody("application/json".toMediaType()))
                .build()
        }.build())
        .build()
        .create(ApiService::class.java)

    @Test
    fun create_sendsExactBackendFieldsAndJpegPart() = runBlocking {
        val image = MultipartBody.Part.createFormData(
            "image", "alat.jpg", "jpeg-content".toRequestBody("image/jpeg".toMediaType())
        )
        api.createAlat(
            "3".toRequestBody(textType),
            "Model HP".toRequestBody(textType),
            "Spesifikasi".toRequestBody(textType),
            "125000.5".toRequestBody(textType),
            image
        )
        assertEquals("POST", capturedRequest.method)
        assertEquals("/api/alat", capturedRequest.url.encodedPath)
        assertTrue(capturedRequest.body?.contentType().toString().startsWith("multipart/form-data"))
        listOf("idKategori", "nama", "deskripsi", "hargaSewa").forEach {
            assertTrue(capturedBody.contains("name=\"$it\""))
        }
        assertTrue(capturedBody.contains("Content-Type: text/plain"))
        assertTrue(capturedBody.contains("name=\"image\"; filename=\"alat.jpg\""))
        assertTrue(capturedBody.contains("Content-Type: image/jpeg"))
        assertTrue(capturedBody.contains("125000.5"))
        assertFalse(capturedBody.contains("imagePath"))
    }

    @Test
    fun update_withoutNewPhotoOmitsImagePart() = runBlocking {
        api.updateAlat(
            17,
            "3".toRequestBody(textType),
            "Model HP".toRequestBody(textType),
            "".toRequestBody(textType),
            "125000".toRequestBody(textType)
        )
        assertEquals("PUT", capturedRequest.method)
        assertEquals("/api/alat/17", capturedRequest.url.encodedPath)
        assertFalse(capturedBody.contains("name=\"image\""))
        assertFalse(capturedBody.contains("imagePath"))
    }
}
