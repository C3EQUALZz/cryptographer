package com.example.cryptographer.domain.text.services

import com.example.cryptographer.domain.common.errors.UnsupportedAlgorithmError
import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm
import com.example.cryptographer.test.factories.KeyFactory
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import javax.crypto.Cipher

/**
 * Unit tests for ChaCha20EncryptionService.
 */
class ChaCha20EncryptionServiceTest {

    private lateinit var chaCha20Service: ChaCha20EncryptionService

    @Before
    fun setUp() {
        chaCha20Service = ChaCha20EncryptionService()
    }

    /**
     * Checks if ChaCha20-Poly1305 is available in this Java environment.
     * ChaCha20-Poly1305 requires Java 11+ or Android API 28+.
     */
    private fun isChaCha20Available(): Boolean {
        return try {
            Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding")
            true
        } catch (e: Exception) {
            false
        }
    }

    @Test
    fun `generateKey should succeed for CHACHA20_256`() {
        // When
        val result = chaCha20Service.generateKey(EncryptionAlgorithm.CHACHA20_256)

        // Then
        assertTrue(result.isSuccess)
        val key = result.getOrThrow()
        assertEquals(EncryptionAlgorithm.CHACHA20_256, key.algorithm)
        assertEquals(32, key.value.size) // 256 bits = 32 bytes
    }

    @Test
    fun `generateKey should fail for non-ChaCha20 algorithms`() {
        // When
        val result = chaCha20Service.generateKey(EncryptionAlgorithm.AES_256)

        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertNotNull("Error should not be null", error)
        assertTrue("Error should be UnsupportedAlgorithmError, but was ${error?.javaClass?.simpleName}", error is UnsupportedAlgorithmError)
        assertEquals(EncryptionAlgorithm.AES_256, (error as UnsupportedAlgorithmError).algorithm)
        assertEquals("ChaCha20EncryptionService", (error as UnsupportedAlgorithmError).serviceName)
    }

    @Test
    fun `generateKey should fail for all AES algorithms`() {
        // Given
        val aesAlgorithms = listOf(
            EncryptionAlgorithm.AES_128,
            EncryptionAlgorithm.AES_192,
            EncryptionAlgorithm.AES_256
        )

        // When & Then
        aesAlgorithms.forEach { algorithm ->
            val result = chaCha20Service.generateKey(algorithm)
            assertTrue("Should fail for $algorithm", result.isFailure)
            val error = result.exceptionOrNull()
            assertNotNull("Error should not be null for $algorithm", error)
            assertTrue("Error should be UnsupportedAlgorithmError for $algorithm, but was ${error?.javaClass?.simpleName}", error is UnsupportedAlgorithmError)
            assertEquals(algorithm, (error as UnsupportedAlgorithmError).algorithm)
            assertEquals("ChaCha20EncryptionService", (error as UnsupportedAlgorithmError).serviceName)
        }
    }

    @Test
    fun `encrypt and decrypt should work correctly`() {
        // Assume ChaCha20 is available (requires Java 11+ or Android API 28+)
        assumeTrue("ChaCha20-Poly1305 is not available in this environment", isChaCha20Available())

        // Given
        val key = KeyFactory.createChaCha20_256()
        val originalData = "Hello, ChaCha20!".toByteArray(Charsets.UTF_8)

        // When
        val encryptResult = chaCha20Service.encrypt(originalData, key)
        assertTrue("Encryption should succeed: ${encryptResult.exceptionOrNull()?.message}", encryptResult.isSuccess)
        val encryptedText = encryptResult.getOrThrow()

        val decryptResult = chaCha20Service.decrypt(encryptedText, key)

        // Then
        assertTrue("Decryption should succeed: ${decryptResult.exceptionOrNull()?.message}", decryptResult.isSuccess)
        val decryptedData = decryptResult.getOrThrow()
        assertArrayEquals(originalData, decryptedData)
    }

