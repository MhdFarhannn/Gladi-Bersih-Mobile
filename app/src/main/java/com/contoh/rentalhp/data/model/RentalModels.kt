package com.contoh.rentalhp.data.model

import java.math.BigDecimal

data class PeminjamanRequest(
    val idPeminjam: Long?,
    val tanggalPeminjaman: String,
    val tanggalRencanaKembali: String,
    val items: List<PeminjamanItemRequest>
)

data class PeminjamanItemRequest(
    val idAlat: Long,
    val jumlah: Int
)

data class PenolakanPeminjamanRequest(val alasan: String?)

data class PeminjamanResponse(
    val id: Long,
    val idPeminjam: Long,
    val usernamePeminjam: String,
    val namaPeminjam: String,
    val idPetugas: Long?,
    val namaPetugas: String?,
    val status: String,
    val tanggalPeminjaman: String,
    val tanggalRencanaKembali: String,
    val totalBiaya: BigDecimal,
    val createdAt: String
)

data class PeminjamanDetailResponse(
    val id: Long,
    val idPeminjam: Long,
    val usernamePeminjam: String,
    val namaPeminjam: String,
    val idPetugas: Long?,
    val namaPetugas: String?,
    val status: String,
    val tanggalPeminjaman: String,
    val tanggalRencanaKembali: String,
    val totalBiaya: BigDecimal,
    val createdAt: String,
    val items: List<DetailPeminjamanResponse>
)

data class DetailPeminjamanResponse(
    val id: Long,
    val idAlat: Long,
    val namaAlat: String,
    val jumlah: Int,
    val hargaSewaSaatTransaksi: BigDecimal,
    val unit: List<UnitPeminjamanResponse>
)

data class UnitPeminjamanResponse(
    val idUnitAlat: Long,
    val kodeInventaris: String
)
