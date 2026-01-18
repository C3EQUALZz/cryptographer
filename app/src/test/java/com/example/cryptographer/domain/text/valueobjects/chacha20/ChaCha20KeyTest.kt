package com.example.cryptographer.domain.text.valueobjects.chacha20

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for ChaCha20Key.
 */
class ChaCha20KeyTest {

    @Test
    fun `create should succeed for CHACHA20_256`() {
        // Given
        val keyBytes = ByteArray(32) { it.toByte() }

        // When
        val result = ChaCha20Key.Companion.create(EncryptionAlgorithm.CHACHA20_256, keyBytes)

        // Then
        Assert.assertTrue(result.isSuccess)
        val key = result.getOrThrow()
        Assert.assertEquals(EncryptionAlgorithm.CHACHA20_256, key.algorithm)
        Assert.assertArrayEquals(keyBytes, key.toByteArray())
    }

    @Test
    fun `create should fail with invalid length`() {
        // Given
        val keyBytes = ByteArray(31)

        // When
        val result = ChaCha20Key.Companion.create(EncryptionAlgorithm.CHACHA20_256, keyBytes)

        // Then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is DomainFieldError)
    }

    @Test
    fun `create should fail with unsupported algorithm`() {
        // Given
        val keyBytes = ByteArray(32)

        // When
        val result = ChaCha20Key.Companion.create(EncryptionAlgorithm.AES_256, keyBytes)

        // Then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is DomainFieldError)
    }

    @Test
    fun `toByteArray should return a copy`() {
        // Given
        val keyBytes = ByteArray(32) { it.toByte() }
        val key = ChaCha20Key.Companion.create(EncryptionAlgorithm.CHACHA20_256, keyBytes).getOrThrow()

        // When
        val copy = key.toByteArray()
        copy[0] = 0x7F
        val secondCopy = key.toByteArray()

        // Then
        Assert.assertNotEquals(copy[0], secondCopy[0])
    }
}
