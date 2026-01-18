package com.example.cryptographer.domain.text.valueobjects.tdes

import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for PKCS#5 padding.
 */
class Pkcs5PaddingTest {

    @Test
    fun `pad should append correct padding bytes`() {
        // Given
        val data = ByteArray(10) { it.toByte() }

        // When
        val padded = Pkcs5Padding.pad(data)

        // Then
        Assert.assertEquals(16, padded.size)
        val padLength = padded.last().toInt() and 0xFF
        Assert.assertEquals(6, padLength)
        val padding = padded.sliceArray(padded.size - padLength until padded.size)
        Assert.assertTrue(padding.all { (it.toInt() and 0xFF) == padLength })
    }

    @Test
    fun `unpad should restore original data`() {
        // Given
        val data = "Padding test".toByteArray(Charsets.UTF_8)
        val padded = Pkcs5Padding.pad(data)

        // When
        val unpadded = Pkcs5Padding.unpad(padded)

        // Then
        Assert.assertArrayEquals(data, unpadded)
    }

    @Test
    fun `unpad should return original data when padding is invalid`() {
        // Given
        val data = ByteArray(8) { it.toByte() }
        data[7] = 0x09

        // When
        val result = Pkcs5Padding.unpad(data)

        // Then
        Assert.assertArrayEquals(data, result)
    }
}
