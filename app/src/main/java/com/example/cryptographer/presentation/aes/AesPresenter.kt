package com.example.cryptographer.presentation.aes

import com.example.cryptographer.application.commands.text.decrypt.AesDecryptTextCommand
import com.example.cryptographer.application.commands.text.decrypt.AesDecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.AesEncryptTextCommand
import com.example.cryptographer.application.commands.text.encrypt.AesEncryptTextCommandHandler
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Base64
import javax.inject.Inject

/**
 * Presenter for AES encryption/decryption screen.
 * Coordinates AES-specific command handlers.
 */
class AesPresenter @Inject constructor(
    private val aesEncryptHandler: AesEncryptTextCommandHandler,
    private val aesDecryptHandler: AesDecryptTextCommandHandler,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Encrypts raw text string using AES encryption.
     */
    fun encryptText(rawText: String, key: EncryptionKey): Result<EncryptedTextInfo> {
        return try {
            logger.debug { "AES Presenter: Encrypting text: length=${rawText.length}, algorithm=${key.algorithm}" }

            if (key.algorithm !in listOf(
                    EncryptionAlgorithm.AES_128,
                    EncryptionAlgorithm.AES_192,
                    EncryptionAlgorithm.AES_256,
                )
            ) {
                Result.failure(AppError("Invalid algorithm for AES encryption: ${key.algorithm}"))
            } else {
                performEncryption(rawText, key)
            }
        } catch (e: AppError) {
            logger.error(e) { "AES Presenter: Error encrypting text: ${e.message}" }
            Result.failure(e)
        }
    }

    private fun performEncryption(rawText: String, key: EncryptionKey): Result<EncryptedTextInfo> {
        val command = AesEncryptTextCommand(rawText, key)
        val encryptedTextViewResult = aesEncryptHandler(command)

        if (encryptedTextViewResult.isFailure) {
            val error = encryptedTextViewResult.exceptionOrNull() ?: Exception("Encryption failed")
            logger.error(error) { "AES Presenter: Encryption failed: ${error.message}" }
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
            "AES Presenter: " +
                "Text encrypted successfully:" +
                " algorithm=${key.algorithm}," +
                " size=${encryptedText.encryptedData.size} bytes"
        }
        return Result.success(
            EncryptedTextInfo(
                encryptedBase64 = encryptedBase64,
                ivBase64 = ivBase64,
                algorithm = key.algorithm,
            ),
        )
    }

    /**
     * Decrypts encrypted text using AES decryption.
     */
    fun decryptText(encryptedBase64: String, ivBase64: String?, key: EncryptionKey): Result<String> {
        return try {
            logger.debug { "AES Presenter: Decrypting text: algorithm=${key.algorithm}" }

            // Verify algorithm is AES
            val result = if (key.algorithm !in listOf(
                    EncryptionAlgorithm.AES_128,
                    EncryptionAlgorithm.AES_192,
                    EncryptionAlgorithm.AES_256,
                )
            ) {
                Result.failure(AppError("Invalid algorithm for AES decryption: ${key.algorithm}"))
            } else {
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

                val command = AesDecryptTextCommand(encryptedText, key)
                val decryptedTextViewResult = aesDecryptHandler(command)

                if (decryptedTextViewResult.isFailure) {
                    val error = decryptedTextViewResult.exceptionOrNull() ?: AppError("Decryption failed")
                    logger.error(error) { "AES Presenter: Decryption failed: ${error.message}" }
                    Result.failure(error)
                } else {
                    val decryptedTextView = decryptedTextViewResult.getOrThrow()
                    val decryptedText = decryptedTextView.decryptedText

                    logger.info { "AES Presenter: Text decrypted successfully: length=${decryptedText.length}" }
                    Result.success(decryptedText)
                }
            }
            result
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "AES Presenter: Invalid Base64 input for decryption: ${e.message}" }
            Result.failure(AppError("Invalid Base64 format for encrypted text or IV.", e))
        } catch (e: AppError) {
            logger.error(e) { "AES Presenter: Error decrypting text: ${e.message}" }
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
