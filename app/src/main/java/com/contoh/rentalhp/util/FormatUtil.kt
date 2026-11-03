package com.contoh.rentalhp.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object FormatUtil {
    private val localeIndonesia = Locale.forLanguageTag("id-ID")
    private val apiDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val displayDateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", localeIndonesia)
    private val displayDateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH.mm", localeIndonesia)

    fun formatRupiah(value: BigDecimal?): String {
        val formatter = NumberFormat.getCurrencyInstance(localeIndonesia).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
        return formatter.format(value ?: BigDecimal.ZERO).replace("Rp", "Rp ")
    }

    fun formatDate(value: String?): String {
        if (value.isNullOrBlank()) return "-"
        return runCatching {
            LocalDate.parse(value, apiDateFormatter).format(displayDateFormatter)
        }.getOrDefault(value)
    }

    fun formatDateTime(value: String?): String {
        if (value.isNullOrBlank()) return "-"
        val localDateTime = runCatching {
            OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
        }.recoverCatching {
            Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }.recoverCatching {
            LocalDateTime.parse(value)
        }.getOrNull()
        return localDateTime?.format(displayDateTimeFormatter) ?: value
    }

    fun toApiDate(utcMillis: Long): String {
        return Instant.ofEpochMilli(utcMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .format(apiDateFormatter)
    }

    fun todayApiDate(): String = LocalDate.now().format(apiDateFormatter)

    fun rentalDays(start: String, end: String): Long {
        return runCatching {
            maxOf(1, ChronoUnit.DAYS.between(LocalDate.parse(start), LocalDate.parse(end)))
        }.getOrDefault(1)
    }
}
