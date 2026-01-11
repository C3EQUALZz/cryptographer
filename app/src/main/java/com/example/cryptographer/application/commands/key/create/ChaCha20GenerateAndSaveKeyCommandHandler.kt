package com.example.cryptographer.application.commands.key.create

import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.application.common.views.KeyIdView
import com.example.cryptographer.domain.text.services.ChaCha20EncryptionService
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID

/**
 * Command Handler for generating and saving a ChaCha20 encryption key.
 *
 * This is a specialized Command Handler for ChaCha20 key generation.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses domain services for business logic
 * - Uses Gateway for infrastructure operations
 * - Returns View (DTO) for presentation layer
 */
class ChaCha20GenerateAndSaveKeyCommandHandler(
    private val chaCha20EncryptionService: ChaCha20EncryptionService,
    private val commandGateway: KeyCommandGateway,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the ChaCha20GenerateAndSaveKeyCommand.
     *
     * @param command Command to execute
     * @return Result with KeyIdView or error
     */
    operator fun invoke(command: ChaCha20GenerateAndSaveKeyCommand): Result<KeyIdView> {
        return try {
            logger.debug { "Handling ChaCha20 GenerateAndSaveKeyCommand: algorithm=${command.algorithm}" }

            // Generate key using ChaCha20 domain service
            val keyResult = chaCha20EncryptionService.generateKey(command.algorithm)
            if (keyResult.isFailure) {
                val error = keyResult.exceptionOrNull() ?: Exception("ChaCha20 key generation failed")
                logger.error(error) { "ChaCha20 key generation failed: ${error.message}" }
                return Result.failure(error)
            }

            val key = keyResult.getOrThrow()

            // Generate unique ID and save key via Gateway
            val keyId = UUID.randomUUID().toString()
            logger.debug { "Saving ChaCha20 key: keyId=$keyId, algorithm=${key.algorithm}" }

            val saved = commandGateway.saveKey(keyId, key)

            if (saved) {
                logger.info {
                    "ChaCha20 key generated and saved successfully: " +
                        "keyId=$keyId, algorithm=${command.algorithm}"
                }
                Result.success(KeyIdView(keyId))
            } else {
                logger.error { "Failed to save ChaCha20 key: keyId=$keyId" }
                Result.failure(Exception("Failed to save ChaCha20 key"))
            }
        } catch (e: Exception) {
            logger.error(e) {
                "Error handling ChaCha20 GenerateAndSaveKeyCommand: algorithm=${command.algorithm}"
            }
            Result.failure(e)
        }
    }
}
