package com.example.cryptographer.application.commands.text.encrypt

import com.example.cryptographer.application.common.views.EncryptedTextView
import com.example.cryptographer.domain.text.valueobjects.TextEncoding
import com.example.cryptographer.domain.text.services.AesEncryptionService
import com.example.cryptographer.domain.text.services.TextService
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for encrypting text using AES algorithm.
 *
 * This is a specialized Command Handler for AES encryption.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses domain services for business logic
 * - Returns View (DTO) for presentation layer
 */
class AesEncryptTextCommandHandler(
    private val aesEncryptionService: AesEncryptionService,
    private val textService: TextService
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the AesEncryptTextCommand.
     *
     * @param command Command to execute
     * @return Result with EncryptedTextView or error
     */
    operator fun invoke(command: AesEncryptTextCommand): Result<EncryptedTextView> {
        return try {
            logger.debug {
                "Handling AES EncryptTextCommand: length=${command.rawText.length},"
                " algorithm=${command.key.algorithm}"
            }

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
                logger.info {
                    "AES text encryption successful: algorithm=${command.key.algorithm}, " +
                        "encryptedSize=${result.getOrNull()?.encryptedData?.size ?: 0} bytes"
                }
            }

            result.map { encryptedText ->
                EncryptedTextView(encryptedText)
            }
        } catch (e: Exception) {
            logger.error(e) { "Error handling AES EncryptTextCommand: ${e.message}" }
            Result.failure(
                Exception("AES text encryption error: ${e.message}", e)
            )
        }
    }
}

