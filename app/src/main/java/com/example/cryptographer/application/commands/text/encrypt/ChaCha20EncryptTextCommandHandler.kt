package com.example.cryptographer.application.commands.text.encrypt

import com.example.cryptographer.application.common.views.EncryptedTextView
import com.example.cryptographer.domain.text.valueobjects.TextEncoding
import com.example.cryptographer.domain.text.services.ChaCha20EncryptionService
import com.example.cryptographer.domain.text.services.TextService
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for encrypting text using ChaCha20 algorithm.
 *
 * This is a specialized Command Handler for ChaCha20 encryption.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses domain services for business logic
 * - Returns View (DTO) for presentation layer
 */
class ChaCha20EncryptTextCommandHandler(
    private val chaCha20EncryptionService: ChaCha20EncryptionService,
    private val textService: TextService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the ChaCha20EncryptTextCommand.
     *
     * @param command Command to execute
     * @return Result with EncryptedTextView or error
     */
    operator fun invoke(command: ChaCha20EncryptTextCommand): Result<EncryptedTextView> {
        return try {
            logger.debug {
                "Handling ChaCha20 EncryptTextCommand: length=${command.rawText.length}, " +
                    "algorithm=${command.key.algorithm}"
            }

            // Validate text using TextService (ensures consistency)
            val text = textService.create(command.rawText, TextEncoding.UTF8).getOrElse { error ->
                logger.error(error) { "Text validation failed: ${error.message}" }
                return Result.failure(error)
            }

            // Convert validated text to bytes
            val textBytes = text.content.toBytes()

            // Encrypt using ChaCha20 service
            val result = chaCha20EncryptionService.encrypt(textBytes, command.key)
            if (result.isSuccess) {
                logger.info {
                    "ChaCha20 text encryption successful: algorithm=${command.key.algorithm}, " +
                        "encryptedSize=${result.getOrNull()?.encryptedData?.size ?: 0} bytes"
                }
            }

            result.map { encryptedText ->
                EncryptedTextView(encryptedText)
            }
        } catch (e: Exception) {
            logger.error(e) { "Error handling ChaCha20 EncryptTextCommand: ${e.message}" }
            Result.failure(
                Exception("ChaCha20 text encryption error: ${e.message}", e)
            )
        }
    }
}

