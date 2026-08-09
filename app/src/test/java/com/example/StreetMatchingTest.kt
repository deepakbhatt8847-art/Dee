package com.example

import com.example.data.StreetEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class StreetMatchingTest {

    @Test
    fun testStreetNormalizationAndMatch() {
        val sampleStreet = StreetEntity("HIGH STREET", "ROUND 01")
        val scannedLabel = "123 High Street, Richmond VIC 3121 Australia"

        val isMatch = scannedLabel.uppercase().contains(sampleStreet.streetName)
        assertEquals(true, isMatch)
    }

    @Test
    fun testRoundParsingFormat() {
        val line = "STATION RD - ROUND 05"
        val parts = line.split("-")
        assertEquals(2, parts.size)
        assertEquals("STATION RD", parts[0].trim())
        assertEquals("ROUND 05", parts[1].trim())
    }
}
