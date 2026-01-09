package com.example.cryptographer.application.commands.key.delete_all

import com.example.cryptographer.application.common.ports.key.KeyCommandGateway
import com.example.cryptographer.setup.configs.getLogger

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
    private val commandGateway: KeyCommandGateway
) {
    private val logger = getLogger<DeleteAllKeysCommandHandler>()

    /**
     * Handles the DeleteAllKeysCommand.
     *
     * @param command Command to execute
     * @return Result with Unit or error
     */
    operator fun invoke(command: DeleteAllKeysCommand): Result<Unit> {
        return try {
            logger.d("Handling DeleteAllKeysCommand")
            val success = commandGateway.deleteAllKeys()

            if (success) {
                logger.i("All keys deleted successfully")
                Result.success(Unit)
            } else {
                logger.e("Failed to delete all keys")
                Result.failure(Exception("Failed to delete all keys"))
            }
        } catch (e: Exception) {
            logger.e("Error handling DeleteAllKeysCommand", e)
            Result.failure(e)
        }
    }
}
