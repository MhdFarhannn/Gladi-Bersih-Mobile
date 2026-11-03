package com.contoh.rentalhp.data.model

import java.math.BigDecimal

data class LaporanPeminjamanResponse(
    val dari: String,
    val sampai: String,
    val dibuatPada: String,
    val ringkasan: RingkasanPeminjaman,
    val items: List<ItemLaporanPeminjaman>
)

data class RingkasanPeminjaman(
    val jumlahPeminjaman: Int,
    val jumlahUnit: Int,
    val totalBiaya: BigDecimal
)

data class ItemLaporanPeminjaman(
    val idPeminjaman: Long,
    val tanggalPeminjaman: String,
    val tanggalRencanaKembali: String,
    val usernamePeminjam: String,
    val namaPeminjam: String,
    val namaPetugas: String?,
    val statusPeminjaman: String,
    val namaKategori: String,
    val namaAlat: String,
    val jumlah: Int,
    val hargaSewaSaatTransaksi: BigDecimal,
    val subtotalPerHari: BigDecimal,
    val totalBiaya: BigDecimal,
    val kodeUnit: String?,
    val tanggalKembali: String?,
    val keterlambatanPengembalian: Int?,
    val denda: BigDecimal?
)

data class LaporanPengembalianResponse(
    val dari: String,
    val sampai: String,
    val dibuatPada: String,
    val ringkasan: RingkasanPengembalian,
    val items: List<ItemLaporanPengembalian>
)

data class RingkasanPengembalian(
    val jumlahPengembalian: Int,
    val jumlahUnit: Int,
    val totalDenda: BigDecimal
)

data class ItemLaporanPengembalian(
    val idPengembalian: Long,
    val idPeminjaman: Long,
    val usernamePeminjam: String,
    val namaPeminjam: String,
    val namaPetugas: String?,
    val tanggalRencanaKembali: String,
    val tanggalKembali: String,
    val keterlambatanPengembalian: Int,
    val dendaTotal: BigDecimal,
    val namaAlat: String,
    val kodeInventaris: String,
    val statusKondisi: String,
    val dendaKondisi: BigDecimal,
    val catatanUnit: String?
)
