package com.example.cryptographer.domain.text.valueobjects.aes

import com.example.cryptographer.domain.common.errors.DomainFieldError
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for AesNumRounds.
 */
class AesNumRoundsTest {

    @Test
    fun `create should succeed for AES_128`() {
        // When
        val result = AesNumRounds.create(EncryptionAlgorithm.AES_128)

        // Then
        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(10, result.getOrThrow().rounds)
    }

    @Test
    fun `create should succeed for AES_192`() {
        // When
        val result = AesNumRounds.create(EncryptionAlgorithm.AES_192)

        // Then
        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(12, result.getOrThrow().rounds)
    }

    @Test
    fun `create should succeed for AES_256`() {
        // When
        val result = AesNumRounds.create(EncryptionAlgorithm.AES_256)

        // Then
        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(14, result.getOrThrow().rounds)
    }

    @Test
    fun `create should fail for non-AES algorithms`() {
        // When
        val result = AesNumRounds.create(EncryptionAlgorithm.TDES_168)

        // Then
        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is DomainFieldError)
    }
}
