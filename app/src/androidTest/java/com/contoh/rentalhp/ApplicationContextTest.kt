package com.contoh.rentalhp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApplicationContextTest {
    @Test
    fun applicationId_isCorrect() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.contoh.rentalhp", context.packageName)
    }
}
