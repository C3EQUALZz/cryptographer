package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.infrastructure.key.KeyStorageAdapter
import com.example.cryptographer.setup.configs.getLogger

/**
 * Use Case for deleting a single encryption key.
 */
class DeleteKeyUseCase(
    private val keyStorageAdapter: KeyStorageAdapter
) {
    private val logger = getLogger<DeleteKeyUseCase>()

    /**
     * Deletes an encryption key by ID.
     *
     * @param keyId Key identifier
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(keyId: String): Result<Unit> {
        return try {
            logger.d("Deleting key: keyId=$keyId")
            val deleted = keyStorageAdapter.deleteKey(keyId)
            
            if (deleted) {
                logger.i("Key deleted successfully: keyId=$keyId")
                Result.success(Unit)
            } else {
                logger.e("Failed to delete key: keyId=$keyId")
                Result.failure(Exception("Failed to delete key"))
            }
        } catch (e: Exception) {
            logger.e("Error deleting key: keyId=$keyId", e)
            Result.failure(e)
        }
    }
}

