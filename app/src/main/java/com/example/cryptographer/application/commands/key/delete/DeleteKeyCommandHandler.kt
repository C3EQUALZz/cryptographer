package com.example.cryptographer.application.commands.key.delete

import com.example.cryptographer.application.errors.KeyDeleteError
import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.domain.common.errors.AppError
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for deleting a single encryption key.
 *
 * This is a Command Handler in CQRS pattern - it handles write operations.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses Gateway for infrastructure operations
 * - Returns Unit (void) for successful deletion
 */
class DeleteKeyCommandHandler(
    private val commandGateway: KeyCommandGateway,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the DeleteKeyCommand.
     *
     * @param command Command to execute
     * @return Result with Unit or error
     */
    operator fun invoke(command: DeleteKeyCommand): Result<Unit> {
        return try {
            logger.debug { "Handling DeleteKeyCommand: keyId=${command.keyId}" }
            val deleted = commandGateway.deleteKey(command.keyId)

            if (deleted) {
                logger.info { "Key deleted successfully: keyId=${command.keyId}" }
                Result.success(Unit)
            } else {
                logger.error { "Failed to delete key: keyId=${command.keyId}" }
                Result.failure(KeyDeleteError(command.keyId))
            }
        } catch (e: AppError) {
            logger.error(e) { "Error handling DeleteKeyCommand: keyId=${command.keyId}" }
            Result.failure(e)
        }
    }
}
