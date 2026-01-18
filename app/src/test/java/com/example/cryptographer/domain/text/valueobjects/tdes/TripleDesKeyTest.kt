package com.example.cryptographer.domain.text.valueobjects.tdes

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for TripleDesKey.
 */
class TripleDesKeyTest {

    @Test
    fun `create should succeed for TDES_112`() {
        // Given
        val keyBytes = ByteArray(16) { it.toByte() }

        // When
        val result = TripleDesKey.Companion.create(EncryptionAlgorithm.TDES_112, keyBytes)

        // Then
        Assert.assertTrue(result.isSuccess)
        val key = result.getOrThrow()
        Assert.assertEquals(EncryptionAlgorithm.TDES_112, key.algorithm)
        Assert.assertTrue(key.isTwoKey)
        Assert.assertArrayEquals(keyBytes, key.toByteArray())
        Assert.assertNotNull(key.key1Raw())
        Assert.assertNotNull(key.key2Raw())
        Assert.assertTrue(key.key3Raw() == null)
    }

    @Test
    fun `create should succeed for TDES_168`() {
        // Given
        val keyBytes = ByteArray(24) { it.toByte() }

        // When
        val result = TripleDesKey.Companion.create(EncryptionAlgorithm.TDES_168, keyBytes)

        // Then
        Assert.assertTrue(result.isSuccess)
        val key = result.getOrThrow()
        Assert.assertEquals(EncryptionAlgorithm.TDES_168, key.algorithm)
        Assert.assertTrue(!key.isTwoKey)
        Assert.assertArrayEquals(keyBytes, key.toByteArray())
        Assert.assertNotNull(key.key1Raw())
        Assert.assertNotNull(key.key2Raw())
        Assert.assertNotNull(key.key3Raw())
    }

    @Test
    fun `create should fail with invalid length`() {
        // Given
        val keyBytes = ByteArray(10)

        // When
        val result = TripleDesKey.Companion.create(EncryptionAlgorithm.TDES_112, keyBytes)

        // Then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is DomainFieldError)
    }

    @Test
    fun `create should fail with unsupported algorithm`() {
        // Given
        val keyBytes = ByteArray(16)

        // When
        val result = TripleDesKey.Companion.create(EncryptionAlgorithm.AES_256, keyBytes)

        // Then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is DomainFieldError)
    }
}
