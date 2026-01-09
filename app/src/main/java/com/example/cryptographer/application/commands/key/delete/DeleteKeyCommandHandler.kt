package com.example.cryptographer.application.commands.key.delete

import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.setup.configs.getLogger

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
    private val commandGateway: KeyCommandGateway
) {
    private val logger = getLogger<DeleteKeyCommandHandler>()

    /**
     * Handles the DeleteKeyCommand.
     *
     * @param command Command to execute
     * @return Result with Unit or error
     */
    operator fun invoke(command: DeleteKeyCommand): Result<Unit> {
        return try {
            logger.d("Handling DeleteKeyCommand: keyId=${command.keyId}")
            val deleted = commandGateway.deleteKey(command.keyId)

            if (deleted) {
                logger.i("Key deleted successfully: keyId=${command.keyId}")
                Result.success(Unit)
            } else {
                logger.e("Failed to delete key: keyId=${command.keyId}")
                Result.failure(Exception("Failed to delete key"))
            }
        } catch (e: Exception) {
            logger.e("Error handling DeleteKeyCommand: keyId=${command.keyId}", e)
            Result.failure(e)
        }
    }
}
