package com.example.cryptographer.application.commands.key.create

import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.application.common.views.KeyIdView
import com.example.cryptographer.domain.text.services.AesEncryptionService
import com.example.cryptographer.setup.configs.getLogger
import java.util.UUID

/**
 * Command Handler for generating and saving an encryption key.
 *
 * This is a Command Handler in CQRS pattern - it handles write operations.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses domain services for business logic
 * - Uses Gateway for infrastructure operations
 * - Returns View (DTO) for presentation layer
 */
class GenerateAndSaveKeyCommandHandler(
    private val aesEncryptionService: AesEncryptionService,
    private val commandGateway: KeyCommandGateway
) {
    private val logger = getLogger<GenerateAndSaveKeyCommandHandler>()

    /**
     * Handles the GenerateAndSaveKeyCommand.
     *
     * @param command Command to execute
     * @return Result with KeyIdView or error
     */
    operator fun invoke(command: GenerateAndSaveKeyCommand): Result<KeyIdView> {
        return try {
            logger.d("Handling GenerateAndSaveKeyCommand: algorithm=${command.algorithm}")

            // Generate key using domain service
            val keyResult = aesEncryptionService.generateKey(command.algorithm)
            if (keyResult.isFailure) {
                val error = keyResult.exceptionOrNull() ?: Exception("Key generation failed")
                logger.e("Key generation failed: ${error.message}", error)
                return Result.failure(error)
            }

            val key = keyResult.getOrThrow()

            // Generate unique ID and save key via Gateway
            val keyId = UUID.randomUUID().toString()
            logger.d("Saving key: keyId=$keyId, algorithm=${key.algorithm}")

            val saved = commandGateway.saveKey(keyId, key)

            if (saved) {
                logger.i("Key generated and saved successfully: keyId=$keyId, algorithm=${command.algorithm}")
                Result.success(KeyIdView(keyId))
            } else {
                logger.e("Failed to save key: keyId=$keyId")
                Result.failure(Exception("Failed to save key"))
            }
        } catch (e: Exception) {
            logger.e("Error handling GenerateAndSaveKeyCommand: algorithm=${command.algorithm}", e)
            Result.failure(e)
        }
    }
}
