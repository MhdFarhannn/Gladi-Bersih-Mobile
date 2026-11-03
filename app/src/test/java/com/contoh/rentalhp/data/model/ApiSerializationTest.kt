package com.contoh.rentalhp.data.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiSerializationTest {
    private val gson = Gson()

    @Test
    fun loginRequest_usesBackendFieldNames() {
        val json = gson.toJson(LoginRequest("peminjam", "rahasia"))
        assertTrue(json.contains("\"username\":\"peminjam\""))
        assertTrue(json.contains("\"password\":\"rahasia\""))
    }

    @Test
    fun rentalRequest_usesDateAndItemContract() {
        val json = gson.toJson(
            PeminjamanRequest(
                idPeminjam = null,
                tanggalPeminjaman = "2026-11-03",
                tanggalRencanaKembali = "2026-11-05",
                items = listOf(PeminjamanItemRequest(7, 2))
            )
        )
        val root = gson.fromJson(json, Map::class.java)
        assertEquals("2026-11-03", root["tanggalPeminjaman"])
        assertEquals("2026-11-05", root["tanggalRencanaKembali"])
        assertTrue(json.contains("\"idAlat\":7"))
        assertTrue(json.contains("\"jumlah\":2"))
    }
}
