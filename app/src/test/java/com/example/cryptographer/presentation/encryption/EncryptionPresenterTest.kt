package com.example.cryptographer.presentation.encryption

import com.example.cryptographer.application.commands.text.decrypt.AesDecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.decrypt.ChaCha20DecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.AesEncryptTextCommand
import com.example.cryptographer.application.commands.text.encrypt.AesEncryptTextCommandHandler
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Base64

/**
 * Unit tests for EncryptionPresenter.
 */
class EncryptionPresenterTest {

    private lateinit var aesEncryptHandler: AesEncryptTextCommandHandler
    private lateinit var chaCha20EncryptHandler: ChaCha20EncryptTextCommandHandler
    private lateinit var aesDecryptHandler: AesDecryptTextCommandHandler
    private lateinit var chaCha20DecryptHandler: ChaCha20DecryptTextCommandHandler
    private lateinit var presenter: EncryptionPresenter

    @Before
    fun setUp() {
        aesEncryptHandler = mockk()
        chaCha20EncryptHandler = mockk()
        aesDecryptHandler = mockk()
        chaCha20DecryptHandler = mockk()
        presenter = EncryptionPresenter(
            aesEncryptHandler = aesEncryptHandler,
            chaCha20EncryptHandler = chaCha20EncryptHandler,
            aesDecryptHandler = aesDecryptHandler,
            chaCha20DecryptHandler = chaCha20DecryptHandler
        )
    }

    @Test
    fun `encryptText should return success when encryption succeeds`() {
        // Given
        val rawText = "Hello, World!"
        val key = KeyFactory.createAes256()
        val encryptedData = ByteArray(32)
        val iv = ByteArray(16)
        val encryptedTextEntity = EncryptedText(
            encryptedData = encryptedData,
            algorithm = EncryptionAlgorithm.AES_256,
            initializationVector = iv
        )

        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val ivBase64 = Base64.getEncoder().encodeToString(iv)

        val encryptedTextView = EncryptedTextView(encryptedText = encryptedTextEntity)

        every { aesEncryptHandler(AesEncryptTextCommand(rawText, key)) } returns Result.success(encryptedTextView)

        // When
        val result = presenter.encryptText(rawText, key)

        // Then
        assertTrue(result.isSuccess)
        val encryptedInfo = result.getOrThrow()
        assertEquals(encryptedBase64, encryptedInfo.encryptedBase64)
        assertEquals(ivBase64, encryptedInfo.ivBase64)
        assertEquals(EncryptionAlgorithm.AES_256, encryptedInfo.algorithm)

        verify(exactly = 1) { aesEncryptHandler(AesEncryptTextCommand(rawText, key)) }
    }

    @Test
    fun `encryptText should return failure when encryption fails`() {
        // Given
        val rawText = "Hello, World!"
        val key = KeyFactory.createAes256()
        val error = Exception("Encryption failed")

        every { aesEncryptHandler(AesEncryptTextCommand(rawText, key)) } returns Result.failure(error)

        // When
        val result = presenter.encryptText(rawText, key)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())