    @Test
    fun `encrypt should fail with invalid key length`() {
        // Given
        val invalidKey = KeyFactory.create(
            algorithm = EncryptionAlgorithm.CHACHA20_256,
            keyBytes = ByteArray(16) // Invalid length (should be 32 bytes)
        )
        val data = "Test".toByteArray(Charsets.UTF_8)

        // When
        val result = chaCha20Service.encrypt(data, invalidKey)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `encrypt should fail with wrong algorithm`() {
        // Given
        val wrongKey = KeyFactory.createAes256() // AES key, not ChaCha20
        val data = "Test".toByteArray(Charsets.UTF_8)

        // When
        val result = chaCha20Service.encrypt(data, wrongKey)

        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is UnsupportedAlgorithmError)
        assertEquals(EncryptionAlgorithm.AES_256, (error as UnsupportedAlgorithmError).algorithm)
        assertEquals("ChaCha20EncryptionService", (error as UnsupportedAlgorithmError).serviceName)
    }

    @Test
    fun `encrypt should fail for all AES algorithms`() {
        // Given
        val aesKeys = listOf(
            KeyFactory.createAes128(),
            KeyFactory.createAes192(),
            KeyFactory.createAes256()
        )
        val data = "Test".toByteArray(Charsets.UTF_8)

        // When & Then
        aesKeys.forEach { key ->
            val result = chaCha20Service.encrypt(data, key)
            assertTrue("Should fail for ${key.algorithm}", result.isFailure)
            val error = result.exceptionOrNull()
            assertTrue("Error should be UnsupportedAlgorithmError for ${key.algorithm}", error is UnsupportedAlgorithmError)
            assertEquals(key.algorithm, (error as UnsupportedAlgorithmError).algorithm)
            assertEquals("ChaCha20EncryptionService", (error as UnsupportedAlgorithmError).serviceName)
        }
    }

    @Test
    fun `decrypt should fail with wrong algorithm`() {
        // Given
        val wrongKey = KeyFactory.createAes256() // AES key, not ChaCha20
        val encryptedText = com.example.cryptographer.test.factories.EncryptedTextFactory.create(
            algorithm = EncryptionAlgorithm.AES_256
        )

        // When
        val result = chaCha20Service.decrypt(encryptedText, wrongKey)

        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is UnsupportedAlgorithmError)
        assertEquals(EncryptionAlgorithm.AES_256, (error as UnsupportedAlgorithmError).algorithm)
        assertEquals("ChaCha20EncryptionService", (error as UnsupportedAlgorithmError).serviceName)
    }

    @Test
    fun `decrypt should fail without nonce`() {
        // Given
        val key = KeyFactory.createChaCha20_256()
        val encryptedText = com.example.cryptographer.test.factories.EncryptedTextFactory.createWithoutIv(
            encryptedData = ByteArray(16),
            algorithm = EncryptionAlgorithm.CHACHA20_256
        )

        // When
        val result = chaCha20Service.decrypt(encryptedText, key)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `encrypt should generate different nonce each time`() {
        // Assume ChaCha20 is available (requires Java 11+ or Android API 28+)
        assumeTrue("ChaCha20-Poly1305 is not available in this environment", isChaCha20Available())

        // Given
        val key = KeyFactory.createChaCha20_256()
        val data = "Test data".toByteArray(Charsets.UTF_8)

        // When
        val result1 = chaCha20Service.encrypt(data, key)
        val result2 = chaCha20Service.encrypt(data, key)

        // Then
        assertTrue("First encryption should succeed: ${result1.exceptionOrNull()?.message}", result1.isSuccess)
        assertTrue("Second encryption should succeed: ${result2.exceptionOrNull()?.message}", result2.isSuccess)
        val encrypted1 = result1.getOrThrow()
        val encrypted2 = result2.getOrThrow()
        
        // Nonces should be different
        assertNotNull(encrypted1.initializationVector)
        assertNotNull(encrypted2.initializationVector)
        assertFalse(
            encrypted1.initializationVector!!.contentEquals(encrypted2.initializationVector!!)
        )
        
        // Encrypted data should be different (due to different nonces)
        assertFalse(encrypted1.encryptedData.contentEquals(encrypted2.encryptedData))
    }
}

