package com.example.cryptographer.application.commands.text.decrypt

import com.example.cryptographer.domain.text.services.AesEncryptionService
import com.example.cryptographer.test.factories.EncryptedTextFactory
import com.example.cryptographer.test.factories.KeyFactory
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for AesDecryptTextCommandHandler.
 */
class AesDecryptTextCommandHandlerTest {

    private lateinit var aesService: AesEncryptionService
    private lateinit var handler: AesDecryptTextCommandHandler

    @Before
    fun setUp() {
        aesService = AesEncryptionService()
        handler = AesDecryptTextCommandHandler(aesService)
    }

    @Test
    fun `invoke should decrypt text successfully`() {
        // Given
        val key = KeyFactory.createAes256()
        val originalText = "Hello, World!"
        val originalData = originalText.toByteArray(Charsets.UTF_8)

        // Encrypt first
        val encryptResult = aesService.encrypt(originalData, key)
        assertTrue(encryptResult.isSuccess)
        val encryptedText = encryptResult.getOrThrow()

        val command = AesDecryptTextCommand(encryptedText, key)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isSuccess)
        val decryptedTextView = result.getOrThrow()
        assertEquals(originalText, decryptedTextView.decryptedText)
    }

    @Test
    fun `invoke should fail when decryption fails`() {
        // Given
        val mockAesService = mockk<AesEncryptionService>()
        every { mockAesService.decrypt(any(), any()) } returns Result.failure(Exception("Decryption failed"))

        val handlerWithMock = AesDecryptTextCommandHandler(mockAesService)
        val key = KeyFactory.createAes256()
        val encryptedText = EncryptedTextFactory.create()
        val command = AesDecryptTextCommand(encryptedText, key)

        // When
        val result = handlerWithMock(command)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke should fail without IV`() {
        // Given
        val key = KeyFactory.createAes256()
        val encryptedText = EncryptedTextFactory.createWithoutIv()
        val command = AesDecryptTextCommand(encryptedText, key)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isFailure)
    }
}
