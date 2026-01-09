package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm
import com.example.cryptographer.test.factories.KeyFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AesEncryptionService.
 */
class AesEncryptionServiceTest {

    private lateinit var aesService: AesEncryptionService

    @Before
    fun setUp() {
        aesService = AesEncryptionService()
    }

    @Test
    fun `generateKey should succeed for AES_128`() {
        // When
        val result = aesService.generateKey(EncryptionAlgorithm.AES_128)

        // Then
        assertTrue(result.isSuccess)
        val key = result.getOrThrow()
        assertEquals(EncryptionAlgorithm.AES_128, key.algorithm)
        assertEquals(16, key.value.size) // 128 bits = 16 bytes
    }

    @Test
    fun `generateKey should succeed for AES_192`() {
        // When
        val result = aesService.generateKey(EncryptionAlgorithm.AES_192)

        // Then
        assertTrue(result.isSuccess)
        val key = result.getOrThrow()
        assertEquals(EncryptionAlgorithm.AES_192, key.algorithm)
        assertEquals(24, key.value.size) // 192 bits = 24 bytes
    }

    @Test
    fun `generateKey should succeed for AES_256`() {
        // When
        val result = aesService.generateKey(EncryptionAlgorithm.AES_256)

        // Then
        assertTrue(result.isSuccess)
        val key = result.getOrThrow()
        assertEquals(EncryptionAlgorithm.AES_256, key.algorithm)
        assertEquals(32, key.value.size) // 256 bits = 32 bytes
    }

    @Test
    fun `encrypt and decrypt should work correctly`() {
        // Given
        val key = KeyFactory.createAes256()
        val originalData = "Hello, World!".toByteArray(Charsets.UTF_8)

        // When
        val encryptResult = aesService.encrypt(originalData, key)
        assertTrue(encryptResult.isSuccess)
        val encryptedText = encryptResult.getOrThrow()

        val decryptResult = aesService.decrypt(encryptedText, key)

        // Then
        assertTrue(decryptResult.isSuccess)
        val decryptedData = decryptResult.getOrThrow()
        assertArrayEquals(originalData, decryptedData)
    }

    @Test
    fun `encrypt should fail with invalid key length`() {
        // Given
        val invalidKey = KeyFactory.create(
            keyBytes = ByteArray(10) // Invalid length
        )
        val data = "Test".toByteArray(Charsets.UTF_8)

        // When
        val result = aesService.encrypt(data, invalidKey)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `decrypt should fail without IV`() {
        // Given
        val key = KeyFactory.createAes256()
        val encryptedText = com.example.cryptographer.test.factories.EncryptedTextFactory.createWithoutIv(
            encryptedData = ByteArray(16),
            algorithm = EncryptionAlgorithm.AES_256
        )

        // When
        val result = aesService.decrypt(encryptedText, key)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `encrypt should generate different IV each time`() {
        // Given
        val key = KeyFactory.createAes256()
        val data = "Test data".toByteArray(Charsets.UTF_8)

        // When
        val result1 = aesService.encrypt(data, key)
        val result2 = aesService.encrypt(data, key)

        // Then
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        val encrypted1 = result1.getOrThrow()
        val encrypted2 = result2.getOrThrow()
        
        // IVs should be different
        assertNotNull(encrypted1.initializationVector)
        assertNotNull(encrypted2.initializationVector)
        assertFalse(
            encrypted1.initializationVector!!.contentEquals(encrypted2.initializationVector!!)
        )
        
        // Encrypted data should be different (due to different IVs)
        assertFalse(encrypted1.encryptedData.contentEquals(encrypted2.encryptedData))
    }
}

