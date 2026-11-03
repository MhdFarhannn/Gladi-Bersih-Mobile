package com.contoh.rentalhp.util

object PhoneValidationUtil {
    private val allowedCharacters = Regex("^[0-9+() .-]+$")

    fun isValid(value: String): Boolean {
        if (!value.matches(allowedCharacters)) return false
        val digits = value.filter(Char::isDigit)
        val normalized = when {
            digits.startsWith("62") -> "0${digits.drop(2)}"
            digits.startsWith("8") -> "0$digits"
            else -> digits
        }
        return normalized.startsWith("08") && normalized.length in 10..15
    }
}
