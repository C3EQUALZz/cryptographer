package com.example.cryptographer.application.commands.text.decrypt

import com.example.cryptographer.application.common.views.DecryptedTextView
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.services.TripleDesEncryptionService
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for decrypting text using Triple DES algorithm.
 *
 * This is a specialized Command Handler for 3DES decryption.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses domain services for business logic
 * - Returns View (DTO) for presentation layer
 */
class TripleDesDecryptTextCommandHandler(
    private val tripleDesEncryptionService: TripleDesEncryptionService,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the TripleDesDecryptTextCommand.
     *
     * @param command Command to execute
     * @return Result with DecryptedTextView or error
     */
    operator fun invoke(command: TripleDesDecryptTextCommand): Result<DecryptedTextView> {
        return try {
            logger.debug {
                "Handling 3DES DecryptTextCommand: " +
                    "algorithm=${command.key.algorithm}, " +
                    "encryptedSize=${command.encryptedText.encryptedData.size} bytes"
            }

            // Decrypt using 3DES service
            val decryptedBytes = tripleDesEncryptionService.decrypt(
                command.encryptedText,
                command.key,
            ).getOrElse { error ->
                logger.error(error) { "3DES text decryption failed: ${error.message}" }
                return Result.failure(error)
            }

            // Convert bytes back to text string
            val decryptedContent = String(decryptedBytes, Charsets.UTF_8)

            logger.info {
                "3DES text decryption successful: " +
                    "algorithm=${command.key.algorithm}, " +
                    "decryptedLength=${decryptedContent.length}"
            }
            Result.success(DecryptedTextView(decryptedContent))
        } catch (e: AppError) {
            logger.error(e) { "Error handling 3DES DecryptTextCommand: ${e.message}" }
            Result.failure(e)
        }
    }
}
