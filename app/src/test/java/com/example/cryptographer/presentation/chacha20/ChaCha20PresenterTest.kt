package com.example.cryptographer.presentation.chacha20

import com.example.cryptographer.application.commands.text.decrypt.ChaCha20DecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.ChaCha20EncryptTextCommand
import com.example.cryptographer.application.commands.text.encrypt.ChaCha20EncryptTextCommandHandler
import com.example.cryptographer.application.common.views.DecryptedTextView
import com.example.cryptographer.application.common.views.EncryptedTextView
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.test.factories.KeyFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Base64

/**
 * Unit tests for ChaCha20Presenter.
 */
class ChaCha20PresenterTest {

    private lateinit var chaCha20EncryptHandler: ChaCha20EncryptTextCommandHandler
    private lateinit var chaCha20DecryptHandler: ChaCha20DecryptTextCommandHandler
    private lateinit var presenter: ChaCha20Presenter

    @Before
    fun setUp() {
        chaCha20EncryptHandler = mockk()
        chaCha20DecryptHandler = mockk()
        presenter = ChaCha20Presenter(
            chaCha20EncryptHandler = chaCha20EncryptHandler,
            chaCha20DecryptHandler = chaCha20DecryptHandler,
        )
    }

    @Test
    fun `encryptText should return success when encryption succeeds`() {
        // Given
        val rawText = "Hello, ChaCha20!"
        val key = KeyFactory.createChaCha256()
        val encryptedData = ByteArray(32)
        val nonce = ByteArray(12) // ChaCha20 uses 96-bit (12-byte) nonce
        val encryptedTextEntity = EncryptedText(
            encryptedData = encryptedData,
            algorithm = EncryptionAlgorithm.CHACHA20_256,
            initializationVector = nonce,
        )

        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val nonceBase64 = Base64.getEncoder().encodeToString(nonce)
        val encryptedTextView = EncryptedTextView(encryptedText = encryptedTextEntity)

        every {
            chaCha20EncryptHandler(
                ChaCha20EncryptTextCommand(rawText, key),
            )
        } returns Result.success(encryptedTextView)

        // When
        val result = presenter.encryptText(rawText, key)

        // Then
        assertTrue(result.isSuccess)
        val encryptedInfo = result.getOrThrow()
        assertEquals(encryptedBase64, encryptedInfo.encryptedBase64)
        assertEquals(nonceBase64, encryptedInfo.nonceBase64)

        verify(exactly = 1) { chaCha20EncryptHandler(ChaCha20EncryptTextCommand(rawText, key)) }
    }

    @Test
    fun `encryptText should return failure when encryption fails`() {
        // Given
        val rawText = "Hello, ChaCha20!"
        val key = KeyFactory.createChaCha256()
        val error = Exception("Encryption failed")

        every {
            chaCha20EncryptHandler(ChaCha20EncryptTextCommand(rawText, key))
        } returns Result.failure(error)

        // When
        val result = presenter.encryptText(rawText, key)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())

