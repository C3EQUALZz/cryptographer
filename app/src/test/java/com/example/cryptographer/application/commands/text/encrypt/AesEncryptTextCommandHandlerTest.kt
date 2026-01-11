package com.example.cryptographer.application.commands.text.encrypt

import com.example.cryptographer.domain.text.services.AesEncryptionService
import com.example.cryptographer.domain.text.services.TextService
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.test.factories.KeyFactory
import com.example.cryptographer.test.stubs.StubTextIdGenerator
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AesEncryptTextCommandHandler.
 */
class AesEncryptTextCommandHandlerTest {

    private lateinit var aesService: AesEncryptionService
    private lateinit var textService: TextService
    private lateinit var handler: AesEncryptTextCommandHandler

    @Before
    fun setUp() {
        aesService = AesEncryptionService()
        textService = TextService(StubTextIdGenerator())
        handler = AesEncryptTextCommandHandler(aesService, textService)
    }

    @Test
    fun `invoke should encrypt text successfully with AES_256`() {
        // Given
        val key = KeyFactory.createAes256()
        val rawText = "Hello, World!"
        val command = AesEncryptTextCommand(rawText, key)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isSuccess)
        val encryptedTextView = result.getOrThrow()
        assertNotNull(encryptedTextView.encryptedText)
        assertNotNull(encryptedTextView.encryptedText.initializationVector)
        assertEquals(EncryptionAlgorithm.AES_256, encryptedTextView.encryptedText.algorithm)
    }

    @Test
    fun `invoke should encrypt text successfully with AES_128`() {
        // Given
        val key = KeyFactory.createAes128()
        val rawText = "Hello, AES-128!"
        val command = AesEncryptTextCommand(rawText, key)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isSuccess)
        val encryptedTextView = result.getOrThrow()
        assertNotNull(encryptedTextView.encryptedText)
        assertEquals(EncryptionAlgorithm.AES_128, encryptedTextView.encryptedText.algorithm)
    }

    @Test
    fun `invoke should fail with invalid text`() {
        // Given
        val key = KeyFactory.createAes256()
        val invalidText = "" // Empty text
        val command = AesEncryptTextCommand(invalidText, key)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke should fail when encryption fails`() {
        // Given
        val mockAesService = mockk<AesEncryptionService>()
        every { mockAesService.encrypt(any(), any()) } returns Result.failure(Exception("Encryption failed"))

        val handlerWithMock = AesEncryptTextCommandHandler(mockAesService, textService)
        val key = KeyFactory.createAes256()
        val command = AesEncryptTextCommand("Test text", key)

        // When
        val result = handlerWithMock(command)

        // Then
        assertTrue(result.isFailure)
    }
}
