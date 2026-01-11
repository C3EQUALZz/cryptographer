package com.example.cryptographer.application.commands.key.deleteall

import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.application.errors.KeyDeleteAllError
import com.example.cryptographer.domain.common.errors.AppError
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Command Handler for deleting all encryption keys.
 *
 * This is a Command Handler in CQRS pattern - it handles write operations.
 * Following Clean Architecture principles:
 * - Located in Application layer (application boundary)
 * - Uses Gateway for infrastructure operations
 * - Returns Unit (void) for successful deletion
 */
class DeleteAllKeysCommandHandler(
    private val commandGateway: KeyCommandGateway,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Handles the DeleteAllKeysCommand.
     *
     * @param command Command to execute
     * @return Result with Unit or error
     */
    operator fun invoke(command: DeleteAllKeysCommand): Result<Unit> {
        return try {
            logger.debug { "Handling DeleteAllKeysCommand" }
            val success = commandGateway.deleteAllKeys()

            if (success) {
                logger.info { "All keys deleted successfully" }
                Result.success(Unit)
            } else {
                logger.error { "Failed to delete all keys" }
                Result.failure(KeyDeleteAllError())
            }
        } catch (e: AppError) {
            logger.error(e) { "Error handling DeleteAllKeysCommand" }
            Result.failure(e)
        }
    }
}
