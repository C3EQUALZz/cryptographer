package com.example.cryptographer.application.commands.text.decrypt

import com.example.cryptographer.application.common.views.DecryptedTextView
import com.example.cryptographer.domain.text.services.AesEncryptionService
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for decrypting text using AES algorithm.
 *
 * This is a specialized Command Handler for AES decryption.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses domain services for business logic
 * - Returns View (DTO) for presentation layer
 */
class AesDecryptTextCommandHandler(
    private val aesEncryptionService: AesEncryptionService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the AesDecryptTextCommand.
     *
     * @param command Command to execute
     * @return Result with DecryptedTextView or error
     */
    operator fun invoke(command: AesDecryptTextCommand): Result<DecryptedTextView> {
        return try {
            logger.debug { "Handling AES DecryptTextCommand: algorithm=${command.key.algorithm}, encryptedSize=${command.encryptedText.encryptedData.size} bytes" }

            // Decrypt using AES service
            val decryptedBytes = aesEncryptionService.decrypt(command.encryptedText, command.key).getOrElse { error ->
                logger.error(error) { "AES text decryption failed: ${error.message}" }
                return Result.failure(error)
            }

            // Convert bytes back to text string
            val decryptedContent = String(decryptedBytes, Charsets.UTF_8)

            logger.info { "AES text decryption successful: algorithm=${command.key.algorithm}, decryptedLength=${decryptedContent.length}" }
            Result.success(DecryptedTextView(decryptedContent))
        } catch (e: Exception) {
            logger.error(e) { "Error handling AES DecryptTextCommand: ${e.message}" }
            Result.failure(
                Exception("AES text decryption error: ${e.message}", e)
            )
        }
    }
}

