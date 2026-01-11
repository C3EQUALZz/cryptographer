package com.example.cryptographer.presentation.encryption

import com.example.cryptographer.application.commands.text.decrypt.AesDecryptTextCommand
import com.example.cryptographer.application.commands.text.decrypt.AesDecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.decrypt.ChaCha20DecryptTextCommand
import com.example.cryptographer.application.commands.text.decrypt.ChaCha20DecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.AesEncryptTextCommand
import com.example.cryptographer.application.commands.text.encrypt.AesEncryptTextCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.ChaCha20EncryptTextCommand
import com.example.cryptographer.application.commands.text.encrypt.ChaCha20EncryptTextCommandHandler
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Base64

/**
 * Presenter for encryption/decryption screen.
 * Coordinates command handlers and transforms between presentation DTOs and Views.
 *
 * This presenter follows Clean Architecture and CQRS:
 * - Accepts raw strings (DTOs) from ViewModel
 * - Uses specialized CommandHandlers from Application layer based on algorithm
 * - Converts Views to presentation DTOs
 */
class EncryptionPresenter(
    private val aesEncryptHandler: AesEncryptTextCommandHandler,
    private val chaCha20EncryptHandler: ChaCha20EncryptTextCommandHandler,
    private val aesDecryptHandler: AesDecryptTextCommandHandler,
    private val chaCha20DecryptHandler: ChaCha20DecryptTextCommandHandler,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Encrypts raw text string using the provided key.
     *
     * @param rawText Raw text string (DTO from presentation layer)
     * @param key Encryption key
     * @return Result with encrypted text info (Base64 encoded) or error
     */
    fun encryptText(rawText: String, key: EncryptionKey): Result<EncryptedTextInfo> {
        return try {
            logger.debug { "Presenter: Encrypting text: length=${rawText.length}, algorithm=${key.algorithm}" }

            // Select handler and create command based on algorithm
            val encryptedTextViewResult = when (key.algorithm) {
                EncryptionAlgorithm.AES_128,
                EncryptionAlgorithm.AES_192,
                EncryptionAlgorithm.AES_256,
                -> {
                    val command = AesEncryptTextCommand(rawText, key)
                    aesEncryptHandler(command)
                }
                EncryptionAlgorithm.CHACHA20_256 -> {
                    val command = ChaCha20EncryptTextCommand(rawText, key)
                    chaCha20EncryptHandler(command)
                }
            }

            if (encryptedTextViewResult.isFailure) {
                val error = encryptedTextViewResult.exceptionOrNull() ?: Exception("Encryption failed")
                logger.error(error) { "Presenter: Encryption failed: ${error.message}" }
                return Result.failure(error)
            }

            val encryptedTextView = encryptedTextViewResult.getOrThrow()
            val encryptedText = encryptedTextView.encryptedText

            // Convert View to presentation DTO
            val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedText.encryptedData)
            val ivBase64 = encryptedText.initializationVector?.let {
                Base64.getEncoder().encodeToString(it)
            }

            logger.info {
                "Presenter: Text encrypted successfully: algorithm=${key.algorithm}, size=${encryptedText.encryptedData.size} bytes"
            }
            Result.success(
                EncryptedTextInfo(
                    encryptedBase64 = encryptedBase64,
                    ivBase64 = ivBase64,
                    algorithm = key.algorithm,
                ),
            )
        } catch (e: AppError) {
            logger.error(e) { "Presenter: Error encrypting text: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Decrypts encrypted text using the provided key.
     *
     * @param encryptedBase64 Base64 encoded encrypted data (DTO from presentation layer)
     * @param ivBase64 Base64 encoded initialization vector (optional, DTO from presentation layer)
     * @param key Encryption key
     * @return Result with decrypted text string (DTO for presentation layer) or error
     */
    fun decryptText(encryptedBase64: String, ivBase64: String?, key: EncryptionKey): Result<String> {
        return try {
            logger.debug { "Presenter: Decrypting text: algorithm=${key.algorithm}" }

            // Convert presentation DTOs to domain entity
            val encryptedData = Base64.getDecoder().decode(encryptedBase64)
            val iv = ivBase64?.takeIf { it.isNotBlank() }?.let {
                Base64.getDecoder().decode(it)
            }

            val encryptedText = EncryptedText(
                encryptedData = encryptedData,
                algorithm = key.algorithm,
                initializationVector = iv,
            )

            // Select handler and create command based on algorithm
            val decryptedTextViewResult = when (key.algorithm) {
                EncryptionAlgorithm.AES_128,
                EncryptionAlgorithm.AES_192,
                EncryptionAlgorithm.AES_256,
                -> {
                    val command = AesDecryptTextCommand(encryptedText, key)
                    aesDecryptHandler(command)
                }
                EncryptionAlgorithm.CHACHA20_256 -> {
                    val command = ChaCha20DecryptTextCommand(encryptedText, key)
                    chaCha20DecryptHandler(command)
                }
            }

            if (decryptedTextViewResult.isFailure) {
                val error = decryptedTextViewResult.exceptionOrNull() ?: AppError("Decryption failed")
                logger.error(error) { "Presenter: Decryption failed: ${error.message}" }
                return Result.failure(error)
            }

            val decryptedTextView = decryptedTextViewResult.getOrThrow()
            val decryptedText = decryptedTextView.decryptedText

            logger.info { "Presenter: Text decrypted successfully: length=${decryptedText.length}" }
            Result.success(decryptedText)
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Presenter: Invalid Base64 input for decryption: ${e.message}" }
            Result.failure(AppError("Некорректный формат Base64 для зашифрованного текста или IV.", e))
        } catch (e: AppError) {
            logger.error(e) { "Presenter: Error decrypting text: ${e.message}" }
            Result.failure(e)
        }
    }
}

/**
 * Presentation model for encrypted text information.
 */
data class EncryptedTextInfo(
    val encryptedBase64: String,
    val ivBase64: String?,
    val algorithm: EncryptionAlgorithm,
)
