package com.example.cryptographer.presentation.chacha20

import com.example.cryptographer.application.commands.text.decrypt.ChaCha20DecryptTextCommand
import com.example.cryptographer.application.commands.text.decrypt.ChaCha20DecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.ChaCha20EncryptTextCommand
import com.example.cryptographer.application.commands.text.encrypt.ChaCha20EncryptTextCommandHandler
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Base64
import javax.inject.Inject

/**
 * Presenter for ChaCha20 encryption/decryption screen.
 * Coordinates ChaCha20-specific command handlers.
 */
class ChaCha20Presenter @Inject constructor(
    private val chaCha20EncryptHandler: ChaCha20EncryptTextCommandHandler,
    private val chaCha20DecryptHandler: ChaCha20DecryptTextCommandHandler,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Encrypts raw text string using ChaCha20 encryption.
     */
    fun encryptText(rawText: String, key: EncryptionKey): Result<EncryptedTextInfo> {
        return try {
            logger.debug { "ChaCha20 Presenter: Encrypting text: length=${rawText.length}, algorithm=${key.algorithm}" }

            // Verify algorithm is ChaCha20
            if (key.algorithm != EncryptionAlgorithm.CHACHA20_256) {
                return Result.failure(AppError("Invalid algorithm for ChaCha20 encryption: ${key.algorithm}"))
            }

            val command = ChaCha20EncryptTextCommand(rawText, key)
            val encryptedTextViewResult = chaCha20EncryptHandler(command)

            if (encryptedTextViewResult.isFailure) {
                val error = encryptedTextViewResult.exceptionOrNull() ?: Exception("Encryption failed")
                logger.error(error) { "ChaCha20 Presenter: Encryption failed: ${error.message}" }
                return Result.failure(error)
            }

            val encryptedTextView = encryptedTextViewResult.getOrThrow()
            val encryptedText = encryptedTextView.encryptedText

            // Convert View to presentation DTO
            val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedText.encryptedData)
            val nonceBase64 = encryptedText.initializationVector?.let {
                Base64.getEncoder().encodeToString(it)
            }

            logger.info {
                "ChaCha20 Presenter: " +
                    "Text encrypted successfully:" +
                    " algorithm=${key.algorithm}," +
                    " size=${encryptedText.encryptedData.size} bytes"
            }
            Result.success(
                EncryptedTextInfo(
                    encryptedBase64 = encryptedBase64,
                    nonceBase64 = nonceBase64,
                ),
            )
        } catch (e: AppError) {
            logger.error(e) { "ChaCha20 Presenter: Error encrypting text: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Decrypts encrypted text using ChaCha20 decryption.
     */
    fun decryptText(encryptedBase64: String, nonceBase64: String?, key: EncryptionKey): Result<String> {
        return try {
            logger.debug { "ChaCha20 Presenter: Decrypting text: algorithm=${key.algorithm}" }

            // Verify algorithm is ChaCha20
            if (key.algorithm != EncryptionAlgorithm.CHACHA20_256) {
                return Result.failure(AppError("Invalid algorithm for ChaCha20 decryption: ${key.algorithm}"))
            }

            // Convert presentation DTOs to domain entity
            val encryptedData = Base64.getDecoder().decode(encryptedBase64)
            val nonce = nonceBase64?.takeIf { it.isNotBlank() }?.let {
                Base64.getDecoder().decode(it)
            }

            val encryptedText = EncryptedText(
                encryptedData = encryptedData,
                algorithm = key.algorithm,
                initializationVector = nonce,
            )

            val command = ChaCha20DecryptTextCommand(encryptedText, key)
            val decryptedTextViewResult = chaCha20DecryptHandler(command)

            if (decryptedTextViewResult.isFailure) {
                val error = decryptedTextViewResult.exceptionOrNull() ?: AppError("Decryption failed")
                logger.error(error) { "ChaCha20 Presenter: Decryption failed: ${error.message}" }
                return Result.failure(error)
            }

            val decryptedTextView = decryptedTextViewResult.getOrThrow()
            val decryptedText = decryptedTextView.decryptedText

            logger.info { "ChaCha20 Presenter: Text decrypted successfully: length=${decryptedText.length}" }
            Result.success(decryptedText)
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "ChaCha20 Presenter: Invalid Base64 input for decryption: ${e.message}" }
            Result.failure(AppError("Некорректный формат Base64 для зашифрованного текста или nonce.", e))
        } catch (e: AppError) {
            logger.error(e) { "ChaCha20 Presenter: Error decrypting text: ${e.message}" }
            Result.failure(e)
        }
    }
}

/**
 * Presentation model for encrypted text information.
 */
data class EncryptedTextInfo(
    val encryptedBase64: String,
    val nonceBase64: String?,
)

