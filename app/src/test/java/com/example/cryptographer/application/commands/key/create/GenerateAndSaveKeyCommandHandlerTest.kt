package com.example.cryptographer.application.commands.key.create

import com.example.cryptographer.domain.text.services.AesEncryptionService
import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm
import com.example.cryptographer.test.stubs.StubKeyCommandGateway
import com.example.cryptographer.test.stubs.StubKeyQueryGateway
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GenerateAndSaveKeyCommandHandler.
 */
class GenerateAndSaveKeyCommandHandlerTest {

    private lateinit var aesService: AesEncryptionService
    private lateinit var queryGateway: StubKeyQueryGateway
    private lateinit var commandGateway: StubKeyCommandGateway
    private lateinit var handler: GenerateAndSaveKeyCommandHandler

    @Before
    fun setUp() {
        aesService = AesEncryptionService()
        queryGateway = StubKeyQueryGateway()
        commandGateway = StubKeyCommandGateway(queryGateway)
        handler = GenerateAndSaveKeyCommandHandler(aesService, commandGateway)
    }

    @Test
    fun `invoke should generate and save key successfully`() {
        // Given
        val command = GenerateAndSaveKeyCommand(EncryptionAlgorithm.AES_256)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isSuccess)
        val keyIdView = result.getOrThrow()
        assertNotNull(keyIdView.keyId)

        // Verify key was saved
        val savedKey = queryGateway.getKey(keyIdView.keyId)
        assertNotNull(savedKey)
        assertEquals(EncryptionAlgorithm.AES_256, savedKey?.algorithm)
    }

    @Test
    fun `invoke should fail when key generation fails`() {
        // Given
        val mockAesService = mockk<AesEncryptionService>()
        every { mockAesService.generateKey(any()) } returns Result.failure(Exception("Generation failed"))

        val handlerWithMock = GenerateAndSaveKeyCommandHandler(mockAesService, commandGateway)
        val command = GenerateAndSaveKeyCommand(EncryptionAlgorithm.AES_256)

        // When
        val result = handlerWithMock(command)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke should fail when save fails`() {
        // Given
        commandGateway.setShouldFailSave(true)
        val command = GenerateAndSaveKeyCommand(EncryptionAlgorithm.AES_256)

        // When
        val result = handler(command)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke should work with different algorithms`() {
        // Given
        val algorithms = listOf(
            EncryptionAlgorithm.AES_128,
            EncryptionAlgorithm.AES_192,
            EncryptionAlgorithm.AES_256
        )

        // When & Then
        algorithms.forEach { algorithm ->
            val command = GenerateAndSaveKeyCommand(algorithm)
            val result = handler(command)

            assertTrue("Should succeed for $algorithm", result.isSuccess)
            val keyIdView = result.getOrThrow()
            val savedKey = queryGateway.getKey(keyIdView.keyId)
            assertEquals(algorithm, savedKey?.algorithm)
        }
    }
}

