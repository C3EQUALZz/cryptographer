package com.example.cryptographer.application.commands.key.create

import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.application.common.views.KeyIdView
import com.example.cryptographer.domain.text.services.AesEncryptionService
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID

/**
 * Command Handler for generating and saving an AES encryption key.
 *
 * This is a specialized Command Handler for AES key generation.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses domain services for business logic
 * - Uses Gateway for infrastructure operations
 * - Returns View (DTO) for presentation layer
 */
class AesGenerateAndSaveKeyCommandHandler(
    private val aesEncryptionService: AesEncryptionService,
    private val commandGateway: KeyCommandGateway,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the AesGenerateAndSaveKeyCommand.
     *
     * @param command Command to execute
     * @return Result with KeyIdView or error
     */
    operator fun invoke(command: AesGenerateAndSaveKeyCommand): Result<KeyIdView> {
        return try {
            logger.debug { "Handling AES GenerateAndSaveKeyCommand: algorithm=${command.algorithm}" }

            // Generate key using AES domain service
            val keyResult = aesEncryptionService.generateKey(command.algorithm)
            if (keyResult.isFailure) {
                val error = keyResult.exceptionOrNull() ?: Exception("AES key generation failed")
                logger.error(error) { "AES key generation failed: ${error.message}" }
                return Result.failure(error)
            }

            val key = keyResult.getOrThrow()

            // Generate unique ID and save key via Gateway
            val keyId = UUID.randomUUID().toString()
            logger.debug { "Saving AES key: keyId=$keyId, algorithm=${key.algorithm}" }

            val saved = commandGateway.saveKey(keyId, key)

            if (saved) {
                logger.info { "AES key generated and saved successfully: keyId=$keyId, algorithm=${command.algorithm}" }
                Result.success(KeyIdView(keyId))
            } else {
                logger.error { "Failed to save AES key: keyId=$keyId" }
                Result.failure(Exception("Failed to save AES key"))
            }
        } catch (e: Exception) {
            logger.error(e) { "Error handling AES GenerateAndSaveKeyCommand: algorithm=${command.algorithm}" }
            Result.failure(e)
        }
    }
}
