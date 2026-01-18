package com.example.cryptographer.domain.text.valueobjects.chacha20

import com.example.cryptographer.domain.common.errors.DomainFieldError
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for Poly1305Tag.
 */
class Poly1305TagTest {

    @Test
    fun `create should succeed with 16 bytes`() {
        // Given
        val tagBytes = ByteArray(16) { it.toByte() }

        // When
        val result = Poly1305Tag.Companion.create(tagBytes)

        // Then
        Assert.assertTrue(result.isSuccess)
        Assert.assertArrayEquals(tagBytes, result.getOrThrow().bytes)
    }

    @Test
    fun `create should fail with invalid length`() {
        // Given
        val tagBytes = ByteArray(15)

        // When
        val result = Poly1305Tag.Companion.create(tagBytes)

        // Then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is DomainFieldError)
    }

    @Test
    fun `toByteArray should return a copy`() {
        // Given
        val tagBytes = ByteArray(16) { it.toByte() }
        val tag = Poly1305Tag.Companion.create(tagBytes).getOrThrow()

        // When
        val copy = tag.toByteArray()
        copy[0] = 0x7F
        val secondCopy = tag.toByteArray()

        // Then
        Assert.assertNotEquals(copy[0], secondCopy[0])
    }
}
