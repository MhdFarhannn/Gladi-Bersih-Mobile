package com.contoh.rentalhp.data.model

import java.math.BigDecimal

data class KategoriRequest(val nama: String)

data class KategoriResponse(
    val id: Long,
    val nama: String
)

data class AlatResponse(
    val id: Long,
    val idKategori: Long,
    val kategori: String,
    val nama: String,
    val deskripsi: String?,
    val hargaSewa: BigDecimal,
    val imagePath: String?,
    val stokTersedia: Int,
    val createdAt: String
)

data class UnitAlatCreateRequest(
    val idAlat: Long,
    val kodeInventaris: String,
    val statusKetersediaan: String,
    val statusBarang: String
)

data class UnitAlatUpdateRequest(
    val idAlat: Long,
    val kodeInventaris: String,
    val statusKetersediaan: String,
    val statusBarang: String
)

data class UnitAlatResponse(
    val id: Long,
    val idAlat: Long,
    val alat: String,
    val kodeInventaris: String,
    val statusKetersediaan: String,
    val statusBarang: String,
    val createdAt: String
)
