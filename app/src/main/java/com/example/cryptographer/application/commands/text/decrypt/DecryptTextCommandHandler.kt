package com.example.cryptographer.application.commands.text.decrypt

import com.example.cryptographer.application.common.views.DecryptedTextView
import com.example.cryptographer.domain.text.services.AesEncryptionService
import com.example.cryptographer.setup.configs.getLogger

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
    private val logger = getLogger<DecryptTextCommandHandler>()

    /**
     * Handles the DecryptTextCommand.
     *
     * @param command Command to execute
     * @return Result with DecryptedTextView or error
     */
    operator fun invoke(command: DecryptTextCommand): Result<DecryptedTextView> {
        return try {
            logger.d("Handling DecryptTextCommand: algorithm=${command.key.algorithm}, encryptedSize=${command.encryptedText.encryptedData.size} bytes")

            // Decrypt using AES service
            val decryptedBytes = aesEncryptionService.decrypt(command.encryptedText, command.key).getOrElse { error ->
                logger.e("Text decryption failed: ${error.message}", error)
                return Result.failure(error)
            }

            // Convert bytes back to text string
            val decryptedContent = String(decryptedBytes, Charsets.UTF_8)

            logger.i("Text decryption successful: algorithm=${command.key.algorithm}, decryptedLength=${decryptedContent.length}")
            Result.success(DecryptedTextView(decryptedContent))
        } catch (e: Exception) {
            logger.e("Error handling DecryptTextCommand: ${e.message}", e)
            Result.failure(
                Exception("Text decryption error: ${e.message}", e)
            )
        }
    }
}
