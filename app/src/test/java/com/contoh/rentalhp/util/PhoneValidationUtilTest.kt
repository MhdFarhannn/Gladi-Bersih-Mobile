package com.contoh.rentalhp.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneValidationUtilTest {
    @Test
    fun acceptsBackendSupportedIndonesianFormats() {
        assertTrue(PhoneValidationUtil.isValid("081234567890"))
        assertTrue(PhoneValidationUtil.isValid("81234567890"))
        assertTrue(PhoneValidationUtil.isValid("6281234567890"))
        assertTrue(PhoneValidationUtil.isValid("+62 812-3456-7890"))
    }

    @Test
    fun rejectsInvalidPhoneNumbers() {
        assertFalse(PhoneValidationUtil.isValid("021234567"))
        assertFalse(PhoneValidationUtil.isValid("08abc123456"))
        assertFalse(PhoneValidationUtil.isValid("08123"))
    }
}
