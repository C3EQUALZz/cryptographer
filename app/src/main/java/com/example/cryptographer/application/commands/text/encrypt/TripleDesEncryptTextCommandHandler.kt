package com.example.cryptographer.application.commands.text.encrypt

import com.example.cryptographer.application.common.views.EncryptedTextView
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.services.TextService
import com.example.cryptographer.domain.text.services.TripleDesEncryptionService
import com.example.cryptographer.domain.text.valueobjects.TextEncoding
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for encrypting text using Triple DES algorithm.
 *
 * This is a specialized Command Handler for 3DES encryption.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses domain services for business logic
 * - Returns View (DTO) for presentation layer
 */
class TripleDesEncryptTextCommandHandler(
    private val tripleDesEncryptionService: TripleDesEncryptionService,
    private val textService: TextService,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the TripleDesEncryptTextCommand.
     *
     * @param command Command to execute
     * @return Result with EncryptedTextView or error
     */
    operator fun invoke(command: TripleDesEncryptTextCommand): Result<EncryptedTextView> {
        return try {
            logger.debug {
                "Handling 3DES EncryptTextCommand: length=${command.rawText.length}, " +
                    "algorithm=${command.key.algorithm}"
            }

            // Validate text using TextService (ensures consistency)
            val text = textService.create(command.rawText, TextEncoding.UTF8).getOrElse { error ->
                logger.error(error) { "Text validation failed: ${error.message}" }
                return Result.failure(error)
            }

            // Convert validated text to bytes
            val textBytes = text.content.toBytes()

            // Encrypt using 3DES service
            val result = tripleDesEncryptionService.encrypt(textBytes, command.key)
            if (result.isSuccess) {
                logger.info {
                    "3DES text encryption successful: algorithm=${command.key.algorithm}, " +
                        "encryptedSize=${result.getOrNull()?.encryptedData?.size ?: 0} bytes"
                }
            }

            result.map { encryptedText ->
                EncryptedTextView(encryptedText)
            }
        } catch (e: AppError) {
            logger.error(e) { "Error handling 3DES EncryptTextCommand: ${e.message}" }
            Result.failure(e)
        }
    }
}
