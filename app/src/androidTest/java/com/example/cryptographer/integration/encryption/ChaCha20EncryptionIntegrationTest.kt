package com.example.cryptographer.integration.encryption

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cryptographer.domain.text.services.ChaCha20EncryptionService
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
 * Integration tests for ChaCha20 encryption service.
 *
 * These tests verify that the encryption service works correctly
 * in a real Android environment with actual device/emulator.
 *
 * Category: Integration Tests
 * Scope: End-to-end encryption/decryption flow
 */
@RunWith(AndroidJUnit4::class)
class ChaCha20EncryptionIntegrationTest {

    private lateinit var chaCha20Service: ChaCha20EncryptionService
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        chaCha20Service = ChaCha20EncryptionService()
    }

    @Test
    fun appContext_shouldHaveCorrectPackageName() {
        assertEquals("com.example.cryptographer", appContext.packageName)
    }

    @Test
    fun generateKey_shouldCreateValidChaCha20Key() {
        // When
        val result = chaCha20Service.generateKey(EncryptionAlgorithm.CHACHA20_256)

        // Then
        assertTrue("Key generation should succeed", result.isSuccess)
        val key = result.getOrThrow()
        assertEquals(EncryptionAlgorithm.CHACHA20_256, key.algorithm)
        assertEquals(32, key.value.size) // 256 bits = 32 bytes
    }

    @Test
    fun encryptAndDecrypt_shouldWorkCorrectly() {
        // Given
        val key = TestFixtures.createEncryptionKey(EncryptionAlgorithm.CHACHA20_256)
        val originalText = "Hello, ChaCha20 on Android!"
        val originalData = originalText.toByteArray(Charsets.UTF_8)

        // When
        val encryptResult = chaCha20Service.encrypt(originalData, key)
        assertTrue("Encryption should succeed: ${encryptResult.exceptionOrNull()?.message}", encryptResult.isSuccess)
        val encryptedText = encryptResult.getOrThrow()

        val decryptResult = chaCha20Service.decrypt(encryptedText, key)

        // Then
        assertTrue("Decryption should succeed: ${decryptResult.exceptionOrNull()?.message}", decryptResult.isSuccess)
        val decryptedData = decryptResult.getOrThrow()
        assertArrayEquals("Decrypted data should match original", originalData, decryptedData)

        val decryptedText = String(decryptedData, Charsets.UTF_8)
        assertEquals("Decrypted text should match original", originalText, decryptedText)
    }

    @Test
    fun encrypt_shouldGenerateDifferentNonces() {
        // Given
        val key = TestFixtures.createEncryptionKey(EncryptionAlgorithm.CHACHA20_256)
        val data = TestFixtures.createTestText("Test message")

        // When
        val result1 = chaCha20Service.encrypt(data, key)
        val result2 = chaCha20Service.encrypt(data, key)

        // Then
        assertTrue("First encryption should succeed", result1.isSuccess)
        assertTrue("Second encryption should succeed", result2.isSuccess)

        val encrypted1 = result1.getOrThrow()
        val encrypted2 = result2.getOrThrow()

        assertNotNull("Nonce 1 should not be null", encrypted1.initializationVector)
        assertNotNull("Nonce 2 should not be null", encrypted2.initializationVector)

        // Nonces should be different
        assertFalse(
            "Nonces should be different",
            encrypted1.initializationVector!!.contentEquals(encrypted2.initializationVector!!),
        )

        // Encrypted data should be different due to different nonces
        assertFalse(
            "Encrypted data should be different",
            encrypted1.encryptedData.contentEquals(encrypted2.encryptedData),
        )
    }

    @Test
    fun decrypt_shouldFailWithWrongKey() {
        // Given - Create two different keys
        val key1 = TestFixtures.createEncryptionKey(EncryptionAlgorithm.CHACHA20_256)
        val key2 = TestFixtures.createDifferentKey(EncryptionAlgorithm.CHACHA20_256, key1)
        val data = TestFixtures.createTestText("Secret message")

        // When
        val encryptResult = chaCha20Service.encrypt(data, key1)
        assertTrue("Encryption should succeed", encryptResult.isSuccess)
        val encryptedText = encryptResult.getOrThrow()

        val decryptResult = chaCha20Service.decrypt(encryptedText, key2)

        // Then
        assertTrue("Decryption should fail with wrong key", decryptResult.isFailure)
    }

    @Test
    fun decrypt_shouldFailWithTamperedData() {
        // Given
        val key = TestFixtures.createEncryptionKey(EncryptionAlgorithm.CHACHA20_256)
        val data = TestFixtures.createTestText("Test message")

        // When
        val encryptResult = chaCha20Service.encrypt(data, key)
        assertTrue("Encryption should succeed", encryptResult.isSuccess)
        val encryptedText = encryptResult.getOrThrow()

        // Tamper with encrypted data
        val tamperedData = encryptedText.encryptedData.copyOf()
        tamperedData[0] = (tamperedData[0].toInt() xor 0xFF).toByte()

        val tamperedEncryptedText = com.example.cryptographer.domain.text.entities.EncryptedText(
            id = encryptedText.id,
            encryptedData = tamperedData,
            algorithm = encryptedText.algorithm,
            initializationVector = encryptedText.initializationVector,
            createdAt = encryptedText.createdAt,
            updatedAt = encryptedText.updatedAt,
        )
        val decryptResult = chaCha20Service.decrypt(tamperedEncryptedText, key)

        // Then
        assertTrue("Decryption should fail with tampered data", decryptResult.isFailure)
    }

    @Test
    fun encryptAndDecrypt_shouldHandleEmptyData() {
        // Given
        val key = TestFixtures.createEncryptionKey(EncryptionAlgorithm.CHACHA20_256)
        val emptyData = ByteArray(0)

        // When
        val encryptResult = chaCha20Service.encrypt(emptyData, key)
        assertTrue("Encryption should succeed", encryptResult.isSuccess)
        val encryptedText = encryptResult.getOrThrow()

        val decryptResult = chaCha20Service.decrypt(encryptedText, key)

        // Then
        assertTrue("Decryption should succeed", decryptResult.isSuccess)
        val decryptedData = decryptResult.getOrThrow()
        assertArrayEquals("Decrypted empty data should match", emptyData, decryptedData)
    }

    @Test
    fun encryptAndDecrypt_shouldHandleLargeData() {
        // Given
        val key = TestFixtures.createEncryptionKey(EncryptionAlgorithm.CHACHA20_256)
        val largeData = TestFixtures.createRandomData(10000)

        // When
        val encryptResult = chaCha20Service.encrypt(largeData, key)
        assertTrue("Encryption should succeed", encryptResult.isSuccess)
        val encryptedText = encryptResult.getOrThrow()

        val decryptResult = chaCha20Service.decrypt(encryptedText, key)

        // Then
        assertTrue("Decryption should succeed", decryptResult.isSuccess)
        val decryptedData = decryptResult.getOrThrow()
        assertArrayEquals("Decrypted large data should match", largeData, decryptedData)
    }

    @Test
    fun encryptAndDecrypt_shouldHandleUnicodeText() {
        // Given
        val key = TestFixtures.createEncryptionKey(EncryptionAlgorithm.CHACHA20_256)
        val unicodeText = "Привет, ChaCha20! 🚀 你好"
        val unicodeData = unicodeText.toByteArray(Charsets.UTF_8)

        // When
        val encryptResult = chaCha20Service.encrypt(unicodeData, key)
        assertTrue("Encryption should succeed", encryptResult.isSuccess)
        val encryptedText = encryptResult.getOrThrow()

        val decryptResult = chaCha20Service.decrypt(encryptedText, key)

        // Then
        assertTrue("Decryption should succeed", decryptResult.isSuccess)
        val decryptedData = decryptResult.getOrThrow()
        assertArrayEquals("Decrypted Unicode data should match", unicodeData, decryptedData)

        val decryptedText = String(decryptedData, Charsets.UTF_8)
        assertEquals("Decrypted Unicode text should match", unicodeText, decryptedText)
    }
}
