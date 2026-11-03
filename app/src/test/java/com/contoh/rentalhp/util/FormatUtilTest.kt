package com.contoh.rentalhp.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatUtilTest {
    @Test
    fun rentalDays_usesAtLeastOneDay() {
        assertEquals(1L, FormatUtil.rentalDays("2026-11-03", "2026-11-03"))
        assertEquals(1L, FormatUtil.rentalDays("2026-11-03", "2026-11-04"))
    }

    @Test
    fun rentalDays_countsLongerPeriod() {
        assertEquals(4L, FormatUtil.rentalDays("2026-11-03", "2026-11-07"))
    }

    @Test
    fun formatDate_usesIndonesianDate() {
        assertEquals("03 November 2026", FormatUtil.formatDate("2026-11-03"))
    }
}
