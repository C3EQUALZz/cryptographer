package com.example.cryptographer.application.commands.text.encrypt

import com.example.cryptographer.application.common.views.EncryptedTextView
import com.example.cryptographer.domain.text.value_objects.TextEncoding
import com.example.cryptographer.domain.text.services.AesEncryptionService
import com.example.cryptographer.domain.text.services.TextService
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for encrypting text.
 *
 * This is a Command Handler in CQRS pattern - it handles write operations.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses domain services for business logic
 * - Returns View (DTO) for presentation layer
 */
class EncryptTextCommandHandler(
    private val aesEncryptionService: AesEncryptionService,
    private val textService: TextService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the EncryptTextCommand.
     *
     * @param command Command to execute
     * @return Result with EncryptedTextView or error
     */
    operator fun invoke(command: EncryptTextCommand): Result<EncryptedTextView> {
        return try {
            logger.debug { "Handling EncryptTextCommand: length=${command.rawText.length}, algorithm=${command.key.algorithm}" }

            // Validate text using TextService (ensures consistency)
            val text = textService.create(command.rawText, TextEncoding.UTF8).getOrElse { error ->
                logger.error(error) { "Text validation failed: ${error.message}" }
                return Result.failure(error)
            }

            // Convert validated text to bytes
            val textBytes = text.content.toBytes()

            // Encrypt using AES service
            val result = aesEncryptionService.encrypt(textBytes, command.key)
            if (result.isSuccess) {
                logger.info { "Text encryption successful: algorithm=${command.key.algorithm}, encryptedSize=${result.getOrNull()?.encryptedData?.size ?: 0} bytes" }
            }

            result.map { encryptedText ->
                EncryptedTextView(encryptedText)
            }
        } catch (e: Exception) {
            logger.error(e) { "Error handling EncryptTextCommand: ${e.message}" }
            Result.failure(
                Exception("Text encryption error: ${e.message}", e)
            )
        }
    }
}
