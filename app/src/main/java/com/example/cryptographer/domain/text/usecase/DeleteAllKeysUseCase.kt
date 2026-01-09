package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.infrastructure.key.KeyStorageAdapter
import com.example.cryptographer.setup.configs.getLogger

/**
 * Use Case for deleting all saved encryption keys.
 * Encapsulates the logic of removing all keys from storage.
 */
class DeleteAllKeysUseCase(
    private val keyStorageAdapter: KeyStorageAdapter
) {
    private val logger = getLogger<DeleteAllKeysUseCase>()
    /**
     * Deletes all saved encryption keys.
     *
     * @return Result indicating success or failure
     */
    operator fun invoke(): Result<Unit> {
        return try {
            logger.d("Starting deletion of all keys")
            val success = keyStorageAdapter.deleteAllKeys()
            
            if (success) {
                logger.i("All keys deleted successfully")
                Result.success(Unit)
            } else {
                logger.e("Failed to delete all keys")
                Result.failure(Exception("Failed to delete all keys"))
            }
        } catch (e: Exception) {
            logger.e("Error deleting all keys", e)
            Result.failure(e)
        }
    }
}

