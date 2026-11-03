package com.contoh.rentalhp.data.model

data class LogAktivitasResponse(
    val id: Long,
    val idUser: Long?,
    val username: String?,
    val namaUser: String?,
    val aktivitas: String,
    val waktu: String
)
