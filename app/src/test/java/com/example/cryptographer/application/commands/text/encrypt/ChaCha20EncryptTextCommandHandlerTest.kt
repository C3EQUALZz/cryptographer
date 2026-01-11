package com.example.cryptographer.application.commands.text.encrypt

import com.example.cryptographer.domain.text.services.ChaCha20EncryptionService
import com.example.cryptographer.domain.text.services.TextService
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.test.factories.KeyFactory
import com.example.cryptographer.test.stubs.StubTextIdGenerator
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import javax.crypto.Cipher

/**
 * Unit tests for ChaCha20EncryptTextCommandHandler.
 */
class ChaCha20EncryptTextCommandHandlerTest {

    private lateinit var chaCha20Service: ChaCha20EncryptionService
    private lateinit var textService: TextService
    private lateinit var handler: ChaCha20EncryptTextCommandHandler

    @Before
    fun setUp() {
        chaCha20Service = ChaCha20EncryptionService()
        textService = TextService(StubTextIdGenerator())
        handler = ChaCha20EncryptTextCommandHandler(chaCha20Service, textService)
    }

    /**
     * Checks if ChaCha20-Poly1305 is available in this Java environment.
     * ChaCha20-Poly1305 requires Java 11+ or Android API 28+.
     */
    private fun isChaCha20Available(): Boolean {
        return try {
            Cipher.getInstance("ChaCha20-Poly1305/None/NoPadding")
            true
        } catch (_: Exception) {
            false
        }
    }

    @Test
    fun `invoke should encrypt text successfully with ChaCha20_256`() {
        // Assume ChaCha20 is available (requires Java 11+ or Android API 28+)
        assumeTrue("ChaCha20-Poly1305 is not available in this environment", isChaCha20Available())

        // Given
        val key = KeyFactory.createChaCha256()
        val rawText = "Hello, ChaCha20!"
        val command = ChaCha20EncryptTextCommand(rawText, key)

        // When
        val result = handler(command)

        // Then
        assertTrue("Encryption should succeed: ${result.exceptionOrNull()?.message}", result.isSuccess)
        val encryptedTextView = result.getOrThrow()
        assertNotNull(encryptedTextView.encryptedText)
        assertNotNull(encryptedTextView.encryptedText.initializationVector)
        assertEquals(EncryptionAlgorithm.CHACHA20_256, encryptedTextView.encryptedText.algorithm)
        assertEquals(12, encryptedTextView.encryptedText.initializationVector!!.size) // 96 bits = 12 bytes
    }

    @Test
    fun `invoke should fail with invalid text`() {
        // Given
        val key = KeyFactory.createChaCha256()
        val invalidText = "" // Empty text
        val command = ChaCha20EncryptTextCommand(invalidText, key)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke should fail when encryption fails`() {
        // Given
        val mockChaCha20Service = mockk<ChaCha20EncryptionService>()
        every {
            mockChaCha20Service.encrypt(
                any(),
                any(),
            )
        } returns Result.failure(Exception("ChaCha20 encryption failed"))

        val handlerWithMock = ChaCha20EncryptTextCommandHandler(mockChaCha20Service, textService)
        val key = KeyFactory.createChaCha256()
        val command = ChaCha20EncryptTextCommand("Test text", key)

        // When
        val result = handlerWithMock(command)

        // Then
        assertTrue(result.isFailure)
    }
}
