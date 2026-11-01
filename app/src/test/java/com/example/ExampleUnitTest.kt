package com.example

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCalculatorBasicOperations() {
        // Test basic arithmetic logic
        val a = 12.0
        val b = 4.0
        assertEquals(16.0, a + b, 0.001)
        assertEquals(8.0, a - b, 0.001)
        assertEquals(48.0, a * b, 0.001)
        assertEquals(3.0, a / b, 0.001)
    }

    @Test
    fun testVaultPinDefault() {
        val defaultPin = "1234"
        assertTrue(defaultPin.length == 4)
        assertTrue(defaultPin == "1234")
    }

    @Test
    fun testUrlFormatting() {
        val input = "google.com"
        val formatted = if (input.startsWith("http://") || input.startsWith("https://")) input else "https://$input"
        assertEquals("https://google.com", formatted)
    }
}
