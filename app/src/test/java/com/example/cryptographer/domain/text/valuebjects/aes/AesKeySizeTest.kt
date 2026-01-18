package com.example.cryptographer.domain.text.valuebjects.aes

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.domain.text.valueobjects.aes.AesKeySize
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for AesKeySize.
 */
class AesKeySizeTest {

    @Test
    fun `create should succeed for AES_128`() {
        // When
        val result = AesKeySize.create(EncryptionAlgorithm.AES_128)

        // Then
        Assert.assertTrue(result.isSuccess)
        val keySize = result.getOrThrow()
        Assert.assertEquals(16, keySize.sizeBytes)
        Assert.assertEquals(10, keySize.numRounds)
    }

    @Test
    fun `create should succeed for AES_192`() {
        // When
        val result = AesKeySize.create(EncryptionAlgorithm.AES_192)

        // Then
        Assert.assertTrue(result.isSuccess)
        val keySize = result.getOrThrow()
        Assert.assertEquals(24, keySize.sizeBytes)
        Assert.assertEquals(12, keySize.numRounds)
    }

    @Test
    fun `create should succeed for AES_256`() {
        // When
        val result = AesKeySize.create(EncryptionAlgorithm.AES_256)

        // Then
        Assert.assertTrue(result.isSuccess)
        val keySize = result.getOrThrow()
        Assert.assertEquals(32, keySize.sizeBytes)
        Assert.assertEquals(14, keySize.numRounds)
    }

    @Test
    fun `create should fail for non-AES algorithms`() {
        // When
        val result = AesKeySize.create(EncryptionAlgorithm.CHACHA20_256)

        // Then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is DomainFieldError)
    }

    @Test
    fun `validateKeyBytes should return success for matching size`() {
        // Given
        val keySize = AesKeySize.create(EncryptionAlgorithm.AES_128).getOrThrow()

        // When
        val result = keySize.validateKeyBytes(ByteArray(16))

        // Then
        Assert.assertTrue(result.isSuccess)
    }

    @Test
    fun `validateKeyBytes should return failure for mismatched size`() {
        // Given
        val keySize = AesKeySize.create(EncryptionAlgorithm.AES_128).getOrThrow()

        // When
        val result = keySize.validateKeyBytes(ByteArray(15))

        // Then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is DomainFieldError)
    }
}
