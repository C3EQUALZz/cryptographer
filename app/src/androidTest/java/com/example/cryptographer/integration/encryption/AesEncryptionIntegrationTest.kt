package com.example.cryptographer.integration.encryption

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cryptographer.domain.text.services.AesEncryptionService
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.fixtures.TestFixtures
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for AES encryption service.
 *
 * These tests verify that the AES encryption service works correctly
 * in a real Android environment with actual device/emulator.
 *
 * Category: Integration Tests
 * Scope: End-to-end encryption/decryption flow
 */
@RunWith(AndroidJUnit4::class)
class AesEncryptionIntegrationTest {

    private lateinit var aesService: AesEncryptionService

    @Before
    fun setUp() {
        aesService = AesEncryptionService()
    }

    @Test
    fun generateKey_shouldCreateValidAes256Key() {
        // When
        val result = aesService.generateKey(EncryptionAlgorithm.AES_256)

        // Then
        assertTrue("Key generation should succeed", result.isSuccess)
        val key = result.getOrThrow()
        assertEquals(EncryptionAlgorithm.AES_256, key.algorithm)
        assertEquals(32, key.value.size) // 256 bits = 32 bytes
    }

    @Test
    fun encryptAndDecrypt_shouldWorkCorrectly() {
        // Given
        val key = TestFixtures.createEncryptionKey(EncryptionAlgorithm.AES_256)
        val originalText = "Hello, AES on Android!"
        val originalData = originalText.toByteArray(Charsets.UTF_8)

        // When
        val encryptResult = aesService.encrypt(originalData, key)
        assertTrue("Encryption should succeed", encryptResult.isSuccess)
        val encryptedText = encryptResult.getOrThrow()

        val decryptResult = aesService.decrypt(encryptedText, key)

        // Then
        assertTrue("Decryption should succeed", decryptResult.isSuccess)
        val decryptedData = decryptResult.getOrThrow()
        assertArrayEquals("Decrypted data should match original", originalData, decryptedData)

        val decryptedText = String(decryptedData, Charsets.UTF_8)
        assertEquals("Decrypted text should match original", originalText, decryptedText)
    }

    @Test
    fun encrypt_shouldGenerateDifferentIVs() {
        // Given
        val key = TestFixtures.createEncryptionKey(EncryptionAlgorithm.AES_256)
        val data = TestFixtures.createTestText("Test message")

        // When
        val result1 = aesService.encrypt(data, key)
        val result2 = aesService.encrypt(data, key)

        // Then
        assertTrue("First encryption should succeed", result1.isSuccess)
        assertTrue("Second encryption should succeed", result2.isSuccess)

        val encrypted1 = result1.getOrThrow()
        val encrypted2 = result2.getOrThrow()

        assertNotNull("IV 1 should not be null", encrypted1.initializationVector)
        assertNotNull("IV 2 should not be null", encrypted2.initializationVector)

        // IVs should be different
        assertFalse(
            "IVs should be different",
            encrypted1.initializationVector!!.contentEquals(encrypted2.initializationVector!!),
        )

        // Encrypted data should be different due to different IVs
        assertFalse(
            "Encrypted data should be different",
            encrypted1.encryptedData.contentEquals(encrypted2.encryptedData),
        )
    }
}
