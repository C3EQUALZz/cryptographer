package com.example.cryptographer.application.commands.text.encrypt

import com.example.cryptographer.domain.text.services.AesEncryptionService
import com.example.cryptographer.domain.text.services.TextService
import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm
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
 * Unit tests for EncryptTextCommandHandler.
 */
class EncryptTextCommandHandlerTest {

    private lateinit var aesService: AesEncryptionService
    private lateinit var textService: TextService
    private lateinit var handler: EncryptTextCommandHandler

    @Before
    fun setUp() {
        aesService = AesEncryptionService()
        textService = TextService(StubTextIdGenerator())
        handler = EncryptTextCommandHandler(aesService, textService)
    }

    @Test
    fun `invoke should encrypt text successfully`() {
        // Given
        val key = KeyFactory.createAes256()
        val rawText = "Hello, World!"
        val command = EncryptTextCommand(rawText, key)

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
    fun `invoke should fail with invalid text`() {
        // Given
        val key = KeyFactory.createAes256()
        val invalidText = "" // Empty text
        val command = EncryptTextCommand(invalidText, key)

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

        val handlerWithMock = EncryptTextCommandHandler(mockAesService, textService)
        val key = KeyFactory.createAes256()
        val command = EncryptTextCommand("Test text", key)

        // When
        val result = handlerWithMock(command)

        // Then
        assertTrue(result.isFailure)
    }
}

