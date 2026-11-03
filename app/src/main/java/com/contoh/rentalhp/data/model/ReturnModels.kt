package com.contoh.rentalhp.data.model

import java.math.BigDecimal

data class PengembalianRequest(
    val idPeminjaman: Long,
    val tanggalKembali: String,
    val catatan: String?,
    val items: List<KondisiUnitRequest>
)

data class KondisiUnitRequest(
    val idUnitAlat: Long,
    val statusKondisi: String,
    val catatan: String?
)

data class VerifikasiPengembalianRequest(
    val tanggalKembali: String?,
    val catatan: String?,
    val items: List<KondisiUnitRequest>
)

data class PengembalianResponse(
    val id: Long,
    val idPeminjaman: Long,
    val idPeminjam: Long,
    val usernamePeminjam: String,
    val namaPeminjam: String,
    val idPetugas: Long?,
    val namaPetugas: String?,
    val statusPeminjaman: String,
    val tanggalPeminjaman: String,
    val tanggalRencanaKembali: String,
    val tanggalKembali: String,
    val keterlambatanPengembalian: Int,
    val denda: BigDecimal,
    val catatan: String?
)

data class PengembalianDetailResponse(
    val id: Long,
    val idPeminjaman: Long,
    val idPeminjam: Long,
    val usernamePeminjam: String,
    val namaPeminjam: String,
    val idPetugas: Long?,
    val namaPetugas: String?,
    val statusPeminjaman: String,
    val tanggalPeminjaman: String,
    val tanggalRencanaKembali: String,
    val tanggalKembali: String,
    val keterlambatanPengembalian: Int,
    val denda: BigDecimal,
    val catatan: String?,
    val items: List<DetailPengembalianResponse>
)

data class DetailPengembalianResponse(
    val id: Long,
    val idUnitAlat: Long,
    val kodeInventaris: String,
    val namaAlat: String,
    val statusKondisi: String,
    val dendaKondisi: BigDecimal,
    val catatan: String?
)
