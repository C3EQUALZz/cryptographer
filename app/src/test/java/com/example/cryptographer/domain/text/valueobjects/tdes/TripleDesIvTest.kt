package com.example.cryptographer.domain.text.valueobjects.tdes

import com.example.cryptographer.domain.common.errors.DomainFieldError
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for TripleDesIv.
 */
class TripleDesIvTest {

    @Test
    fun `create should succeed with 8 bytes`() {
        // Given
        val ivBytes = ByteArray(8) { it.toByte() }

        // When
        val result = TripleDesIv.Companion.create(ivBytes)

        // Then
        Assert.assertTrue(result.isSuccess)
        Assert.assertArrayEquals(ivBytes, result.getOrThrow().bytes)
    }

    @Test
    fun `create should fail when iv is missing`() {
        // When
        val result = TripleDesIv.Companion.create(null)

        // Then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is DomainFieldError)
    }

    @Test
    fun `create should fail with invalid size`() {
        // Given
        val ivBytes = ByteArray(7)

        // When
        val result = TripleDesIv.Companion.create(ivBytes)

        // Then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is DomainFieldError)
    }

    @Test
    fun `toByteArray should return a copy`() {
        // Given
        val ivBytes = ByteArray(8) { it.toByte() }
        val iv = TripleDesIv.Companion.create(ivBytes).getOrThrow()

        // When
        val copy = iv.toByteArray()
        copy[0] = 0x7F

        // Then
        Assert.assertNotEquals(copy[0], iv.bytes[0])
    }
}
