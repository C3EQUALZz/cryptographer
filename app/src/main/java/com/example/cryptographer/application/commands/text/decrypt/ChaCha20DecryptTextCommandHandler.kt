package com.example.cryptographer.application.commands.text.decrypt

import com.example.cryptographer.application.common.views.DecryptedTextView
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.services.ChaCha20EncryptionService
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for decrypting text using ChaCha20 algorithm.
 *
 * This is a specialized Command Handler for ChaCha20 decryption.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses domain services for business logic
 * - Returns View (DTO) for presentation layer
 */
class ChaCha20DecryptTextCommandHandler(
    private val chaCha20EncryptionService: ChaCha20EncryptionService,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the ChaCha20DecryptTextCommand.
     *
     * @param command Command to execute
     * @return Result with DecryptedTextView or error
     */
    operator fun invoke(command: ChaCha20DecryptTextCommand): Result<DecryptedTextView> {
        return try {
            logger.debug {
                "Handling ChaCha20 DecryptTextCommand: algorithm=${command.key.algorithm}, encryptedSize=${command.encryptedText.encryptedData.size} bytes"
            }

            // Decrypt using ChaCha20 service
            val decryptedBytes = chaCha20EncryptionService.decrypt(
                command.encryptedText,
                command.key,
            ).getOrElse { error ->
                logger.error(error) { "ChaCha20 text decryption failed: ${error.message}" }
                return Result.failure(error)
            }

            // Convert bytes back to text string
            val decryptedContent = String(decryptedBytes, Charsets.UTF_8)

            logger.info {
                "ChaCha20 text decryption successful: " +
                    "algorithm=${command.key.algorithm}, " +
                    "decryptedLength=${decryptedContent.length}"
            }
            Result.success(DecryptedTextView(decryptedContent))
        } catch (e: AppError) {
            logger.error(e) { "Error handling ChaCha20 DecryptTextCommand: ${e.message}" }
            Result.failure(e)
        }
    }
}
