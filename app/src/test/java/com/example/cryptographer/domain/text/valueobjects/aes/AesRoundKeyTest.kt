package com.example.cryptographer.domain.text.valueobjects.aes

import com.example.cryptographer.domain.common.errors.DomainFieldError
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for AesRoundKey.
 */
class AesRoundKeyTest {

    @Test
    fun `create should succeed with 16 bytes`() {
        // Given
        val bytes = ByteArray(16) { it.toByte() }

        // When
        val result = AesRoundKey.create(bytes)

        // Then
        Assert.assertTrue(result.isSuccess)
        Assert.assertArrayEquals(bytes, result.getOrThrow().bytes)
    }

    @Test
    fun `create should fail with invalid size`() {
        // Given
        val bytes = ByteArray(17)

        // When
        val result = AesRoundKey.create(bytes)

        // Then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is DomainFieldError)
    }

    @Test
    fun `toByteArray should return a copy`() {
        // Given
        val bytes = ByteArray(16) { it.toByte() }
        val roundKey = AesRoundKey.create(bytes).getOrThrow()

        // When
        val copy = roundKey.toByteArray()
        copy[0] = 0x7F

        // Then
        Assert.assertNotEquals(copy[0], roundKey.bytes[0])
    }
}
