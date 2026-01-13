package com.example.cryptographer.presentation.tdes

import com.example.cryptographer.application.commands.text.decrypt.TripleDesDecryptTextCommand
import com.example.cryptographer.application.commands.text.decrypt.TripleDesDecryptTextCommandHandler
import com.example.cryptographer.application.commands.text.encrypt.TripleDesEncryptTextCommand
import com.example.cryptographer.application.commands.text.encrypt.TripleDesEncryptTextCommandHandler
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.entities.EncryptedText
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Base64
import javax.inject.Inject

/**
 * Presenter for Triple DES encryption/decryption screen.
 * Coordinates 3DES-specific command handlers.
 */
class TripleDesPresenter @Inject constructor(
    private val tripleDesEncryptHandler: TripleDesEncryptTextCommandHandler,
    private val tripleDesDecryptHandler: TripleDesDecryptTextCommandHandler,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Encrypts raw text string using Triple DES encryption.
     */
    fun encryptText(rawText: String, key: EncryptionKey): Result<EncryptedTextInfo> {
        return try {
            logger.debug { "3DES Presenter: Encrypting text: length=${rawText.length}, algorithm=${key.algorithm}" }

            if (key.algorithm != EncryptionAlgorithm.TDES_112 && key.algorithm != EncryptionAlgorithm.TDES_168) {
                Result.failure(
                    AppError("Invalid algorithm for 3DES encryption: ${key.algorithm}"),
                )
            } else {
                performEncryption(rawText, key)
            }
        } catch (e: AppError) {
            logger.error(e) { "3DES Presenter: Error encrypting text: ${e.message}" }
            Result.failure(e)
        }
    }

    private fun performEncryption(rawText: String, key: EncryptionKey): Result<EncryptedTextInfo> {
        val command = TripleDesEncryptTextCommand(rawText, key)
        val encryptedTextViewResult = tripleDesEncryptHandler(command)

        if (encryptedTextViewResult.isFailure) {
            val error = encryptedTextViewResult.exceptionOrNull() ?: Exception("Encryption failed")
            logger.error(error) { "3DES Presenter: Encryption failed: ${error.message}" }
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
            "3DES Presenter: " +
                "Text encrypted successfully:" +
                " algorithm=${key.algorithm}," +
                " size=${encryptedText.encryptedData.size} bytes"
        }
        return Result.success(
            EncryptedTextInfo(
                encryptedBase64 = encryptedBase64,
                nonceBase64 = nonceBase64,
            ),
        )
    }

    /**
     * Decrypts encrypted text using Triple DES decryption.
     */
    fun decryptText(encryptedBase64: String, nonceBase64: String?, key: EncryptionKey): Result<String> {
        return try {
            logger.debug { "3DES Presenter: Decrypting text: algorithm=${key.algorithm}" }

            // Verify algorithm is 3DES
            val result = if (
                key.algorithm != EncryptionAlgorithm.TDES_112 &&
                key.algorithm != EncryptionAlgorithm.TDES_168
            ) {
                Result.failure(
                    AppError("Invalid algorithm for 3DES decryption: ${key.algorithm}"),
                )
            } else {
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

                val command = TripleDesDecryptTextCommand(encryptedText, key)
                val decryptedTextViewResult = tripleDesDecryptHandler(command)

                if (decryptedTextViewResult.isFailure) {
                    val error = decryptedTextViewResult.exceptionOrNull() ?: AppError("Decryption failed")
                    logger.error(error) { "3DES Presenter: Decryption failed: ${error.message}" }
                    Result.failure(error)
                } else {
                    val decryptedTextView = decryptedTextViewResult.getOrThrow()
                    val decryptedText = decryptedTextView.decryptedText

                    logger.info { "3DES Presenter: Text decrypted successfully: length=${decryptedText.length}" }
                    Result.success(decryptedText)
                }
            }
            result
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "3DES Presenter: Invalid Base64 input for decryption: ${e.message}" }
            Result.failure(
                AppError("Invalid Base64 format for encrypted text or nonce.", e),
            )
        } catch (e: AppError) {
            logger.error(e) { "3DES Presenter: Error decrypting text: ${e.message}" }
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
