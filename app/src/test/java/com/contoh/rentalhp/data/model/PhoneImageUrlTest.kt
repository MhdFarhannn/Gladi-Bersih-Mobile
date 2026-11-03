package com.contoh.rentalhp.data.model

import com.contoh.rentalhp.BuildConfig
import com.contoh.rentalhp.ui.common.PhoneImageLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneImageUrlTest {
    @Test
    fun backendRelativePath_resolvesAgainstServerRoot() {
        val expected = "${BuildConfig.BASE_URL.trimEnd('/')}/uploads/alat/phone.jpg"
        assertEquals(expected, PhoneImageLoader.url("uploads/alat/phone.jpg"))
        assertEquals(expected, PhoneImageLoader.url("/uploads/alat/phone.jpg"))
        assertEquals(expected, PhoneImageLoader.url("//uploads/alat/phone.jpg"))
    }

    @Test
    fun missingPhotoAndExistingAbsoluteUrls_areHandled() {
        assertNull(PhoneImageLoader.url(null))
        assertNull(PhoneImageLoader.url(" "))
        assertEquals("https://example.com/phone.jpg", PhoneImageLoader.url("https://example.com/phone.jpg"))
    }
}
