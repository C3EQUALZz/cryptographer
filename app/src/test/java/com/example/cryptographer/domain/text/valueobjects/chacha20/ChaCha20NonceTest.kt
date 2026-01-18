package com.example.cryptographer.domain.text.valueobjects.chacha20

import com.example.cryptographer.domain.common.errors.DomainFieldError
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for ChaCha20Nonce.
 */
class ChaCha20NonceTest {

    @Test
    fun `create should succeed with 12 bytes`() {
        // Given
        val nonceBytes = ByteArray(12) { it.toByte() }

        // When
        val result = ChaCha20Nonce.Companion.create(nonceBytes)

        // Then
        Assert.assertTrue(result.isSuccess)
        Assert.assertArrayEquals(nonceBytes, result.getOrThrow().bytes)
    }

    @Test
    fun `create should fail when nonce is missing`() {
        // When
        val result = ChaCha20Nonce.Companion.create(null)

        // Then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is DomainFieldError)
    }

    @Test
    fun `create should fail with invalid size`() {
        // Given
        val nonceBytes = ByteArray(11)

        // When
        val result = ChaCha20Nonce.Companion.create(nonceBytes)

        // Then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is DomainFieldError)
    }

    @Test
    fun `toByteArray should return a copy`() {
        // Given
        val nonceBytes = ByteArray(12) { it.toByte() }
        val nonce = ChaCha20Nonce.Companion.create(nonceBytes).getOrThrow()

        // When
        val copy = nonce.toByteArray()
        copy[0] = 0x7F
        val secondCopy = nonce.toByteArray()

        // Then
        Assert.assertNotEquals(copy[0], secondCopy[0])
    }
}