        verify(exactly = 1) { chaCha20EncryptHandler(ChaCha20EncryptTextCommand(rawText, key)) }
    }

    @Test
    fun `encryptText should return failure when algorithm is invalid`() {
        // Given
        val rawText = "Test text"
        val key = KeyFactory.createAes256() // Wrong algorithm

        // When
        val result = presenter.encryptText(rawText, key)

        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull()?.message)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid algorithm") == true)

        verify(exactly = 0) { chaCha20EncryptHandler(any()) }
    }

    @Test
    fun `encryptText should handle null nonce correctly`() {
        // Given
        val rawText = "Test text"
        val key = KeyFactory.createChaCha256()
        val encryptedData = ByteArray(32)
        val encryptedTextEntity = EncryptedText(
            encryptedData = encryptedData,
            algorithm = EncryptionAlgorithm.CHACHA20_256,
            initializationVector = null,
        )

        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val encryptedTextView = EncryptedTextView(encryptedText = encryptedTextEntity)

        every {
            chaCha20EncryptHandler(ChaCha20EncryptTextCommand(rawText, key))
        } returns Result.success(encryptedTextView)

        // When
        val result = presenter.encryptText(rawText, key)

        // Then
        assertTrue(result.isSuccess)
        val encryptedInfo = result.getOrThrow()
        assertEquals(encryptedBase64, encryptedInfo.encryptedBase64)
        assertNull(encryptedInfo.nonceBase64)
    }

    @Test
    fun `decryptText should return success when decryption succeeds`() {
        // Given
        val decryptedText = "Hello, ChaCha20!"
        val key = KeyFactory.createChaCha256()
        val encryptedData = ByteArray(32)
        val nonce = ByteArray(12) // ChaCha20 uses 96-bit (12-byte) nonce
        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val nonceBase64 = Base64.getEncoder().encodeToString(nonce)

        val decryptedTextView = DecryptedTextView(decryptedText = decryptedText)

        // Use any() matcher since EncryptedText is created inside the presenter with a new ID
        every { chaCha20DecryptHandler(any()) } returns Result.success(decryptedTextView)

        // When
        val result = presenter.decryptText(encryptedBase64, nonceBase64, key)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(decryptedText, result.getOrThrow())

        verify(exactly = 1) { chaCha20DecryptHandler(any()) }
    }

    @Test
    fun `decryptText should return success when nonce is null`() {
        // Given
        val decryptedText = "Test text"
        val key = KeyFactory.createChaCha256()
        val encryptedData = ByteArray(32)
        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val nonceBase64: String? = null

        val decryptedTextView = DecryptedTextView(decryptedText = decryptedText)

        // Use any() matcher since EncryptedText is created inside the presenter with a new ID
        every { chaCha20DecryptHandler(any()) } returns Result.success(decryptedTextView)

        // When
        val result = presenter.decryptText(encryptedBase64, nonceBase64, key)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(decryptedText, result.getOrThrow())

        verify(exactly = 1) { chaCha20DecryptHandler(any()) }
    }

    @Test
    fun `decryptText should return success when nonce is blank`() {
        // Given
        val decryptedText = "Test text"
        val key = KeyFactory.createChaCha256()
        val encryptedData = ByteArray(32)
        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val nonceBase64 = ""

        val decryptedTextView = DecryptedTextView(decryptedText = decryptedText)

        // Use any() matcher since EncryptedText is created inside the presenter with a new ID
        every { chaCha20DecryptHandler(any()) } returns Result.success(decryptedTextView)

        // When
        val result = presenter.decryptText(encryptedBase64, nonceBase64, key)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(decryptedText, result.getOrThrow())
    }

    @Test
    fun `decryptText should return failure when decryption fails`() {
        // Given
        val key = KeyFactory.createChaCha256()
        val encryptedData = ByteArray(32)
        val nonce = ByteArray(12)
        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val nonceBase64 = Base64.getEncoder().encodeToString(nonce)
        val error = Exception("Decryption failed")

        // Use any() matcher since EncryptedText is created inside the presenter with a new ID
        every { chaCha20DecryptHandler(any()) } returns Result.failure(error)

        // When
        val result = presenter.decryptText(encryptedBase64, nonceBase64, key)

        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull()?.message)

        verify(exactly = 1) { chaCha20DecryptHandler(any()) }
    }

    @Test
    fun `decryptText should return failure when algorithm is invalid`() {
        // Given
        val key = KeyFactory.createAes256() // Wrong algorithm
        val encryptedData = ByteArray(32)
        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)

        // When
        val result = presenter.decryptText(encryptedBase64, null, key)

        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull()?.message)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid algorithm") == true)

        verify(exactly = 0) { chaCha20DecryptHandler(any()) }
    }

    @Test
    fun `decryptText should return failure when Base64 is invalid`() {
        // Given
        val key = KeyFactory.createChaCha256()
        val invalidBase64 = "Invalid Base64!!!"

        // When
        val result = presenter.decryptText(invalidBase64, null, key)

        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull()?.message)
        assertTrue(result.exceptionOrNull()?.message?.contains("Base64") == true)

        verify(exactly = 0) { chaCha20DecryptHandler(any()) }
    }

    @Test
    fun `decryptText should return failure when nonce Base64 is invalid`() {
        // Given
        val key = KeyFactory.createChaCha256()
        val encryptedData = ByteArray(32)
        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val invalidNonceBase64 = "Invalid nonce Base64!!!"

        // When
        val result = presenter.decryptText(encryptedBase64, invalidNonceBase64, key)

        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull()?.message)
        assertTrue(result.exceptionOrNull()?.message?.contains("Base64") == true)

        verify(exactly = 0) { chaCha20DecryptHandler(any()) }
    }
}
