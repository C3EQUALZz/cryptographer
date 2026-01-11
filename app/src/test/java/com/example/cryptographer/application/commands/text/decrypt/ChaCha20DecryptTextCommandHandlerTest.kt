package com.example.cryptographer.application.commands.text.decrypt

import com.example.cryptographer.domain.text.services.ChaCha20EncryptionService
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.test.factories.EncryptedTextFactory
import com.example.cryptographer.test.factories.KeyFactory
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import javax.crypto.Cipher

/**
 * Unit tests for ChaCha20DecryptTextCommandHandler.
 */
class ChaCha20DecryptTextCommandHandlerTest {

    private lateinit var chaCha20Service: ChaCha20EncryptionService
    private lateinit var handler: ChaCha20DecryptTextCommandHandler

    @Before
    fun setUp() {
        chaCha20Service = ChaCha20EncryptionService()
        handler = ChaCha20DecryptTextCommandHandler(chaCha20Service)
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
    fun `invoke should decrypt text successfully`() {
        // Assume ChaCha20 is available (requires Java 11+ or Android API 28+)
        assumeTrue("ChaCha20-Poly1305 is not available in this environment", isChaCha20Available())

        // Given
        val key = KeyFactory.createChaCha20_256()
        val originalText = "Hello, ChaCha20!"
        val originalData = originalText.toByteArray(Charsets.UTF_8)

        // Encrypt first
        val encryptResult = chaCha20Service.encrypt(originalData, key)
        assertTrue("Encryption should succeed: ${encryptResult.exceptionOrNull()?.message}", encryptResult.isSuccess)
        val encryptedText = encryptResult.getOrThrow()

        val command = ChaCha20DecryptTextCommand(encryptedText, key)

        // When
        val result = handler(command)

        // Then
        assertTrue("Decryption should succeed: ${result.exceptionOrNull()?.message}", result.isSuccess)
        val decryptedTextView = result.getOrThrow()
        assertEquals(originalText, decryptedTextView.decryptedText)
    }

    @Test
    fun `invoke should fail when decryption fails`() {
        // Given
        val mockChaCha20Service = mockk<ChaCha20EncryptionService>()
        every { mockChaCha20Service.decrypt(any(), any()) } returns Result.failure(Exception("ChaCha20 decryption failed"))

        val handlerWithMock = ChaCha20DecryptTextCommandHandler(mockChaCha20Service)
        val key = KeyFactory.createChaCha20_256()
        val encryptedText = EncryptedTextFactory.create(
            algorithm = EncryptionAlgorithm.CHACHA20_256
        )
        val command = ChaCha20DecryptTextCommand(encryptedText, key)

        // When
        val result = handlerWithMock(command)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke should fail without nonce`() {
        // Given
        val key = KeyFactory.createChaCha20_256()
        val encryptedText = EncryptedTextFactory.createWithoutIv(
            algorithm = EncryptionAlgorithm.CHACHA20_256
        )
        val command = ChaCha20DecryptTextCommand(encryptedText, key)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isFailure)
    }
}

