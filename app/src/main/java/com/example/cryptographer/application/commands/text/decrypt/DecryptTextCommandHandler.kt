package com.example.cryptographer.application.commands.text.decrypt

import com.example.cryptographer.application.common.views.DecryptedTextView
import com.example.cryptographer.domain.text.services.AesEncryptionService
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for decrypting text.
 *
 * This is a Command Handler in CQRS pattern - it handles read operations (decryption).
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses domain services for business logic
 * - Returns View (DTO) for presentation layer
 */
class DecryptTextCommandHandler(
    private val aesEncryptionService: AesEncryptionService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the DecryptTextCommand.
     *
     * @param command Command to execute
     * @return Result with DecryptedTextView or error
     */
    operator fun invoke(command: DecryptTextCommand): Result<DecryptedTextView> {
        return try {
            logger.debug { "Handling DecryptTextCommand: algorithm=${command.key.algorithm}, encryptedSize=${command.encryptedText.encryptedData.size} bytes" }

            // Decrypt using AES service
            val decryptedBytes = aesEncryptionService.decrypt(command.encryptedText, command.key).getOrElse { error ->
                logger.error(error) { "Text decryption failed: ${error.message}" }
                return Result.failure(error)
            }

            // Convert bytes back to text string
            val decryptedContent = String(decryptedBytes, Charsets.UTF_8)

            logger.info { "Text decryption successful: algorithm=${command.key.algorithm}, decryptedLength=${decryptedContent.length}" }
            Result.success(DecryptedTextView(decryptedContent))
        } catch (e: Exception) {
            logger.error(e) { "Error handling DecryptTextCommand: ${e.message}" }
            Result.failure(
                Exception("Text decryption error: ${e.message}", e)
            )
        }
    }
}
