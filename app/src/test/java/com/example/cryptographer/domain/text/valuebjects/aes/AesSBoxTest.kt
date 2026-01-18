package com.example.cryptographer.domain.text.valuebjects.aes

import com.example.cryptographer.domain.text.valueobjects.aes.AesSBox
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for AesSBox.
 */
class AesSBoxTest {

    @Test
    fun `getSBox should return expected substitution values`() {
        // Given/When
        val value00 = AesSBox.getSBox(0x00)
        val value53 = AesSBox.getSBox(0x53)
        val valueFF = AesSBox.getSBox(0xFF)

        // Then
        Assert.assertEquals(0x63.toByte(), value00)
        Assert.assertEquals(0xED.toByte(), value53)
        Assert.assertEquals(0x16.toByte(), valueFF)
    }

    @Test
    fun `getSBox should throw for out of range values`() {
        // When/Then
        val negativeError = try {
            AesSBox.getSBox(-1)
            null
        } catch (e: IllegalArgumentException) {
            e
        }
        val tooLargeError = try {
            AesSBox.getSBox(256)
            null
        } catch (e: IllegalArgumentException) {
            e
        }

        Assert.assertNotNull(negativeError)
        Assert.assertNotNull(tooLargeError)
    }
}
