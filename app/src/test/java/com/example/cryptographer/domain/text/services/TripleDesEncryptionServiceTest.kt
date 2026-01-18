package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.errors.UnsupportedAlgorithmError
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.test.factories.EncryptedTextFactory
import com.example.cryptographer.test.factories.KeyFactory
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TripleDesEncryptionService.
 */
class TripleDesEncryptionServiceTest {

    private lateinit var tripleDesService: TripleDesEncryptionService

    @Before
    fun setUp() {
        tripleDesService = TripleDesEncryptionService()
    }

    @Test
    fun `generateKey should succeed for TDES_112`() {
        // When
        val result = tripleDesService.generateKey(EncryptionAlgorithm.TDES_112)

        // Then
        assertTrue(result.isSuccess)
        val key = result.getOrThrow()
        assertEquals(EncryptionAlgorithm.TDES_112, key.algorithm)
        assertEquals(16, key.value.size)
    }

    @Test
    fun `generateKey should succeed for TDES_168`() {
        // When
        val result = tripleDesService.generateKey(EncryptionAlgorithm.TDES_168)

        // Then
        assertTrue(result.isSuccess)
        val key = result.getOrThrow()
        assertEquals(EncryptionAlgorithm.TDES_168, key.algorithm)
        assertEquals(24, key.value.size)
    }

    @Test
    fun `generateKey should fail for non-3des algorithms`() {
        // When
        val result = tripleDesService.generateKey(EncryptionAlgorithm.AES_256)

        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertNotNull("Error should not be null", error)
        assertTrue(error is UnsupportedAlgorithmError)
        assertEquals(EncryptionAlgorithm.AES_256, (error as UnsupportedAlgorithmError).algorithm)
        assertEquals("TripleDesEncryptionService", error.serviceName)
    }

    @Test
    fun `encrypt and decrypt should work correctly`() {
        // Given
        val key = KeyFactory.create(
            algorithm = EncryptionAlgorithm.TDES_112,
            keyBytes = ByteArray(16) { it.toByte() },
        )
        val originalData = "Hello, 3DES!".toByteArray(Charsets.UTF_8)

        // When
        val encryptResult = tripleDesService.encrypt(originalData, key)
        assertTrue(encryptResult.isSuccess)
        val encryptedText = encryptResult.getOrThrow()

        val decryptResult = tripleDesService.decrypt(encryptedText, key)

        // Then
        assertTrue(decryptResult.isSuccess)
        assertArrayEquals(originalData, decryptResult.getOrThrow())
    }

    @Test
    fun `encrypt should fail with invalid key length`() {
        // Given
        val invalidKey = KeyFactory.create(
            algorithm = EncryptionAlgorithm.TDES_112,
            keyBytes = ByteArray(10),
        )
        val data = "Test".toByteArray(Charsets.UTF_8)

        // When
        val result = tripleDesService.encrypt(data, invalidKey)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `decrypt should fail without IV`() {
        // Given
        val key = KeyFactory.create(
            algorithm = EncryptionAlgorithm.TDES_112,
            keyBytes = ByteArray(16),
        )
        val encryptedText = EncryptedTextFactory.createWithoutIv(
            encryptedData = ByteArray(16),
            algorithm = EncryptionAlgorithm.TDES_112,
        )

        // When
        val result = tripleDesService.decrypt(encryptedText, key)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `decrypt should fail with wrong algorithm`() {
        // Given
        val wrongKey = KeyFactory.createAes256()
        val encryptedText = EncryptedText(
            encryptedData = ByteArray(16),
            algorithm = EncryptionAlgorithm.TDES_112,
            initializationVector = ByteArray(8),
        )

        // When
        val result = tripleDesService.decrypt(encryptedText, wrongKey)

        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is UnsupportedAlgorithmError)
        assertEquals(EncryptionAlgorithm.AES_256, (error as UnsupportedAlgorithmError).algorithm)
        assertEquals("TripleDesEncryptionService", error.serviceName)
    }
}
