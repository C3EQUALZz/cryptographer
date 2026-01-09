package com.example.cryptographer.presentation.encryption

import com.example.cryptographer.application.commands.text.decrypt.DecryptTextCommand
import com.example.cryptographer.application.commands.text.decrypt.DecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.EncryptTextCommand
import com.example.cryptographer.application.commands.text.encrypt.EncryptTextCommandHandler
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.value_objects.EncryptionAlgorithm
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.setup.configs.getLogger
import java.util.Base64

/**
 * Presenter for encryption/decryption screen.
 * Coordinates command handlers and transforms between presentation DTOs and Views.
 *
 * This presenter follows Clean Architecture and CQRS:
 * - Accepts raw strings (DTOs) from ViewModel
 * - Uses CommandHandlers from Application layer
 * - Converts Views to presentation DTOs
 */
class EncryptionPresenter(
    private val encryptTextHandler: EncryptTextCommandHandler,
    private val decryptTextHandler: DecryptTextCommandHandler
) {
    private val logger = getLogger<EncryptionPresenter>()

    /**
     * Encrypts raw text string using the provided key.
     *
     * @param rawText Raw text string (DTO from presentation layer)
     * @param key Encryption key
     * @return Result with encrypted text info (Base64 encoded) or error
     */
    fun encryptText(rawText: String, key: EncryptionKey): Result<EncryptedTextInfo> {
        return try {
            logger.d("Presenter: Encrypting text: length=${rawText.length}, algorithm=${key.algorithm}")

            // Execute command via CommandHandler
            val command = EncryptTextCommand(rawText, key)
            val encryptedTextViewResult = encryptTextHandler(command)

            if (encryptedTextViewResult.isFailure) {
                val error = encryptedTextViewResult.exceptionOrNull() ?: Exception("Encryption failed")
                logger.e("Presenter: Encryption failed: ${error.message}", error)
                return Result.failure(error)
            }

            val encryptedTextView = encryptedTextViewResult.getOrThrow()
            val encryptedText = encryptedTextView.encryptedText

            // Convert View to presentation DTO
            val encryptedBase64 = Base64.getEncoder().encodeToString(encryptedText.encryptedData)
            val ivBase64 = encryptedText.initializationVector?.let {
                Base64.getEncoder().encodeToString(it)
            }

            logger.i("Presenter: Text encrypted successfully: algorithm=${key.algorithm}, size=${encryptedText.encryptedData.size} bytes")
            Result.success(
                EncryptedTextInfo(
                    encryptedBase64 = encryptedBase64,
                    ivBase64 = ivBase64,
                    algorithm = key.algorithm
                )
            )
        } catch (e: Exception) {
            logger.e("Presenter: Error encrypting text: ${e.message}", e)
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
    fun decryptText(
        encryptedBase64: String,
        ivBase64: String?,
        key: EncryptionKey
    ): Result<String> {
        return try {
            logger.d("Presenter: Decrypting text: algorithm=${key.algorithm}")

            // Convert presentation DTOs to domain entity
            val encryptedData = Base64.getDecoder().decode(encryptedBase64)
            val iv = ivBase64?.takeIf { it.isNotBlank() }?.let {
                Base64.getDecoder().decode(it)
            }

            val encryptedText = EncryptedText(
                encryptedData = encryptedData,
                algorithm = key.algorithm,
                initializationVector = iv
            )

            // Execute command via CommandHandler
            val command = DecryptTextCommand(encryptedText, key)
            val decryptedTextViewResult = decryptTextHandler(command)

            if (decryptedTextViewResult.isFailure) {
                val error = decryptedTextViewResult.exceptionOrNull() ?: Exception("Decryption failed")
                logger.e("Presenter: Decryption failed: ${error.message}", error)
                return Result.failure(error)
            }

            val decryptedTextView = decryptedTextViewResult.getOrThrow()
            val decryptedText = decryptedTextView.decryptedText

            logger.i("Presenter: Text decrypted successfully: length=${decryptedText.length}")
            Result.success(decryptedText)
        } catch (e: IllegalArgumentException) {
            logger.e("Presenter: Invalid Base64 input for decryption: ${e.message}", e)
            Result.failure(Exception("Некорректный формат Base64 для зашифрованного текста или IV."))
        } catch (e: Exception) {
            logger.e("Presenter: Error decrypting text: ${e.message}", e)
            Result.failure(Exception("Ошибка дешифрования: ${e.message}"))
        }
    }
}

/**
 * Presentation model for encrypted text information.
 */
data class EncryptedTextInfo(
    val encryptedBase64: String,
    val ivBase64: String?,
    val algorithm: EncryptionAlgorithm
)