        verify(exactly = 1) { aesEncryptHandler(AesEncryptTextCommand(rawText, key)) }
    }

    @Test
    fun `encryptText should handle null IV correctly`() {
        // Given
        val rawText = "Test text"
        val key = KeyFactory.createAes128()
        val encryptedData = ByteArray(16)
        val encryptedTextEntity = EncryptedText(
            encryptedData = encryptedData,
            algorithm = EncryptionAlgorithm.AES_128,
            initializationVector = null
        )

        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val encryptedTextView = EncryptedTextView(encryptedText = encryptedTextEntity)

        every { aesEncryptHandler(AesEncryptTextCommand(rawText, key)) } returns Result.success(encryptedTextView)

        // When
        val result = presenter.encryptText(rawText, key)

        // Then
        assertTrue(result.isSuccess)
        val encryptedInfo = result.getOrThrow()
        assertEquals(encryptedBase64, encryptedInfo.encryptedBase64)
        assertNull(encryptedInfo.ivBase64)
        assertEquals(EncryptionAlgorithm.AES_128, encryptedInfo.algorithm)
    }

    @Test
    fun `decryptText should return success when decryption succeeds`() {
        // Given
        val decryptedText = "Hello, World!"
        val key = KeyFactory.createAes256()
        val encryptedData = ByteArray(32)
        val iv = ByteArray(16)
        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val ivBase64 = Base64.getEncoder().encodeToString(iv)

        val decryptedTextView = DecryptedTextView(decryptedText = decryptedText)

        // Use any() matcher since EncryptedText is created inside the presenter with a new ID
        every { aesDecryptHandler(any()) } returns Result.success(decryptedTextView)

        // When
        val result = presenter.decryptText(encryptedBase64, ivBase64, key)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(decryptedText, result.getOrThrow())

        verify(exactly = 1) { aesDecryptHandler(any()) }
    }

    @Test
    fun `decryptText should return success when IV is null`() {
        // Given
        val decryptedText = "Test text"
        val key = KeyFactory.createAes128()
        val encryptedData = ByteArray(16)
        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val ivBase64: String? = null

        val decryptedTextView = DecryptedTextView(decryptedText = decryptedText)

        // Use any() matcher since EncryptedText is created inside the presenter with a new ID
        every { aesDecryptHandler(any()) } returns Result.success(decryptedTextView)

        // When
        val result = presenter.decryptText(encryptedBase64, ivBase64, key)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(decryptedText, result.getOrThrow())

        verify(exactly = 1) { aesDecryptHandler(any()) }
    }

    @Test
    fun `decryptText should return success when IV is blank`() {
        // Given
        val decryptedText = "Test text"
        val key = KeyFactory.createAes128()
        val encryptedData = ByteArray(16)
        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val ivBase64 = ""

        val decryptedTextView = DecryptedTextView(decryptedText = decryptedText)

        // Use any() matcher since EncryptedText is created inside the presenter with a new ID
        every { aesDecryptHandler(any()) } returns Result.success(decryptedTextView)

        // When
        val result = presenter.decryptText(encryptedBase64, ivBase64, key)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(decryptedText, result.getOrThrow())
    }

    @Test
    fun `decryptText should return failure when decryption fails`() {
        // Given
        val key = KeyFactory.createAes256()
        val encryptedData = ByteArray(32)
        val iv = ByteArray(16)
        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val ivBase64 = Base64.getEncoder().encodeToString(iv)
        val error = Exception("Decryption failed")

        // Use any() matcher since EncryptedText is created inside the presenter with a new ID
        every { aesDecryptHandler(any()) } returns Result.failure(error)

        // When
        val result = presenter.decryptText(encryptedBase64, ivBase64, key)

        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull()?.message)

        verify(exactly = 1) { aesDecryptHandler(any()) }
    }

    @Test
    fun `decryptText should return failure when Base64 is invalid`() {
        // Given
        val key = KeyFactory.createAes256()
        val invalidBase64 = "Invalid Base64!!!"

        // When
        val result = presenter.decryptText(invalidBase64, null, key)

        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull()?.message)
        assertTrue(result.exceptionOrNull()?.message?.contains("Base64") == true)

        verify(exactly = 0) { aesDecryptHandler(any()) }
        verify(exactly = 0) { chaCha20DecryptHandler(any()) }
    }

    @Test
    fun `decryptText should return failure when IV Base64 is invalid`() {
        // Given
        val key = KeyFactory.createAes256()
        val encryptedData = ByteArray(32)
        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val invalidIvBase64 = "Invalid IV Base64!!!"

        // When
        val result = presenter.decryptText(encryptedBase64, invalidIvBase64, key)

        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull()?.message)
        assertTrue(result.exceptionOrNull()?.message?.contains("Base64") == true)

        verify(exactly = 0) { aesDecryptHandler(any()) }
        verify(exactly = 0) { chaCha20DecryptHandler(any()) }
    }

    @Test
    fun `encryptText should work with ChaCha20 algorithm`() {
        // Given
        val rawText = "Hello, ChaCha20!"
        val key = KeyFactory.createChaCha20_256()
        val encryptedData = ByteArray(32)
        val iv = ByteArray(12) // ChaCha20 uses 96-bit (12-byte) nonce
        val encryptedTextEntity = EncryptedText(
            encryptedData = encryptedData,
            algorithm = EncryptionAlgorithm.CHACHA20_256,
            initializationVector = iv
        )

        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val ivBase64 = Base64.getEncoder().encodeToString(iv)
        val encryptedTextView = EncryptedTextView(encryptedText = encryptedTextEntity)

        every { chaCha20EncryptHandler(ChaCha20EncryptTextCommand(rawText, key)) } returns Result.success(encryptedTextView)

        // When
        val result = presenter.encryptText(rawText, key)

        // Then
        assertTrue(result.isSuccess)
        val encryptedInfo = result.getOrThrow()
        assertEquals(encryptedBase64, encryptedInfo.encryptedBase64)
        assertEquals(ivBase64, encryptedInfo.ivBase64)
        assertEquals(EncryptionAlgorithm.CHACHA20_256, encryptedInfo.algorithm)

        verify(exactly = 1) { chaCha20EncryptHandler(ChaCha20EncryptTextCommand(rawText, key)) }
    }

    @Test
    fun `decryptText should work with ChaCha20 algorithm`() {
        // Given
        val decryptedText = "Hello, ChaCha20!"
        val key = KeyFactory.createChaCha20_256()
        val encryptedData = ByteArray(32)
        val iv = ByteArray(12) // ChaCha20 uses 96-bit (12-byte) nonce
        val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData)
        val ivBase64 = Base64.getEncoder().encodeToString(iv)

        val decryptedTextView = DecryptedTextView(decryptedText = decryptedText)

        // Use any() matcher since EncryptedText is created inside the presenter with a new ID
        every { chaCha20DecryptHandler(any()) } returns Result.success(decryptedTextView)

        // When
        val result = presenter.decryptText(encryptedBase64, ivBase64, key)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(decryptedText, result.getOrThrow())

        verify(exactly = 1) { chaCha20DecryptHandler(any()) }
    }
}
