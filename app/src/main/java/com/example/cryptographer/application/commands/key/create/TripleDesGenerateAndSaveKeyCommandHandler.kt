package com.example.cryptographer.application.commands.key.create

import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.application.common.views.KeyIdView
import com.example.cryptographer.application.errors.KeyGenerationError
import com.example.cryptographer.application.errors.KeySaveError
import com.example.cryptographer.domain.common.errors.AppError
import com.example.cryptographer.domain.text.services.TripleDesEncryptionService
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID

/**
 * Command Handler for generating and saving a Triple DES encryption key.
 *
 * This is a specialized Command Handler for 3DES key generation.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses domain services for business logic
 * - Uses Gateway for infrastructure operations
 * - Returns View (DTO) for presentation layer
 */
class TripleDesGenerateAndSaveKeyCommandHandler(
    private val tripleDesEncryptionService: TripleDesEncryptionService,
    private val commandGateway: KeyCommandGateway,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the TripleDesGenerateAndSaveKeyCommand.
     *
     * @param command Command to execute
     * @return Result with KeyIdView or error
     */
    operator fun invoke(command: TripleDesGenerateAndSaveKeyCommand): Result<KeyIdView> {
        return try {
            logger.debug { "Handling 3DES GenerateAndSaveKeyCommand: algorithm=${command.algorithm}" }

            // Generate key using 3DES domain service
            val keyResult = tripleDesEncryptionService.generateKey(command.algorithm)
            if (keyResult.isFailure) {
                val error = keyResult.exceptionOrNull() ?: KeyGenerationError(command.algorithm)
                logger.error(error) { "3DES key generation failed: ${error.message}" }
                return Result.failure(
                    error as? AppError ?: KeyGenerationError(command.algorithm, error),
                )
            }

            val key = keyResult.getOrThrow()

            // Generate unique ID and save key via Gateway
            val keyId = UUID.randomUUID().toString()
            logger.debug { "Saving 3DES key: keyId=$keyId, algorithm=${key.algorithm}" }

            val saved = commandGateway.saveKey(keyId, key)

            if (saved) {
                logger.info {
                    "3DES key generated and saved successfully: " +
                        "keyId=$keyId, " +
                        "algorithm=${command.algorithm}"
                }
                Result.success(KeyIdView(keyId))
            } else {
                logger.error { "Failed to save 3DES key: keyId=$keyId" }
                Result.failure(KeySaveError(keyId))
            }
        } catch (e: AppError) {
            logger.error(e) {
                "Error handling 3DES GenerateAndSaveKeyCommand: algorithm=${command.algorithm}"
            }
            Result.failure(e)
        }
    }
}
